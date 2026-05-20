package com.bocoo.healthbrain.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.healthbrain.domain.entity.HealthbrainFundData;
import com.bocoo.healthbrain.domain.vo.HealthbrainFundDataVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资金数据 数据层
 *
 * @author cmx
 */
public interface HealthbrainFundDataMapper extends BaseMapperPlus<HealthbrainFundData, HealthbrainFundDataVo> {

}
