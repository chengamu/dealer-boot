package com.bocoo.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Payment channel codes.
 */
@Getter
@AllArgsConstructor
public enum PayChannelCode {

    ALIPAY_PC("alipay_pc"),
    ALIPAY_WAP("alipay_wap"),
    ALIPAY_APP("alipay_app"),
    ALIPAY_QR("alipay_qr"),
    ALIPAY_BAR("alipay_bar"),
    WX_PUB("wx_pub"),
    WX_LITE("wx_lite"),
    WX_APP("wx_app"),
    WX_NATIVE("wx_native"),
    WX_WAP("wx_wap"),
    WX_BAR("wx_bar"),
    PAYPAL("paypal"),
    BANK_TRANSFER("bank_transfer"),
    CREDIT_LIMIT("credit_limit"),
    MANUAL("manual"),
    STRIPE("stripe"),
    MOCK("mock"),
    WALLET("wallet");

    private final String code;
}
