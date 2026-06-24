package com.bocoo.product.service;

import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.FabricSeriesMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.service.impl.FabricSeriesServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FabricSeriesServiceTest {

    @Mock
    private FabricSeriesMapper fabricSeriesMapper;
    @Mock
    private ProductMediaBindingMapper mediaBindingMapper;

    private FabricSeriesServiceImpl fabricSeriesService;

    @BeforeEach
    void setUp() {
        fabricSeriesService = new FabricSeriesServiceImpl(
            fabricSeriesMapper,
            mediaBindingMapper
        );
    }

    @Test
    void fabricSeriesReferenceCheckCountsMediaBindings() {
        when(mediaBindingMapper.selectCount(any())).thenReturn(2L);

        ReferenceCheckResultVo result = fabricSeriesService.checkReferences(140001L);

        assertThat(result.getAllowed()).isFalse();
        assertThat(result.getReferenceCount()).isEqualTo(2L);
        assertThat(result.getBlockerReasonKey()).isEqualTo("product.fabricSeries.hasReferences");
        assertThat(result.getReferenceSummaries()).containsExactly(
            "Media bindings: 2"
        );
    }

    @Test
    void fabricSeriesReferenceCheckAllowsUnusedSeries() {
        when(mediaBindingMapper.selectCount(any())).thenReturn(0L);

        ReferenceCheckResultVo result = fabricSeriesService.checkReferences(140001L);

        assertThat(result.getAllowed()).isTrue();
        assertThat(result.getReferenceCount()).isZero();
        assertThat(result.getReferenceSummaries()).isEmpty();
    }
}
