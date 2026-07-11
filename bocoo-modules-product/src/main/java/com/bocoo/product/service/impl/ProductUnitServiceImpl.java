package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductUnitBo;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductMaterialAttribute;
import com.bocoo.product.domain.entity.ProductUnit;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductUnitVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductMaterialAttributeMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductUnitMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductUnitServiceImpl extends ProductServiceSupport implements ProductUnitService {

    private final ProductUnitMapper unitMapper;
    private final ProductMaterialMapper materialMapper;
    private final ProductMaterialAttributeMapper materialAttributeMapper;

    @Override
    public TableDataInfo<ProductUnitVo> queryPageList(ProductUnitBo bo, PageQuery pageQuery) {
        return page(unitMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "unit_id"));
    }

    @Override
    public List<ProductUnitVo> queryList(ProductUnitBo bo) {
        return unitMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "unit_id")));
    }

    @Override
    public ProductUnitVo queryById(Long id) {
        return unitMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductUnitBo bo) {
        normalizeUnit(bo);
        validateUnitCodeUnique(bo);
        ProductUnit entity = MapstructUtils.convert(bo, ProductUnit.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return unitMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductUnitBo bo) {
        normalizeUnit(bo);
        validateUnitCodeUnique(bo);
        if (bo != null && bo.getUnitId() != null) {
            ProductUnit current = unitMapper.selectById(bo.getUnitId());
            if (current != null) {
                assertNormalEditable(current.getStatus());
            }
        }
        ProductUnit entity = MapstructUtils.convert(bo, ProductUnit.class);
        return entity != null && unitMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            ProductUnit current = unitMapper.selectById(id);
            if (current != null) {
                assertDisabledBeforeDelete(current.getStatus());
            }
            assertNoReferences(checkReferences(id));
        }
        return remove(unitMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return unitMapper.update(null, new LambdaUpdateWrapper<ProductUnit>()
            .eq(ProductUnit::getUnitId, id)
            .set(ProductUnit::getStatus, status)) > 0;
    }

    @Override
    public BaseEditCheckResultVo checkEditAllowed(Long id) {
        ProductUnit unit = unitMapper.selectById(id);
        if (unit == null) {
            return deniedEditCheck(null, "product.base.edit.notFound", null);
        }
        return editCheckResult(unit.getStatus(), checkReferences(id));
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long unitId) {
        ProductUnit unit = unitMapper.selectById(unitId);
        if (unit == null || unit.getUnitCode() == null) {
            return referenceResult(0, null, null);
        }
        String code = unit.getUnitCode();
        long count = materialMapper.selectCount(activeQuery(ProductMaterial.class)
            .and(q -> q.eq("unit_code", code).or().eq("secondary_unit_code", code)))
            + materialAttributeMapper.selectCount(activeQuery(ProductMaterialAttribute.class).eq("value_unit_code", code));
        return referenceResult(count, "product.unit.hasReferences", "Unit code references: " + count);
    }

    private QueryWrapper<ProductUnit> buildQueryWrapper(ProductUnitBo bo) {
        QueryWrapper<ProductUnit> q = activeQuery(ProductUnit.class);
        if (bo != null) {
            like(q, "unit_code", bo.getUnitCode());
            if (StringUtils.isNotBlank(bo.getUnitNameCn())) {
                q.and(wrapper -> wrapper.like("unit_name_cn", bo.getUnitNameCn()).or().like("unit_name_en", bo.getUnitNameCn()));
            }
            eq(q, "unit_type", bo.getUnitType());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void normalizeUnit(ProductUnitBo bo) {
        if (bo == null) {
            return;
        }
        normalizePrecision(bo);
        if (StringUtils.isBlank(bo.getBaseUnitCode())) {
            bo.setBaseUnitCode(bo.getUnitCode());
        }
        if (bo.getConversionRate() == null) {
            bo.setConversionRate(BigDecimal.ONE);
        }
        if (StringUtils.isBlank(bo.getBaseUnitCode()) || StringUtils.isBlank(bo.getUnitType())) {
            return;
        }
        ProductUnit baseUnit = unitMapper.selectOne(activeQuery(ProductUnit.class).eq("unit_code", bo.getBaseUnitCode()));
        if (baseUnit == null && StringUtils.equals(bo.getUnitCode(), bo.getBaseUnitCode())) {
            return;
        }
        if (baseUnit == null) {
            throw ServiceException.ofMessageKey("product.unit.baseUnitNotFound");
        }
        if (!StringUtils.equals(bo.getUnitType(), baseUnit.getUnitType())) {
            throw ServiceException.ofMessageKey("product.unit.baseUnitTypeMismatch");
        }
    }

    private void normalizePrecision(ProductUnitBo bo) {
        int precision = bo.getPrecisionScale() == null ? ("COUNT".equals(bo.getUnitType()) ? 0 : 6) : bo.getPrecisionScale();
        if (precision < 0 || precision > 6 || ("COUNT".equals(bo.getUnitType()) && precision != 0)) {
            throw ServiceException.ofMessageKey("product.unit.precisionInvalid");
        }
        bo.setPrecisionScale(precision);
        bo.setRoundingMode(StringUtils.blankToDefault(bo.getRoundingMode(), RoundingMode.HALF_UP.name()));
        try {
            RoundingMode.valueOf(bo.getRoundingMode());
        } catch (IllegalArgumentException ex) {
            throw ServiceException.ofMessageKey("product.unit.roundingModeInvalid");
        }
    }

    private void validateUnitCodeUnique(ProductUnitBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getUnitCode())) {
            return;
        }
        long count = unitMapper.selectCount(activeQuery(ProductUnit.class)
            .eq("unit_code", bo.getUnitCode())
            .ne(bo.getUnitId() != null, "unit_id", bo.getUnitId()));
        if (count > 0) {
            throw ServiceException.ofMessageKey("product.unit.codeExists");
        }
    }
}
