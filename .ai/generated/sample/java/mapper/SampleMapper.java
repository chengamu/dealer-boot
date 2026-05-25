package com.bocoo.demo.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.demo.domain.entity.Sample;
import com.bocoo.demo.domain.vo.SampleVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 样本 数据层
 *
 * @author validator
 */
public interface SampleMapper extends BaseMapperPlus<Sample, SampleVo> {

}
