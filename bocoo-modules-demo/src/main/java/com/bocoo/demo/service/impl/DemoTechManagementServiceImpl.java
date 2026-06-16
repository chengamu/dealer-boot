package com.bocoo.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.MessageUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.mybatis.core.service.impl.BaseServiceImpl;
import com.bocoo.demo.domain.bo.DemoTechManagementBo;
import com.bocoo.demo.domain.entity.DemoTechManagement;
import com.bocoo.demo.domain.vo.DemoTechManagementVo;
import com.bocoo.demo.domain.vo.DemoTechnologyStatusCountVo;
import com.bocoo.demo.mapper.DemoTechManagementMapper;
import com.bocoo.demo.service.DemoTechManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 技术管理示例服务实现层。
 */
@Service
@RequiredArgsConstructor
public class DemoTechManagementServiceImpl
    extends BaseServiceImpl<DemoTechManagementMapper, DemoTechManagement, DemoTechManagementVo>
    implements DemoTechManagementService {

    @Override
    public TableDataInfo<DemoTechManagementVo> queryPageList(DemoTechManagementBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DemoTechManagement> lqw = buildQueryWrapper(bo);
        Page<DemoTechManagementVo> result = pageVo(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<DemoTechManagementVo> queryList(DemoTechManagementBo bo) {
        return listVo(buildQueryWrapper(bo));
    }

    @Override
    public DemoTechManagementVo queryById(Long managementId) {
        return getVoById(managementId);
    }

    @Override
    public Boolean insertByBo(DemoTechManagementBo bo) {
        DemoTechManagement entity = MapstructUtils.convert(bo, DemoTechManagement.class);
        validEntityBeforeSave(entity);
        return save(entity);
    }

    @Override
    public Boolean updateByBo(DemoTechManagementBo bo) {
        DemoTechManagement entity = MapstructUtils.convert(bo, DemoTechManagement.class);
        validEntityBeforeSave(entity);
        return updateById(entity);
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (Boolean.TRUE.equals(isValid)) {
            // 示例位置：存在业务引用时，在这里补充删除前校验。
        }
        return removeByIds(ids);
    }

    @Override
    public int updateStatus(Long managementId, String status) {
        if (managementId == null) {
            throw new ServiceException(MessageUtils.message("gen.validation.id.required"));
        }
        if (StringUtils.isBlank(status)) {
            throw new ServiceException(MessageUtils.message("gen.validation.status.required"));
        }
        return baseMapper.update(null,
            new LambdaUpdateWrapper<DemoTechManagement>()
                .eq(DemoTechManagement::getManagementId, managementId)
                .set(DemoTechManagement::getStatus, status));
    }

    @Override
    public List<DemoTechnologyStatusCountVo> selectTechnologyStatusCount() {
        return baseMapper.selectTechnologyStatusCount();
    }

    private void validEntityBeforeSave(DemoTechManagement entity) {
        if (entity == null) {
            throw new ServiceException(MessageUtils.message("demo.request.invalid"));
        }
        Long currentId = entity.getManagementId() == null ? -1L : entity.getManagementId();
        LambdaQueryWrapper<DemoTechManagement> queryWrapper = Wrappers.<DemoTechManagement>lambdaQuery()
            .eq(DemoTechManagement::getTitle, entity.getTitle())
            .ne(DemoTechManagement::getManagementId, currentId);
        if (this.count(queryWrapper) > 0) {
            throw new ServiceException(MessageUtils.message(
                "demo.techManagement.title.exists",
                Map.of("title", entity.getTitle())));
        }
    }

    private LambdaQueryWrapper<DemoTechManagement> buildQueryWrapper(DemoTechManagementBo bo) {
        LambdaQueryWrapper<DemoTechManagement> lqw = Wrappers.lambdaQuery();
        if (bo == null) {
            return lqw.orderByDesc(DemoTechManagement::getCreateTime);
        }
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), DemoTechManagement::getTitle, bo.getTitle());
        lqw.like(StringUtils.isNotBlank(bo.getLabel()), DemoTechManagement::getLabel, bo.getLabel());
        lqw.like(StringUtils.isNotBlank(bo.getCustomLabel()), DemoTechManagement::getCustomLabel, bo.getCustomLabel());
        lqw.eq(StringUtils.isNotBlank(bo.getDeadline()), DemoTechManagement::getDeadline, bo.getDeadline());
        lqw.eq(bo.getEnterpriseId() != null, DemoTechManagement::getEnterpriseId, bo.getEnterpriseId());
        lqw.like(StringUtils.isNotBlank(bo.getContact()), DemoTechManagement::getContact, bo.getContact());
        lqw.like(StringUtils.isNotBlank(bo.getContactInfo()), DemoTechManagement::getContactInfo, bo.getContactInfo());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), DemoTechManagement::getStatus, bo.getStatus());
        return lqw.orderByDesc(DemoTechManagement::getCreateTime);
    }
}
