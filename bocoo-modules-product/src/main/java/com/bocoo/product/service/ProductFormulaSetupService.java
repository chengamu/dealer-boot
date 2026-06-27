package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ProductFormulaSetupBo;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;

import java.util.Map;

public interface ProductFormulaSetupService {

    ProductFormulaSetupVo querySetup(Long formulaId);

    Boolean saveSetup(Long formulaId, ProductFormulaSetupBo bo);

    Boolean validateSetup(Long formulaId);

    int materialCount(Long formulaId);

    String validationMessageKey(Long formulaId);

    Map<String, Object> snapshot(Long formulaId);
}
