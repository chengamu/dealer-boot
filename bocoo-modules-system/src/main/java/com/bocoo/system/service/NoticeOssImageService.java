package com.bocoo.system.service;

import com.bocoo.common.core.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeOssImageService {

    private static final Pattern OSS_ID_PATTERN = Pattern.compile("data-oss-id\\s*=\\s*['\"](\\d+)['\"]", Pattern.CASE_INSENSITIVE);

    private final OssService ossService;

    public void cleanupRemoved(String oldContent, String newContent) {
        Set<Long> removed = extract(oldContent);
        removed.removeAll(extract(newContent));
        cleanup(removed);
    }

    public void cleanup(String content) {
        cleanup(extract(content));
    }

    private Set<Long> extract(String content) {
        Set<Long> ossIds = new HashSet<>();
        if (content == null || content.isBlank()) return ossIds;
        Matcher matcher = OSS_ID_PATTERN.matcher(content);
        while (matcher.find()) ossIds.add(Long.valueOf(matcher.group(1)));
        return ossIds;
    }

    private void cleanup(Set<Long> ossIds) {
        if (ossIds.isEmpty()) return;
        try {
            ossService.deleteByIds(ossIds);
        } catch (RuntimeException exception) {
            log.warn("Failed to cleanup notice OSS images: {}", ossIds, exception);
        }
    }
}
