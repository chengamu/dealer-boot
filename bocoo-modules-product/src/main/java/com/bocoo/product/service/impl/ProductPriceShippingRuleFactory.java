package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductPriceFeeRuleBo;
import com.bocoo.product.domain.entity.ProductPriceFeeRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.vo.ProductPriceFeeRuleVo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductPriceShippingRuleFactory {

    public static final String CODE_MANUAL = "MANUAL";
    public static final String CODE_MOTORIZED = "MOTORIZED";

    public List<ProductPriceFeeRuleVo> defaultVos(ProductPriceSetting setting) {
        return List.of(toVo(defaultRule(setting, CODE_MANUAL, "不带电邮费", "motorized = false", 0)),
            toVo(defaultRule(setting, CODE_MOTORIZED, "带电邮费", "motorized = true", 1)));
    }

    public ProductPriceFeeRule fromBo(ProductPriceFeeRuleBo bo, ProductPriceSetting setting, int index) {
        String code = normalizeCode(StringUtils.blankToDefault(bo.getFeeCode(), index == 0 ? CODE_MANUAL : CODE_MOTORIZED));
        ProductPriceFeeRule entity = defaultRule(setting, code, defaultName(code), defaultTrigger(code), index);
        entity.setFeeRuleId(bo.getFeeRuleId());
        entity.setFeeAmount(bo.getFeeAmount());
        entity.setFormulaText(bo.getFormulaText());
        entity.setRemark(bo.getRemark());
        return entity;
    }

    private ProductPriceFeeRule defaultRule(ProductPriceSetting setting, String code, String name, String trigger, int sort) {
        ProductPriceFeeRule entity = new ProductPriceFeeRule();
        entity.setTenantId(setting.getTenantId());
        entity.setPriceSettingId(setting.getPriceSettingId());
        entity.setSaleProductId(setting.getSaleProductId());
        entity.setFormulaVersionId(setting.getFormulaVersionId());
        entity.setFeeCode(code);
        entity.setFeeName(name);
        entity.setFeeCategory("SHIPPING");
        entity.setTriggerCondition(trigger);
        entity.setFeeMode("FORMULA");
        entity.setStatus(ProductServiceSupport.STATUS_ENABLED);
        entity.setSortOrder(sort);
        return entity;
    }

    private ProductPriceFeeRuleVo toVo(ProductPriceFeeRule entity) {
        ProductPriceFeeRuleVo vo = new ProductPriceFeeRuleVo();
        vo.setTenantId(entity.getTenantId());
        vo.setPriceSettingId(entity.getPriceSettingId());
        vo.setSaleProductId(entity.getSaleProductId());
        vo.setFormulaVersionId(entity.getFormulaVersionId());
        vo.setFeeCode(entity.getFeeCode());
        vo.setFeeName(entity.getFeeName());
        vo.setFeeCategory(entity.getFeeCategory());
        vo.setTriggerCondition(entity.getTriggerCondition());
        vo.setFeeMode(entity.getFeeMode());
        vo.setFeeAmount(entity.getFeeAmount());
        vo.setFormulaText(entity.getFormulaText());
        vo.setStatus(entity.getStatus());
        vo.setSortOrder(entity.getSortOrder());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private String defaultName(String code) {
        return CODE_MOTORIZED.equals(code) ? "带电邮费" : "不带电邮费";
    }

    private String defaultTrigger(String code) {
        return CODE_MOTORIZED.equals(code) ? "motorized = true" : "motorized = false";
    }

    private String normalizeCode(String code) {
        return CODE_MOTORIZED.equalsIgnoreCase(code) ? CODE_MOTORIZED : CODE_MANUAL;
    }
}
