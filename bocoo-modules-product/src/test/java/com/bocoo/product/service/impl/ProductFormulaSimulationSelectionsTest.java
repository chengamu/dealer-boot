package com.bocoo.product.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFormulaSimulationSelectionsTest {

    @Test
    void selectedValueMatchesSingleAndCommaSeparatedSelections() {
        assertThat(ProductFormulaSimulationSelections.selectedValueMatches("MOTOR", "MOTOR")).isTrue();
        assertThat(ProductFormulaSimulationSelections.selectedValueMatches("CHAIN,MOTOR", "MOTOR")).isTrue();
        assertThat(ProductFormulaSimulationSelections.selectedValueMatches("CHAIN, MOTOR", "MOTOR")).isTrue();
        assertThat(ProductFormulaSimulationSelections.selectedValueMatches("CHAIN", "MOTOR")).isFalse();
    }

    @Test
    void selectedValueMatchesBlankExpectedOnlyWhenSelectionIsBlank() {
        assertThat(ProductFormulaSimulationSelections.selectedValueMatches("", "")).isTrue();
        assertThat(ProductFormulaSimulationSelections.selectedValueMatches(null, "")).isTrue();
        assertThat(ProductFormulaSimulationSelections.selectedValueMatches("MOTOR", "")).isFalse();
    }
}
