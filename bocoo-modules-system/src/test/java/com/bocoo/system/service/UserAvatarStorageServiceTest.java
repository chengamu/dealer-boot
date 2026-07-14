package com.bocoo.system.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.mapper.SysUserMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAvatarStorageServiceTest {

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysOssService ossService;
    private UserAvatarStorageService service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SysUser.class);
        service = new UserAvatarStorageService(userMapper, ossService);
    }

    @Test
    void replaceDeletesOldAvatarAfterUpdate() {
        SysUser current = new SysUser();
        current.setAvatar("1001");
        when(userMapper.selectOne(any())).thenReturn(current);
        when(userMapper.update(any(), any())).thenReturn(1);

        assertThat(service.replace("admin", 1002L)).isTrue();

        verify(ossService).deleteByIds(List.of(1001L));
    }

    @Test
    void replaceDeletesNewAvatarWhenUpdateFails() {
        when(userMapper.update(any(), any())).thenReturn(0);

        assertThat(service.replace("admin", 1002L)).isFalse();

        verify(ossService).deleteByIds(List.of(1002L));
    }
}
