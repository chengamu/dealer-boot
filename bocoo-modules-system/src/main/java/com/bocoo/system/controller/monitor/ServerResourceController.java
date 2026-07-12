package com.bocoo.system.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.system.domain.vo.ServerResourceVo;
import com.bocoo.system.service.ServerResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/server")
@Tag(name = "服务器资源", description = "服务器资源快照接口")
public class ServerResourceController {

    private final ServerResourceService serverResourceService;

    @SaCheckPermission("monitor:server:list")
    @GetMapping
    @Operation(summary = "获取服务器资源快照")
    public R<ServerResourceVo> get() {
        return R.ok(serverResourceService.snapshot());
    }
}
