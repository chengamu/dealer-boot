package com.bocoo.system.service;

import com.bocoo.common.core.service.OssService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeOssImageServiceTest {

    @Mock
    private OssService ossService;

    @Test
    void cleanupRemovedDeletesOnlyMissingImages() {
        NoticeOssImageService service = new NoticeOssImageService(ossService);

        service.cleanupRemoved(
            "<p><img src='a' data-oss-id='1001'><img src='b' data-oss-id=\"1002\"></p>",
            "<p><img src='b' data-oss-id=\"1002\"></p>");

        verify(ossService).deleteByIds(Set.of(1001L));
    }
}
