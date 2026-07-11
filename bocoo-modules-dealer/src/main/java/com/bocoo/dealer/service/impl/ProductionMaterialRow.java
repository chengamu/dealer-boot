package com.bocoo.dealer.service.impl;

import java.math.BigDecimal;

record ProductionMaterialRow(String room, String product, String materialCode, String materialName,
                             String groupName, String unitCode, BigDecimal usageQty, String remark) {
}
