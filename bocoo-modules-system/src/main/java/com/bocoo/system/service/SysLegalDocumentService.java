package com.bocoo.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.system.domain.bo.SysLegalDocumentBo;
import com.bocoo.system.domain.entity.SysLegalDocument;
import com.bocoo.system.domain.vo.SysLegalDocumentVo;
import com.bocoo.system.mapper.SysLegalDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class SysLegalDocumentService {

    private static final String PUBLISHED = "PUBLISHED";
    private static final String DRAFT = "DRAFT";

    private final SysLegalDocumentMapper legalDocumentMapper;

    public TableDataInfo<SysLegalDocumentVo> selectPage(SysLegalDocumentBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysLegalDocument> wrapper = new LambdaQueryWrapper<SysLegalDocument>()
            .eq(StringUtils.isNotBlank(bo.getDocumentType()), SysLegalDocument::getDocumentType, bo.getDocumentType())
            .eq(StringUtils.isNotBlank(bo.getLocale()), SysLegalDocument::getLocale, bo.getLocale())
            .eq(StringUtils.isNotBlank(bo.getStatus()), SysLegalDocument::getStatus, bo.getStatus())
            .orderByDesc(SysLegalDocument::getPublishedTime)
            .orderByDesc(SysLegalDocument::getDocumentId);
        Page<SysLegalDocumentVo> page = legalDocumentMapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    public SysLegalDocumentVo selectById(Long documentId) {
        return legalDocumentMapper.selectVoById(documentId);
    }

    public SysLegalDocumentVo selectPublished(String documentType) {
        String locale = normalizeLocale(LocaleContextHolder.getLocale());
        SysLegalDocument document = selectPublishedByLocale(documentType, locale);
        if (document == null && !"en_US".equals(locale)) {
            document = selectPublishedByLocale(documentType, "en_US");
        }
        if (document == null) {
            throw ServiceException.ofMessageKey("legal.document.notFound");
        }
        return MapstructUtils.convert(document, SysLegalDocumentVo.class);
    }

    public void insert(SysLegalDocumentBo bo) {
        SysLegalDocument document = MapstructUtils.convert(bo, SysLegalDocument.class);
        if (StringUtils.isBlank(document.getStatus())) {
            document.setStatus(DRAFT);
        }
        preparePublishFields(document);
        legalDocumentMapper.insert(document);
    }

    public void update(SysLegalDocumentBo bo) {
        SysLegalDocument document = MapstructUtils.convert(bo, SysLegalDocument.class);
        preparePublishFields(document);
        legalDocumentMapper.updateById(document);
    }

    public void deleteById(Long documentId) {
        legalDocumentMapper.deleteById(documentId);
    }

    private SysLegalDocument selectPublishedByLocale(String documentType, String locale) {
        return legalDocumentMapper.selectOne(new LambdaQueryWrapper<SysLegalDocument>()
            .eq(SysLegalDocument::getDocumentType, documentType)
            .eq(SysLegalDocument::getLocale, locale)
            .eq(SysLegalDocument::getStatus, PUBLISHED)
            .orderByDesc(SysLegalDocument::getPublishedTime)
            .orderByDesc(SysLegalDocument::getDocumentId)
            .last("limit 1"), false);
    }

    private void preparePublishFields(SysLegalDocument document) {
        if (!PUBLISHED.equals(document.getStatus())) {
            return;
        }
        if (document.getPublishedTime() == null) {
            document.setPublishedTime(TimeUtils.utcNow());
        }
        if (StringUtils.isBlank(document.getVersion())) {
            document.setVersion(document.getPublishedTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }

    private String normalizeLocale(Locale locale) {
        if (locale == null) {
            return "en_US";
        }
        if ("zh".equalsIgnoreCase(locale.getLanguage())) {
            return "zh_CN";
        }
        return "en_US";
    }
}
