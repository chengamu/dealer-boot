package com.bocoo.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.system.domain.entity.SysDept;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.domain.entity.SysUserPost;
import com.bocoo.system.domain.entity.SysUserRole;
import com.bocoo.system.mapper.*;
import com.bocoo.common.core.constant.CacheNames;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.service.UserService;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StreamUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.mybatis.helper.DataBaseHelper;
import com.bocoo.common.mybatis.helper.DataPermissionHelper;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.bo.SysUserBo;
import com.bocoo.system.domain.vo.SysPostVo;
import com.bocoo.system.domain.vo.SysRoleVo;
import com.bocoo.system.domain.vo.SysUserExportVo;
import com.bocoo.system.domain.vo.SysUserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 用户 业务层处理
 *
 * @author cmx
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysUserService implements UserService {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysPostMapper postMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserPostMapper userPostMapper;
    private final SysOssService ossService;
    private final SysConfigService configService;
    private final UserAvatarStorageService avatarStorageService;

    private static final String USER_INIT_PASSWORD_CONFIG_KEY = "sys.user.initPassword";
    private static final int USER_INIT_PASSWORD_LENGTH = 12;
    private static final String USER_INIT_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#%";

    public TableDataInfo<SysUserVo> selectPageUserList(SysUserBo user, PageQuery pageQuery) {
        Page<SysUserVo> page = callWithPlatformBypass(() ->
            userMapper.selectPageUserList(pageQuery.build(), this.buildQueryWrapper(user)));
        return TableDataInfo.build(page);
    }

    public TableDataInfo<SysUserVo> selectPageMerchantUserList(SysUserBo user, PageQuery pageQuery) {
        return selectPageMerchantUserList(user, pageQuery, false);
    }

    public TableDataInfo<SysUserVo> selectPageMerchantUserList(SysUserBo user, PageQuery pageQuery, boolean merchantTenantOnly) {
        Page<SysUserVo> page = callWithPlatformBypass(() ->
            userMapper.selectPageUserList(pageQuery.build(), this.buildQueryWrapper(user, merchantTenantOnly)));
        page.getRecords().forEach(item -> item.setRoles(roleMapper.selectRolesByUserId(item.getUserId())));
        return TableDataInfo.build(page);
    }


    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUserVo> selectUserList(SysUserBo user) {
        return callWithPlatformBypass(() -> userMapper.selectUserList(this.buildQueryWrapper(user)));
    }

    private Wrapper<SysUser> buildQueryWrapper(SysUserBo user) {
        return buildQueryWrapper(user, false);
    }

    private Wrapper<SysUser> buildQueryWrapper(SysUserBo user, boolean merchantTenantOnly) {
        Map<String, Object> params = user.getParams();
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", UserConstants.NOT_DELETED)
            .eq(ObjectUtil.isNotNull(user.getUserId()), "u.user_id", user.getUserId())
            .eq(ObjectUtil.isNotNull(user.getTenantId()), "u.tenant_id", user.getTenantId())
            .inSql(merchantTenantOnly, "u.tenant_id", "select tenant_id from sys_tenant where tenant_type = '" + TenantType.MERCHANT.getCode() + "'")
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                "u.create_time", params.get("beginTime"), params.get("endTime"))
            .and(ObjectUtil.isNotNull(user.getDeptId()), w -> {
                List<SysDept> deptList = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                    .select(SysDept::getDeptId)
                    .apply(DataBaseHelper.findInSet(user.getDeptId(), "ancestors")));
                List<Long> ids = StreamUtils.toList(deptList, SysDept::getDeptId);
                ids.add(user.getDeptId());
                w.in("u.dept_id", ids);
            });
        return wrapper;
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public TableDataInfo<SysUserVo> selectAllocatedList(SysUserBo user, PageQuery pageQuery) {
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", UserConstants.NOT_DELETED)
            .eq(ObjectUtil.isNotNull(user.getRoleId()), "r.role_id", user.getRoleId())
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber());
        Page<SysUserVo> page = userMapper.selectAllocatedList(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public TableDataInfo<SysUserVo> selectUnallocatedList(SysUserBo user, PageQuery pageQuery) {
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(user.getRoleId());
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", UserConstants.NOT_DELETED)
            .and(w -> w.ne("r.role_id", user.getRoleId()).or().isNull("r.role_id"))
            .notIn(CollUtil.isNotEmpty(userIds), "u.user_id", userIds)
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber());
        Page<SysUserVo> page = userMapper.selectUnallocatedList(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    public SysUserVo selectUserByUserName(String userName) {
        return userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, userName));
    }

    /**
     * 通过手机号查询用户
     *
     * @param phonenumber 手机号
     * @return 用户对象信息
     */
    public SysUserVo selectUserByPhonenumber(String phonenumber) {
        return userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhonenumber, phonenumber));
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    public SysUserVo selectUserById(Long userId) {
        SysUserVo user = userMapper.selectVoById(userId);
        if (ObjectUtil.isNull(user)) {
            return user;
        }
        user.setRoles(roleMapper.selectRolesByUserId(user.getUserId()));
        user.setAvatar(ossService.resolveUrl(user.getAvatar()));
        return user;
    }

    /**
     * 查询用户所属角色组
     *
     * @param userId 用户ID
     * @return 结果
     */
    public String selectUserRoleGroup(Long userId) {
        List<SysRoleVo> list = roleMapper.selectRolesByUserId(userId);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysRoleVo::getRoleName);
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 结果
     */
    public String selectUserPostGroup(Long userId) {
        List<SysPostVo> list = postMapper.selectPostsByUserId(userId);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysPostVo::getPostName);
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean checkUserNameUnique(SysUserBo user) {
        boolean exist = userMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUserName, user.getUserName())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        return !exist;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     */
    public boolean checkPhoneUnique(SysUserBo user) {
        boolean exist = userMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getPhonenumber, user.getPhonenumber())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        return !exist;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     */
    public boolean checkEmailUnique(SysUserBo user) {
        boolean exist = TenantContextHolder.callWithIgnore(() -> userMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getEmail, user.getEmail())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId())));
        return !exist;
    }

    public boolean userHasRoleKey(Long userId, String roleKey) {
        List<SysRoleVo> roles = roleMapper.selectRolesByUserId(userId);
        return roles.stream().anyMatch(role -> roleKey.equals(role.getRoleKey()));
    }

    public String generateRandomPassword() {
        return RandomUtil.randomString(USER_INIT_PASSWORD_CHARS, USER_INIT_PASSWORD_LENGTH);
    }

    public String resolveInitialPassword(String initPassword) {
        if (StringUtils.isNotBlank(initPassword) && !"123456".equals(initPassword)) {
            return initPassword;
        }
        String configuredPassword = StringUtils.trimToEmpty(configService.selectConfigByKey(USER_INIT_PASSWORD_CONFIG_KEY));
        if (StringUtils.isNotBlank(configuredPassword) && !"123456".equals(configuredPassword)) {
            return configuredPassword;
        }
        return generateRandomPassword();
    }

    /**
     * 校验用户是否允许操作
     *
     * @param userId 用户ID
     */
    public void checkUserAllowed(Long userId) {
        if (ObjectUtil.isNotNull(userId) && LoginHelper.isAdmin(userId)) {
            throw ServiceException.ofMessageKey("sys.user.admin.operation.denied");
        }
    }

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    public void checkUserDataScope(Long userId) {
        if (ObjectUtil.isNull(userId)) {
            return;
        }
        if (LoginHelper.isAdmin()) {
            return;
        }
        Long count = callWithPlatformBypass(() -> userMapper.countUserById(userId));
        if (count == 0) {
            throw ServiceException.ofMessageKey("sys.user.data.permission.denied");
        }
    }

    private <T> T callWithPlatformBypass(Supplier<T> supplier) {
        if (!LoginHelper.isPlatformTenant()) {
            return supplier.get();
        }
        return TenantContextHolder.callWithIgnore(() -> DataPermissionHelper.ignore(supplier));
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public int insertUser(SysUserBo user) {
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        // 新增用户信息
        int rows = userMapper.insert(sysUser);
        user.setUserId(sysUser.getUserId());
        // 新增用户岗位关联
        insertUserPost(user);
        // 新增用户与角色管理
        insertUserRole(user);
        return rows;
    }

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean registerUser(SysUserBo user) {
        user.setCreateBy(user.getUserName());
        user.setUpdateBy(user.getUserName());
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        boolean inserted = userMapper.insert(sysUser) > 0;
        if (inserted) {
            user.setUserId(sysUser.getUserId());
        }
        return inserted;
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(SysUserBo user) {
        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 新增用户与角色管理
        insertUserRole(user);
        // 删除用户与岗位关联
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, userId));
        // 新增用户与岗位管理
        insertUserPost(user);
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        return userMapper.updateById(sysUser);
    }

    public int updateMerchantUser(SysUserBo user) {
        return userMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(ObjectUtil.isNotNull(user.getNickName()), SysUser::getNickName, user.getNickName())
                .set(SysUser::getPhonenumber, user.getPhonenumber())
                .set(SysUser::getEmail, user.getEmail())
                .set(SysUser::getSex, user.getSex())
                .set(StringUtils.isNotBlank(user.getStatus()), SysUser::getStatus, user.getStatus())
                .set(SysUser::getRemark, user.getRemark())
                .eq(SysUser::getUserId, user.getUserId()));
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertUserAuth(Long userId, Long[] roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getUserId, userId));
        insertUserRole(userId, roleIds);
    }

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 帐号状态
     * @return 结果
     */
    public int updateUserStatus(Long userId, String status) {
        return userMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getStatus, status)
                .eq(SysUser::getUserId, userId));
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @CacheEvict(cacheNames = CacheNames.SYS_NICKNAME, key = "#user.userId")
    public int updateUserProfile(SysUserBo user) {
        return userMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(ObjectUtil.isNotNull(user.getNickName()), SysUser::getNickName, user.getNickName())
                .set(SysUser::getPhonenumber, user.getPhonenumber())
                .set(SysUser::getSex, user.getSex())
                .eq(SysUser::getUserId, user.getUserId()));
    }

    /**
     * 修改用户头像
     *
     * @param userName 用户名
     * @param avatar   头像地址
     * @return 结果
     */
    public boolean updateUserAvatar(String userName, String avatar) {
        return userMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getAvatar, avatar)
                .eq(SysUser::getUserName, userName)) > 0;
    }

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param password 密码
     * @return 结果
     */
    public int resetUserPwd(Long userId, String password) {
        return resetUserPwd(userId, password, "1");
    }

    public int resetUserPwd(Long userId, String password, String forcePasswordChange) {
        return userMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getPassword, password)
                .set(SysUser::getForcePasswordChange, forcePasswordChange)
                .eq(SysUser::getUserId, userId));
    }

    /**
     * 新增用户角色信息
     *
     * @param user 用户对象
     */
    public void insertUserRole(SysUserBo user) {
        this.insertUserRole(user.getUserId(), user.getRoleIds());
    }

    /**
     * 新增用户岗位信息
     *
     * @param user 用户对象
     */
    public void insertUserPost(SysUserBo user) {
        Long[] posts = user.getPostIds();
        if (ArrayUtil.isNotEmpty(posts)) {
            // 新增用户与岗位管理
            List<SysUserPost> list = StreamUtils.toList(Arrays.asList(posts), postId -> {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                return up;
            });
            userPostMapper.insertBatch(list);
        }
    }

    /**
     * 新增用户角色信息
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    public void insertUserRole(Long userId, Long[] roleIds) {
        if (ArrayUtil.isNotEmpty(roleIds)) {
            // 新增用户与角色管理
            List<SysUserRole> list = StreamUtils.toList(Arrays.asList(roleIds), roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            });
            userRoleMapper.insertBatch(list);
        }
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserById(Long userId) {
        List<Long> avatarOssIds = avatarStorageService.findByUserIds(List.of(userId));
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 删除用户与岗位表
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, userId));
        int rows = userMapper.deleteById(userId);
        if (rows > 0) avatarStorageService.cleanup(avatarOssIds);
        return rows;
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserByIds(Long[] userIds) {
        for (Long userId : userIds) {
            checkUserAllowed(userId);
            checkUserDataScope(userId);
        }
        List<Long> ids = Arrays.asList(userIds);
        List<Long> avatarOssIds = avatarStorageService.findByUserIds(ids);
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, ids));
        // 删除用户与岗位表
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().in(SysUserPost::getUserId, ids));
        int rows = userMapper.deleteBatchIds(ids);
        if (rows > 0) avatarStorageService.cleanup(avatarOssIds);
        return rows;
    }

    @Cacheable(cacheNames = CacheNames.SYS_USER_NAME, key = "#userId")
    public String selectUserNameById(Long userId) {
        SysUser sysUser = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getUserName).eq(SysUser::getUserId, userId));
        return ObjectUtil.isNull(sysUser) ? null : sysUser.getUserName();
    }

    public List<SysUserExportVo> selectUserExportList(SysUserBo user) {
        return userMapper.selectUserExportList(this.buildQueryWrapper(user));
    }
}
