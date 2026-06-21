package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ConfigQuestionBo;
import com.bocoo.product.domain.entity.ConfigOption;
import com.bocoo.product.domain.entity.ConfigQuestion;
import com.bocoo.product.domain.vo.ConfigQuestionVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ConfigOptionMapper;
import com.bocoo.product.mapper.ConfigQuestionMapper;
import com.bocoo.product.service.ConfigQuestionService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigQuestionServiceImpl extends ProductServiceSupport implements ConfigQuestionService {

    private final ConfigQuestionMapper configQuestionMapper;
    private final ConfigOptionMapper optionMapper;

    @Override
    public TableDataInfo<ConfigQuestionVo> queryPageList(ConfigQuestionBo bo, PageQuery pageQuery) {
        return page(configQuestionMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "question_id"));
    }

    @Override
    public List<ConfigQuestionVo> queryList(ConfigQuestionBo bo) {
        return configQuestionMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "question_id")));
    }

    @Override
    public ConfigQuestionVo queryById(Long id) {
        return configQuestionMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ConfigQuestionBo bo) {
        ConfigQuestion entity = MapstructUtils.convert(bo, ConfigQuestion.class);
        if (entity == null) { return Boolean.FALSE; }
        ProductEntityDefaults.prepareInsert(entity);
        return configQuestionMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ConfigQuestionBo bo) {
        ConfigQuestion entity = MapstructUtils.convert(bo, ConfigQuestion.class);
        if (entity == null) { return Boolean.FALSE; }
        return configQuestionMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(configQuestionMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return configQuestionMapper.update(null, new LambdaUpdateWrapper<ConfigQuestion>()
            .eq(ConfigQuestion::getQuestionId, id)
            .set(ConfigQuestion::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long id) {
        long count = optionMapper.selectCount(activeQuery(ConfigOption.class).eq("question_id", id));
        return referenceResult(count, "product.configQuestion.hasReferences", "Config options: " + count);
    }

    private QueryWrapper<ConfigQuestion> buildQueryWrapper(ConfigQuestionBo bo) {
        QueryWrapper<ConfigQuestion> q = activeQuery(ConfigQuestion.class);
        if (bo != null) {
        eq(q, "template_version_id", bo.getTemplateVersionId());
        eq(q, "question_group_id", bo.getQuestionGroupId());
        like(q, "question_code", bo.getQuestionCode());
        like(q, "question_name_cn", bo.getQuestionNameCn());
        like(q, "question_name_en", bo.getQuestionNameEn());
        like(q, "input_type", bo.getInputType());
        like(q, "status", bo.getStatus());
        }
        return q;
    }

}
