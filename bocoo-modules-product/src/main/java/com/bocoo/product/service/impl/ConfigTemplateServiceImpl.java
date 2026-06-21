package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ConfigTemplateBo;
import com.bocoo.product.domain.entity.ConfigTemplate;
import com.bocoo.product.domain.entity.ConfigTemplateVersion;
import com.bocoo.product.domain.entity.SalesProduct;
import com.bocoo.product.domain.vo.ConfigTemplateVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ConfigTemplateMapper;
import com.bocoo.product.mapper.ConfigTemplateVersionMapper;
import com.bocoo.product.mapper.SalesProductMapper;
import com.bocoo.product.service.ConfigTemplateService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigTemplateServiceImpl extends ProductServiceSupport implements ConfigTemplateService {

    private final ConfigTemplateMapper configTemplateMapper;
    private final ConfigTemplateVersionMapper templateVersionMapper;

    private final SalesProductMapper salesProductMapper;

    @Override
    public TableDataInfo<ConfigTemplateVo> queryPageList(ConfigTemplateBo bo, PageQuery pageQuery) {
        return page(configTemplateMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByDesc("update_time"));
    }

    @Override
    public List<ConfigTemplateVo> queryList(ConfigTemplateBo bo) {
        return configTemplateMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByDesc("update_time")));
    }

    @Override
    public ConfigTemplateVo queryById(Long id) {
        return configTemplateMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ConfigTemplateBo bo) {
        ConfigTemplate entity = MapstructUtils.convert(bo, ConfigTemplate.class);
        if (entity == null) { return Boolean.FALSE; }
        ProductEntityDefaults.prepareInsert(entity);
        return configTemplateMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ConfigTemplateBo bo) {
        ConfigTemplate entity = MapstructUtils.convert(bo, ConfigTemplate.class);
        if (entity == null) { return Boolean.FALSE; }
        return configTemplateMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(configTemplateMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return configTemplateMapper.update(null, new LambdaUpdateWrapper<ConfigTemplate>()
            .eq(ConfigTemplate::getTemplateId, id)
            .set(ConfigTemplate::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long id) {
        long count = templateVersionMapper.selectCount(activeQuery(ConfigTemplateVersion.class).eq("template_id", id))
            + salesProductMapper.selectCount(activeQuery(SalesProduct.class).eq("template_id", id));
        return referenceResult(count, "product.configTemplate.hasReferences", "Template versions / sales products: " + count);
    }

    private QueryWrapper<ConfigTemplate> buildQueryWrapper(ConfigTemplateBo bo) {
        QueryWrapper<ConfigTemplate> q = activeQuery(ConfigTemplate.class);
        if (bo != null) {
        like(q, "template_code", bo.getTemplateCode());
        like(q, "template_name_cn", bo.getTemplateNameCn());
        like(q, "template_name_en", bo.getTemplateNameEn());
        like(q, "product_model_code", bo.getProductModelCode());
        eq(q, "sales_product_id", bo.getSalesProductId());
        like(q, "sales_product_code", bo.getSalesProductCode());
        like(q, "biz_status", bo.getBizStatus());
        like(q, "status", bo.getStatus());
        }
        return q;
    }

}
