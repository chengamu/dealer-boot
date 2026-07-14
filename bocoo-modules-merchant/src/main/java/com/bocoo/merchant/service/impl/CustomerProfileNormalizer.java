package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.merchant.domain.bo.CustomerProfileBo;
import com.bocoo.merchant.domain.entity.CustomerProfile;
import com.bocoo.system.domain.entity.MerchantProfile;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.mapper.MerchantProfileMapper;
import com.bocoo.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CustomerProfileNormalizer extends MerchantServiceSupport {

    private final MerchantProfileMapper merchantProfileMapper;
    private final SysUserMapper userMapper;
    private final SalesOwnershipResolver ownershipResolver;

    void normalizeNew(CustomerProfileBo bo) {
        SalesOwnership ownership = ownershipResolver.currentForCreate();
        applyOwnership(bo, ownership);
        normalizeCommon(bo, ownership.tenantId());
    }

    void normalizeUpdate(CustomerProfileBo bo, CustomerProfile current) {
        bo.setTenantId(current.getTenantId());
        bo.setBusinessOrigin(current.getBusinessOrigin());
        bo.setSalesStoreId(current.getSalesStoreId());
        bo.setDeptId(current.getDeptId());
        if (bo.getOwnerUserId() == null) {
            bo.setOwnerUserId(current.getOwnerUserId());
            bo.setOwnerName(current.getOwnerName());
        }
        normalizeCommon(bo, current.getTenantId());
    }

    private void normalizeCommon(CustomerProfileBo bo, Long tenantId) {
        bo.setEmail(StringUtils.trimToEmpty(bo.getEmail()).toLowerCase(Locale.ROOT));
        bo.setDelFlag(DEL_FLAG_NORMAL);
        bo.setStatus(StringUtils.isBlank(bo.getStatus()) ? STATUS_ENABLED : normalizeStatus(bo.getStatus()));
        resolveMerchantSnapshot(bo, tenantId);
        resolveOwnerSnapshot(bo, tenantId);
    }

    private void applyOwnership(CustomerProfileBo bo, SalesOwnership ownership) {
        bo.setTenantId(ownership.tenantId());
        bo.setBusinessOrigin(ownership.businessOrigin());
        bo.setSalesStoreId(ownership.salesStoreId());
        bo.setDeptId(ownership.deptId());
        if (bo.getOwnerUserId() == null) {
            bo.setOwnerUserId(ownership.ownerUserId());
        }
    }

    private void resolveMerchantSnapshot(CustomerProfileBo bo, Long tenantId) {
        if (TenantType.MERCHANT.getCode().equals(LoginHelper.getTenantType())) {
            bo.setMerchantId(LoginHelper.getMerchantId());
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
}
