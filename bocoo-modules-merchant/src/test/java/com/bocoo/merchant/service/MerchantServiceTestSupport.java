package com.bocoo.merchant.service;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.merchant.domain.bo.CustomerProfileBo;
import com.bocoo.merchant.domain.bo.MerchantLevelBo;
import com.bocoo.merchant.domain.bo.MerchantLevelDiscountBo;
import com.bocoo.merchant.domain.entity.CustomerProfile;
import com.bocoo.merchant.domain.entity.MerchantLevel;
import com.bocoo.merchant.domain.entity.MerchantLevelDiscount;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.entity.ProductDictItem;
import com.bocoo.system.domain.entity.MerchantProfile;
import com.bocoo.system.domain.entity.SysUser;
import io.github.linpeilie.Converter;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.springframework.context.support.GenericApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

final class MerchantServiceTestSupport {

    private static boolean contextPrepared;

    private MerchantServiceTestSupport() {
    }

    static synchronized void prepare() {
        prepareTableInfo(MerchantLevel.class);
        prepareTableInfo(MerchantLevelDiscount.class);
        prepareTableInfo(CustomerProfile.class);
        prepareTableInfo(ProductCategory.class);
        prepareTableInfo(ProductDictItem.class);
        prepareTableInfo(MerchantProfile.class);
        prepareTableInfo(SysUser.class);
        if (contextPrepared) {
            return;
        }
        Converter converter = mock(Converter.class);
        lenient().when(converter.convert(any(MerchantLevelBo.class), eq(MerchantLevel.class))).thenAnswer(invocation -> {
            MerchantLevelBo source = invocation.getArgument(0);
            MerchantLevel target = new MerchantLevel();
            target.setLevelId(source.getLevelId());
            target.setTenantId(source.getTenantId());
            target.setLevelCode(source.getLevelCode());
            target.setLevelName(source.getLevelName());
            target.setDefaultDiscountRate(source.getDefaultDiscountRate());
            target.setDefaultCreditLimit(source.getDefaultCreditLimit());
            target.setDefaultFlag(source.getDefaultFlag());
            target.setStatus(source.getStatus());
            target.setDelFlag(source.getDelFlag());
            return target;
        });
        lenient().when(converter.convert(any(MerchantLevelDiscountBo.class), eq(MerchantLevelDiscount.class))).thenAnswer(invocation -> {
            MerchantLevelDiscountBo source = invocation.getArgument(0);
            MerchantLevelDiscount target = new MerchantLevelDiscount();
            target.setDiscountId(source.getDiscountId());
            target.setLevelId(source.getLevelId());
            target.setLevelCode(source.getLevelCode());
            target.setLevelName(source.getLevelName());
            target.setCategoryId(source.getCategoryId());
            target.setCategoryCode(source.getCategoryCode());
            target.setCategoryNameCn(source.getCategoryNameCn());
            target.setProductTypeCode(source.getProductTypeCode());
            target.setProductTypeNameCn(source.getProductTypeNameCn());
            target.setDiscountRate(source.getDiscountRate());
            target.setStatus(source.getStatus());
            target.setDelFlag(source.getDelFlag());
            return target;
        });
        lenient().when(converter.convert(any(CustomerProfileBo.class), eq(CustomerProfile.class))).thenAnswer(invocation -> {
            CustomerProfileBo source = invocation.getArgument(0);
            CustomerProfile target = new CustomerProfile();
            target.setCustomerId(source.getCustomerId());
            target.setTenantId(source.getTenantId());
            target.setMerchantId(source.getMerchantId());
            target.setMerchantName(source.getMerchantName());
            target.setCustomerName(source.getCustomerName());
            target.setEmail(source.getEmail());
            target.setOwnerUserId(source.getOwnerUserId());
            target.setOwnerName(source.getOwnerName());
            target.setStatus(source.getStatus());
            target.setDelFlag(source.getDelFlag());
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
