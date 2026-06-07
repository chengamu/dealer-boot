package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.*;
import com.bocoo.product.domain.entity.*;
import com.bocoo.product.domain.vo.*;
import com.bocoo.product.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 产品能力基础信息服务。
 */
@Service
@RequiredArgsConstructor
public class ProductBaseInfoService {

    private final ProductCategoryMapper categoryMapper;
    private final ProductMaterialMapper materialMapper;
    private final ProductComponentMapper componentMapper;
    private final ProductMediaAssetMapper mediaAssetMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;
    private final ProductModelMapper modelMapper;
    private final SalesVariantMapper variantMapper;
    private final ConfigTemplateVersionMapper templateVersionMapper;
    private final PricePlanVersionMapper pricePlanVersionMapper;
    private final PublishCheckResultMapper checkResultMapper;
    private final ProductSyncOutboxMapper syncOutboxMapper;
    private final ConfigOptionMapper configOptionMapper;

    public WorkbenchSummaryVo getWorkbenchSummary() {
        WorkbenchSummaryVo summary = new WorkbenchSummaryVo();
        summary.setModelCount(modelMapper.selectCount(Wrappers.<ProductModel>lambdaQuery()
            .eq(ProductModel::getDelFlag, "0")));
        summary.setDraftCount(modelMapper.selectCount(Wrappers.<ProductModel>lambdaQuery()
            .eq(ProductModel::getDelFlag, "0")
            .eq(ProductModel::getPublishStatus, "DRAFT")));
        summary.setPublishedCount(modelMapper.selectCount(Wrappers.<ProductModel>lambdaQuery()
            .eq(ProductModel::getDelFlag, "0")
            .eq(ProductModel::getPublishStatus, "PUBLISHED")));
        return summary;
    }

    public TableDataInfo<WorkbenchProgressVo> queryWorkbenchProgressPage() {
        List<ProductModel> models = modelMapper.selectList(Wrappers.<ProductModel>lambdaQuery()
            .eq(ProductModel::getDelFlag, "0")
            .orderByDesc(ProductModel::getUpdateTime)
            .last("limit 10"));
        List<WorkbenchProgressVo> rows = models.stream().map(model -> {
            WorkbenchProgressVo vo = new WorkbenchProgressVo();
            vo.setModelId(model.getModelId());
            vo.setModelCode(model.getModelCode());
            vo.setModelName(StringUtils.blankToDefault(model.getModelNameCn(), model.getModelNameEn()));
            vo.setCategoryName(model.getCategoryNameCn());
            vo.setTemplateStatus(hasTemplate(model) ? "READY" : "MISSING");
            vo.setPriceStatus(hasPrice(model) ? "READY" : "MISSING");
            vo.setAssetStatus(hasAsset(model) ? "READY" : "MISSING");
            vo.setPublishStatus(StringUtils.blankToDefault(model.getPublishStatus(), "DRAFT"));
            vo.setBlockerCount(countChecks(model, "BLOCKER").intValue());
            vo.setWarningCount(countChecks(model, "WARNING").intValue());
            vo.setUpdatedTime(model.getUpdateTime());
            return vo;
        }).toList();
        return TableDataInfo.build(rows);
    }

    public TableDataInfo<WorkbenchPriorityVo> queryWorkbenchPriorityPage() {
        List<PublishCheckResult> checks = checkResultMapper.selectList(Wrappers.<PublishCheckResult>lambdaQuery()
            .in(PublishCheckResult::getCheckLevel, List.of("BLOCKER", "WARNING"))
            .eq(PublishCheckResult::getResolvedFlag, "0")
            .eq(PublishCheckResult::getStatus, "1")
            .orderByDesc(PublishCheckResult::getCreateTime)
            .last("limit 10"));
        List<WorkbenchPriorityVo> rows = checks.stream().map(check -> {
            WorkbenchPriorityVo vo = new WorkbenchPriorityVo();
            vo.setTaskId(check.getCheckId());
            vo.setTaskType("PUBLISH_CHECK");
            vo.setSeverity(check.getCheckLevel());
            vo.setTargetType(check.getTargetType());
            vo.setTargetCode(check.getTargetCode());
            vo.setTargetName(StringUtils.blankToDefault(check.getCheckNameCn(), check.getCheckNameEn()));
            vo.setStatus(check.getCheckStatus());
            return vo;
        }).toList();
        return TableDataInfo.build(rows);
    }

