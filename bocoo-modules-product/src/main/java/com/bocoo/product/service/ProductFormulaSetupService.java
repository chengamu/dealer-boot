package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ProductFormulaSetupBo;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;

import java.util.Map;

public interface ProductFormulaSetupService {

    ProductFormulaSetupVo querySetup(Long formulaId);

    Boolean saveSetup(Long formulaId, ProductFormulaSetupBo bo);

    Boolean saveMaterials(Long formulaId, ProductFormulaSetupBo bo);

    Boolean saveOptions(Long formulaId, ProductFormulaSetupBo bo);

    Boolean validateSetup(Long formulaId);

    Boolean validateMaterials(Long formulaId);

    Boolean validateOptions(Long formulaId);

    int materialCount(Long formulaId);

    String validationMessageKey(Long formulaId);

    String materialValidationMessageKey(Long formulaId);

    String optionValidationMessageKey(Long formulaId);

    Map<String, Object> snapshot(Long formulaId);
}
