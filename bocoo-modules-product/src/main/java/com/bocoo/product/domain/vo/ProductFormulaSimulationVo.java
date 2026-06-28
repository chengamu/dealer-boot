package com.bocoo.product.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class ProductFormulaSimulationVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long formulaId;
    private String status;
    private String message;
    private LocalDateTime simulationTime;
    private BigDecimal orderWidth;
    private BigDecimal orderHeight;
    private Map<String, String> selectedOptionValues = new LinkedHashMap<>();
    private List<ProductFormulaSimulationItemVo> items = new ArrayList<>();
    private BigDecimal totalAmount;
}
