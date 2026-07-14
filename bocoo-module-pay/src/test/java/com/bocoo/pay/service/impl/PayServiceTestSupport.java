package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import com.bocoo.pay.domain.entity.MerchantCreditTransaction;
import com.bocoo.pay.domain.entity.MerchantReceivable;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.entity.PayWebhookEvent;
import com.bocoo.pay.domain.entity.PayReconciliationCase;
import com.bocoo.pay.domain.entity.PayReconciliationAction;
import org.apache.ibatis.builder.MapperBuilderAssistant;

abstract class PayServiceTestSupport {
    static {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, PayOrder.class);
        TableInfoHelper.initTableInfo(assistant, PayOrderExtension.class);
        TableInfoHelper.initTableInfo(assistant, PayWebhookEvent.class);
        TableInfoHelper.initTableInfo(assistant, MerchantCreditAccount.class);
        TableInfoHelper.initTableInfo(assistant, MerchantCreditTransaction.class);
        TableInfoHelper.initTableInfo(assistant, MerchantReceivable.class);
        TableInfoHelper.initTableInfo(assistant, PayChannel.class);
        TableInfoHelper.initTableInfo(assistant, PayReconciliationCase.class);
        TableInfoHelper.initTableInfo(assistant, PayReconciliationAction.class);
    }
}
