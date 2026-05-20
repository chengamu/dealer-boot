package com.bocoo.healthbrain.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.healthbrain.domain.entity.HealthbrainPolicy;
import com.bocoo.healthbrain.domain.vo.HealthbrainPolicyTypeCountVo;
import com.bocoo.healthbrain.domain.vo.HealthbrainPolicyVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 政策管理 数据层
 *
 * @author cmx
 */
public interface HealthbrainPolicyMapper extends BaseMapperPlus<HealthbrainPolicy, HealthbrainPolicyVo> {

    /**
     * 按政策类型统计
     *
     * @return 统计结果
     */
    List<HealthbrainPolicyTypeCountVo> selectCountByType();
}