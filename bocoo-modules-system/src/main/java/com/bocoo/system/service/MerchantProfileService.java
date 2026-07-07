package com.bocoo.system.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.bo.MerchantProfileBo;
import com.bocoo.system.domain.entity.MerchantProfile;
import com.bocoo.system.domain.vo.MerchantProfileLevelSnapshot;
import com.bocoo.system.domain.vo.MerchantProfileVo;
import com.bocoo.system.mapper.MerchantProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MerchantProfileService {

    private final MerchantProfileMapper merchantProfileMapper;
    private final ObjectProvider<MerchantProfileLevelSupport> levelSupportProvider;

    public TableDataInfo<MerchantProfileVo> selectPage(MerchantProfileBo bo, PageQuery pageQuery) {
        checkPlatformTenant();
        Page<MerchantProfile> pageQueryParam = pageQuery.build();
        Page<MerchantProfileVo> page = TenantContextHolder.callWithIgnore(() ->
            merchantProfileMapper.selectVoPage(pageQueryParam, buildQueryWrapper(bo)));
        return TableDataInfo.build(page);
    }

    public MerchantProfileVo selectById(Long merchantId) {
        checkPlatformTenant();
        return TenantContextHolder.callWithIgnore(() -> merchantProfileMapper.selectVoById(merchantId));
    }

    public int updateById(MerchantProfileBo bo) {
        checkPlatformTenant();
        MerchantProfile profile = TenantContextHolder.callWithIgnore(() -> merchantProfileMapper.selectById(bo.getMerchantId()));
        if (profile == null) {
            throw ServiceException.ofMessageKey("merchant.profile.notFound");
        }
        applyMerchantEditableFields(profile, bo);
        applyPlatformPricingFields(profile, bo);
        return TenantContextHolder.callWithIgnore(() -> merchantProfileMapper.updateById(profile));
    }

    public MerchantProfileVo selectCurrent() {
        checkMerchantTenant();
        return merchantProfileMapper.selectVoOne(new LambdaQueryWrapper<MerchantProfile>()
            .eq(MerchantProfile::getTenantId, getCurrentTenantId()), false);
    }

    public MerchantProfileVo selectByTenantId(Long tenantId) {
        return TenantContextHolder.callWithIgnore(() -> merchantProfileMapper.selectVoOne(new LambdaQueryWrapper<MerchantProfile>()
            .eq(MerchantProfile::getTenantId, tenantId), false));
    }

    public int insertProfile(MerchantProfileBo bo) {
        MerchantProfile profile = MapstructUtils.convert(bo, MerchantProfile.class);
        applyDefaultLevel(profile);
        return merchantProfileMapper.insert(profile);
    }

    public int updateCurrent(MerchantProfileBo bo) {
        checkMerchantTenant();
        MerchantProfile profile = merchantProfileMapper.selectOne(new LambdaQueryWrapper<MerchantProfile>()
            .eq(MerchantProfile::getTenantId, getCurrentTenantId()), false);
        if (profile == null) {
            throw ServiceException.ofMessageKey("merchant.profile.notFound");
        }
        applyMerchantEditableFields(profile, bo);
        return merchantProfileMapper.updateById(profile);
    }

    private LambdaQueryWrapper<MerchantProfile> buildQueryWrapper(MerchantProfileBo bo) {
        LambdaQueryWrapper<MerchantProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ObjectUtil.isNotNull(bo.getMerchantId()), MerchantProfile::getMerchantId, bo.getMerchantId())
            .eq(ObjectUtil.isNotNull(bo.getTenantId()), MerchantProfile::getTenantId, bo.getTenantId())
            .like(StringUtils.isNotBlank(bo.getMerchantName()), MerchantProfile::getMerchantName, bo.getMerchantName())
            .like(StringUtils.isNotBlank(bo.getCompanyName()), MerchantProfile::getCompanyName, bo.getCompanyName())
            .like(StringUtils.isNotBlank(bo.getPrimaryEmail()), MerchantProfile::getPrimaryEmail, bo.getPrimaryEmail())
            .eq(ObjectUtil.isNotNull(bo.getLevelId()), MerchantProfile::getLevelId, bo.getLevelId())
            .like(StringUtils.isNotBlank(bo.getLevelName()), MerchantProfile::getLevelName, bo.getLevelName())
            .eq(StringUtils.isNotBlank(bo.getStatus()), MerchantProfile::getStatus, bo.getStatus())
            .orderByDesc(MerchantProfile::getCreateTime);
        return wrapper;
    }

    private void applyMerchantEditableFields(MerchantProfile profile, MerchantProfileBo bo) {
        profile.setContactFirstName(bo.getContactFirstName());
        profile.setContactLastName(bo.getContactLastName());
        profile.setContactName(resolveContactName(bo));
        profile.setOfficePhone(bo.getOfficePhone());
        profile.setMobilePhone(bo.getMobilePhone());
        profile.setState(bo.getState());
        profile.setCity(bo.getCity());
        profile.setAddressLine1(bo.getAddressLine1());
        profile.setAddressLine2(bo.getAddressLine2());
        profile.setPostalCode(bo.getPostalCode());
        profile.setRemark(bo.getRemark());
    }

    private void applyPlatformPricingFields(MerchantProfile profile, MerchantProfileBo bo) {
        if (bo.getLevelId() != null) {
            MerchantProfileLevelSnapshot level = requireLevelSupport().selectEnabledLevel(bo.getLevelId());
            profile.setLevelId(level.levelId());
            profile.setLevelCode(level.levelCode());
            profile.setLevelName(level.levelName());
            profile.setDiscountRate(bo.getDiscountRate() == null ? level.defaultDiscountRate() : bo.getDiscountRate());
            profile.setCreditLimit(bo.getCreditLimit() == null ? level.defaultCreditLimit() : bo.getCreditLimit());
            return;
        }
        if (bo.getDiscountRate() != null) {
            profile.setDiscountRate(bo.getDiscountRate());
        }
        if (bo.getCreditLimit() != null) {
            profile.setCreditLimit(bo.getCreditLimit());
        }
    }

    private void applyDefaultLevel(MerchantProfile profile) {
        MerchantProfileLevelSupport support = levelSupportProvider.getIfAvailable();
        if (support == null || profile == null || profile.getLevelId() != null) {
            return;
        }
        MerchantProfileLevelSnapshot level = support.selectDefaultLevel();
        if (level == null) {
            return;
        }
        profile.setLevelId(level.levelId());
        profile.setLevelCode(level.levelCode());
        profile.setLevelName(level.levelName());
        profile.setDiscountRate(level.defaultDiscountRate());
        profile.setCreditLimit(level.defaultCreditLimit());
    }

    private MerchantProfileLevelSupport requireLevelSupport() {
        MerchantProfileLevelSupport support = levelSupportProvider.getIfAvailable();
        if (support == null) {
            throw ServiceException.ofMessageKey("merchant.level.notFound");
        }
        return support;
    }

    private String resolveContactName(MerchantProfileBo bo) {
        String firstName = StringUtils.trimToEmpty(bo.getContactFirstName());
        String lastName = StringUtils.trimToEmpty(bo.getContactLastName());
        if (StringUtils.isBlank(firstName)) {
            return lastName;
        }
        if (StringUtils.isBlank(lastName)) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    private Long getCurrentTenantId() {
        Long tenantId = LoginHelper.getTenantId();
        if (tenantId == null) {
            throw ServiceException.ofMessageKey("merchant.profile.merchantOnly");
        }
        return tenantId;
    }

    private void checkPlatformTenant() {
        if (!LoginHelper.isPlatformTenant()) {
            throw ServiceException.ofMessageKey("merchant.profile.platformOnly");
        }
    }

    private void checkMerchantTenant() {
        if (!TenantType.MERCHANT.getCode().equals(LoginHelper.getTenantType())) {
            throw ServiceException.ofMessageKey("merchant.profile.merchantOnly");
        }
    }
}
