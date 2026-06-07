package com.bocoo.product.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.json.utils.JsonUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductImportBatchBo;
import com.bocoo.product.domain.bo.ProductImportRowIssueBo;
import com.bocoo.product.domain.entity.PricePlan;
import com.bocoo.product.domain.entity.ProductComponent;
import com.bocoo.product.domain.entity.ProductImportBatch;
import com.bocoo.product.domain.entity.ProductImportRowIssue;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductModel;
import com.bocoo.product.domain.vo.ProductImportBatchVo;
import com.bocoo.product.domain.vo.ProductImportRowIssueVo;
import com.bocoo.product.mapper.PricePlanMapper;
import com.bocoo.product.mapper.ProductComponentMapper;
import com.bocoo.product.mapper.ProductImportBatchMapper;
import com.bocoo.product.mapper.ProductImportRowIssueMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 产品能力导入中心服务。
 *
 * 当前批次维护导入批次、XLS/XLSX 解析预览和行级问题；正式提交仍走后续人工确认。
 */
@Service
@RequiredArgsConstructor
public class ProductImportService {

    private static final String DEFAULT_SOURCE_SYSTEM = "MANUAL";
    private static final String DEFAULT_IMPORT_STATUS = "DRAFT";
    private static final String IMPORT_STATUS_PARSED = "PARSED";
    private static final String DEFAULT_EXCEL_IMPORT_TYPE = "MIXED_XLS";
    private static final String ISSUE_STATUS_PENDING = "1";
    private static final String ISSUE_LEVEL_ERROR = "ERROR";
    private static final String ISSUE_LEVEL_WARNING = "WARNING";
    private static final int PREVIEW_ROW_LIMIT = 200;

    private final ProductImportBatchMapper importBatchMapper;
    private final ProductImportRowIssueMapper importRowIssueMapper;
    private final ProductComponentMapper productComponentMapper;
    private final ProductMaterialMapper productMaterialMapper;
    private final ProductModelMapper productModelMapper;
    private final PricePlanMapper pricePlanMapper;

    public TableDataInfo<ProductImportBatchVo> queryImportBatchPage(ProductImportBatchBo bo, PageQuery pageQuery) {
        Page<ProductImportBatchVo> result = importBatchMapper.selectVoPage(pageQuery.build(), buildImportBatchWrapper(bo));
        return TableDataInfo.build(result);
    }

