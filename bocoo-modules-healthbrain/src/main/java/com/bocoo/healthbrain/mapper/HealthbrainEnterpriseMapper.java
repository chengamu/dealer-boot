package com.bocoo.healthbrain.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.healthbrain.domain.entity.HealthbrainEnterprise;
import com.bocoo.healthbrain.domain.vo.HealthbrainEnterpriseVo;
import com.bocoo.healthbrain.domain.vo.HealthbrainEnterpriseTypeCountVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 企业管理 数据层
 *
 * @author cmx
 */
public interface HealthbrainEnterpriseMapper extends BaseMapperPlus<HealthbrainEnterprise, HealthbrainEnterpriseVo> {

    /**
     * 按企业类型统计
     * 
     * @return 统计结果
     */
    List<HealthbrainEnterpriseTypeCountVo> selectCountByType();

    /**
     * 按企业发展阶段统计
     *
     * @return 统计结果
     */
    List<HealthbrainEnterpriseTypeCountVo> selectCountByDevelopment();
}