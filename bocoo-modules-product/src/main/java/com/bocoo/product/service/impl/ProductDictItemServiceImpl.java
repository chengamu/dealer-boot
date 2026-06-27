package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductDictItemBo;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.entity.ProductDictItem;
import com.bocoo.product.domain.entity.ProductDictType;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductMediaAsset;
import com.bocoo.product.domain.entity.ProductUnit;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductDictItemVo;
import com.bocoo.product.domain.vo.ProductDictOptionVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductCategoryMapper;
import com.bocoo.product.mapper.ProductDictItemMapper;
import com.bocoo.product.mapper.ProductDictTypeMapper;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductMediaAssetMapper;
import com.bocoo.product.mapper.ProductUnitMapper;
import com.bocoo.product.service.ProductDictItemService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDictItemServiceImpl extends ProductServiceSupport implements ProductDictItemService {

    private final ProductDictTypeMapper dictTypeMapper;
    private final ProductDictItemMapper dictItemMapper;
    private final ProductUnitMapper unitMapper;
    private final ProductMaterialMapper materialMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ProductMediaAssetMapper mediaAssetMapper;
    private final ProductFormulaMapper formulaMapper;

    @Override
    public TableDataInfo<ProductDictItemVo> queryPageList(ProductDictItemBo bo, PageQuery pageQuery) {
        return page(dictItemMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("dict_type_code", "sort_order", "dict_item_id"));
    }

    @Override
    public List<ProductDictItemVo> queryList(ProductDictItemBo bo) {
        return dictItemMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("dict_type_code", "sort_order", "dict_item_id")));
    }

    @Override
    public List<ProductDictOptionVo> queryOptionsByType(String dictTypeCode) {
        ProductDictItemBo bo = new ProductDictItemBo();
        bo.setDictTypeCode(dictTypeCode);
        bo.setStatus("ENABLED");
        return queryList(bo).stream().map(this::toOption).toList();
    }

    @Override
    public ProductDictItemVo queryById(Long id) {
        return dictItemMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductDictItemBo bo) {
        validateDictItem(bo);
        ProductDictItem entity = MapstructUtils.convert(bo, ProductDictItem.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return dictItemMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductDictItemBo bo) {
        validateDictItem(bo);
        if (bo != null && bo.getDictItemId() != null) {
            ProductDictItem current = dictItemMapper.selectById(bo.getDictItemId());
            if (current != null) {
                assertDictEditable(current.getSystemFlag(), current.getEditableFlag());
                assertNormalEditable(current.getStatus());
            }
        }
        ProductDictItem entity = MapstructUtils.convert(bo, ProductDictItem.class);
        return entity != null && dictItemMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            ProductDictItem entity = dictItemMapper.selectById(id);
            if (entity != null && Boolean.TRUE.equals(entity.getSystemFlag())) {
                throw ServiceException.ofMessageKey("product.dict.systemItemCannotDelete");
            }
            if (entity != null) {
                assertDisabledBeforeDelete(entity.getStatus());
            }
            assertNoReferences(checkReferences(id));
        }
        return remove(dictItemMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        ProductDictItem current = dictItemMapper.selectById(id);
        if (current != null) {
            assertDictEditable(current.getSystemFlag(), current.getEditableFlag());
        }
        return dictItemMapper.update(null, new LambdaUpdateWrapper<ProductDictItem>()
            .eq(ProductDictItem::getDictItemId, id)
            .set(ProductDictItem::getStatus, status)) > 0;
    }

    @Override
    public BaseEditCheckResultVo checkEditAllowed(Long id) {
        ProductDictItem entity = dictItemMapper.selectById(id);
        if (entity == null) {
            return deniedEditCheck(null, "product.base.edit.notFound", null);
        }
        ReferenceCheckResultVo references = checkReferences(id);
        if (isDictLocked(entity.getSystemFlag(), entity.getEditableFlag())) {
            return deniedEditCheck(entity.getStatus(), "product.dict.notEditable", references);
        }
        return editCheckResult(entity.getStatus(), references);
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long id) {
        ProductDictItem entity = dictItemMapper.selectById(id);
        if (entity != null && Boolean.TRUE.equals(entity.getSystemFlag())) {
            return referenceResult(1, "product.dict.systemItemCannotDelete", "System dictionary item");
        }
        if (entity == null || StringUtils.isBlank(entity.getDictTypeCode()) || StringUtils.isBlank(entity.getDictItemValue())) {
            return referenceResult(0, null, null);
        }
        return checkBusinessReferences(entity.getDictTypeCode(), entity.getDictItemValue());
    }

    private QueryWrapper<ProductDictItem> buildQueryWrapper(ProductDictItemBo bo) {
        QueryWrapper<ProductDictItem> q = activeQuery(ProductDictItem.class);
        if (bo != null) {
            eq(q, "dict_type_code", bo.getDictTypeCode());
            like(q, "dict_item_value", bo.getDictItemValue());
            if (StringUtils.isNotBlank(bo.getDictItemLabelCn())) {
                q.and(wrapper -> wrapper.like("dict_item_label_cn", bo.getDictItemLabelCn()).or().like("dict_item_label_en", bo.getDictItemLabelCn()));
            }
            eq(q, "parent_value", bo.getParentValue());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void validateDictItem(ProductDictItemBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getDictTypeCode()) || StringUtils.isBlank(bo.getDictItemValue())) {
            throw ServiceException.ofMessageKey("product.dict.itemValueRequired");
        }
        long typeCount = dictTypeMapper.selectCount(activeQuery(ProductDictType.class).eq("dict_type_code", bo.getDictTypeCode()));
        if (typeCount <= 0) {
            throw ServiceException.ofMessageKey("product.dict.typeNotFound");
        }
        QueryWrapper<ProductDictItem> q = activeQuery(ProductDictItem.class)
            .eq("dict_type_code", bo.getDictTypeCode())
            .eq("dict_item_value", bo.getDictItemValue());
        if (bo.getDictItemId() != null) {
            q.ne("dict_item_id", bo.getDictItemId());
        }
        if (dictItemMapper.selectCount(q) > 0) {
            throw ServiceException.ofMessageKey("product.dict.itemValueExists");
        }
    }

    private ProductDictOptionVo toOption(ProductDictItemVo row) {
        ProductDictOptionVo vo = new ProductDictOptionVo();
        vo.setLabel(row.getDictItemLabelCn());
        vo.setLabelCn(row.getDictItemLabelCn());
        vo.setLabelEn(row.getDictItemLabelEn());
        vo.setValue(row.getDictItemValue());
        vo.setDictTypeCode(row.getDictTypeCode());
        vo.setParentValue(row.getParentValue());
        return vo;
    }

    private void assertDictEditable(Boolean systemFlag, Boolean editableFlag) {
        if (isDictLocked(systemFlag, editableFlag)) {
            throw ServiceException.ofMessageKey("product.dict.notEditable");
        }
    }

    private boolean isDictLocked(Boolean systemFlag, Boolean editableFlag) {
        return Boolean.TRUE.equals(systemFlag) && Boolean.FALSE.equals(editableFlag);
    }

    private ReferenceCheckResultVo checkBusinessReferences(String dictTypeCode, String value) {
        long count = switch (dictTypeCode) {
            case "product_unit_type" -> unitMapper.selectCount(activeQuery(ProductUnit.class).eq("unit_type", value));
            case "product_business_type" -> categoryMapper.selectCount(activeQuery(ProductCategory.class).eq("business_type", value));
            case "product_asset_type" -> mediaAssetMapper.selectCount(activeQuery(ProductMediaAsset.class).eq("asset_type", value));
            case "product_type" -> formulaMapper.selectCount(activeQuery(ProductFormula.class).eq("product_type_code", value));
            default -> 0L;
        };
        return referenceResult(count, "product.dict.itemHasReferences", "Dictionary item references: " + count);
    }
}
