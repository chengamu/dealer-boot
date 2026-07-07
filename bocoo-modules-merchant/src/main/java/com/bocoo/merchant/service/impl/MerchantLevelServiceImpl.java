package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.merchant.domain.bo.MerchantLevelBo;
import com.bocoo.merchant.domain.entity.MerchantLevel;
import com.bocoo.merchant.domain.entity.MerchantLevelDiscount;
import com.bocoo.merchant.domain.vo.MerchantLevelVo;
import com.bocoo.merchant.mapper.MerchantLevelDiscountMapper;
import com.bocoo.merchant.mapper.MerchantLevelMapper;
import com.bocoo.merchant.service.MerchantLevelService;
import com.bocoo.system.domain.entity.MerchantProfile;
import com.bocoo.system.mapper.MerchantProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantLevelServiceImpl extends MerchantServiceSupport implements MerchantLevelService {

    private final MerchantLevelMapper levelMapper;
    private final MerchantLevelDiscountMapper discountMapper;
    private final MerchantProfileMapper profileMapper;

    @Override
    public TableDataInfo<MerchantLevelVo> queryPageList(MerchantLevelBo bo, PageQuery pageQuery) {
        checkPlatformTenant();
        return page(levelMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "level_id"));
    }

    @Override
    public List<MerchantLevelVo> queryList(MerchantLevelBo bo) {
        checkPlatformTenant();
        QueryWrapper<MerchantLevel> q = buildQueryWrapper(bo).orderByAsc("sort_order", "level_id");
        return levelMapper.selectVoList(q);
    }

    @Override
    public MerchantLevelVo queryById(Long id) {
        checkPlatformTenant();
        return levelMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(MerchantLevelBo bo) {
        checkPlatformTenant();
        normalize(bo);
        validateCodeUnique(bo);
        MerchantLevel entity = MapstructUtils.convert(bo, MerchantLevel.class);
        boolean saved = entity != null && levelMapper.insert(entity) > 0;
        if (saved) {
            ensureSingleDefault(entity);
        }
        return saved;
    }

    @Override
    public Boolean updateByBo(MerchantLevelBo bo) {
        checkPlatformTenant();
        normalize(bo);
        validateCodeUnique(bo);
        MerchantLevel current = levelMapper.selectById(bo.getLevelId());
        if (current == null || !DEL_FLAG_NORMAL.equals(current.getDelFlag())) {
            throw ServiceException.ofMessageKey("merchant.level.notFound");
        }
        if (enabled(current.getStatus())) {
            throw ServiceException.ofMessageKey("merchant.level.edit.enabledDenied");
        }
        MerchantLevel entity = MapstructUtils.convert(bo, MerchantLevel.class);
        boolean saved = entity != null && levelMapper.updateById(entity) > 0;
        if (saved) {
            ensureSingleDefault(entity);
        }
        return saved;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        checkPlatformTenant();
        for (Long id : ids) {
            MerchantLevel current = levelMapper.selectById(id);
            if (current == null) {
                continue;
            }
            if (enabled(current.getStatus())) {
                throw ServiceException.ofMessageKey("merchant.level.delete.enabledDenied");
            }
            long profileCount = platformIgnoreTenant(() -> profileMapper.selectCount(new QueryWrapper<MerchantProfile>()
                .eq("level_id", id)));
            long discountCount = discountMapper.selectCount(this.<MerchantLevelDiscount>activeQuery().eq("level_id", id));
            if (profileCount + discountCount > 0) {
                throw ServiceException.ofMessageKey("merchant.level.delete.referenced");
            }
        }
        return levelMapper.deleteBatchIds(List.of(ids)) > 0;
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        checkPlatformTenant();
        String normalizedStatus = normalizeStatus(status);
        return levelMapper.update(null, new LambdaUpdateWrapper<MerchantLevel>()
            .eq(MerchantLevel::getLevelId, id)
            .set(MerchantLevel::getStatus, normalizedStatus)) > 0;
    }

    private QueryWrapper<MerchantLevel> buildQueryWrapper(MerchantLevelBo bo) {
        QueryWrapper<MerchantLevel> q = this.<MerchantLevel>activeQuery();
        if (bo != null) {
            like(q, "level_code", bo.getLevelCode());
            like(q, "level_name", bo.getLevelName());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void normalize(MerchantLevelBo bo) {
        bo.setTenantId(PLATFORM_TENANT_ID);
        bo.setLevelCode(StringUtils.trimToEmpty(bo.getLevelCode()).toUpperCase());
        if (bo.getDefaultDiscountRate() == null) {
            bo.setDefaultDiscountRate(BigDecimal.ONE);
        }
        if (bo.getDefaultCreditLimit() == null) {
            bo.setDefaultCreditLimit(BigDecimal.ZERO);
        }
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(STATUS_DISABLED);
        } else {
            bo.setStatus(normalizeStatus(bo.getStatus()));
        }
        if (bo.getDefaultFlag() == null) {
            bo.setDefaultFlag(false);
        }
        bo.setDelFlag(DEL_FLAG_NORMAL);
    }

    private void validateCodeUnique(MerchantLevelBo bo) {
        long count = levelMapper.selectCount(this.<MerchantLevel>activeQuery()
            .eq("level_code", bo.getLevelCode())
            .ne(bo.getLevelId() != null, "level_id", bo.getLevelId()));
        if (count > 0) {
            throw ServiceException.ofMessageKey("merchant.level.code.exists");
        }
    }

    private void ensureSingleDefault(MerchantLevel entity) {
        if (entity == null || !Boolean.TRUE.equals(entity.getDefaultFlag())) {
            return;
        }
        levelMapper.update(null, new LambdaUpdateWrapper<MerchantLevel>()
            .eq(MerchantLevel::getTenantId, PLATFORM_TENANT_ID)
            .eq(MerchantLevel::getDelFlag, DEL_FLAG_NORMAL)
            .ne(MerchantLevel::getLevelId, entity.getLevelId())
            .set(MerchantLevel::getDefaultFlag, false));
    }
}
