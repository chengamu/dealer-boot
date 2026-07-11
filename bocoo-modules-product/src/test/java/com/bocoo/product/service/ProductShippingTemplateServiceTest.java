package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductShippingTemplateBo;
import com.bocoo.product.domain.bo.ProductShippingTemplateRuleBo;
import com.bocoo.product.domain.entity.ProductShippingTemplate;
import com.bocoo.product.domain.entity.ProductShippingTemplateRule;
import com.bocoo.product.domain.vo.ProductShippingTemplateRuleVo;
import com.bocoo.product.domain.vo.ProductShippingTemplateVo;
import com.bocoo.product.mapper.ProductShippingTemplateMapper;
import com.bocoo.product.mapper.ProductShippingTemplateRuleMapper;
import com.bocoo.product.service.impl.ProductShippingTemplateRuleImporter;
import com.bocoo.product.service.impl.ProductShippingTemplateRuleValidator;
import com.bocoo.product.service.impl.ProductShippingTemplateServiceImpl;
import com.bocoo.product.service.impl.ProductQuoteReferenceGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductShippingTemplateServiceTest {

    @Mock
    private ProductShippingTemplateMapper templateMapper;
    @Mock
    private ProductShippingTemplateRuleMapper ruleMapper;
    private ProductShippingTemplateServiceImpl service;

    @BeforeEach
    void setUp() {
        TenantContextHolder.setTenantId(1L);
        ProductServiceTestSupport.prepareMapperAndConverter();
        service = new ProductShippingTemplateServiceImpl(templateMapper, ruleMapper,
            new ProductShippingTemplateRuleValidator(), new ProductShippingTemplateRuleImporter(),
            new ProductQuoteReferenceGuard(List.of()));
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void insertRejectsDuplicateTemplateCode() {
        ProductShippingTemplateBo bo = templateBo();
        when(templateMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.insertByBo(bo))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).insert(any());
        verify(ruleMapper, never()).insert(any());
    }

    @Test
    void insertRequiresManualAndMotorizedRules() {
        ProductShippingTemplateBo bo = templateBo();
        bo.setRules(List.of(ruleBo("MANUAL", "18")));

        assertThatThrownBy(() -> service.insertByBo(bo))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).insert(any());
        verify(ruleMapper, never()).insert(any());
    }

    @Test
    void insertRejectsNegativeShippingFee() {
        ProductShippingTemplateBo bo = templateBo();
        bo.getRules().get(0).setFeeAmount(new BigDecimal("-1"));

        assertThatThrownBy(() -> service.insertByBo(bo))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).insert(any());
        verify(ruleMapper, never()).insert(any());
    }

    @Test
    void insertRejectsUnsupportedShippingScenario() {
        ProductShippingTemplateBo bo = templateBo();
        bo.setRules(List.of(
            ruleBo("MANUAL", "18"),
            ruleBo("MOTORIZED", "25"),
            ruleBo("EXPRESS", "30")
        ));

        assertThatThrownBy(() -> service.insertByBo(bo))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).insert(any());
    }

    @Test
    void insertRejectsRangeAfterUnboundedRule() {
        ProductShippingTemplateBo bo = templateBo();
        ProductShippingTemplateRuleBo unbounded = ruleBo("MANUAL", "18");
        ProductShippingTemplateRuleBo later = ruleBo("MANUAL", "25");
        later.setMinAreaSqft(new BigDecimal("100"));
        later.setMaxAreaSqft(new BigDecimal("200"));
        bo.setRules(List.of(unbounded, later, ruleBo("MOTORIZED", "25")));

        assertThatThrownBy(() -> service.insertByBo(bo))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).insert(any());
    }

    @Test
    void insertRejectsAreaRangeGap() {
        ProductShippingTemplateBo bo = templateBo();
        ProductShippingTemplateRuleBo first = ruleBo("MANUAL", "18");
        first.setMaxAreaSqft(new BigDecimal("20"));
        ProductShippingTemplateRuleBo second = ruleBo("MANUAL", "25");
        second.setMinAreaSqft(new BigDecimal("25"));
        bo.setRules(List.of(first, second, ruleBo("MOTORIZED", "25")));

        assertThatThrownBy(() -> service.insertByBo(bo))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).insert(any());
    }

    @Test
    void queryPageListReturnsRuleCount() {
        Page<ProductShippingTemplateVo> page = new Page<>(1, 20, 1);
        page.setRecords(List.of(templateVo()));
        ProductShippingTemplateRule manual = new ProductShippingTemplateRule();
        manual.setShippingTemplateId(9301L);
        ProductShippingTemplateRule motorized = new ProductShippingTemplateRule();
        motorized.setShippingTemplateId(9301L);
        when(templateMapper.selectVoPage(any(), any())).thenReturn(page);
        when(ruleMapper.selectList(any())).thenReturn(List.of(manual, motorized));

        TableDataInfo<ProductShippingTemplateVo> result =
            service.queryPageList(new ProductShippingTemplateBo(), new PageQuery());

        assertThat(result.getRows()).singleElement()
            .extracting(ProductShippingTemplateVo::getRuleCount)
            .isEqualTo(2);
    }

    @Test
    void insertSavesTemplateAndRules() {
        ProductShippingTemplateBo bo = templateBo();
        when(templateMapper.insert(any(ProductShippingTemplate.class))).thenReturn(1);
        when(ruleMapper.insert(any(ProductShippingTemplateRule.class))).thenReturn(1);

        assertThat(service.insertByBo(bo)).isTrue();

        verify(templateMapper).insert(any(ProductShippingTemplate.class));
        verify(ruleMapper, times(2)).insert(any(ProductShippingTemplateRule.class));
    }

    @Test
    void insertGeneratesTemplateCode() {
        ProductShippingTemplateBo bo = templateBo();
        bo.setTemplateCode("USER-INPUT");
        when(templateMapper.insert(any(ProductShippingTemplate.class))).thenReturn(1);
        when(ruleMapper.insert(any(ProductShippingTemplateRule.class))).thenReturn(1);

        assertThat(service.insertByBo(bo)).isTrue();

        verify(templateMapper).insert(org.mockito.ArgumentMatchers.argThat(entity ->
            entity.getTemplateCode() != null && entity.getTemplateCode().startsWith("SHIP-")
                && !"USER-INPUT".equals(entity.getTemplateCode())));
    }

    @Test
    void insertAlwaysCreatesDisabledTemplate() {
        ProductShippingTemplateBo bo = templateBo();
        bo.setStatus("ENABLED");
        when(templateMapper.insert(any(ProductShippingTemplate.class))).thenReturn(1);
        when(ruleMapper.insert(any(ProductShippingTemplateRule.class))).thenReturn(1);

        assertThat(service.insertByBo(bo)).isTrue();

        verify(templateMapper).insert(org.mockito.ArgumentMatchers.argThat(entity ->
            "DISABLED".equals(entity.getStatus())));
    }

    @Test
    void insertNormalizesCurrencyCode() {
        ProductShippingTemplateBo bo = templateBo();
        bo.setCurrencyCode(" usd ");
        when(templateMapper.insert(any(ProductShippingTemplate.class))).thenReturn(1);
        when(ruleMapper.insert(any(ProductShippingTemplateRule.class))).thenReturn(1);

        assertThat(service.insertByBo(bo)).isTrue();

        verify(templateMapper).insert(org.mockito.ArgumentMatchers.argThat(entity ->
            "USD".equals(entity.getCurrencyCode())));
    }

    @Test
    void insertIgnoresTenantIdFromRequest() {
        ProductShippingTemplateBo bo = templateBo();
        bo.setTenantId(999L);
        when(templateMapper.insert(any(ProductShippingTemplate.class))).thenReturn(1);
        when(ruleMapper.insert(any(ProductShippingTemplateRule.class))).thenReturn(1);

        assertThat(service.insertByBo(bo)).isTrue();

        verify(templateMapper).insert(org.mockito.ArgumentMatchers.argThat(entity -> entity.getTenantId().equals(1L)));
        verify(ruleMapper, atLeastOnce()).insert(org.mockito.ArgumentMatchers.argThat(rule -> rule.getTenantId().equals(1L)));
    }

    @Test
    void enabledTemplateCannotBeEdited() {
        ProductShippingTemplate current = new ProductShippingTemplate();
        current.setShippingTemplateId(9301L);
        current.setStatus("ENABLED");
        when(templateMapper.selectById(9301L)).thenReturn(current);

        assertThatThrownBy(() -> service.updateByBo(templateBo()))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).updateById(any());
    }

    @Test
    void enabledTemplateCannotBeDeleted() {
        ProductShippingTemplate current = new ProductShippingTemplate();
        current.setShippingTemplateId(9301L);
        current.setStatus("ENABLED");
        when(templateMapper.selectBatchIds(List.of(9301L))).thenReturn(List.of(current));

        assertThatThrownBy(() -> service.deleteWithValidByIds(new Long[] {9301L}))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).deleteBatchIds(any());
        verify(ruleMapper, never()).delete(any());
    }

    @Test
    void deletingDisabledTemplateAlsoDeletesItsRules() {
        ProductShippingTemplate current = new ProductShippingTemplate();
        current.setShippingTemplateId(9301L);
        current.setStatus("DISABLED");
        when(templateMapper.selectBatchIds(List.of(9301L))).thenReturn(List.of(current));
        when(templateMapper.deleteBatchIds(any())).thenReturn(1);

        assertThat(service.deleteWithValidByIds(new Long[] {9301L})).isTrue();

        verify(ruleMapper).delete(any());
        verify(templateMapper).deleteBatchIds(any());
    }

    @Test
    void enableRejectsBrokenRules() {
        when(templateMapper.selectVoById(9301L)).thenReturn(templateVo());
        when(ruleMapper.selectVoList(any())).thenReturn(List.of(ruleVo("MANUAL", "18")));

        assertThatThrownBy(() -> service.updateStatus(9301L, "ENABLED"))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void enableRejectsLegacyFormulaRulesUntilTemplateIsResaved() {
        when(templateMapper.selectVoById(9301L)).thenReturn(templateVo());
        when(ruleMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.updateStatus(9301L, "ENABLED"))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void updateStatusRejectsUnsupportedValue() {
        assertThatThrownBy(() -> service.updateStatus(9301L, "ARCHIVED"))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void enablingTemplateDisablesOtherTemplatesWithSameCurrency() {
        when(templateMapper.selectVoById(9301L)).thenReturn(templateVo());
        when(ruleMapper.selectVoList(any())).thenReturn(List.of(
            ruleVo("MANUAL", "18"), ruleVo("MOTORIZED", "25")));
        when(templateMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        assertThat(service.updateStatus(9301L, "ENABLED")).isTrue();

        verify(templateMapper, times(2)).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void importTemplateContainsManualAndMotorizedRows() {
        assertThat(service.importTemplateRows())
            .extracting(row -> row.getCondition())
            .containsExactly("不带电", "不带电", "带电", "带电");
    }

    private ProductShippingTemplateBo templateBo() {
        ProductShippingTemplateBo bo = new ProductShippingTemplateBo();
        bo.setShippingTemplateId(9301L);
        bo.setTemplateCode("SHIP-US");
        bo.setTemplateName("美国邮费模板");
        bo.setCurrencyCode("USD");
        bo.setStatus("ENABLED");
        bo.setRules(List.of(
            ruleBo("MANUAL", "18"),
            ruleBo("MOTORIZED", "25")
        ));
        return bo;
    }

    private ProductShippingTemplateRuleBo ruleBo(String code, String amount) {
        ProductShippingTemplateRuleBo rule = new ProductShippingTemplateRuleBo();
        rule.setFeeCode(code);
        rule.setMinAreaSqft(BigDecimal.ZERO);
        rule.setFeeAmount(new BigDecimal(amount));
        return rule;
    }

    private ProductShippingTemplateVo templateVo() {
        ProductShippingTemplateVo vo = new ProductShippingTemplateVo();
        vo.setShippingTemplateId(9301L);
        vo.setTemplateCode("SHIP-US");
        vo.setTemplateName("美国邮费模板");
        vo.setTenantId(1L);
        vo.setCurrencyCode("USD");
        vo.setStatus("DISABLED");
        return vo;
    }

    private ProductShippingTemplateRuleVo ruleVo(String code, String amount) {
        ProductShippingTemplateRuleVo rule = new ProductShippingTemplateRuleVo();
        rule.setFeeCode(code);
        rule.setMinAreaSqft(BigDecimal.ZERO);
        rule.setFeeAmount(new BigDecimal(amount));
        return rule;
    }
}
