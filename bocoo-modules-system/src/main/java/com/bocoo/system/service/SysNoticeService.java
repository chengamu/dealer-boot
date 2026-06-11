package com.bocoo.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.bo.SysNoticeBo;
import com.bocoo.system.domain.entity.SysNotice;
import com.bocoo.system.domain.entity.SysNoticeRead;
import com.bocoo.system.domain.vo.SysNoticeVo;
import com.bocoo.system.mapper.SysNoticeMapper;
import com.bocoo.system.mapper.SysNoticeReadMapper;
import com.bocoo.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 公告 服务层实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysNoticeService {

    private final SysNoticeMapper noticeMapper;
    private final SysNoticeReadMapper noticeReadMapper;
    private final SysUserMapper userMapper;

    public TableDataInfo<SysNoticeVo> selectPageNoticeList(SysNoticeBo notice, PageQuery pageQuery) {
        LambdaQueryWrapper<SysNotice> lqw = buildQueryWrapper(notice);
        Page<SysNoticeVo> page = noticeMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    public TableDataInfo<SysNoticeVo> selectMyNoticePage(PageQuery pageQuery) {
        Long userId = LoginHelper.getUserId();
        LambdaQueryWrapper<SysNotice> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysNotice::getStatus, "1");
        lqw.orderByDesc(SysNotice::getCreateTime);
        lqw.orderByDesc(SysNotice::getNoticeId);
        Page<SysNoticeVo> page = noticeMapper.selectVoPage(pageQuery.build(), lqw);
        fillReadState(page.getRecords(), userId);
        return TableDataInfo.build(page);
    }

    public Long selectMyUnreadCount() {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return 0L;
        }
        return noticeMapper.selectCount(Wrappers.<SysNotice>lambdaQuery()
            .eq(SysNotice::getStatus, "1")
            .notExists("select 1 from sys_notice_read nr where nr.notice_id = sys_notice.notice_id and nr.user_id = {0}", userId));
    }

    /**
     * 查询公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    public SysNoticeVo selectNoticeById(Long noticeId) {
        return noticeMapper.selectVoById(noticeId);
    }

    public SysNoticeVo selectMyNoticeById(Long noticeId) {
        SysNoticeVo notice = noticeMapper.selectVoOne(Wrappers.<SysNotice>lambdaQuery()
            .eq(SysNotice::getNoticeId, noticeId)
            .eq(SysNotice::getStatus, "1"));
        if (notice == null) {
            return null;
        }
        markMyNoticeRead(noticeId);
        fillReadState(List.of(notice), LoginHelper.getUserId());
        return notice;
    }

    public boolean markMyNoticeRead(Long noticeId) {
        Long userId = LoginHelper.getUserId();
        Long tenantId = LoginHelper.getTenantId();
        if (userId == null || noticeId == null) {
            return false;
        }
        boolean exists = noticeReadMapper.exists(Wrappers.<SysNoticeRead>lambdaQuery()
            .eq(SysNoticeRead::getNoticeId, noticeId)
            .eq(SysNoticeRead::getUserId, userId));
        if (exists) {
            return true;
        }
        SysNotice notice = noticeMapper.selectOne(Wrappers.<SysNotice>lambdaQuery()
            .eq(SysNotice::getNoticeId, noticeId)
            .eq(SysNotice::getStatus, "1"));
        if (notice == null) {
            return false;
        }
        SysNoticeRead read = new SysNoticeRead();
        read.setTenantId(tenantId);
        read.setNoticeId(noticeId);
        read.setUserId(userId);
        read.setReadTime(TimeUtils.utcNow());
        return noticeReadMapper.insert(read) > 0;
    }

    public int markAllMyNoticesRead() {
        Long userId = LoginHelper.getUserId();
        Long tenantId = LoginHelper.getTenantId();
        if (userId == null) {
            return 0;
        }
        List<SysNotice> notices = noticeMapper.selectList(Wrappers.<SysNotice>lambdaQuery()
            .select(SysNotice::getNoticeId)
            .eq(SysNotice::getStatus, "1"));
        if (notices.isEmpty()) {
            return 0;
        }
        List<Long> noticeIds = notices.stream().map(SysNotice::getNoticeId).toList();
        Set<Long> readNoticeIds = noticeReadMapper.selectList(Wrappers.<SysNoticeRead>lambdaQuery()
                .select(SysNoticeRead::getNoticeId)
                .eq(SysNoticeRead::getUserId, userId)
                .in(SysNoticeRead::getNoticeId, noticeIds))
            .stream()
            .map(SysNoticeRead::getNoticeId)
            .collect(Collectors.toSet());
        List<SysNoticeRead> pendingReads = noticeIds.stream()
            .filter(noticeId -> !readNoticeIds.contains(noticeId))
            .map(noticeId -> {
                SysNoticeRead read = new SysNoticeRead();
                read.setTenantId(tenantId);
                read.setNoticeId(noticeId);
                read.setUserId(userId);
                read.setReadTime(TimeUtils.utcNow());
                return read;
            })
            .toList();
        if (pendingReads.isEmpty()) {
            return 0;
        }
        return noticeReadMapper.insertBatch(pendingReads) ? pendingReads.size() : 0;
    }

    /**
     * 查询公告列表
     *
     * @param notice 公告信息
     * @return 公告集合
     */
    public List<SysNoticeVo> selectNoticeList(SysNoticeBo notice) {
        LambdaQueryWrapper<SysNotice> lqw = buildQueryWrapper(notice);
        return noticeMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysNotice> buildQueryWrapper(SysNoticeBo bo) {
        LambdaQueryWrapper<SysNotice> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getNoticeTitle()), SysNotice::getNoticeTitle, bo.getNoticeTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getNoticeType()), SysNotice::getNoticeType, bo.getNoticeType());
        lqw.eq(StringUtils.isNotBlank(bo.getCreateBy()), SysNotice::getCreateBy, bo.getCreateBy());
        lqw.orderByAsc(SysNotice::getNoticeId);
        return lqw;
    }

    private void fillReadState(List<SysNoticeVo> notices, Long userId) {
        if (notices == null || notices.isEmpty() || userId == null) {
            if (notices != null) {
                notices.forEach(item -> item.setRead(false));
            }
            return;
        }
        List<Long> noticeIds = notices.stream().map(SysNoticeVo::getNoticeId).toList();
        Map<Long, SysNoticeRead> readMap = noticeReadMapper.selectList(Wrappers.<SysNoticeRead>lambdaQuery()
                .eq(SysNoticeRead::getUserId, userId)
                .in(SysNoticeRead::getNoticeId, noticeIds))
            .stream()
            .collect(Collectors.toMap(SysNoticeRead::getNoticeId, Function.identity(), (left, right) -> left));
        notices.forEach(notice -> {
            SysNoticeRead read = readMap.get(notice.getNoticeId());
            notice.setRead(read != null);
            notice.setReadTime(read == null ? null : read.getReadTime());
        });
    }

    /**
     * 新增公告
     *
     * @param bo 公告信息
     * @return 结果
     */
    public int insertNotice(SysNoticeBo bo) {
        SysNotice notice = MapstructUtils.convert(bo, SysNotice.class);
        return noticeMapper.insert(notice);
    }

    /**
     * 修改公告
     *
     * @param bo 公告信息
     * @return 结果
     */
    public int updateNotice(SysNoticeBo bo) {
        SysNotice notice = MapstructUtils.convert(bo, SysNotice.class);
        return noticeMapper.updateById(notice);
    }

    /**
     * 删除公告对象
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    public int deleteNoticeById(Long noticeId) {
        return noticeMapper.deleteById(noticeId);
    }

    /**
     * 批量删除公告信息
     *
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    public int deleteNoticeByIds(Long[] noticeIds) {
        return noticeMapper.deleteBatchIds(Arrays.asList(noticeIds));
    }
}
