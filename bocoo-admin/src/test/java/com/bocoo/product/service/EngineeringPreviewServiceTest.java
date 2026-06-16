package com.bocoo.product.service;

import com.bocoo.product.domain.entity.EngineeringItem;
import com.bocoo.product.domain.entity.EngineeringItemScope;
import com.bocoo.product.domain.entity.EngineeringOutputRule;
import com.bocoo.product.domain.entity.EngineeringRule;
import com.bocoo.product.mapper.EngineeringCheckCaseMapper;
import com.bocoo.product.mapper.EngineeringItemMapper;
import com.bocoo.product.mapper.EngineeringItemScopeMapper;
import com.bocoo.product.mapper.EngineeringOutputRuleMapper;
import com.bocoo.product.mapper.EngineeringRuleMapper;
import com.bocoo.product.mapper.FabricSeriesMapper;
import com.bocoo.product.mapper.ProductComponentMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.StandardSkuEngineeringMapper;
import com.bocoo.product.service.impl.EngineeringDataReader;
import com.bocoo.product.service.impl.EngineeringPreviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EngineeringPreviewServiceTest {

    @Mock
    private EngineeringItemMapper itemMapper;
    @Mock
    private EngineeringItemScopeMapper scopeMapper;
    @Mock
    private EngineeringRuleMapper ruleMapper;
    @Mock
    private EngineeringOutputRuleMapper outputRuleMapper;
    @Mock
    private StandardSkuEngineeringMapper standardSkuEngineeringMapper;
    @Mock
    private EngineeringCheckCaseMapper checkCaseMapper;
    @Mock
    private ProductMaterialMapper materialMapper;
    @Mock
    private ProductComponentMapper componentMapper;
    @Mock
    private FabricSeriesMapper fabricSeriesMapper;

    private EngineeringDataReader dataReader;

    private EngineeringPreviewServiceImpl engineeringPreviewService;

    @BeforeEach
    void setUp() {
        dataReader = new EngineeringDataReader(
            itemMapper,
            scopeMapper,
            ruleMapper,
            outputRuleMapper,
            standardSkuEngineeringMapper,
            checkCaseMapper,
            materialMapper,
            componentMapper,
            fabricSeriesMapper
        );
        engineeringPreviewService = new EngineeringPreviewServiceImpl(dataReader);
    }

    @Test
    void previewDisablesOversizeChainAndOutputsMotorComponent() {
        Long versionId = 246101L;
        EngineeringItem systemItem = item(246301L, versionId, "SYSTEM", "系统", "System", true);
        EngineeringItemScope chainScope = scope(246401L, versionId, 246301L, "MANUAL", "CHAIN_ROLLER_WHITE", "拉珠");
        EngineeringItemScope motorScope = scope(246402L, versionId, 246301L, "MANUAL", "MOTOR_AOK_45_ZIGBEE", "奥克电机");
        EngineeringRule widthRule = rule(versionId);
        EngineeringOutputRule motorOutput = outputRule(versionId);

        when(itemMapper.selectList(any())).thenReturn(List.of(systemItem));
        when(scopeMapper.selectList(any())).thenReturn(List.of(chainScope, motorScope));
        when(ruleMapper.selectList(any())).thenReturn(List.of(widthRule));
        when(outputRuleMapper.selectList(any())).thenReturn(List.of(motorOutput));

        Map<String, Object> result = engineeringPreviewService.preview(Map.of(
            "versionId", versionId,
            "width", 260,
            "height", 160,
            "selectedItems", Map.of("SYSTEM", "MOTOR_AOK_45_ZIGBEE")
        ));

        assertThat(result.get("resultStatus")).isEqualTo("WARNING");
        assertThat((List<?>) result.get("missingItems")).isEmpty();
        assertThat(((Map<?, ?>) result.get("availableOptions")).containsKey("SYSTEM")).isTrue();
        assertThat((List<?>) result.get("disabledOptions")).singleElement()
            .satisfies(row -> assertThat(((Map<?, ?>) row).get("scopeCode")).isEqualTo("CHAIN_ROLLER_WHITE"));
        assertThat((List<?>) result.get("outputComponents")).singleElement()
            .satisfies(row -> assertThat(((Map<?, ?>) row).get("outputCode")).isEqualTo("COMP_ROLLER_MOTOR"));
    }

    @Test
    void previewSupportsRequireItemMediaHintAndBlockerActions() {
        Long versionId = 246102L;
        EngineeringItem systemItem = item(246501L, versionId, "SYSTEM", "系统", "System", true);
        EngineeringItem accessoryItem = item(246502L, versionId, "ACCESSORY", "配件", "Accessory", false);
        EngineeringRule requireRule = ruleWithAction(
            versionId,
            "MOTOR_REQUIRE_ACCESSORY",
            "{\"all\":[{\"left\":\"selected.SYSTEM\",\"op\":\"EQ\",\"right\":\"MOTOR_AOK_45_ZIGBEE\"}]}",
            "{\"type\":\"REQUIRE_ITEM\",\"itemCode\":\"ACCESSORY\"}",
            "WARNING"
        );
        EngineeringRule mediaRule = ruleWithAction(
            versionId,
            "MOTOR_MEDIA_HINT",
            "{\"all\":[{\"left\":\"selected.SYSTEM\",\"op\":\"EQ\",\"right\":\"MOTOR_AOK_45_ZIGBEE\"}]}",
            "{\"type\":\"MEDIA_HINT\",\"assetCode\":\"ASSET_MOTOR_SPEC\",\"assetNameCn\":\"电机规格书\"}",
            "INFO"
        );
        EngineeringRule blockerRule = ruleWithAction(
            versionId,
            "THICKNESS_BLOCK",
            "{\"all\":[{\"left\":\"input.combinedThickness\",\"op\":\"GT\",\"right\":0.95}]}",
            "{\"type\":\"BLOCK\"}",
            "BLOCKER"
        );

        when(itemMapper.selectList(any())).thenReturn(List.of(systemItem, accessoryItem));
        when(scopeMapper.selectList(any())).thenReturn(List.of());
        when(ruleMapper.selectList(any())).thenReturn(List.of(requireRule, mediaRule, blockerRule));
        when(outputRuleMapper.selectList(any())).thenReturn(List.of());

        Map<String, Object> result = engineeringPreviewService.preview(Map.of(
            "versionId", versionId,
            "combinedThickness", 1.2,
            "selectedItems", Map.of("SYSTEM", "MOTOR_AOK_45_ZIGBEE")
        ));

        assertThat(result.get("resultStatus")).isEqualTo("BLOCKER");
        assertThat((List<?>) result.get("missingItems")).anySatisfy(row -> assertThat(((Map<?, ?>) row).get("itemCode")).isEqualTo("ACCESSORY"));
        assertThat((List<?>) result.get("outputMediaAssets")).anySatisfy(row -> assertThat(((Map<?, ?>) row).get("outputCode")).isEqualTo("ASSET_MOTOR_SPEC"));
        assertThat((List<?>) result.get("blockers")).singleElement()
            .satisfies(row -> assertThat(((Map<?, ?>) row).get("code")).isEqualTo("THICKNESS_BLOCK"));
    }

    private EngineeringItem item(Long itemId, Long versionId, String code, String nameCn, String nameEn, boolean required) {
        EngineeringItem item = new EngineeringItem();
        item.setItemId(itemId);
        item.setVersionId(versionId);
        item.setItemCode(code);
        item.setItemNameCn(nameCn);
        item.setItemNameEn(nameEn);
        item.setRequiredFlag(required ? "1" : "0");
        item.setStatus("ENABLED");
        return item;
    }

    private EngineeringItemScope scope(Long scopeId, Long versionId, Long itemId, String type, String code, String nameCn) {
        EngineeringItemScope scope = new EngineeringItemScope();
        scope.setScopeId(scopeId);
        scope.setVersionId(versionId);
        scope.setItemId(itemId);
        scope.setScopeType(type);
        scope.setScopeCode(code);
        scope.setScopeNameCn(nameCn);
        scope.setIncludeFlag("INCLUDE");
        scope.setStatus("ENABLED");
        return scope;
    }

    private EngineeringRule rule(Long versionId) {
        EngineeringRule rule = new EngineeringRule();
        rule.setVersionId(versionId);
        rule.setRuleCode("WIDTH_DISABLE_CHAIN");
        rule.setRuleNameCn("超宽禁用拉珠");
        rule.setRuleType("DIMENSION_LIMIT");
        rule.setSeverity("WARNING");
        rule.setConditionJson("""
            {"all":[{"left":"input.width","op":"GT","right":250}]}
            """);
        rule.setActionJson("""
            {"type":"DISABLE_OPTION","itemCode":"SYSTEM","scopeCode":"CHAIN_ROLLER_WHITE"}
            """);
        rule.setMessageCn("宽度超过 250cm 时不能选择拉珠系统");
        rule.setMessageEn("Chain system is unavailable when width is over 250cm");
        rule.setStatus("ENABLED");
        return rule;
    }

    private EngineeringOutputRule outputRule(Long versionId) {
        EngineeringOutputRule outputRule = new EngineeringOutputRule();
        outputRule.setVersionId(versionId);
        outputRule.setRuleCode("MOTOR_OUTPUT");
        outputRule.setRuleNameCn("电机带出组件");
        outputRule.setOutputType("COMPONENT");
        outputRule.setOutputCode("COMP_ROLLER_MOTOR");
        outputRule.setOutputNameCn("电动卷帘系统组件包");
        outputRule.setDefaultQty(BigDecimal.ONE);
        outputRule.setUnitCode("SET");
        outputRule.setConditionJson("""
            {"all":[{"left":"selected.SYSTEM","op":"EQ","right":"MOTOR_AOK_45_ZIGBEE"}]}
            """);
        outputRule.setStatus("ENABLED");
        return outputRule;
    }

    private EngineeringRule ruleWithAction(Long versionId, String code, String conditionJson, String actionJson, String severity) {
        EngineeringRule rule = new EngineeringRule();
        rule.setVersionId(versionId);
        rule.setRuleCode(code);
        rule.setRuleNameCn(code);
        rule.setRuleType("OPTION_LIMIT");
        rule.setSeverity(severity);
        rule.setConditionJson(conditionJson);
        rule.setActionJson(actionJson);
        rule.setMessageCn(code + " message");
        rule.setMessageEn(code + " message");
        rule.setStatus("ENABLED");
        return rule;
    }
}
