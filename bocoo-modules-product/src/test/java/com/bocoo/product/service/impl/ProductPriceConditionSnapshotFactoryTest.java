package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductPriceConditionSnapshotFactoryTest {

    private final ProductPriceConditionSnapshotFactory factory =
        new ProductPriceConditionSnapshotFactory(new ProductPriceSnapshotReader());

    @Test
    void snapshotsStableOptionAndValueReferences() {
        ProductPriceConditionSnapshotFactory.Snapshot snapshot = factory.snapshot(
            version(), "option_OPT_REF_SYSTEM == \"VAL_REF_MOTOR\" && width > 20", "系统 = 电机 且 宽度 > 20");

        assertThat(snapshot.key()).isEqualTo("option_OPT_REF_SYSTEM == \"VAL_REF_MOTOR\" && width > 20");
        assertThat(snapshot.json())
            .contains("\"optionRefKey\":\"OPT_REF_SYSTEM\"")
            .contains("\"valueRefKey\":\"VAL_REF_MOTOR\"")
            .contains("\"field\":\"width\"");
    }

    @Test
    void rejectsMutableOptionCodesAsInternalReferences() {
        assertThatThrownBy(() -> factory.snapshot(
            version(), "option_SYSTEM == \"MOTOR\"", "系统 = 电机"))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void rejectsUnquotedOptionValueReferences() {
        assertThatThrownBy(() -> factory.snapshot(
            version(), "option_OPT_REF_SYSTEM == 11139", "系统 = 11139"))
            .isInstanceOf(ServiceException.class);
    }

    private ProductFormulaVersion version() {
        ProductFormulaVersion version = new ProductFormulaVersion();
        version.setVersionStatus("EFFECTIVE");
        version.setSetupSnapshotJson("""
            {"options":[{"optionRefKey":"OPT_REF_SYSTEM","optionCode":"SYSTEM","optionNameCn":"系统"}],"optionValues":[{"optionRefKey":"OPT_REF_SYSTEM","optionCode":"SYSTEM","valueRefKey":"VAL_REF_MOTOR","valueCode":"MOTOR","valueNameCn":"电机"}]}
            """);
        return version;
    }
}
