package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductPublishPackageBo;
import com.bocoo.product.domain.entity.ProductPublishPackage;
import com.bocoo.product.domain.vo.ProductPublishPackageVo;
import com.bocoo.product.mapper.ProductPublishPackageMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductPublishPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPublishPackageServiceImpl implements ProductPublishPackageService {

    private final ProductPublishPackageMapper packageMapper;

    @Override
    public TableDataInfo<ProductPublishPackageVo> queryPageList(ProductPublishPackageBo bo, PageQuery pageQuery) {
        Page<ProductPublishPackageVo> result = packageMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<ProductPublishPackageVo> queryList(ProductPublishPackageBo bo) {
        return packageMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public ProductPublishPackageVo queryById(Long id) {
        return packageMapper.selectVoById(id);
    }

    @Override
    public Boolean save(ProductPublishPackageBo bo) {
        ProductPublishPackage entity = MapstructUtils.convert(bo, ProductPublishPackage.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getPackageId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return packageMapper.insert(entity) > 0;
        }
        return packageMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return packageMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<ProductPublishPackage> buildQueryWrapper(ProductPublishPackageBo bo) {
        if (bo == null) {
            bo = new ProductPublishPackageBo();
        }
        LambdaQueryWrapper<ProductPublishPackage> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getPackageCode()), ProductPublishPackage::getPackageCode, bo.getPackageCode());
        lqw.like(StringUtils.isNotBlank(bo.getPackageType()), ProductPublishPackage::getPackageType, bo.getPackageType());
        lqw.like(StringUtils.isNotBlank(bo.getProductModelCode()), ProductPublishPackage::getProductModelCode, bo.getProductModelCode());
        lqw.like(StringUtils.isNotBlank(bo.getSalesVariantCode()), ProductPublishPackage::getSalesVariantCode, bo.getSalesVariantCode());
        lqw.like(StringUtils.isNotBlank(bo.getPricePlanCode()), ProductPublishPackage::getPricePlanCode, bo.getPricePlanCode());
        lqw.like(StringUtils.isNotBlank(bo.getPackageStatus()), ProductPublishPackage::getPackageStatus, bo.getPackageStatus());
        lqw.like(StringUtils.isNotBlank(bo.getPackageHash()), ProductPublishPackage::getPackageHash, bo.getPackageHash());
        return lqw;
    }
}
