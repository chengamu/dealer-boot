# 真实支付渠道 TODO

本轮只保留真实渠道依赖、数据结构和可追踪状态，不做真实支付平台下单、退款、转账或 webhook 联调。

## 暂缓范围

- PayPal：下单、退款、webhook 验签与 sandbox smoke。
- Stripe：Checkout / PaymentIntent、退款、webhook 验签与 sandbox smoke。
- 支付宝：PC / WAP / App / 扫码 / 条码支付、退款、notify 验签。
- 微信支付：JSAPI / 小程序 / App / Native / WAP / 条码支付、退款、notify 验签。
- 真实转账：各渠道真实打款提交、状态查询和回调同步。

## 解冻条件

- 提供 sandbox 或生产商户配置，包含应用、渠道、证书或 webhook secret。
- 提供可访问的公网回调入口；本地开发可通过 Nginx 同源代理加临时公网隧道完成。
- 完成支付配置管理端和脱敏输出，避免密钥通过日志或 API 明文泄露。

## 当前代码约束

- 非 Mock / Wallet 支付渠道不应伪造成功。
- Mock 仅用于 dev/local smoke。
- 真实渠道回调必须先验签，验签失败不得更新订单、退款或转账状态。
