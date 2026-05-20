package com.bocoo.healthbrain.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.healthbrain.domain.entity.HealthbrainFundType;
import com.bocoo.healthbrain.domain.vo.HealthbrainFundTypeVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资金类型 数据层
 *
 * @author cmx
 */
public interface HealthbrainFundTypeMapper extends BaseMapperPlus<HealthbrainFundType, HealthbrainFundTypeVo> {

}
