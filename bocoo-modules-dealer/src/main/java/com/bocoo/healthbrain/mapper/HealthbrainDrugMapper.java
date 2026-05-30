package com.bocoo.healthbrain.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.healthbrain.domain.entity.HealthbrainDrug;
import com.bocoo.healthbrain.domain.vo.HealthbrainDrugPhaseCountVo;
import com.bocoo.healthbrain.domain.vo.HealthbrainDrugVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 创新药管理 数据层
 *
 * @author cmx
 */
public interface HealthbrainDrugMapper extends BaseMapperPlus<HealthbrainDrug, HealthbrainDrugVo> {
    
    /**
     * 按药物阶段统计数量
     *
     * @return 药物阶段统计列表
     */
    List<HealthbrainDrugPhaseCountVo> selectDrugPhaseCount();
}