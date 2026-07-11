package com.bocoo.product.service.impl;

import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceMaterial;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.mapper.ProductPriceMaterialMapper;
import com.bocoo.product.mapper.ProductPriceMaterialRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductPriceMaterialSyncServiceTest {

    @Mock
    private ProductPriceMaterialMapper materialMapper;
    @Mock
    private ProductPriceMaterialRuleMapper ruleMapper;
    @Mock
    private ProductPriceSettingMapper settingMapper;

    private ProductPriceMaterialSyncService service;
    private final List<ProductPriceMaterial> insertedMaterials = new ArrayList<>();
    private final List<ProductPriceMaterialRule> insertedRules = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new ProductPriceMaterialSyncService(
            materialMapper, ruleMapper, settingMapper, new ProductPriceSnapshotReader(),
            new ProductPriceConditionSnapshotFactory(new ProductPriceSnapshotReader()));
        when(materialMapper.insert(any())).thenAnswer(invocation -> {
            ProductPriceMaterial row = invocation.getArgument(0);
            row.setPriceMaterialId(9200L + insertedMaterials.size() + 1);
            insertedMaterials.add(row);
            return 1;
        });
        when(ruleMapper.insert(any())).thenAnswer(invocation -> {
            insertedRules.add(invocation.getArgument(0));
            return 1;
        });
    }

    @Test
    void synchronizesEveryFormulaMaterialWithGroupSpecificDefaultFormula() {
        when(materialMapper.selectList(any())).thenAnswer(invocation ->
            insertedMaterials.isEmpty() ? List.of() : List.copyOf(insertedMaterials));
        when(settingMapper.selectList(any())).thenReturn(List.of());

        List<ProductPriceMaterial> result = service.sync(setting(9101L, 7001L), version(), false);

        assertThat(result).extracting(ProductPriceMaterial::getMaterialCode)
            .containsExactly("MAT-FABRIC", "MAT-BAR");
        assertThat(insertedRules).extracting(ProductPriceMaterialRule::getPriceFormula)
            .containsExactly("unitPrice * MAX(areaM2, 1)", "unitPrice * usageQty");
        verify(ruleMapper, never()).selectCount(any());
    }

    @Test
    void newVersionCopiesDefaultAndConditionalRulesFromPreviousVersion() {
        ProductPriceSetting previous = setting(9001L, 6001L);
        ProductPriceMaterial oldMaterial = material(9100L, 9001L);
        ProductPriceMaterialRule defaultRule = rule(9100L, "DEFAULT", true, new BigDecimal("20"));
        ProductPriceMaterialRule conditionRule = rule(9100L, "width > 20", false, new BigDecimal("35"));
        ProductPriceMaterialRule removedOptionRule = rule(
            9100L, "option_OLD_REF == \"OLD_VALUE_REF\"", false, new BigDecimal("99"));
        AtomicInteger materialQuery = new AtomicInteger();
        when(settingMapper.selectList(any())).thenReturn(List.of(previous));
        when(materialMapper.selectList(any())).thenAnswer(invocation -> {
            int call = materialQuery.incrementAndGet();
            if (call <= 2) return List.of();
            if (call == 3) return List.of(oldMaterial);
            return List.copyOf(insertedMaterials);
        });
        when(ruleMapper.selectList(any())).thenReturn(List.of(defaultRule, conditionRule, removedOptionRule));

        service.sync(setting(9101L, 7001L), fabricOnlyVersion(), false);

        assertThat(insertedRules).hasSize(2);
        assertThat(insertedRules).extracting(ProductPriceMaterialRule::getConditionKey)
            .containsExactly("DEFAULT", "width > 20");
        assertThat(insertedRules).extracting(ProductPriceMaterialRule::getUnitPrice)
            .containsExactly(new BigDecimal("20"), new BigDecimal("35"));
    }

    private ProductPriceSetting setting(Long settingId, Long versionId) {
        ProductPriceSetting setting = new ProductPriceSetting();
        setting.setTenantId(1L);
        setting.setPriceSettingId(settingId);
        setting.setSaleProductId(8001L);
        setting.setFormulaVersionId(versionId);
        return setting;
    }

    private ProductPriceMaterial material(Long id, Long settingId) {
        ProductPriceMaterial material = new ProductPriceMaterial();
        material.setPriceMaterialId(id);
        material.setPriceSettingId(settingId);
        material.setMaterialId(4001L);
        material.setMaterialCode("MAT-FABRIC");
        return material;
    }

    private ProductPriceMaterialRule rule(Long materialId, String condition, boolean defaultRule, BigDecimal price) {
        ProductPriceMaterialRule rule = new ProductPriceMaterialRule();
        rule.setPriceMaterialId(materialId);
        rule.setConditionType(defaultRule ? "DEFAULT" : "EXPRESSION");
        rule.setConditionJson(defaultRule ? "{\"type\":\"DEFAULT\"}" : "{\"type\":\"EXPRESSION\"}");
        rule.setConditionExpression(condition);
        rule.setConditionText(condition);
        rule.setConditionKey(condition);
        rule.setUnitPrice(price);
        rule.setPriceFormula("unitPrice * MAX(areaM2, 1)");
        rule.setDefaultRuleFlag(defaultRule);
        return rule;
    }

    private ProductFormulaVersion version() {
        ProductFormulaVersion version = fabricOnlyVersion();
        version.setSetupSnapshotJson("""
            {"materials":[{"materialId":4001,"attributeGroupCode":"FABRIC","materialCode":"MAT-FABRIC","materialNameCn":"面料","unitCode":"m2"},{"materialId":4002,"attributeGroupCode":"ALUMINUM","materialCode":"MAT-BAR","materialNameCn":"下杆","unitCode":"m"}],"priceSnapshot":[{"materialCode":"MAT-FABRIC","salesPrice":20},{"materialCode":"MAT-BAR","salesPrice":3}]}
            """);
        return version;
    }

    private ProductFormulaVersion fabricOnlyVersion() {
        ProductFormulaVersion version = new ProductFormulaVersion();
        version.setVersionId(7001L);
        version.setVersionStatus("EFFECTIVE");
        version.setSetupSnapshotJson("""
            {"materials":[{"materialId":4001,"attributeGroupCode":"FABRIC","materialCode":"MAT-FABRIC","materialNameCn":"面料","unitCode":"m2"}],"priceSnapshot":[{"materialCode":"MAT-FABRIC","salesPrice":20}]}
            """);
        return version;
    }
}
