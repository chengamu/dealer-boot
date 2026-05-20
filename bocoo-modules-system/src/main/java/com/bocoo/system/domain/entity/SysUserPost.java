package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户和岗位关联 sys_user_post
 *
 * @author CMX
 */

@Data
@TableName("sys_user_post")
@Schema(description = "用户和岗位关联")
public class SysUserPost {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 岗位ID
     */
    @Schema(description = "岗位ID")
    private Long postId;

}
