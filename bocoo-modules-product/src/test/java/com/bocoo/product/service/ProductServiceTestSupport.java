package com.bocoo.product.service;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.product.domain.bo.ProductCategoryBo;
import com.bocoo.product.domain.bo.ProductComponentBo;
import com.bocoo.product.domain.bo.ProductComponentItemBo;
import com.bocoo.product.domain.bo.ProductMaterialBo;
import com.bocoo.product.domain.bo.ProductMaterialTypeBo;
import com.bocoo.product.domain.bo.ProductMaterialTypeGroupBo;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.entity.ProductComponent;
import com.bocoo.product.domain.entity.ProductComponentItem;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductMaterialType;
import com.bocoo.product.domain.entity.ProductMaterialTypeGroup;
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
        prepareTableInfo(ProductComponent.class);
        prepareTableInfo(ProductComponentItem.class);
        prepareTableInfo(ProductMaterial.class);
        prepareTableInfo(ProductMaterialType.class);
        prepareTableInfo(ProductMaterialTypeGroup.class);
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
        lenient().when(converter.convert(any(ProductComponentBo.class), eq(ProductComponent.class))).thenAnswer(invocation -> {
            ProductComponentBo source = invocation.getArgument(0);
            ProductComponent target = new ProductComponent();
            target.setComponentId(source.getComponentId());
            target.setComponentCode(source.getComponentCode());
            target.setComponentNameCn(source.getComponentNameCn());
            target.setStatus(source.getStatus());
            return target;
        });
        lenient().when(converter.convert(any(ProductComponentItemBo.class), eq(ProductComponentItem.class))).thenAnswer(invocation -> {
            ProductComponentItemBo source = invocation.getArgument(0);
            ProductComponentItem target = new ProductComponentItem();
            target.setComponentItemId(source.getComponentItemId());
            target.setComponentId(source.getComponentId());
            target.setMaterialId(source.getMaterialId());
            target.setSortOrder(source.getSortOrder());
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
            target.setManufacturerCode(source.getManufacturerCode());
            target.setManufacturerName(source.getManufacturerName());
            target.setManufacturerItemNo(source.getManufacturerItemNo());
            target.setModel(source.getModel());
            target.setSpec(source.getSpec());
            target.setSpecModelText(source.getSpecModelText());
            target.setColorName(source.getColorName());
            target.setWeightValue(source.getWeightValue());
            target.setUnitPrice(source.getUnitPrice());
            target.setAuditStatus(source.getAuditStatus());
            target.setAuditById(source.getAuditById());
            target.setAuditBy(source.getAuditBy());
            target.setAuditTime(source.getAuditTime());
            target.setSortOrder(source.getSortOrder());
            target.setStatus(source.getStatus());
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
