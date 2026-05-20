package com.bocoo.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.mybatis.helper.DataBaseHelper;
import com.bocoo.common.core.utils.StreamUtils;
import com.bocoo.system.domain.entity.SysDept;
import com.bocoo.system.domain.entity.SysRoleDept;
import com.bocoo.system.mapper.SysDeptMapper;
import com.bocoo.system.mapper.SysRoleDeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据权限 实现
 * <p>
 * 注意: 此Service内不允许调用标注`数据权限`注解的方法
 * 例如: deptMapper.selectList 此 selectList 方法标注了`数据权限`注解 会出现循环解析的问题
 *
 * @author cmx
 */
@RequiredArgsConstructor
@Service("sdss")
public class SysDataScopeService {

    private final SysRoleDeptMapper roleDeptMapper;
    private final SysDeptMapper deptMapper;

    /**
     * 获取角色自定义部门数据权限
     *
     * @param roleId 角色ID
     * @return 部门ID字符串，多个ID用逗号分隔，如果没有数据则返回null
     */
    public String getRoleCustom(Long roleId) {
        // 查询角色关联的部门列表
        List<SysRoleDept> list = roleDeptMapper.selectList(
            new LambdaQueryWrapper<SysRoleDept>()
                .select(SysRoleDept::getDeptId)
                .eq(SysRoleDept::getRoleId, roleId));
        if (CollUtil.isNotEmpty(list)) {
            // 将部门ID列表转换为逗号分隔的字符串
            return StreamUtils.join(list, rd -> Convert.toStr(rd.getDeptId()));
        }
        return null;
    }

    /**
     * 获取部门及其子部门数据权限
     *
     * @param deptId 部门ID
     * @return 部门ID字符串，包含当前部门及其所有子部门ID，多个ID用逗号分隔，如果没有数据则返回null
     */
    public String getDeptAndChild(Long deptId) {
        // 查询当前部门的所有子孙部门
        List<SysDept> deptList = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
            .select(SysDept::getDeptId)
            .apply(DataBaseHelper.findInSet(deptId, "ancestors")));
        // 提取部门ID列表
        List<Long> ids = StreamUtils.toList(deptList, SysDept::getDeptId);
        // 添加当前部门ID
        ids.add(deptId);
        if (CollUtil.isNotEmpty(ids)) {
            // 将部门ID列表转换为逗号分隔的字符串
            return StreamUtils.join(ids, Convert::toStr);
        }
        return null;
    }


}
