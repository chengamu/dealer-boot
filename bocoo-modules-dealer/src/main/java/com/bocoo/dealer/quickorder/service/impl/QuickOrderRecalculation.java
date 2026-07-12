package com.bocoo.dealer.quickorder.service.impl;

import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.system.domain.vo.MerchantProfileVo;

import java.util.List;

record QuickOrderRecalculation(QuickOrder order, List<QuickOrderItem> items, MerchantProfileVo profile) {
}
