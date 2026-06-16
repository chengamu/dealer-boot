package com.bocoo.product.service.impl;

import com.bocoo.product.domain.bo.PriceCalculationBo;
import com.bocoo.product.domain.bo.PriceRuleItemBo;
import com.bocoo.product.domain.vo.PriceCalculationResultVo;
import com.bocoo.product.service.PriceCalculationEngine;
import com.bocoo.product.service.PriceCalculationService;
import com.bocoo.product.service.PriceRuleItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PriceCalculationServiceImpl implements PriceCalculationService {

    private final PriceRuleItemService priceRuleItemService;
    private final PriceCalculationEngine priceCalculationEngine;

    @Override
    public PriceCalculationResultVo calculate(PriceCalculationBo bo) {
        if (bo == null) {
            bo = new PriceCalculationBo();
        }
        PriceRuleItemBo ruleBo = new PriceRuleItemBo();
        ruleBo.setPricePlanVersionId(bo.getPricePlanVersionId());
        ruleBo.setCurrencyCode(bo.getCurrencyCode());
        ruleBo.setStatus("1");
        return priceCalculationEngine.calculate(bo, priceRuleItemService.queryList(ruleBo));
    }
}