    public TableDataInfo<WorkbenchSyncEventVo> queryWorkbenchSyncEventPage() {
        List<ProductSyncOutbox> events = syncOutboxMapper.selectList(Wrappers.<ProductSyncOutbox>lambdaQuery()
            .eq(ProductSyncOutbox::getStatus, "1")
            .orderByDesc(ProductSyncOutbox::getCreateTime)
            .last("limit 10"));
        List<WorkbenchSyncEventVo> rows = events.stream().map(event -> {
            WorkbenchSyncEventVo vo = new WorkbenchSyncEventVo();
            vo.setEventId(event.getOutboxId());
            vo.setEventType(event.getEventType());
            vo.setTargetType(event.getTargetSystem());
            vo.setTargetCode(event.getPackageCode());
            vo.setStatus(event.getSyncStatus());
            vo.setRetryCount(event.getRetryCount());
            vo.setLastErrorKey(event.getLastErrorKey());
            vo.setCreatedTime(event.getCreateTime());
            vo.setUpdatedTime(event.getUpdateTime());
            return vo;
        }).toList();
        return TableDataInfo.build(rows);
    }

    private Boolean hasTemplate(ProductModel model) {
        return templateVersionMapper.selectCount(Wrappers.<ConfigTemplateVersion>lambdaQuery()
            .eq(model.getModelId() != null, ConfigTemplateVersion::getProductModelId, model.getModelId())) > 0;
    }

    private Boolean hasPrice(ProductModel model) {
        return pricePlanVersionMapper.selectCount(Wrappers.<PricePlanVersion>lambdaQuery()
            .eq(model.getModelId() != null, PricePlanVersion::getProductModelId, model.getModelId())) > 0;
    }

    private Boolean hasAsset(ProductModel model) {
        return mediaBindingMapper.selectCount(Wrappers.<ProductMediaBinding>lambdaQuery()
            .eq(ProductMediaBinding::getTargetType, "PRODUCT_MODEL")
            .eq(ProductMediaBinding::getTargetId, model.getModelId())
            .eq(ProductMediaBinding::getDelFlag, "0")) > 0;
    }

    private Long countChecks(ProductModel model, String level) {
        return checkResultMapper.selectCount(Wrappers.<PublishCheckResult>lambdaQuery()
            .eq(PublishCheckResult::getTargetType, "PRODUCT")
            .eq(PublishCheckResult::getTargetId, model.getModelId())
            .eq(PublishCheckResult::getCheckLevel, level)
            .eq(PublishCheckResult::getResolvedFlag, "0")
            .eq(PublishCheckResult::getStatus, "1"));
    }

    public TableDataInfo<ProductCategoryVo> queryProductCategoryPage(ProductCategoryBo bo, PageQuery pageQuery) {
        Page<ProductCategoryVo> result = categoryMapper.selectVoPage(pageQuery.build(), buildProductCategoryWrapper(bo));
        return TableDataInfo.build(result);
    }

    public List<ProductCategoryVo> queryProductCategoryList(ProductCategoryBo bo) {
        return categoryMapper.selectVoList(buildProductCategoryWrapper(bo));
    }

    public ProductCategoryVo getProductCategoryById(Long id) {
        return categoryMapper.selectVoById(id);
    }

    public Boolean saveProductCategory(ProductCategoryBo bo) {
        ProductCategory entity = MapstructUtils.convert(bo, ProductCategory.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getCategoryId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return categoryMapper.insert(entity) > 0;
        }
        return categoryMapper.updateById(entity) > 0;
    }

