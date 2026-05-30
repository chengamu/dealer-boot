package com.bocoo.system.service;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.core.utils.ip.AddressUtils;
import com.bocoo.common.log.event.OperLogEvent;
import com.bocoo.system.domain.bo.SysOperLogBo;
import com.bocoo.system.domain.entity.SysOperLog;
import com.bocoo.system.domain.vo.SysOperLogVo;
import com.bocoo.system.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 操作日志 服务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysOperLogService {

    private static final int CLEAN_BATCH_SIZE = 1000;

    private final SysOperLogMapper operLogMapper;

    /**
     * 操作日志记录
     *
     * @param operLogEvent 操作日志事件
     */
    @Async
    @EventListener
    public void recordOper(OperLogEvent operLogEvent) {
        SysOperLogBo operLog = MapstructUtils.convert(operLogEvent, SysOperLogBo.class);
        // 远程查询操作地点
        operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
        insertOperlog(operLog);
    }

    public TableDataInfo<SysOperLogVo> selectPageOperLogList(SysOperLogBo operLog, PageQuery pageQuery) {
        Map<String, Object> params = operLog.getParams();
        LambdaQueryWrapper<SysOperLog> lqw = new LambdaQueryWrapper<SysOperLog>()
            .like(StringUtils.isNotBlank(operLog.getOperIp()), SysOperLog::getOperIp, operLog.getOperIp())
            .like(StringUtils.isNotBlank(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle())
            .eq(operLog.getBusinessType() != null && operLog.getBusinessType() > 0,
                SysOperLog::getBusinessType, operLog.getBusinessType())
            .func(f -> {
                if (ArrayUtil.isNotEmpty(operLog.getBusinessTypes())) {
                    f.in(SysOperLog::getBusinessType, Arrays.asList(operLog.getBusinessTypes()));
                }
            })
            .eq(operLog.getStatus() != null,
                SysOperLog::getStatus, operLog.getStatus())
            .like(StringUtils.isNotBlank(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysOperLog::getOperTime, params.get("beginTime"), params.get("endTime"));
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            pageQuery.setOrderByColumn("oper_id");
            pageQuery.setIsAsc("desc");
        }
        Page<SysOperLogVo> page = operLogMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 新增操作日志
     *
     * @param bo 操作日志对象
     */
    public void insertOperlog(SysOperLogBo bo) {
        SysOperLog operLog = MapstructUtils.convert(bo, SysOperLog.class);
        operLog.setOperTime(TimeUtils.utcNow());
        operLogMapper.insert(operLog);
    }

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    public List<SysOperLogVo> selectOperLogList(SysOperLogBo operLog) {
        Map<String, Object> params = operLog.getParams();
        return operLogMapper.selectVoList(new LambdaQueryWrapper<SysOperLog>()
            .like(StringUtils.isNotBlank(operLog.getOperIp()), SysOperLog::getOperIp, operLog.getOperIp())
            .like(StringUtils.isNotBlank(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle())
            .eq(operLog.getBusinessType() != null && operLog.getBusinessType() > 0,
                SysOperLog::getBusinessType, operLog.getBusinessType())
            .func(f -> {
                if (ArrayUtil.isNotEmpty(operLog.getBusinessTypes())) {
                    f.in(SysOperLog::getBusinessType, Arrays.asList(operLog.getBusinessTypes()));
                }
            })
            .eq(operLog.getStatus() != null && operLog.getStatus() > 0,
                SysOperLog::getStatus, operLog.getStatus())
            .like(StringUtils.isNotBlank(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysOperLog::getOperTime, params.get("beginTime"), params.get("endTime"))
            .orderByDesc(SysOperLog::getOperId));
    }

    /**
     * 批量删除系统操作日志
     *
     * @param operIds 需要删除的操作日志ID
     * @return 结果
     */
    public int deleteOperLogByIds(Long[] operIds) {
        return operLogMapper.deleteBatchIds(Arrays.asList(operIds));
    }

    /**
     * 查询操作日志详细
     *
     * @param operId 操作ID
     * @return 操作日志对象
     */
    public SysOperLogVo selectOperLogById(Long operId) {
        return operLogMapper.selectVoById(operId);
    }

    /**
     * 清空操作日志
     */
    public void cleanOperLog() {
        List<Long> ids;
        do {
            ids = operLogMapper.selectObjs(new LambdaQueryWrapper<SysOperLog>()
                .select(SysOperLog::getOperId)
                .last("limit " + CLEAN_BATCH_SIZE))
                .stream()
                .map(obj -> Long.valueOf(String.valueOf(obj)))
                .toList();
            if (!ids.isEmpty()) {
                operLogMapper.deleteBatchIds(ids);
            }
        } while (ids.size() == CLEAN_BATCH_SIZE);
    }
}
