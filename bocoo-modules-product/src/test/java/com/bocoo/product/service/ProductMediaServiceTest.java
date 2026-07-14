package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.service.OssService;
import com.bocoo.product.domain.bo.ProductMediaAssetBo;
import com.bocoo.product.domain.bo.ProductMediaBindingBo;
import com.bocoo.product.domain.entity.ProductMediaAsset;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductMediaAssetMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.service.impl.ProductMediaAssetServiceImpl;
import com.bocoo.product.service.impl.ProductMediaBindingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMediaServiceTest {

    @Mock
    private ProductMediaAssetMapper mediaAssetMapper;
    @Mock
    private ProductMediaBindingMapper mediaBindingMapper;
    @Mock
    private OssService ossService;

    private ProductMediaAssetServiceImpl mediaAssetService;
    private ProductMediaBindingServiceImpl mediaBindingService;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        mediaAssetService = new ProductMediaAssetServiceImpl(mediaAssetMapper, mediaBindingMapper, ossService);
        mediaBindingService = new ProductMediaBindingServiceImpl(mediaBindingMapper, mediaAssetMapper, ossService);
    }

    @Test
    void insertMediaAssetRejectsDuplicateCode() {
        ProductMediaAssetBo bo = validAssetBo();
        when(mediaAssetMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> mediaAssetService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(mediaAssetMapper, never()).insert(any());
    }

    @Test
    void deleteMediaAssetRejectsExistingBindings() {
        ProductMediaAsset asset = new ProductMediaAsset();
        asset.setAssetId(1001L);
        asset.setStatus("DISABLED");
        when(mediaAssetMapper.selectById(1001L)).thenReturn(asset);
        when(mediaBindingMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> mediaAssetService.deleteWithValidByIds(new Long[]{1001L}))
            .isInstanceOf(ServiceException.class);
        verify(mediaAssetMapper, never()).deleteBatchIds(any());
    }

    @Test
    void deleteMediaAssetRejectsEnabledStatus() {
        ProductMediaAsset asset = new ProductMediaAsset();
        asset.setAssetId(1001L);
        asset.setStatus("ENABLED");
        when(mediaAssetMapper.selectById(1001L)).thenReturn(asset);

        assertThatThrownBy(() -> mediaAssetService.deleteWithValidByIds(new Long[]{1001L}))
            .isInstanceOf(ServiceException.class);
        verify(mediaAssetMapper, never()).deleteBatchIds(any());
    }

    @Test
    void mediaAssetReferenceCheckCountsBindings() {
        when(mediaBindingMapper.selectCount(any())).thenReturn(2L);

        ReferenceCheckResultVo result = mediaAssetService.checkReferences(1001L);

        assertThat(result.getAllowed()).isFalse();
        assertThat(result.getReferenceCount()).isEqualTo(2L);
        assertThat(result.getBlockerReasonKey()).isEqualTo("product.mediaAsset.hasReferences");
    }

    @Test
    void insertMediaBindingRejectsDuplicateTargetUsageAndLanguage() {
        ProductMediaBindingBo bo = validBindingBo();
        when(mediaBindingMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> mediaBindingService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(mediaBindingMapper, never()).insert(any());
    }

    @Test
    void deleteMediaBindingRemovesRows() {
        when(mediaBindingMapper.deleteBatchIds(any())).thenReturn(1);

        assertThat(mediaBindingService.deleteWithValidByIds(new Long[]{2001L})).isTrue();
        verify(mediaBindingMapper).deleteBatchIds(any());
    }

    @Test
    void deleteMediaBindingAllowsEnabledStatus() {
        when(mediaBindingMapper.deleteBatchIds(any())).thenReturn(1);

        assertThat(mediaBindingService.deleteWithValidByIds(new Long[]{2001L})).isTrue();
        verify(mediaBindingMapper).deleteBatchIds(any());
    }

    @Test
    void deleteLastMediaBindingCleansAssetAndOss() {
        ProductMediaBinding binding = new ProductMediaBinding();
        binding.setAssetId(1001L);
        ProductMediaAsset asset = new ProductMediaAsset();
        asset.setAssetId(1001L);
        asset.setOssId(9001L);
        when(mediaBindingMapper.selectBatchIds(any())).thenReturn(List.of(binding));
        when(mediaBindingMapper.deleteBatchIds(any())).thenReturn(1);
        when(mediaBindingMapper.selectCount(any())).thenReturn(0L);
        when(mediaAssetMapper.selectById(1001L)).thenReturn(asset);
        when(mediaAssetMapper.deleteById(1001L)).thenReturn(1);

        assertThat(mediaBindingService.deleteWithValidByIds(new Long[]{2001L})).isTrue();

        verify(ossService).deleteByIds(List.of(9001L));
    }

    private ProductMediaAssetBo validAssetBo() {
        ProductMediaAssetBo bo = new ProductMediaAssetBo();
        bo.setAssetCode("ASSET_MATERIAL_1001");
        bo.setAssetNameCn("安装说明");
        bo.setAssetType("FILE");
        bo.setUsageType("INSTALL_GUIDE");
        bo.setLanguageCode("zh_CN");
        bo.setVisibility("INTERNAL");
        bo.setStatus("ENABLED");
        bo.setDelFlag("0");
        return bo;
    }

    private ProductMediaBindingBo validBindingBo() {
        ProductMediaBindingBo bo = new ProductMediaBindingBo();
        bo.setAssetId(1001L);
        bo.setAssetCode("ASSET_MATERIAL_1001");
        bo.setTargetType("MATERIAL");
        bo.setTargetId(3001L);
        bo.setTargetCode("MOTOR_AOK_45");
        bo.setUsageType("INSTALL_GUIDE");
        bo.setLanguageCode("zh_CN");
        bo.setVisibility("INTERNAL");
        bo.setStatus("ENABLED");
        bo.setDelFlag("0");
        return bo;
    }
}
