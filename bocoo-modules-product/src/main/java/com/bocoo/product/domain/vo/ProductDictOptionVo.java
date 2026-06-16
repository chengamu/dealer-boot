package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 产品业务字典选项视图对象
 */
@Data
@Schema(description = "产品业务字典选项")
public class ProductDictOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String label;
    private String labelCn;
    private String labelEn;
    private String value;
    private String dictTypeCode;
    private String parentValue;
}
