package com.bocoo.dealer.fulfillment.domain.bo;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductionCompleteBo {
    @Size(max = 1000)
    private String note;
}
