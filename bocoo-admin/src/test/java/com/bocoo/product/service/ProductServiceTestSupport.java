package com.bocoo.product.service;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.product.domain.bo.ProductCategoryBo;
import com.bocoo.product.domain.bo.ProductComponentBo;
import com.bocoo.product.domain.bo.ProductComponentItemBo;
import com.bocoo.product.domain.bo.ProductMaterialBo;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.entity.ProductComponent;
import com.bocoo.product.domain.entity.ProductComponentItem;
import com.bocoo.product.domain.entity.ProductMaterial;
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
            target.setMaterialType(source.getMaterialType());
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
