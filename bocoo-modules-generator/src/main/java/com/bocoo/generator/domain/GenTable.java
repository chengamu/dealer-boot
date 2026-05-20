package com.bocoo.generator.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.bocoo.generator.constant.GenConstants;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.common.core.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ArrayUtils;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 业务表 gen_table
 *
 * @author CMX
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gen_table")
@Schema(description = "代码生成业务表")
public class GenTable extends BaseEntity {

    /**
     * 编号
     */
    @Schema(description = "编号")
    @TableId(value = "table_id")
    private Long id;

    /**
     * 表名称
     */
    @Schema(description = "表名称")
    @NotBlank(message = "表名称不能为空")
    private String tableName;

    /**
     * 表描述
     */
    @Schema(description = "表描述")
    @NotBlank(message = "表描述不能为空")
    private String tableComment;

    /**
     * 关联父表的表名
     */
    @Schema(description = "关联父表的表名")
    private String subTableName;

    /**
     * 本表关联父表的外键名
     */
    @Schema(description = "本表关联父表的外键名")
    private String subTableFkName;

    /**
     * 实体类名称(首字母大写)
     */
    @Schema(description = "实体类名称(首字母大写)")
    @NotBlank(message = "实体类名称不能为空")
    private String className;

    /**
     * 使用的模板（crud单表操作 tree树表操作 sub主子表操作）
     */
    @Schema(description = "使用的模板（crud单表操作 tree树表操作 sub主子表操作）")
    private String tplCategory;

    /**
     * 生成包路径
     */
    @Schema(description = "生成包路径")
    @NotBlank(message = "生成包路径不能为空")
    private String packageName;

    /**
     * 生成模块名
     */
    @Schema(description = "生成模块名")
    @NotBlank(message = "生成模块名不能为空")
    private String moduleName;

    /**
     * 生成业务名
     */
    @Schema(description = "生成业务名")
    @NotBlank(message = "生成业务名不能为空")
    private String businessName;

    /**
     * 生成功能名
     */
    @Schema(description = "生成功能名")
    @NotBlank(message = "生成功能名不能为空")
    private String functionName;

    /**
     * 生成作者
     */
    @Schema(description = "生成作者")
    @NotBlank(message = "作者不能为空")
    private String functionAuthor;

    /**
     * 生成代码方式（0zip压缩包 1自定义路径）
     */
    @Schema(description = "生成代码方式（0zip压缩包 1自定义路径）")
    private String genType;

    /**
     * 生成路径（不填默认项目路径）
     */
    @Schema(description = "生成路径（不填默认项目路径）")
    @TableField(updateStrategy = FieldStrategy.NOT_EMPTY)
    private String genPath;

    /**
     * 主键信息
     */
    @Schema(description = "主键信息")
    @TableField(exist = false)
    private GenTableColumn pkColumn;

    /**
     * 子表信息
     */
    @Schema(description = "子表信息")
    @TableField(exist = false)
    private GenTable subTable;

    /**
     * 表列信息
     */
    @Schema(description = "表列信息")
    @Valid
    @TableField(exist = false)
    private List<GenTableColumn> columns;

    /**
     * 其它生成选项
     */
    @Schema(description = "其它生成选项")
    private String options;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 树编码字段
     */
    @Schema(description = "树编码字段")
    @TableField(exist = false)
    private String treeCode;

    /**
     * 树父编码字段
     */
    @Schema(description = "树父编码字段")
    @TableField(exist = false)
    private String treeParentCode;

    /**
     * 树名称字段
     */
    @Schema(description = "树名称字段")
    @TableField(exist = false)
    private String treeName;

    /*
     * 菜单id列表
     */
    @Schema(description = "菜单id列表")
    @TableField(exist = false)
    private List<Long> menuIds;

    /**
     * 上级菜单ID字段
     */
    @Schema(description = "上级菜单ID字段")
    @TableField(exist = false)
    private String parentMenuId;

    /**
     * 上级菜单名称字段
     */
    @Schema(description = "上级菜单名称字段")
    @TableField(exist = false)
    private String parentMenuName;

    public boolean isSub() {
        return isSub(this.tplCategory);
    }

    public static boolean isSub(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_SUB, tplCategory);
    }

    public boolean isTree() {
        return isTree(this.tplCategory);
    }

    public static boolean isTree(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_TREE, tplCategory);
    }

    public boolean isCrud() {
        return isCrud(this.tplCategory);
    }

    public static boolean isCrud(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_CRUD, tplCategory);
    }

    public boolean isSuperColumn(String javaField) {
        return isSuperColumn(this.tplCategory, javaField);
    }

    public static boolean isSuperColumn(String tplCategory, String javaField) {
        if (isTree(tplCategory)) {
            return StringUtils.equalsAnyIgnoreCase(javaField,
                ArrayUtils.addAll(GenConstants.TREE_ENTITY, GenConstants.BASE_ENTITY));
        }
        return StringUtils.equalsAnyIgnoreCase(javaField, GenConstants.BASE_ENTITY);
    }

    // --- 关键的桥接方法 ---
    // 为了让旧的业务代码（比如 getTableId()）能继续工作，
    // 我们手动提供这些方法，但它们内部的实现是指向标准化的 getId()/setId()。

    /**
     * 获取表编号 (兼容旧代码)
     * @deprecated 请逐渐改用 {@link #getId()}。此方法为保持旧代码兼容性而保留。
     */
    @Deprecated
    public Long getTableId() {
        return getId();
    }

    /**
     * 设置表编号 (兼容旧代码)
     * @deprecated 请逐渐改用 {@link #setId(Long)}。此方法为保持旧代码兼容性而保留。
     */
    @Deprecated
    public void setTableId(Long tableId) {
        setId(tableId);
    }
}
