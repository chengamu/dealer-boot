package com.bocoo.system.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.common.core.validate.AddGroup;
import com.bocoo.common.core.validate.EditGroup;
import com.bocoo.system.domain.entity.SalesStore;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SalesStore.class, reverseConvertGenerate = false)
@Schema(description = "Sales store request")
public class SalesStoreBo extends BaseBo {

    @NotNull(groups = EditGroup.class, message = "{sales.store.id.required}")
    private Long salesStoreId;

    @NotBlank(groups = {AddGroup.class, EditGroup.class}, message = "{sales.store.code.required}")
    @Size(max = 64, message = "{sales.store.code.max}")
    private String storeCode;

    @NotBlank(groups = {AddGroup.class, EditGroup.class}, message = "{sales.store.name.required}")
    @Size(max = 120, message = "{sales.store.name.max}")
    private String storeName;

    @NotNull(groups = {AddGroup.class, EditGroup.class}, message = "{sales.store.dept.required}")
    private Long deptId;

    @Size(max = 100, message = "{sales.store.contactName.max}")
    private String contactName;

    @Size(max = 64, message = "{sales.store.contactPhone.max}")
    private String contactPhone;

    @Size(max = 64, message = "{sales.store.country.max}")
    private String country;

    @Size(max = 64, message = "{sales.store.state.max}")
    private String state;

    @Size(max = 64, message = "{sales.store.city.max}")
    private String city;

    @Size(max = 255, message = "{sales.store.addressLine1.max}")
    private String addressLine1;

    @Size(max = 255, message = "{sales.store.addressLine2.max}")
    private String addressLine2;

    @Size(max = 32, message = "{sales.store.postalCode.max}")
    private String postalCode;

    @DecimalMin(value = "0", message = "{sales.store.creditLimit.min}")
    private BigDecimal creditLimit;

    @Min(value = 0, message = "{sales.store.paymentTermDays.min}")
    private Integer paymentTermDays;

    private String status;

    @Size(max = 500, message = "{sales.store.remark.max}")
    private String remark;
}
