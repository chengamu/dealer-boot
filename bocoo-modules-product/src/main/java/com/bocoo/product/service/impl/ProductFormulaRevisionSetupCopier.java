package com.bocoo.product.service.impl;

import com.bocoo.product.domain.bo.ProductFormulaSetupBo;
import com.bocoo.product.service.ProductFormulaSetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ProductFormulaRevisionSetupCopier {

    private final ProductFormulaSetupService setupService;
    private final ProductFormulaSetupNormalizer setupNormalizer;
    private final ProductFormulaSetupWriter setupWriter;
    private final ProductFormulaSnapshotJson snapshotJson;

    void restoreFromSnapshot(Long formulaId, String snapshot) {
        ProductFormulaSetupBo setupBo = setupFromSnapshot(snapshot);
        setupWriter.replace(formulaId, setupNormalizer.normalize(formulaId, setupBo));
    }

    ProductFormulaSetupRows copyCurrentSetup(Long targetFormulaId, Long sourceFormulaId, String currentVersionSnapshot) {
        ProductFormulaSetupBo setupBo = currentVersionSnapshot == null
            ? currentSetup(sourceFormulaId)
            : setupFromSnapshot(currentVersionSnapshot);
        clearSetupIds(setupBo);
        ProductFormulaSetupRows rows = setupNormalizer.normalize(targetFormulaId, setupBo);
        setupWriter.replace(targetFormulaId, rows);
        return rows;
    }

    private ProductFormulaSetupBo setupFromSnapshot(String snapshot) {
        ProductFormulaSetupBo setupBo = snapshotJson.fromJson(snapshot, ProductFormulaSetupBo.class);
        return setupBo == null ? new ProductFormulaSetupBo() : setupBo;
    }

    private ProductFormulaSetupBo currentSetup(Long formulaId) {
        ProductFormulaSetupBo setupBo = snapshotJson.fromJson(snapshotJson.toJson(setupService.snapshot(formulaId)), ProductFormulaSetupBo.class);
        return setupBo == null ? new ProductFormulaSetupBo() : setupBo;
    }

    private void clearSetupIds(ProductFormulaSetupBo bo) {
        bo.getMaterials().forEach(row -> row.setFormulaMaterialId(null));
        bo.getOptions().forEach(row -> row.setOptionId(null));
        bo.getOptionValues().forEach(row -> {
            row.setOptionValueId(null);
            row.setOptionId(null);
        });
        bo.getOptionMaterials().forEach(row -> {
            row.setOptionMaterialId(null);
            row.setOptionId(null);
            row.setOptionValueId(null);
            row.setFormulaMaterialId(null);
        });
        bo.getRestrictions().forEach(row -> row.setRestrictionId(null));
        bo.getUsageRules().forEach(row -> {
            row.setUsageRuleId(null);
            row.setFormulaMaterialId(null);
        });
        bo.getVariables().forEach(row -> row.setVariableId(null));
        bo.getVariableRules().forEach(row -> {
            row.setRuleId(null);
            row.setVariableId(null);
        });
    }
}
