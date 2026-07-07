package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.enums.UserStatus;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.merchant.domain.bo.CustomerProfileBo;
import com.bocoo.merchant.domain.entity.CustomerProfile;
import com.bocoo.merchant.domain.vo.CustomerOwnerOptionVo;
import com.bocoo.merchant.domain.vo.CustomerProfileVo;
import com.bocoo.merchant.mapper.CustomerProfileMapper;
import com.bocoo.merchant.service.CustomerProfileService;
import com.bocoo.system.domain.entity.MerchantProfile;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.mapper.MerchantProfileMapper;
import com.bocoo.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomerProfileServiceImpl extends MerchantServiceSupport implements CustomerProfileService {

    private final CustomerProfileMapper customerMapper;
    private final MerchantProfileMapper merchantProfileMapper;
    private final SysUserMapper userMapper;

    @Override
    public TableDataInfo<CustomerProfileVo> queryPageList(CustomerProfileBo bo, PageQuery pageQuery) {
        Long tenantId = currentTenantId();
        return page(customerMapper, pageQuery, buildQueryWrapper(bo).eq("tenant_id", tenantId),
            q -> q.orderByDesc("update_time", "create_time"));
    }

    @Override
    public TableDataInfo<CustomerProfileVo> queryPlatformPageList(CustomerProfileBo bo, PageQuery pageQuery) {
        checkPlatformTenant();
        return platformIgnoreTenant(() -> page(customerMapper, pageQuery, buildQueryWrapper(bo),
            q -> q.orderByDesc("update_time", "create_time")));
    }

    @Override
    public List<CustomerProfileVo> queryOptions(CustomerProfileBo bo) {
        Long tenantId = currentTenantId();
        return customerMapper.selectVoList(buildQueryWrapper(bo)
            .eq("tenant_id", tenantId)
            .eq("status", STATUS_ENABLED)
            .orderByAsc("customer_name", "customer_id"));
    }

    @Override
    public List<CustomerOwnerOptionVo> queryOwnerOptions(Long tenantId) {
        Long targetTenantId = LoginHelper.isPlatformTenant() ? tenantId : currentTenantId();
        if (targetTenantId == null) {
            throw ServiceException.ofMessageKey("tenant.context.missing");
        }
        return platformIgnoreTenant(() -> userMapper.selectList(new QueryWrapper<SysUser>()
                .eq("tenant_id", targetTenantId)
                .eq("status", UserStatus.OK.getCode())
                .eq("del_flag", UserConstants.NOT_DELETED)
                .orderByAsc("nick_name", "user_id")))
            .stream()
            .map(this::toOwnerOption)
            .toList();
    }

    @Override
    public CustomerProfileVo queryById(Long id) {
        Long tenantId = currentTenantId();
        return customerMapper.selectVoOne(this.<CustomerProfile>activeQuery()
            .eq("customer_id", id)
            .eq("tenant_id", tenantId), false);
    }

    @Override
    public CustomerProfileVo queryPlatformById(Long id) {
        checkPlatformTenant();
        return platformIgnoreTenant(() -> customerMapper.selectVoOne(this.<CustomerProfile>activeQuery().eq("customer_id", id), false));
    }

    @Override
    public Boolean insertByBo(CustomerProfileBo bo) {
        normalizeForCurrentTenant(bo);
        validateEmailUnique(bo);
        CustomerProfile entity = MapstructUtils.convert(bo, CustomerProfile.class);
        return entity != null && customerMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(CustomerProfileBo bo) {
        Long tenantId = currentTenantId();
        CustomerProfile current = customerMapper.selectOne(this.<CustomerProfile>activeQuery()
            .eq("customer_id", bo.getCustomerId())
            .eq("tenant_id", tenantId), false);
        if (current == null) {
            throw ServiceException.ofMessageKey("customer.profile.notFound");
        }
        normalizeForCurrentTenant(bo);
        validateEmailUnique(bo);
        CustomerProfile entity = MapstructUtils.convert(bo, CustomerProfile.class);
        return entity != null && customerMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        Long tenantId = currentTenantId();
        List<Long> idList = Arrays.stream(ids).distinct().toList();
        for (Long id : idList) {
            CustomerProfile current = customerMapper.selectOne(this.<CustomerProfile>activeQuery()
                .eq("customer_id", id)
                .eq("tenant_id", tenantId), false);
            if (current == null) {
                throw ServiceException.ofMessageKey("customer.profile.notFound");
            }
            if (enabled(current.getStatus())) {
                throw ServiceException.ofMessageKey("customer.profile.delete.enabledDenied");
            }
        }
        return customerMapper.delete(this.<CustomerProfile>activeQuery()
            .eq("tenant_id", tenantId)
            .in("customer_id", idList)) > 0;
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        Long tenantId = currentTenantId();
        String normalizedStatus = normalizeStatus(status);
        return customerMapper.update(null, new LambdaUpdateWrapper<CustomerProfile>()
            .eq(CustomerProfile::getCustomerId, id)
            .eq(CustomerProfile::getTenantId, tenantId)
            .set(CustomerProfile::getStatus, normalizedStatus)) > 0;
    }

    private QueryWrapper<CustomerProfile> buildQueryWrapper(CustomerProfileBo bo) {
        QueryWrapper<CustomerProfile> q = this.<CustomerProfile>activeQuery();
        if (bo != null) {
            eq(q, "tenant_id", bo.getTenantId());
            eq(q, "merchant_id", bo.getMerchantId());
            like(q, "merchant_name", bo.getMerchantName());
            like(q, "customer_name", bo.getCustomerName());
            like(q, "company_name", bo.getCompanyName());
            like(q, "email", bo.getEmail());
            eq(q, "owner_user_id", bo.getOwnerUserId());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void normalizeForCurrentTenant(CustomerProfileBo bo) {
        Long tenantId = currentTenantId();
        bo.setTenantId(tenantId);
        bo.setEmail(normalizeEmail(bo.getEmail()));
        bo.setDelFlag(DEL_FLAG_NORMAL);
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(STATUS_ENABLED);
        } else {
            bo.setStatus(normalizeStatus(bo.getStatus()));
        }
        resolveMerchantSnapshot(bo, tenantId);
        resolveOwnerSnapshot(bo, tenantId);
    }

    private void resolveMerchantSnapshot(CustomerProfileBo bo, Long tenantId) {
        if (TenantType.MERCHANT.getCode().equals(LoginHelper.getTenantType())) {
            Long merchantId = LoginHelper.getMerchantId();
            bo.setMerchantId(merchantId);
            MerchantProfile profile = merchantProfileMapper.selectOne(new QueryWrapper<MerchantProfile>()
                .eq("tenant_id", tenantId), false);
            bo.setMerchantName(profile == null ? null : profile.getMerchantName());
            return;
        }
        bo.setMerchantId(null);
        bo.setMerchantName(null);
    }

    private void resolveOwnerSnapshot(CustomerProfileBo bo, Long tenantId) {
        if (bo.getOwnerUserId() == null) {
            bo.setOwnerName(null);
            return;
        }
        SysUser owner = platformIgnoreTenant(() -> userMapper.selectOne(new QueryWrapper<SysUser>()
            .eq("tenant_id", tenantId)
            .eq("user_id", bo.getOwnerUserId())
            .eq("del_flag", DEL_FLAG_NORMAL), false));
        if (owner == null) {
            throw ServiceException.ofMessageKey("customer.profile.owner.invalid");
        }
        bo.setOwnerName(owner.getNickName());
    }

    private void validateEmailUnique(CustomerProfileBo bo) {
        long count = customerMapper.selectCount(this.<CustomerProfile>activeQuery()
            .eq("tenant_id", bo.getTenantId())
            .eq("email", bo.getEmail())
            .ne(bo.getCustomerId() != null, "customer_id", bo.getCustomerId()));
        if (count > 0) {
            throw ServiceException.ofMessageKey("customer.profile.email.exists");
        }
    }

    private String normalizeEmail(String email) {
        return StringUtils.trimToEmpty(email).toLowerCase(Locale.ROOT);
    }

    private CustomerOwnerOptionVo toOwnerOption(SysUser user) {
        CustomerOwnerOptionVo option = new CustomerOwnerOptionVo();
        option.setUserId(user.getUserId());
        option.setUserName(user.getUserName());
        option.setNickName(user.getNickName());
        return option;
    }
}
