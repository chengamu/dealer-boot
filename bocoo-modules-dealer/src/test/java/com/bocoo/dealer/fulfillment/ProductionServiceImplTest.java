package com.bocoo.dealer.fulfillment;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.fulfillment.service.impl.FulfillmentAccessSupport;
import com.bocoo.dealer.fulfillment.service.impl.FulfillmentEventRecorder;
import com.bocoo.dealer.fulfillment.service.impl.FulfillmentOrderAssembler;
import com.bocoo.dealer.fulfillment.service.impl.ProductionServiceImpl;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.service.TestSaTokenContext;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionServiceImplTest {
    @Mock private SalesDocumentMapper documentMapper;
    @Mock private FulfillmentAccessSupport access;
    @Mock private FulfillmentOrderAssembler assembler;
    @Mock private FulfillmentEventRecorder events;
    private ProductionServiceImpl service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SalesDocument.class);
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 10L, "factory");
        lenient().when(access.ignoreTenant(any())).thenAnswer(invocation ->
            ((Supplier<?>) invocation.getArgument(0)).get());
        service = new ProductionServiceImpl(documentMapper, access, assembler, events);
    }

    @Test
    void unpaidOrderCannotStartProduction() {
        SalesDocument row = document();
        row.setPaymentStatus("UNPAID");
        when(access.document(1L)).thenReturn(row);

        assertThatThrownBy(() -> service.start(1L)).isInstanceOf(ServiceException.class);
        verifyNoInteractions(documentMapper, events);
    }

    @Test
    void incompleteFrozenSnapshotCannotStartProduction() {
        when(access.document(1L)).thenReturn(document());
        when(assembler.items(any())).thenReturn(List.of(new SalesDocumentItem()));

        assertThatThrownBy(() -> service.start(1L)).isInstanceOf(ServiceException.class);
        verify(documentMapper, never()).update(any(), any());
    }

    @Test
    void startAndCompleteWriteConditionalStateEvents() {
        SalesDocument row = document();
        SalesDocumentItem item = new SalesDocumentItem();
        item.setFormulaVersionId(8L);
        item.setSelectedOptionsJson("{}");
        item.setBomSnapshotJson("[]");
        when(access.document(1L)).thenReturn(row);
        when(assembler.items(row)).thenReturn(List.of(item));
        when(documentMapper.update(isNull(), any())).thenReturn(1);

        service.start(1L);
        verify(events).record(1L, 300001L, "PRODUCTION_STARTED", "PENDING", "IN_PRODUCTION", null);

        row.setProductionStatus("IN_PRODUCTION");
        service.complete(1L, "done");
        verify(events).record(1L, 300001L, "PRODUCTION_COMPLETED", "IN_PRODUCTION", "COMPLETED", "done");
    }

    private SalesDocument document() {
        SalesDocument row = new SalesDocument();
        row.setSalesDocumentId(1L);
        row.setTenantId(300001L);
        row.setDocumentStatus("SUBMITTED");
        row.setPaymentStatus("PAID");
        row.setProductionStatus("PENDING");
        row.setDelFlag("0");
        return row;
    }
}
