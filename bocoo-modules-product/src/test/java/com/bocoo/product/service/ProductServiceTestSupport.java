package com.bocoo.product.service;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.product.domain.bo.ProductCategoryBo;
import com.bocoo.product.domain.bo.ProductMaterialBo;
import com.bocoo.product.domain.bo.ProductMaterialTypeBo;
import com.bocoo.product.domain.bo.ProductMaterialTypeGroupBo;
import com.bocoo.product.domain.bo.ProductManufacturerBo;
import com.bocoo.product.domain.bo.ProductMediaAssetBo;
import com.bocoo.product.domain.bo.ProductMediaBindingBo;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.entity.ProductChangeLog;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductMaterialType;
import com.bocoo.product.domain.entity.ProductMaterialTypeGroup;
import com.bocoo.product.domain.entity.ProductManufacturer;
import com.bocoo.product.domain.entity.ProductMediaAsset;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import io.github.linpeilie.Converter;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.springframework.context.support.GenericApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

final class ProductServiceTestSupport {

    private static boolean contextPrepared;

    private ProductServiceTestSupport() {
    }

    static synchronized void prepareMapperAndConverter() {
        prepareTableInfo(ProductCategory.class);
        prepareTableInfo(ProductChangeLog.class);
        prepareTableInfo(ProductMaterial.class);
        prepareTableInfo(ProductMaterialType.class);
        prepareTableInfo(ProductMaterialTypeGroup.class);
        prepareTableInfo(ProductManufacturer.class);
        prepareTableInfo(ProductMediaAsset.class);
        prepareTableInfo(ProductMediaBinding.class);
        if (contextPrepared) {
            return;
        }
        Converter converter = mock(Converter.class);
        lenient().when(converter.convert(any(ProductCategoryBo.class), eq(ProductCategory.class))).thenAnswer(invocation -> {
            ProductCategoryBo source = invocation.getArgument(0);
            ProductCategory target = new ProductCategory();
            target.setCategoryId(source.getCategoryId());
            target.setCategoryCode(source.getCategoryCode());
            target.setCategoryNameCn(source.getCategoryNameCn());
            target.setStatus(source.getStatus());
            return target;
        });
        lenient().when(converter.convert(any(ProductMaterialBo.class), eq(ProductMaterial.class))).thenAnswer(invocation -> {
            ProductMaterialBo source = invocation.getArgument(0);
            ProductMaterial target = new ProductMaterial();
            target.setMaterialId(source.getMaterialId());
            target.setMaterialCode(source.getMaterialCode());
            target.setMaterialNameCn(source.getMaterialNameCn());
            target.setMaterialNameEn(source.getMaterialNameEn());
            target.setMaterialTypeId(source.getMaterialTypeId());
            target.setMaterialTypeCode(source.getMaterialTypeCode());
            target.setMaterialTypeNameCn(source.getMaterialTypeNameCn());
            target.setAttributeGroupId(source.getAttributeGroupId());
            target.setAttributeGroupCode(source.getAttributeGroupCode());
            target.setAttributeGroupNameCn(source.getAttributeGroupNameCn());
            target.setMaterialType(source.getMaterialType());
            target.setUnitCode(source.getUnitCode());
            target.setSecondaryUnitCode(source.getSecondaryUnitCode());
            target.setManufacturerId(source.getManufacturerId());
            target.setManufacturerCode(source.getManufacturerCode());
            target.setManufacturerName(source.getManufacturerName());
            target.setManufacturerItemNo(source.getManufacturerItemNo());
            target.setModel(source.getModel());
            target.setSpec(source.getSpec());
            target.setSpecModelText(source.getSpecModelText());
            target.setColorName(source.getColorName());
            target.setWeightValue(source.getWeightValue());
            target.setUnitPrice(source.getUnitPrice());
            target.setAuditBy(source.getAuditBy());
            target.setAuditTime(source.getAuditTime());
            target.setSortOrder(source.getSortOrder());
            target.setStatus(source.getStatus());
            target.setRemark(source.getRemark());
            return target;
        });
        lenient().when(converter.convert(any(ProductManufacturerBo.class), eq(ProductManufacturer.class))).thenAnswer(invocation -> {
            ProductManufacturerBo source = invocation.getArgument(0);
            ProductManufacturer target = new ProductManufacturer();
            target.setManufacturerId(source.getManufacturerId());
            target.setManufacturerCode(source.getManufacturerCode());
            target.setManufacturerName(source.getManufacturerName());
            target.setManufacturerShortName(source.getManufacturerShortName());
            target.setManufacturerFlag(source.getManufacturerFlag());
            target.setSupplierFlag(source.getSupplierFlag());
            target.setContactName(source.getContactName());
            target.setContactPhone(source.getContactPhone());
            target.setAddress(source.getAddress());
            target.setStatus(source.getStatus());
            target.setSortOrder(source.getSortOrder());
            target.setRemark(source.getRemark());
            return target;
        });
        lenient().when(converter.convert(any(ProductMaterialTypeGroupBo.class), eq(ProductMaterialTypeGroup.class))).thenAnswer(invocation -> {
            ProductMaterialTypeGroupBo source = invocation.getArgument(0);
            ProductMaterialTypeGroup target = new ProductMaterialTypeGroup();
            target.setGroupId(source.getGroupId());
            target.setGroupCode(source.getGroupCode());
            target.setGroupNameCn(source.getGroupNameCn());
            target.setGroupNameEn(source.getGroupNameEn());
            target.setSystemFlag(source.getSystemFlag());
            target.setEditableFlag(source.getEditableFlag());
            target.setStatus(source.getStatus());
            target.setSortOrder(source.getSortOrder());
            target.setRemark(source.getRemark());
            return target;
        });
        lenient().when(converter.convert(any(ProductMaterialTypeBo.class), eq(ProductMaterialType.class))).thenAnswer(invocation -> {
            ProductMaterialTypeBo source = invocation.getArgument(0);
            ProductMaterialType target = new ProductMaterialType();
            target.setMaterialTypeId(source.getMaterialTypeId());
            target.setMaterialTypeCode(source.getMaterialTypeCode());
            target.setMaterialTypeNameCn(source.getMaterialTypeNameCn());
            target.setMaterialTypeNameEn(source.getMaterialTypeNameEn());
            target.setAttributeGroupId(source.getAttributeGroupId());
            target.setAttributeGroupCode(source.getAttributeGroupCode());
            target.setAttributeGroupNameCn(source.getAttributeGroupNameCn());
            target.setSystemFlag(source.getSystemFlag());
            target.setEditableFlag(source.getEditableFlag());
            target.setStatus(source.getStatus());
            target.setSortOrder(source.getSortOrder());
            target.setRemark(source.getRemark());
            return target;
        });
        lenient().when(converter.convert(any(ProductMediaAssetBo.class), eq(ProductMediaAsset.class))).thenAnswer(invocation -> {
            ProductMediaAssetBo source = invocation.getArgument(0);
            ProductMediaAsset target = new ProductMediaAsset();
            target.setAssetId(source.getAssetId());
            target.setAssetCode(source.getAssetCode());
            target.setAssetNameCn(source.getAssetNameCn());
            target.setAssetNameEn(source.getAssetNameEn());
            target.setAssetType(source.getAssetType());
            target.setUsageType(source.getUsageType());
            target.setLanguageCode(source.getLanguageCode());
            target.setVisibility(source.getVisibility());
            target.setOssId(source.getOssId());
            target.setUrl(source.getUrl());
            target.setAltText(source.getAltText());
            target.setVersionNo(source.getVersionNo());
            target.setStatus(source.getStatus());
            target.setDelFlag(source.getDelFlag());
            target.setRemark(source.getRemark());
            return target;
        });
        lenient().when(converter.convert(any(ProductMediaBindingBo.class), eq(ProductMediaBinding.class))).thenAnswer(invocation -> {
            ProductMediaBindingBo source = invocation.getArgument(0);
            ProductMediaBinding target = new ProductMediaBinding();
            target.setBindingId(source.getBindingId());
            target.setAssetId(source.getAssetId());
            target.setAssetCode(source.getAssetCode());
            target.setTargetType(source.getTargetType());
            target.setTargetId(source.getTargetId());
            target.setTargetCode(source.getTargetCode());
            target.setUsageType(source.getUsageType());
            target.setVisibility(source.getVisibility());
            target.setLanguageCode(source.getLanguageCode());
            target.setRequiredForPublish(source.getRequiredForPublish());
            target.setSortOrder(source.getSortOrder());
            target.setStatus(source.getStatus());
            target.setDelFlag(source.getDelFlag());
            target.setRemark(source.getRemark());
            return target;
        });
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(Converter.class, () -> converter);
        context.refresh();
        new SpringUtil().setApplicationContext(context);
        contextPrepared = true;
    }

    private static void prepareTableInfo(Class<?> entityClass) {
        try {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), entityClass);
        } catch (IllegalStateException ignored) {
            // Already initialized for this JVM.
        }
    }
}
