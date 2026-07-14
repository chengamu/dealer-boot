package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.extra.spring.SpringUtil;
import com.bocoo.common.core.service.I18nService;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;
import com.bocoo.dealer.quickorder.mapper.QuickOrderQueryMapper;
import com.bocoo.dealer.scope.PlatformSalesGuard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Locale;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformQuickOrderQueryServiceImplTest {
    @Mock private QuickOrderQueryMapper mapper;
    @Mock private QuickOrderAssembler assembler;
    @Mock private PlatformSalesGuard guard;

    @BeforeAll
    static void initializeI18n() {
        ApplicationContext context = mock(ApplicationContext.class);
        I18nService i18n = mock(I18nService.class);
        when(context.getBean(I18nService.class)).thenReturn(i18n);
        when(i18n.get(any(Locale.class), anyString(), any(Object[].class))).thenReturn("success");
        new SpringUtil().setApplicationContext(context);
    }

    @Test
    void pageLoadsAllSummariesInOneBatch() {
        List<QuickOrderVo> rows = List.of(order(1L), order(2L));
        Page<QuickOrderVo> page = new Page<>();
        page.setRecords(rows);
        page.setTotal(2);
        when(mapper.selectVoPage(any(), any())).thenReturn(page);
        PlatformQuickOrderQueryServiceImpl service = new PlatformQuickOrderQueryServiceImpl(mapper, assembler, guard);

        assertThat(service.queryPage(new QuickOrderBo(), new PageQuery()).getRows()).hasSize(2);

        verify(assembler).fillSummaries(rows);
    }

    @Test
    void exportLoadsOrdersAndSummariesInBoundedBatches() {
        List<QuickOrderVo> rows = LongStream.rangeClosed(1, 501).mapToObj(this::order).toList();
        Page<QuickOrderVo> firstPage = new Page<>();
        firstPage.setRecords(rows.subList(0, 500));
        Page<QuickOrderVo> secondPage = new Page<>();
        secondPage.setRecords(rows.subList(500, 501));
        when(mapper.selectVoPage(any(), any())).thenReturn(firstPage, secondPage);
        PlatformQuickOrderQueryServiceImpl service = new PlatformQuickOrderQueryServiceImpl(mapper, assembler, guard);

        assertThat(service.export(new QuickOrderBo())).hasSize(501);

        verify(assembler).fillSummaries(rows.subList(0, 500));
        verify(assembler).fillSummaries(rows.subList(500, 501));
    }

    private QuickOrderVo order(long id) {
        QuickOrderVo row = new QuickOrderVo();
        row.setQuickOrderId(id);
        return row;
    }
}
