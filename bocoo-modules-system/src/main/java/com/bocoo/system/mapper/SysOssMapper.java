package com.bocoo.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.mybatis.annotation.DataColumn;
import com.bocoo.common.mybatis.annotation.DataPermission;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.system.domain.entity.SysOss;
import com.bocoo.system.domain.vo.SysOssVo;
import org.apache.ibatis.annotations.Param;

/**
 * 文件上传 数据层
 *
 * @author cmx
 */
public interface SysOssMapper extends BaseMapperPlus<SysOss, SysOssVo> {

    /**
     * 分页查询OSS文件列表
     *
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @return OSS文件分页数据
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by_id")
    })
    Page<SysOssVo> selectPageOssList(@Param("page") Page<SysOss> page, @Param(Constants.WRAPPER) Wrapper<SysOss> queryWrapper);
}
