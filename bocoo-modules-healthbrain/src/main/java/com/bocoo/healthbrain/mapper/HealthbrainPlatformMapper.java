package com.bocoo.healthbrain.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.healthbrain.domain.entity.HealthbrainPlatform;
import com.bocoo.healthbrain.domain.vo.HealthbrainPlatformVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 平台管理 数据层
 *
 * @author cmx
 */
public interface HealthbrainPlatformMapper extends BaseMapperPlus<HealthbrainPlatform, HealthbrainPlatformVo> {

}
