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
import com.bocoo.product.mapper.ProductPriceFeeRuleMapper;
import com.bocoo.product.mapper.ProductShippingTemplateMapper;
import com.bocoo.product.mapper.ProductShippingTemplateRuleMapper;
import com.bocoo.product.service.impl.ProductShippingTemplateServiceImpl;
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
    @Mock
    private ProductPriceFeeRuleMapper feeRuleMapper;

    private ProductShippingTemplateServiceImpl service;

    @BeforeEach
    void setUp() {
        TenantContextHolder.setTenantId(1L);
        ProductServiceTestSupport.prepareMapperAndConverter();
        service = new ProductShippingTemplateServiceImpl(templateMapper, ruleMapper, feeRuleMapper);
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
        bo.setRules(List.of(ruleBo("MANUAL", "18 + MAX(areaSqft - 20, 0) * 0.35")));

        assertThatThrownBy(() -> service.insertByBo(bo))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).insert(any());
        verify(ruleMapper, never()).insert(any());
    }

    @Test
    void insertRejectsShippingFormulaWithUnitPrice() {
        ProductShippingTemplateBo bo = templateBo();
        bo.getRules().get(0).setFormulaText("unitPrice * MAX(areaSqft, 1)");

        assertThatThrownBy(() -> service.insertByBo(bo))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).insert(any());
        verify(ruleMapper, never()).insert(any());
    }

    @Test
    void insertRejectsUnsupportedShippingScenario() {
        ProductShippingTemplateBo bo = templateBo();
        bo.setRules(List.of(
            ruleBo("MANUAL", "18 + areaSqft"),
            ruleBo("MOTORIZED", "25 + areaSqft"),
            ruleBo("EXPRESS", "30 + areaSqft")
        ));

        assertThatThrownBy(() -> service.insertByBo(bo))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).insert(any());
    }

    @Test
    void insertRejectsRangeAfterUnboundedRule() {
        ProductShippingTemplateBo bo = templateBo();
        ProductShippingTemplateRuleBo unbounded = ruleBo("MANUAL", "18 + areaSqft");
        ProductShippingTemplateRuleBo later = ruleBo("MANUAL", "25 + areaSqft");
        later.setMinAreaSqft(new BigDecimal("100"));
        later.setMaxAreaSqft(new BigDecimal("200"));
        bo.setRules(List.of(unbounded, later, ruleBo("MOTORIZED", "25 + areaSqft")));

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
    void referencedTemplateCannotBeDeleted() {
        when(feeRuleMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.deleteWithValidByIds(new Long[] {9301L}))
            .isInstanceOf(ServiceException.class);

        verify(templateMapper, never()).deleteBatchIds(any());
    }

    @Test
    void enableRejectsBrokenRules() {
        when(templateMapper.selectVoById(9301L)).thenReturn(templateVo());
        when(ruleMapper.selectVoList(any())).thenReturn(List.of(ruleVo("MANUAL", "18 + areaSqft")));

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

    private ProductShippingTemplateBo templateBo() {
        ProductShippingTemplateBo bo = new ProductShippingTemplateBo();
        bo.setShippingTemplateId(9301L);
        bo.setTemplateCode("SHIP-US");
        bo.setTemplateName("美国邮费模板");
        bo.setCurrencyCode("USD");
        bo.setStatus("ENABLED");
        bo.setRules(List.of(
            ruleBo("MANUAL", "18 + MAX(areaSqft - 20, 0) * 0.35"),
            ruleBo("MOTORIZED", "25 + MAX(areaSqft - 20, 0) * 0.45")
        ));
        return bo;
    }

    private ProductShippingTemplateRuleBo ruleBo(String code, String formula) {
        ProductShippingTemplateRuleBo rule = new ProductShippingTemplateRuleBo();
        rule.setFeeCode(code);
        rule.setMinAreaSqft(BigDecimal.ZERO);
        rule.setFormulaText(formula);
        return rule;
    }

    private ProductShippingTemplateVo templateVo() {
        ProductShippingTemplateVo vo = new ProductShippingTemplateVo();
        vo.setShippingTemplateId(9301L);
        vo.setTemplateCode("SHIP-US");
        vo.setTemplateName("美国邮费模板");
        vo.setStatus("DISABLED");
        return vo;
    }

    private ProductShippingTemplateRuleVo ruleVo(String code, String formula) {
        ProductShippingTemplateRuleVo rule = new ProductShippingTemplateRuleVo();
        rule.setFeeCode(code);
        rule.setMinAreaSqft(BigDecimal.ZERO);
        rule.setFormulaText(formula);
        return rule;
    }
}
