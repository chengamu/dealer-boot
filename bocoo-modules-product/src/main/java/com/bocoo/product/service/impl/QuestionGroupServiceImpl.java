package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.QuestionGroupBo;
import com.bocoo.product.domain.entity.ConfigQuestion;
import com.bocoo.product.domain.entity.QuestionGroup;
import com.bocoo.product.domain.vo.QuestionGroupVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ConfigQuestionMapper;
import com.bocoo.product.mapper.QuestionGroupMapper;
import com.bocoo.product.service.QuestionGroupService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionGroupServiceImpl extends ProductServiceSupport implements QuestionGroupService {

    private final QuestionGroupMapper questionGroupMapper;
    private final ConfigQuestionMapper questionMapper;

    @Override
    public TableDataInfo<QuestionGroupVo> queryPageList(QuestionGroupBo bo, PageQuery pageQuery) {
        return page(questionGroupMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<QuestionGroupVo> queryList(QuestionGroupBo bo) {
        return questionGroupMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public QuestionGroupVo queryById(Long id) {
        return questionGroupMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(QuestionGroupBo bo) {
        QuestionGroup entity = MapstructUtils.convert(bo, QuestionGroup.class);
        if (entity == null) { return Boolean.FALSE; }
        ProductEntityDefaults.prepareInsert(entity);
        return questionGroupMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(QuestionGroupBo bo) {
        QuestionGroup entity = MapstructUtils.convert(bo, QuestionGroup.class);
        if (entity == null) { return Boolean.FALSE; }
        return questionGroupMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(questionGroupMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return questionGroupMapper.update(null, new LambdaUpdateWrapper<QuestionGroup>()
            .eq(QuestionGroup::getQuestionGroupId, id)
            .set(QuestionGroup::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long id) {
        long count = questionMapper.selectCount(activeQuery(ConfigQuestion.class).eq("question_group_id", id));
        return referenceResult(count, "product.questionGroup.hasReferences", "Config questions: " + count);
    }

    private QueryWrapper<QuestionGroup> buildQueryWrapper(QuestionGroupBo bo) {
        QueryWrapper<QuestionGroup> q = activeQuery(QuestionGroup.class);
        if (bo != null) {
        like(q, "group_code", bo.getGroupCode());
        like(q, "group_name_cn", bo.getGroupNameCn());
        like(q, "group_name_en", bo.getGroupNameEn());
        like(q, "status", bo.getStatus());
        }
        return q.orderByDesc("update_time");
    }

}
