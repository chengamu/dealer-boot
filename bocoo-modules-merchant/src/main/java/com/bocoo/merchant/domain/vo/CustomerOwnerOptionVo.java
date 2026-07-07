package com.bocoo.merchant.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "客户负责人选项")
public class CustomerOwnerOptionVo {

    private Long userId;

    private String userName;

    private String nickName;
}