    public ProductImportBatchVo getImportBatchById(Long batchId) {
        return importBatchMapper.selectVoById(batchId);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProductImportBatchVo parseImportExcel(MultipartFile file, ProductImportBatchBo bo) throws IOException {
        ParsedSheet parsedSheet = readSheet(file);
        ProductImportBatch entity = MapstructUtils.convert(bo, ProductImportBatch.class);
        if (entity == null) {
            entity = new ProductImportBatch();
        }
        if (StringUtils.isBlank(entity.getSourceSystem())) {
            entity.setSourceSystem(DEFAULT_SOURCE_SYSTEM);
        }
        if (StringUtils.isBlank(entity.getImportType())) {
            entity.setImportType(DEFAULT_EXCEL_IMPORT_TYPE);
        }
        entity.setSourceFileName(file.getOriginalFilename());
        entity.setImportStatus(IMPORT_STATUS_PARSED);
        entity.setStartedTime(TimeUtils.utcNow());

        Map<String, String> mapping = buildFieldMapping(parsedSheet.headers());
        List<ProductImportRowIssue> issues = validateParsedRows(parsedSheet.rows(), mapping, entity);
        Set<Integer> errorRows = collectRowsByLevel(issues, ISSUE_LEVEL_ERROR);
        Set<Integer> warningRows = collectRowsByLevel(issues, ISSUE_LEVEL_WARNING);

        entity.setTotalRows(parsedSheet.rows().size());
        entity.setFailedRows(errorRows.size());
        entity.setWarningRows(countWarningOnlyRows(warningRows, errorRows));
        entity.setSuccessRows(Math.max(0, parsedSheet.rows().size() - errorRows.size()));
        entity.setMappingJson(JsonUtils.toJsonString(mapping));
        entity.setPreviewJson(JsonUtils.toJsonString(buildPreview(parsedSheet, mapping, issues)));
        entity.setErrorSummaryJson(JsonUtils.toJsonString(buildErrorSummary(issues, errorRows, warningRows)));
        if (entity.getBatchId() == null) {
            entity.setBatchCode(StringUtils.blankToDefault(entity.getBatchCode(), buildBatchCode()));
            ProductEntityDefaults.prepareInsert(entity);
            importBatchMapper.insert(entity);
        } else {
            importBatchMapper.updateById(entity);
            importRowIssueMapper.delete(Wrappers.lambdaQuery(ProductImportRowIssue.class)
                .eq(ProductImportRowIssue::getBatchId, entity.getBatchId()));
        }
        Long batchId = entity.getBatchId();
        issues.forEach(issue -> issue.setBatchId(batchId));
        if (!issues.isEmpty()) {
            importRowIssueMapper.insertBatch(issues);
        }
        return importBatchMapper.selectVoById(batchId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean saveImportBatch(ProductImportBatchBo bo) {
        ProductImportBatch entity = MapstructUtils.convert(bo, ProductImportBatch.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (StringUtils.isBlank(entity.getSourceSystem())) {
            entity.setSourceSystem(DEFAULT_SOURCE_SYSTEM);
        }
        if (StringUtils.isBlank(entity.getImportStatus())) {
            entity.setImportStatus(DEFAULT_IMPORT_STATUS);
        }
        if (entity.getTotalRows() == null) {
            entity.setTotalRows(0);
        }
        if (entity.getSuccessRows() == null) {
            entity.setSuccessRows(0);
        }
        if (entity.getWarningRows() == null) {
            entity.setWarningRows(0);
        }
        if (entity.getFailedRows() == null) {
            entity.setFailedRows(0);
        }
        if (entity.getStartedTime() == null) {
            entity.setStartedTime(TimeUtils.utcNow());
        }
        if (entity.getBatchId() == null) {
            entity.setBatchCode(StringUtils.blankToDefault(entity.getBatchCode(), buildBatchCode()));
            ProductEntityDefaults.prepareInsert(entity);
            return importBatchMapper.insert(entity) > 0;
        }
        return importBatchMapper.updateById(entity) > 0;
    }

    public Boolean updateImportBatchStatus(Long batchId, String status) {
        ProductImportBatch entity = new ProductImportBatch();
        entity.setBatchId(batchId);
        entity.setImportStatus(status);
        if ("COMMITTED".equals(status) || "FAILED".equals(status) || "CANCELED".equals(status)) {
            entity.setFinishedTime(TimeUtils.utcNow());
        }
        return importBatchMapper.updateById(entity) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean removeImportBatchByIds(Long[] ids) {
        importRowIssueMapper.delete(Wrappers.lambdaQuery(ProductImportRowIssue.class)
            .in(ProductImportRowIssue::getBatchId, Arrays.asList(ids)));
        return importBatchMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public TableDataInfo<ProductImportRowIssueVo> queryImportRowIssuePage(ProductImportRowIssueBo bo, PageQuery pageQuery) {
        Page<ProductImportRowIssueVo> result = importRowIssueMapper.selectVoPage(pageQuery.build(), buildImportRowIssueWrapper(bo));
        return TableDataInfo.build(result);
    }

    public ProductImportRowIssueVo getImportRowIssueById(Long issueId) {
        return importRowIssueMapper.selectVoById(issueId);
    }

    public Boolean saveImportRowIssue(ProductImportRowIssueBo bo) {
        ProductImportRowIssue entity = MapstructUtils.convert(bo, ProductImportRowIssue.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (StringUtils.isBlank(entity.getStatus())) {
            entity.setStatus(ISSUE_STATUS_PENDING);
        }
        if (entity.getIssueId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return importRowIssueMapper.insert(entity) > 0;
        }
        return importRowIssueMapper.updateById(entity) > 0;
    }

    public Boolean removeImportRowIssueByIds(Long[] ids) {
        return importRowIssueMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<ProductImportBatch> buildImportBatchWrapper(ProductImportBatchBo bo) {
        if (bo == null) {
            bo = new ProductImportBatchBo();
        }
        LambdaQueryWrapper<ProductImportBatch> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getBatchCode()), ProductImportBatch::getBatchCode, bo.getBatchCode());
        lqw.like(StringUtils.isNotBlank(bo.getImportType()), ProductImportBatch::getImportType, bo.getImportType());
        lqw.like(StringUtils.isNotBlank(bo.getSourceSystem()), ProductImportBatch::getSourceSystem, bo.getSourceSystem());
        lqw.like(StringUtils.isNotBlank(bo.getSourceFileName()), ProductImportBatch::getSourceFileName, bo.getSourceFileName());
        lqw.like(StringUtils.isNotBlank(bo.getTargetObjectType()), ProductImportBatch::getTargetObjectType, bo.getTargetObjectType());
        lqw.like(StringUtils.isNotBlank(bo.getTargetObjectCode()), ProductImportBatch::getTargetObjectCode, bo.getTargetObjectCode());
        lqw.like(StringUtils.isNotBlank(bo.getImportStatus()), ProductImportBatch::getImportStatus, bo.getImportStatus());
        lqw.orderByDesc(ProductImportBatch::getCreateTime);
        return lqw;
    }

    private LambdaQueryWrapper<ProductImportRowIssue> buildImportRowIssueWrapper(ProductImportRowIssueBo bo) {
        if (bo == null) {
            bo = new ProductImportRowIssueBo();
        }
        LambdaQueryWrapper<ProductImportRowIssue> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getBatchId() != null, ProductImportRowIssue::getBatchId, bo.getBatchId());
        lqw.eq(bo.getRowNo() != null, ProductImportRowIssue::getRowNo, bo.getRowNo());
        lqw.like(StringUtils.isNotBlank(bo.getColumnName()), ProductImportRowIssue::getColumnName, bo.getColumnName());
        lqw.like(StringUtils.isNotBlank(bo.getIssueLevel()), ProductImportRowIssue::getIssueLevel, bo.getIssueLevel());
        lqw.like(StringUtils.isNotBlank(bo.getIssueCode()), ProductImportRowIssue::getIssueCode, bo.getIssueCode());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ProductImportRowIssue::getStatus, bo.getStatus());
        lqw.orderByAsc(ProductImportRowIssue::getRowNo);
        return lqw;
    }

    private String buildBatchCode() {
        return "IMP-" + TimeUtils.utcNow().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private ParsedSheet readSheet(MultipartFile file) throws IOException {
        ProductImportExcelListener listener = new ProductImportExcelListener();
        EasyExcel.read(file.getInputStream(), listener).sheet().doRead();
        return new ParsedSheet(listener.getHeaders(), listener.getRows());
    }

    private Map<String, String> buildFieldMapping(Map<Integer, String> headers) {
        Map<String, String> mapping = new LinkedHashMap<>();
        headers.forEach((index, title) -> {
            String field = resolveCanonicalField(title);
            if (StringUtils.isNotBlank(field)) {
                mapping.put(field, title);
            }
        });
        return mapping;
    }

    private List<ProductImportRowIssue> validateParsedRows(List<Map<String, String>> rows, Map<String, String> mapping, ProductImportBatch entity) {
        List<ProductImportRowIssue> issues = new ArrayList<>();
        if (rows.isEmpty()) {
            issues.add(buildIssue(0, "file", ISSUE_LEVEL_ERROR, "EMPTY_FILE", "Excel 没有可解析的数据行。", Map.of()));
            return issues;
        }
        validateTargetObject(entity, issues);
        rows.forEach(row -> {
            Integer rowNo = Integer.valueOf(row.getOrDefault("_rowNo", "0"));
            validateSharedCode(row, mapping, rowNo, "componentCode", "component_code", ProductComponent::getComponentCode, productComponentMapper, issues);
            validateSharedCode(row, mapping, rowNo, "materialCode", "material_code", ProductMaterial::getMaterialCode, productMaterialMapper, issues);
            validateSharedCode(row, mapping, rowNo, "productModelCode", "model_code", ProductModel::getModelCode, productModelMapper, issues);
            validateSharedCode(row, mapping, rowNo, "pricePlanCode", "price_plan_code", PricePlan::getPricePlanCode, pricePlanMapper, issues);
        });
        if (!mapping.containsKey("componentCode") && !mapping.containsKey("materialCode")
            && !mapping.containsKey("productModelCode") && !mapping.containsKey("pricePlanCode")
            && !mapping.containsKey("questionCode") && !mapping.containsKey("optionCode")) {
            issues.add(buildIssue(1, "header", ISSUE_LEVEL_WARNING, "UNKNOWN_TEMPLATE",
                "未识别到产品模型、组件、物料、问题、答案或价格字段，请检查字段映射。", Map.of()));
        }
        return issues;
    }

    private <T> void validateSharedCode(Map<String, String> row, Map<String, String> mapping, Integer rowNo, String field,
                                        String columnName,
                                        com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, String> column,
                                        com.bocoo.common.mybatis.core.mapper.BaseMapperPlus<T, ?> mapper,
                                        List<ProductImportRowIssue> issues) {
        String sourceColumn = mapping.get(field);
        if (StringUtils.isBlank(sourceColumn)) {
            return;
        }
        String code = StringUtils.trim(row.get(sourceColumn));
        if (StringUtils.isBlank(code)) {
            return;
        }
        Long exists = mapper.selectCount(Wrappers.<T>lambdaQuery().eq(column, code));
        if (exists == null || exists == 0L) {
            issues.add(buildIssue(rowNo, columnName, ISSUE_LEVEL_ERROR, "SHARED_MASTER_NOT_FOUND",
                "共享主数据未找到编码 " + code + "，不会自动创建重复基础数据。", row));
        }
    }

    private void validateTargetObject(ProductImportBatch entity, List<ProductImportRowIssue> issues) {
        if (StringUtils.isBlank(entity.getTargetObjectType()) || StringUtils.isBlank(entity.getTargetObjectCode())) {
            return;
        }
        String targetType = entity.getTargetObjectType();
        String targetCode = entity.getTargetObjectCode();
        boolean exists = switch (targetType) {
            case "PRODUCT_MODEL" -> existsProductModel(targetCode);
            case "PRICE_PLAN" -> existsPricePlan(targetCode);
            default -> true;
        };
        if (!exists) {
            issues.add(buildIssue(0, "targetObjectCode", ISSUE_LEVEL_ERROR, "TARGET_OBJECT_NOT_FOUND",
                "目标对象不存在：" + targetType + " / " + targetCode + "。", Map.of()));
        }
    }

    private boolean existsProductModel(String modelCode) {
        return productModelMapper.selectCount(Wrappers.lambdaQuery(ProductModel.class)
            .eq(ProductModel::getModelCode, modelCode)) > 0;
    }

    private boolean existsPricePlan(String pricePlanCode) {
        return pricePlanMapper.selectCount(Wrappers.lambdaQuery(PricePlan.class)
            .eq(PricePlan::getPricePlanCode, pricePlanCode)) > 0;
    }

    private Map<String, Object> buildPreview(ParsedSheet parsedSheet, Map<String, String> mapping, List<ProductImportRowIssue> issues) {
        Map<String, Object> preview = new LinkedHashMap<>();
        preview.put("headers", parsedSheet.headers());
        preview.put("mapping", mapping);
        preview.put("rowLimit", PREVIEW_ROW_LIMIT);
        preview.put("rows", parsedSheet.rows().stream().limit(PREVIEW_ROW_LIMIT).toList());
        preview.put("issues", issues.stream().limit(PREVIEW_ROW_LIMIT).toList());
        return preview;
    }

    private Map<String, Object> buildErrorSummary(List<ProductImportRowIssue> issues, Set<Integer> errorRows, Set<Integer> warningRows) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("issueCount", issues.size());
        summary.put("errorRows", errorRows.size());
        summary.put("warningRows", countWarningOnlyRows(warningRows, errorRows));
        summary.put("codes", issues.stream().map(ProductImportRowIssue::getIssueCode).distinct().toList());
        return summary;
    }

    private Set<Integer> collectRowsByLevel(List<ProductImportRowIssue> issues, String level) {
        Set<Integer> rows = new LinkedHashSet<>();
        issues.stream()
            .filter(issue -> level.equals(issue.getIssueLevel()))
            .map(ProductImportRowIssue::getRowNo)
            .filter(rowNo -> rowNo != null && rowNo > 0)
            .forEach(rows::add);
        return rows;
    }

    private int countWarningOnlyRows(Set<Integer> warningRows, Set<Integer> errorRows) {
        Set<Integer> result = new LinkedHashSet<>(warningRows);
        result.removeAll(errorRows);
        return result.size();
    }

    private ProductImportRowIssue buildIssue(Integer rowNo, String columnName, String issueLevel, String issueCode,
                                             String issueMessage, Map<String, String> rawRow) {
        ProductImportRowIssue issue = new ProductImportRowIssue();
        issue.setRowNo(rowNo);
        issue.setColumnName(columnName);
        issue.setIssueLevel(issueLevel);
        issue.setIssueCode(issueCode);
        issue.setIssueMessage(issueMessage);
        issue.setRawRowJson(JsonUtils.toJsonString(rawRow));
        issue.setStatus(ISSUE_STATUS_PENDING);
        return issue;
    }

    private String resolveCanonicalField(String title) {
        String normalized = normalizeHeader(title);
        return switch (normalized) {
            case "产品模型编码", "产品编码", "商品编码", "modelcode", "productmodelcode", "productcode" -> "productModelCode";
            case "配置问题编码", "问题编码", "questioncode" -> "questionCode";
            case "答案编码", "选项编码", "optioncode" -> "optionCode";
            case "答案值", "选项值", "optionvalue" -> "optionValue";
            case "组件编码", "配件编码", "部件编码", "componentcode" -> "componentCode";
            case "物料编码", "面料编码", "材料编码", "materialcode", "fabriccode" -> "materialCode";
            case "价格方案编码", "价格编码", "priceplancode" -> "pricePlanCode";
            case "价格项编码", "priceitemcode", "itemcode" -> "priceItemCode";
            default -> "";
        };
    }

    private String normalizeHeader(String title) {
        return StringUtils.defaultString(title)
            .trim()
            .replace(" ", "")
            .replace("_", "")
            .replace("-", "")
            .replace("/", "")
            .toLowerCase(Locale.ROOT);
    }

    private record ParsedSheet(Map<Integer, String> headers, List<Map<String, String>> rows) {
    }

    private static class ProductImportExcelListener extends AnalysisEventListener<Map<Integer, String>> {

        private final Map<Integer, String> headers = new LinkedHashMap<>();
        private final List<Map<String, String>> rows = new ArrayList<>();

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            headers.clear();
            headMap.forEach((index, title) -> {
                if (StringUtils.isNotBlank(title)) {
                    headers.put(index, StringUtils.trim(title));
                }
            });
        }

        @Override
        public void invoke(Map<Integer, String> data, AnalysisContext context) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("_rowNo", String.valueOf(context.readRowHolder().getRowIndex() + 1));
            headers.forEach((index, title) -> row.put(title, StringUtils.trim(data.get(index))));
            rows.add(row);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
        }

        private Map<Integer, String> getHeaders() {
            return headers;
        }

        private List<Map<String, String>> getRows() {
            return rows;
        }
    }
}
