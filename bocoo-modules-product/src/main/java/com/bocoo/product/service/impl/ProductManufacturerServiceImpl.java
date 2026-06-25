package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductManufacturerBo;
import com.bocoo.product.domain.entity.ProductManufacturer;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductManufacturerVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductManufacturerMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductManufacturerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductManufacturerServiceImpl extends ProductServiceSupport implements ProductManufacturerService {

    private final ProductManufacturerMapper manufacturerMapper;
    private final ProductMaterialMapper materialMapper;

    @Override
    public TableDataInfo<ProductManufacturerVo> queryPageList(ProductManufacturerBo bo, PageQuery pageQuery) {
        return page(manufacturerMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "manufacturer_id"));
    }

    @Override
    public List<ProductManufacturerVo> queryList(ProductManufacturerBo bo) {
        return manufacturerMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "manufacturer_id")));
    }

    @Override
    public ProductManufacturerVo queryById(Long id) {
        return manufacturerMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductManufacturerBo bo) {
        normalizeManufacturer(bo);
        validateManufacturerCodeUnique(bo);
        ProductManufacturer entity = MapstructUtils.convert(bo, ProductManufacturer.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return manufacturerMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductManufacturerBo bo) {
        normalizeManufacturer(bo);
        validateManufacturerCodeUnique(bo);
        if (bo != null && bo.getManufacturerId() != null) {
            ProductManufacturer current = manufacturerMapper.selectById(bo.getManufacturerId());
            if (current != null) {
                assertNormalEditable(current.getStatus());
            }
        }
        ProductManufacturer entity = MapstructUtils.convert(bo, ProductManufacturer.class);
        return entity != null && manufacturerMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            ProductManufacturer current = manufacturerMapper.selectById(id);
            if (current != null) {
                assertDisabledBeforeDelete(current.getStatus());
            }
            assertNoReferences(checkReferences(id));
        }
        return remove(manufacturerMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return manufacturerMapper.update(null, new LambdaUpdateWrapper<ProductManufacturer>()
            .eq(ProductManufacturer::getManufacturerId, id)
            .set(ProductManufacturer::getStatus, status)) > 0;
    }

    @Override
    public BaseEditCheckResultVo checkEditAllowed(Long id) {
        ProductManufacturer manufacturer = manufacturerMapper.selectById(id);
        if (manufacturer == null) {
            return deniedEditCheck(null, "product.base.edit.notFound", null);
        }
        return editCheckResult(manufacturer.getStatus(), checkReferences(id));
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long manufacturerId) {
        long count = materialMapper.selectCount(activeQuery(ProductMaterial.class).eq("manufacturer_id", manufacturerId));
        return referenceResult(count, "product.manufacturer.hasReferences", "Material references: " + count);
    }

    private QueryWrapper<ProductManufacturer> buildQueryWrapper(ProductManufacturerBo bo) {
        QueryWrapper<ProductManufacturer> q = activeQuery(ProductManufacturer.class);
        if (bo != null) {
            like(q, "manufacturer_code", bo.getManufacturerCode());
            if (StringUtils.isNotBlank(bo.getManufacturerName())) {
                q.and(wrapper -> wrapper.like("manufacturer_name", bo.getManufacturerName())
                    .or().like("manufacturer_short_name", bo.getManufacturerName()));
            }
            eq(q, "manufacturer_flag", bo.getManufacturerFlag());
            eq(q, "supplier_flag", bo.getSupplierFlag());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void normalizeManufacturer(ProductManufacturerBo bo) {
        if (bo == null) {
            return;
        }
        bo.setManufacturerCode(trimToNull(bo.getManufacturerCode()));
        bo.setManufacturerName(trimToNull(bo.getManufacturerName()));
        bo.setManufacturerShortName(trimToNull(bo.getManufacturerShortName()));
        bo.setContactName(trimToNull(bo.getContactName()));
        bo.setContactPhone(trimToNull(bo.getContactPhone()));
        bo.setAddress(trimToNull(bo.getAddress()));
        bo.setStatus(trimToNull(bo.getStatus()));
        if (StringUtils.isBlank(bo.getManufacturerCode())) {
            throw ServiceException.ofMessageKey("product.manufacturer.codeRequired");
        }
        if (StringUtils.isBlank(bo.getManufacturerName())) {
            throw ServiceException.ofMessageKey("product.manufacturer.nameRequired");
        }
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(STATUS_DISABLED);
        }
        if (bo.getManufacturerFlag() == null) {
            bo.setManufacturerFlag(Boolean.TRUE);
        }
        if (bo.getSupplierFlag() == null) {
            bo.setSupplierFlag(Boolean.TRUE);
        }
    }

    private void validateManufacturerCodeUnique(ProductManufacturerBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getManufacturerCode())) {
            return;
        }
        long count = manufacturerMapper.selectCount(activeQuery(ProductManufacturer.class)
            .eq("manufacturer_code", bo.getManufacturerCode())
            .ne(bo.getManufacturerId() != null, "manufacturer_id", bo.getManufacturerId()));
        if (count > 0) {
            throw ServiceException.ofMessageKey("product.manufacturer.codeExists");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }
}
