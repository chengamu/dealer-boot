package com.bocoo.pay.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayNotifyTask;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayRefund;
import com.bocoo.pay.domain.entity.PayWallet;

/**
 * Payment management query service contract.
 */
public interface PayAdminService {

    TableDataInfo<PayChannel> selectChannelPage(PayChannel query, PageQuery pageQuery);

    TableDataInfo<PayOrder> selectOrderPage(PayOrder query, PageQuery pageQuery);

    TableDataInfo<PayRefund> selectRefundPage(PayRefund query, PageQuery pageQuery);

    TableDataInfo<PayNotifyTask> selectNotifyTaskPage(PayNotifyTask query, PageQuery pageQuery);

    TableDataInfo<PayWallet> selectWalletPage(PayWallet query, PageQuery pageQuery);
}
