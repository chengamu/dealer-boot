package com.bocoo.ai.service;

import com.bocoo.ai.domain.bo.AiAuditReportBo;
import com.bocoo.ai.domain.bo.AiAuditSummaryBo;
import com.bocoo.ai.domain.bo.AiUsageLedgerBo;
import com.bocoo.ai.domain.bo.AiUsageReportBo;
import com.bocoo.ai.domain.bo.AiUserQuotaBo;
import com.bocoo.ai.domain.vo.AiAuditSummaryVo;
import com.bocoo.ai.domain.vo.AiQuotaVo;
import com.bocoo.ai.domain.vo.AiUsageLedgerVo;
import com.bocoo.ai.domain.vo.AiUserQuotaVo;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;

public interface AiUsageService {

    TableDataInfo<AiUserQuotaVo> queryUserQuotaPageList(AiUserQuotaBo bo, PageQuery pageQuery);

    Boolean insertUserQuota(AiUserQuotaBo bo);

    Boolean updateUserQuota(AiUserQuotaBo bo);

    TableDataInfo<AiUsageLedgerVo> queryUsageLedgerPageList(AiUsageLedgerBo bo, PageQuery pageQuery);

    TableDataInfo<AiAuditSummaryVo> queryAuditSummaryPageList(AiAuditSummaryBo bo, PageQuery pageQuery);

    AiQuotaVo remainingQuotaForCurrentUser();

    String quotaDenyReason(Long tenantId, Long userId);

    void reportUsage(AiUsageReportBo bo);

    void reportAudit(AiAuditReportBo bo);
}
