package com.bocoo.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAvatarStorageService {

    private final SysUserMapper userMapper;
    private final SysOssService ossService;

    public boolean replace(String userName, Long newOssId) {
        SysUser current = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getAvatar)
            .eq(SysUser::getUserName, userName));
        int rows = userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
            .set(SysUser::getAvatar, String.valueOf(newOssId))
            .eq(SysUser::getUserName, userName));
        if (rows <= 0) {
            cleanup(List.of(newOssId));
            return false;
        }
        cleanup(parseOssIds(current == null ? null : current.getAvatar()));
        return true;
    }

    public List<Long> findByUserIds(Collection<Long> userIds) {
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getAvatar)
                .in(SysUser::getUserId, userIds)).stream()
            .flatMap(user -> parseOssIds(user.getAvatar()).stream())
            .distinct()
            .toList();
    }

    public void cleanup(Collection<Long> ossIds) {
        if (ossIds == null || ossIds.isEmpty()) return;
        try {
            ossService.deleteByIds(ossIds);
        } catch (RuntimeException exception) {
            log.warn("Failed to cleanup avatar OSS files: {}", ossIds, exception);
        }
    }

    private List<Long> parseOssIds(String avatar) {
        if (avatar == null || avatar.isBlank()) return List.of();
        try {
            return List.of(Long.valueOf(avatar));
        } catch (NumberFormatException ignored) {
            return List.of();
        }
    }
}
