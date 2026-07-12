package com.bocoo.dealer.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.service.TestSaTokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SalesDocumentLifecycleServiceTest {
    @Mock private SalesDocumentMapper mapper;
    @Mock private SalesDocumentEventRecorder events;
    private SalesDocumentLifecycleServiceImpl service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SalesDocument.class);
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 1L, "merchant");
        service = new SalesDocumentLifecycleServiceImpl(mapper, events);
    }

    @Test
    void cancelUsesDocumentLifecycleLock() throws NoSuchMethodException {
        Lock4j lock = SalesDocumentLifecycleServiceImpl.class
            .getMethod("cancel", Long.class, String.class).getAnnotation(Lock4j.class);
        assertThat(lock).isNotNull();
        assertThat(lock.name()).isEqualTo("sales-document-lifecycle");
    }

}
