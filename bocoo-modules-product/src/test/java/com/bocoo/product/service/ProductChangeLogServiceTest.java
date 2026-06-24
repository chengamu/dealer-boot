package com.bocoo.product.service;

import com.bocoo.product.domain.entity.ProductChangeLog;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.mapper.ProductChangeLogMapper;
import com.bocoo.product.service.impl.ProductChangeLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductChangeLogServiceTest {

    @Mock
    private ProductChangeLogMapper changeLogMapper;

    private ProductChangeLogServiceImpl changeLogService;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        changeLogService = new ProductChangeLogServiceImpl(changeLogMapper);
    }

    @Test
    void recordStoresPayloadAndChangedFields() {
        ProductMaterial before = new ProductMaterial();
        before.setMaterialId(1001L);
        before.setMaterialCode("MOTOR_001");
        before.setSpec("45mm");
        before.setStatus("DISABLED");
        ProductMaterial after = new ProductMaterial();
        after.setMaterialId(1001L);
        after.setMaterialCode("MOTOR_001");
        after.setSpec("50mm");
        after.setStatus("DISABLED");
        when(changeLogMapper.insert(any())).thenReturn(1);

        changeLogService.record("BASE_INFO", "MATERIAL", 1001L, "MOTOR_001", "UPDATE", before, after, null);

        ArgumentCaptor<ProductChangeLog> captor = ArgumentCaptor.forClass(ProductChangeLog.class);
        org.mockito.Mockito.verify(changeLogMapper).insert(captor.capture());
        ProductChangeLog log = captor.getValue();
        assertThat(log.getBizType()).isEqualTo("MATERIAL");
        assertThat(log.getActionType()).isEqualTo("UPDATE");
        assertThat(log.getActionName()).isEqualTo("修改");
        assertThat(log.getBeforeJson()).contains("\"spec\":\"45mm\"");
        assertThat(log.getAfterJson()).contains("\"spec\":\"50mm\"");
        assertThat(log.getDiffJson()).contains("\"spec\"");
        assertThat(log.getOperatorName()).isEqualTo("system");
    }
}
