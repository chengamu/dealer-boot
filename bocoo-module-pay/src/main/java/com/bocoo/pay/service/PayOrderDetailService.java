package com.bocoo.pay.service;

import com.bocoo.pay.domain.vo.PayOrderDetailVo;

public interface PayOrderDetailService {
    PayOrderDetailVo getDetail(Long payOrderId);
}