    public Boolean removeProductCategoryByIds(Long[] ids) {
        return categoryMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateProductCategoryStatus(Long id, String status) {
        ProductCategory entity = new ProductCategory();
        entity.setCategoryId(id);
        entity.setStatus(status);
        return categoryMapper.updateById(entity) > 0;
    }

    private LambdaQueryWrapper<ProductCategory> buildProductCategoryWrapper(ProductCategoryBo bo) {
        if (bo == null) {
            bo = new ProductCategoryBo();
        }
        LambdaQueryWrapper<ProductCategory> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getCategoryCode()), ProductCategory::getCategoryCode, bo.getCategoryCode());
        lqw.like(StringUtils.isNotBlank(bo.getCategoryNameCn()), ProductCategory::getCategoryNameCn, bo.getCategoryNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getCategoryNameEn()), ProductCategory::getCategoryNameEn, bo.getCategoryNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ProductCategory::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isBlank(bo.getDelFlag()), ProductCategory::getDelFlag, "0");
        return lqw;
    }

    public TableDataInfo<ProductMaterialVo> queryProductMaterialPage(ProductMaterialBo bo, PageQuery pageQuery) {
        Page<ProductMaterialVo> result = materialMapper.selectVoPage(pageQuery.build(), buildProductMaterialWrapper(bo));
        return TableDataInfo.build(result);
    }

    public List<ProductMaterialVo> queryProductMaterialList(ProductMaterialBo bo) {
        return materialMapper.selectVoList(buildProductMaterialWrapper(bo));
    }

    public ProductMaterialVo getProductMaterialById(Long id) {
        return materialMapper.selectVoById(id);
    }

    public Boolean saveProductMaterial(ProductMaterialBo bo) {
        ProductMaterial entity = MapstructUtils.convert(bo, ProductMaterial.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getMaterialId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return materialMapper.insert(entity) > 0;
        }
        return materialMapper.updateById(entity) > 0;
    }

    public Boolean removeProductMaterialByIds(Long[] ids) {
        return materialMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateProductMaterialStatus(Long id, String status) {
        ProductMaterial entity = new ProductMaterial();
        entity.setMaterialId(id);
        entity.setStatus(status);
        return materialMapper.updateById(entity) > 0;
    }

    private LambdaQueryWrapper<ProductMaterial> buildProductMaterialWrapper(ProductMaterialBo bo) {
        if (bo == null) {
            bo = new ProductMaterialBo();
        }
        LambdaQueryWrapper<ProductMaterial> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getMaterialCode()), ProductMaterial::getMaterialCode, bo.getMaterialCode());
        lqw.like(StringUtils.isNotBlank(bo.getMaterialNameCn()), ProductMaterial::getMaterialNameCn, bo.getMaterialNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getMaterialNameEn()), ProductMaterial::getMaterialNameEn, bo.getMaterialNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getMaterialType()), ProductMaterial::getMaterialType, bo.getMaterialType());
        lqw.like(StringUtils.isNotBlank(bo.getBusinessType()), ProductMaterial::getBusinessType, bo.getBusinessType());
        lqw.like(StringUtils.isNotBlank(bo.getSupplierName()), ProductMaterial::getSupplierName, bo.getSupplierName());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ProductMaterial::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isBlank(bo.getDelFlag()), ProductMaterial::getDelFlag, "0");
        return lqw;
    }

    public TableDataInfo<ProductComponentVo> queryProductComponentPage(ProductComponentBo bo, PageQuery pageQuery) {
        Page<ProductComponentVo> result = componentMapper.selectVoPage(pageQuery.build(), buildProductComponentWrapper(bo));
        return TableDataInfo.build(result);
    }

    public List<ProductComponentVo> queryProductComponentList(ProductComponentBo bo) {
        return componentMapper.selectVoList(buildProductComponentWrapper(bo));
    }

    public ProductComponentVo getProductComponentById(Long id) {
        return componentMapper.selectVoById(id);
    }

    public Boolean saveProductComponent(ProductComponentBo bo) {
        ProductComponent entity = MapstructUtils.convert(bo, ProductComponent.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getComponentId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return componentMapper.insert(entity) > 0;
        }
        return componentMapper.updateById(entity) > 0;
    }

    public Boolean removeProductComponentByIds(Long[] ids) {
        return componentMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateProductComponentStatus(Long id, String status) {
        ProductComponent entity = new ProductComponent();
        entity.setComponentId(id);
        entity.setStatus(status);
        return componentMapper.updateById(entity) > 0;
    }

    private LambdaQueryWrapper<ProductComponent> buildProductComponentWrapper(ProductComponentBo bo) {
        if (bo == null) {
            bo = new ProductComponentBo();
        }
        LambdaQueryWrapper<ProductComponent> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getComponentCode()), ProductComponent::getComponentCode, bo.getComponentCode());
        lqw.like(StringUtils.isNotBlank(bo.getComponentNameCn()), ProductComponent::getComponentNameCn, bo.getComponentNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getComponentNameEn()), ProductComponent::getComponentNameEn, bo.getComponentNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getComponentType()), ProductComponent::getComponentType, bo.getComponentType());
        lqw.like(StringUtils.isNotBlank(bo.getMaterialCode()), ProductComponent::getMaterialCode, bo.getMaterialCode());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ProductComponent::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isBlank(bo.getDelFlag()), ProductComponent::getDelFlag, "0");
        return lqw;
    }

    public TableDataInfo<ProductMediaAssetVo> queryProductMediaAssetPage(ProductMediaAssetBo bo, PageQuery pageQuery) {
        Page<ProductMediaAssetVo> result = mediaAssetMapper.selectVoPage(pageQuery.build(), buildProductMediaAssetWrapper(bo));
        return TableDataInfo.build(result);
    }

    public List<ProductMediaAssetVo> queryProductMediaAssetList(ProductMediaAssetBo bo) {
        return mediaAssetMapper.selectVoList(buildProductMediaAssetWrapper(bo));
    }

    public ProductMediaAssetVo getProductMediaAssetById(Long id) {
        return mediaAssetMapper.selectVoById(id);
    }

    public Boolean saveProductMediaAsset(ProductMediaAssetBo bo) {
        ProductMediaAsset entity = MapstructUtils.convert(bo, ProductMediaAsset.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getAssetId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return mediaAssetMapper.insert(entity) > 0;
        }
        return mediaAssetMapper.updateById(entity) > 0;
    }

    public Boolean removeProductMediaAssetByIds(Long[] ids) {
        return mediaAssetMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateProductMediaAssetStatus(Long id, String status) {
        ProductMediaAsset entity = new ProductMediaAsset();
        entity.setAssetId(id);
        entity.setStatus(status);
        return mediaAssetMapper.updateById(entity) > 0;
    }

    private LambdaQueryWrapper<ProductMediaAsset> buildProductMediaAssetWrapper(ProductMediaAssetBo bo) {
        if (bo == null) {
            bo = new ProductMediaAssetBo();
        }
        LambdaQueryWrapper<ProductMediaAsset> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getAssetCode()), ProductMediaAsset::getAssetCode, bo.getAssetCode());
        lqw.like(StringUtils.isNotBlank(bo.getAssetNameCn()), ProductMediaAsset::getAssetNameCn, bo.getAssetNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getAssetNameEn()), ProductMediaAsset::getAssetNameEn, bo.getAssetNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getAssetType()), ProductMediaAsset::getAssetType, bo.getAssetType());
        lqw.like(StringUtils.isNotBlank(bo.getUsageType()), ProductMediaAsset::getUsageType, bo.getUsageType());
        lqw.like(StringUtils.isNotBlank(bo.getLanguageCode()), ProductMediaAsset::getLanguageCode, bo.getLanguageCode());
        lqw.like(StringUtils.isNotBlank(bo.getVisibility()), ProductMediaAsset::getVisibility, bo.getVisibility());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ProductMediaAsset::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isBlank(bo.getDelFlag()), ProductMediaAsset::getDelFlag, "0");
        return lqw;
    }

    public TableDataInfo<ProductMediaBindingVo> queryProductMediaBindingPage(ProductMediaBindingBo bo, PageQuery pageQuery) {
        Page<ProductMediaBindingVo> result = mediaBindingMapper.selectVoPage(pageQuery.build(), buildProductMediaBindingWrapper(bo));
        return TableDataInfo.build(result);
    }

    public List<ProductMediaBindingVo> queryProductMediaBindingList(ProductMediaBindingBo bo) {
        return mediaBindingMapper.selectVoList(buildProductMediaBindingWrapper(bo));
    }

    public ProductMediaBindingVo getProductMediaBindingById(Long id) {
        return mediaBindingMapper.selectVoById(id);
    }

    public Boolean saveProductMediaBinding(ProductMediaBindingBo bo) {
        ProductMediaBinding entity = MapstructUtils.convert(bo, ProductMediaBinding.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getBindingId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return mediaBindingMapper.insert(entity) > 0;
        }
        return mediaBindingMapper.updateById(entity) > 0;
    }

    public Boolean removeProductMediaBindingByIds(Long[] ids) {
        return mediaBindingMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateProductMediaBindingStatus(Long id, String status) {
        ProductMediaBinding entity = new ProductMediaBinding();
        entity.setBindingId(id);
        entity.setStatus(status);
        return mediaBindingMapper.updateById(entity) > 0;
    }

    private LambdaQueryWrapper<ProductMediaBinding> buildProductMediaBindingWrapper(ProductMediaBindingBo bo) {
        if (bo == null) {
            bo = new ProductMediaBindingBo();
        }
        LambdaQueryWrapper<ProductMediaBinding> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getAssetCode()), ProductMediaBinding::getAssetCode, bo.getAssetCode());
        lqw.like(StringUtils.isNotBlank(bo.getTargetType()), ProductMediaBinding::getTargetType, bo.getTargetType());
        lqw.like(StringUtils.isNotBlank(bo.getTargetCode()), ProductMediaBinding::getTargetCode, bo.getTargetCode());
        lqw.like(StringUtils.isNotBlank(bo.getUsageType()), ProductMediaBinding::getUsageType, bo.getUsageType());
        lqw.like(StringUtils.isNotBlank(bo.getVisibility()), ProductMediaBinding::getVisibility, bo.getVisibility());
        lqw.like(StringUtils.isNotBlank(bo.getLanguageCode()), ProductMediaBinding::getLanguageCode, bo.getLanguageCode());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ProductMediaBinding::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isBlank(bo.getDelFlag()), ProductMediaBinding::getDelFlag, "0");
        return lqw;
    }

    public TableDataInfo<ProductModelVo> queryProductModelPage(ProductModelBo bo, PageQuery pageQuery) {
        Page<ProductModelVo> result = modelMapper.selectVoPage(pageQuery.build(), buildProductModelWrapper(bo));
        return TableDataInfo.build(result);
    }

    public List<ProductModelVo> queryProductModelList(ProductModelBo bo) {
        return modelMapper.selectVoList(buildProductModelWrapper(bo));
    }

    public ProductModelVo getProductModelById(Long id) {
        return modelMapper.selectVoById(id);
    }

    public Boolean saveProductModel(ProductModelBo bo) {
        ProductModel entity = MapstructUtils.convert(bo, ProductModel.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getModelId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return modelMapper.insert(entity) > 0;
        }
        return modelMapper.updateById(entity) > 0;
    }

    public Boolean removeProductModelByIds(Long[] ids) {
        return modelMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateProductModelStatus(Long id, String status) {
        ProductModel entity = new ProductModel();
        entity.setModelId(id);
        entity.setStatus(status);
        return modelMapper.updateById(entity) > 0;
    }

    private LambdaQueryWrapper<ProductModel> buildProductModelWrapper(ProductModelBo bo) {
        if (bo == null) {
            bo = new ProductModelBo();
        }
        LambdaQueryWrapper<ProductModel> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getModelCode()), ProductModel::getModelCode, bo.getModelCode());
        lqw.like(StringUtils.isNotBlank(bo.getModelNameCn()), ProductModel::getModelNameCn, bo.getModelNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getModelNameEn()), ProductModel::getModelNameEn, bo.getModelNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getCategoryCode()), ProductModel::getCategoryCode, bo.getCategoryCode());
        lqw.like(StringUtils.isNotBlank(bo.getProductFamily()), ProductModel::getProductFamily, bo.getProductFamily());
        lqw.like(StringUtils.isNotBlank(bo.getSalesMode()), ProductModel::getSalesMode, bo.getSalesMode());
        lqw.like(StringUtils.isNotBlank(bo.getProductNature()), ProductModel::getProductNature, bo.getProductNature());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ProductModel::getStatus, bo.getStatus());
        lqw.like(StringUtils.isNotBlank(bo.getPublishStatus()), ProductModel::getPublishStatus, bo.getPublishStatus());
        lqw.eq(StringUtils.isBlank(bo.getDelFlag()), ProductModel::getDelFlag, "0");
        return lqw;
    }

    public TableDataInfo<SalesVariantVo> querySalesVariantPage(SalesVariantBo bo, PageQuery pageQuery) {
        Page<SalesVariantVo> result = variantMapper.selectVoPage(pageQuery.build(), buildSalesVariantWrapper(bo));
        return TableDataInfo.build(result);
    }

    public List<SalesVariantVo> querySalesVariantList(SalesVariantBo bo) {
        return variantMapper.selectVoList(buildSalesVariantWrapper(bo));
    }

    public SalesVariantVo getSalesVariantById(Long id) {
        return variantMapper.selectVoById(id);
    }

    public Boolean saveSalesVariant(SalesVariantBo bo) {
        SalesVariant entity = MapstructUtils.convert(bo, SalesVariant.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getVariantId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return variantMapper.insert(entity) > 0;
        }
        return variantMapper.updateById(entity) > 0;
    }

    public Boolean removeSalesVariantByIds(Long[] ids) {
        return variantMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateSalesVariantStatus(Long id, String status) {
        SalesVariant entity = new SalesVariant();
        entity.setVariantId(id);
        entity.setStatus(status);
        return variantMapper.updateById(entity) > 0;
    }

    private LambdaQueryWrapper<SalesVariant> buildSalesVariantWrapper(SalesVariantBo bo) {
        if (bo == null) {
            bo = new SalesVariantBo();
        }
        LambdaQueryWrapper<SalesVariant> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getModelId() != null, SalesVariant::getModelId, bo.getModelId());
        lqw.like(StringUtils.isNotBlank(bo.getVariantCode()), SalesVariant::getVariantCode, bo.getVariantCode());
        lqw.like(StringUtils.isNotBlank(bo.getVariantNameCn()), SalesVariant::getVariantNameCn, bo.getVariantNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getVariantNameEn()), SalesVariant::getVariantNameEn, bo.getVariantNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getMarketCode()), SalesVariant::getMarketCode, bo.getMarketCode());
        lqw.like(StringUtils.isNotBlank(bo.getControlMethod()), SalesVariant::getControlMethod, bo.getControlMethod());
        lqw.like(StringUtils.isNotBlank(bo.getGrade()), SalesVariant::getGrade, bo.getGrade());
        lqw.like(StringUtils.isNotBlank(bo.getPackageType()), SalesVariant::getPackageType, bo.getPackageType());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), SalesVariant::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isBlank(bo.getDelFlag()), SalesVariant::getDelFlag, "0");
        return lqw;
    }

    public ReferenceCheckResultVo checkProductCategoryReferences(Long categoryId) {
        ReferenceCheckResultVo result = new ReferenceCheckResultVo();
        long modelCount = modelMapper.selectCount(Wrappers.<ProductModel>lambdaQuery()
            .eq(ProductModel::getCategoryId, categoryId)
            .eq(ProductModel::getDelFlag, "0"));
        long childCount = categoryMapper.selectCount(Wrappers.<ProductCategory>lambdaQuery()
            .eq(ProductCategory::getParentId, categoryId)
            .eq(ProductCategory::getDelFlag, "0"));
        addReference(result, "productModel", modelCount);
        addReference(result, "childCategory", childCount);
        return result;
    }

    public ReferenceCheckResultVo checkProductMaterialReferences(Long materialId) {
        ReferenceCheckResultVo result = new ReferenceCheckResultVo();
        long componentCount = componentMapper.selectCount(Wrappers.<ProductComponent>lambdaQuery()
            .eq(ProductComponent::getMaterialId, materialId)
            .eq(ProductComponent::getDelFlag, "0"));
        addReference(result, "component", componentCount);
        return result;
    }

    public ReferenceCheckResultVo checkProductComponentReferences(Long componentId) {
        ReferenceCheckResultVo result = new ReferenceCheckResultVo();
        ProductComponent component = componentMapper.selectById(componentId);
        long bindingCount = mediaBindingMapper.selectCount(Wrappers.<ProductMediaBinding>lambdaQuery()
            .eq(ProductMediaBinding::getTargetType, "COMPONENT")
            .eq(ProductMediaBinding::getTargetId, componentId)
            .eq(ProductMediaBinding::getDelFlag, "0"));
        long optionCount = 0L;
        if (component != null) {
            optionCount = configOptionMapper.selectCount(Wrappers.<ConfigOption>lambdaQuery()
                .and(wrapper -> wrapper.like(ConfigOption::getComponentJson, String.valueOf(componentId))
                    .or(StringUtils.isNotBlank(component.getComponentCode()))
                    .like(ConfigOption::getComponentJson, component.getComponentCode())));
        }
        addReference(result, "mediaBinding", bindingCount);
        addReference(result, "configOption", optionCount);
        return result;
    }

    public ReferenceCheckResultVo checkProductMediaAssetReferences(Long assetId) {
        ReferenceCheckResultVo result = new ReferenceCheckResultVo();
        long bindingCount = mediaBindingMapper.selectCount(Wrappers.<ProductMediaBinding>lambdaQuery()
            .eq(ProductMediaBinding::getAssetId, assetId)
            .eq(ProductMediaBinding::getDelFlag, "0"));
        addReference(result, "mediaBinding", bindingCount);
        return result;
    }

    public ReferenceCheckResultVo checkProductMediaBindingReferences(Long bindingId) {
        ReferenceCheckResultVo result = new ReferenceCheckResultVo();
        ProductMediaBinding binding = mediaBindingMapper.selectById(bindingId);
        if (binding == null || "2".equals(binding.getDelFlag())) {
            result.getReferenceSummaries().add("mediaBinding:notFound");
            return result;
        }
        ProductMediaAsset asset = mediaAssetMapper.selectById(binding.getAssetId());
        String assetCode = StringUtils.defaultIfBlank(binding.getAssetCode(), asset == null ? null : asset.getAssetCode());
        result.getReferenceSummaries().add("mediaBinding:" + binding.getBindingId());
        result.getReferenceSummaries().add("asset:" + StringUtils.defaultIfBlank(assetCode, String.valueOf(binding.getAssetId())));
        result.getReferenceSummaries().add("target:" + binding.getTargetType() + ":" + StringUtils.defaultIfBlank(binding.getTargetCode(), String.valueOf(binding.getTargetId())));
        result.getReferenceSummaries().add("usage:" + StringUtils.defaultIfBlank(binding.getUsageType(), "-"));
        result.getReferenceSummaries().add("language:" + StringUtils.defaultIfBlank(binding.getLanguageCode(), "-"));
        result.getReferenceSummaries().add("visibility:" + StringUtils.defaultIfBlank(binding.getVisibility(), "-"));
        if (asset == null || "2".equals(asset.getDelFlag())) {
            result.setAllowed(Boolean.FALSE);
            result.setReferenceCount(1L);
            result.setBlockerReasonKey("product.reference.inUse");
            result.getReferenceSummaries().add("assetMissingOrDeleted:1");
        }
        return result;
    }

    public ReferenceCheckResultVo checkProductModelReferences(Long modelId) {
        ReferenceCheckResultVo result = new ReferenceCheckResultVo();
        long variantCount = variantMapper.selectCount(Wrappers.<SalesVariant>lambdaQuery()
            .eq(SalesVariant::getModelId, modelId)
            .eq(SalesVariant::getDelFlag, "0"));
        long templateCount = templateVersionMapper.selectCount(Wrappers.<ConfigTemplateVersion>lambdaQuery()
            .eq(ConfigTemplateVersion::getProductModelId, modelId));
        long priceCount = pricePlanVersionMapper.selectCount(Wrappers.<PricePlanVersion>lambdaQuery()
            .eq(PricePlanVersion::getProductModelId, modelId));
        long bindingCount = mediaBindingMapper.selectCount(Wrappers.<ProductMediaBinding>lambdaQuery()
            .eq(ProductMediaBinding::getTargetType, "PRODUCT_MODEL")
            .eq(ProductMediaBinding::getTargetId, modelId)
            .eq(ProductMediaBinding::getDelFlag, "0"));
        addReference(result, "salesVariant", variantCount);
        addReference(result, "templateVersion", templateCount);
        addReference(result, "priceVersion", priceCount);
        addReference(result, "mediaBinding", bindingCount);
        return result;
    }

    public ReferenceCheckResultVo checkSalesVariantReferences(Long variantId) {
        ReferenceCheckResultVo result = new ReferenceCheckResultVo();
        long templateCount = templateVersionMapper.selectCount(Wrappers.<ConfigTemplateVersion>lambdaQuery()
            .eq(ConfigTemplateVersion::getSalesVariantId, variantId));
        long priceCount = pricePlanVersionMapper.selectCount(Wrappers.<PricePlanVersion>lambdaQuery()
            .eq(PricePlanVersion::getSalesVariantId, variantId));
        long bindingCount = mediaBindingMapper.selectCount(Wrappers.<ProductMediaBinding>lambdaQuery()
            .eq(ProductMediaBinding::getTargetType, "SALES_VARIANT")
            .eq(ProductMediaBinding::getTargetId, variantId)
            .eq(ProductMediaBinding::getDelFlag, "0"));
        addReference(result, "templateVersion", templateCount);
        addReference(result, "priceVersion", priceCount);
        addReference(result, "mediaBinding", bindingCount);
        return result;
    }

    private void addReference(ReferenceCheckResultVo result, String label, long count) {
        if (count <= 0) {
            return;
        }
        result.setAllowed(Boolean.FALSE);
        result.setReferenceCount(result.getReferenceCount() + count);
        result.setBlockerReasonKey("product.reference.inUse");
        result.getReferenceSummaries().add(label + ":" + count);
    }
}
