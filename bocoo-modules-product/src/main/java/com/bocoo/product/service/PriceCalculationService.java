package com.bocoo.product.service;

import com.bocoo.product.domain.bo.PriceCalculationBo;
import com.bocoo.product.domain.vo.PriceCalculationResultVo;

public interface PriceCalculationService {

    PriceCalculationResultVo calculate(PriceCalculationBo bo);
}
