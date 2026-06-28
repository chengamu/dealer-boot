package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ProductFormulaSimulationBo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationVo;

public interface ProductFormulaSimulationService {

    ProductFormulaSimulationVo query(Long formulaId);

    ProductFormulaSimulationVo run(Long formulaId, ProductFormulaSimulationBo bo);

    Boolean validate(Long formulaId, ProductFormulaSimulationBo bo);
}
