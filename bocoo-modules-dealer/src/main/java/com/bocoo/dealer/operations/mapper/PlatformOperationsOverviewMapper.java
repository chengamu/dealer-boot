package com.bocoo.dealer.operations.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.dealer.operations.domain.bo.OperationsMerchantQueryBo;
import com.bocoo.dealer.operations.domain.vo.OperationsCurrencyAmountVo;
import com.bocoo.dealer.operations.domain.vo.OperationsMerchantLevelOptionVo;
import com.bocoo.dealer.operations.domain.vo.OperationsMerchantVo;
import com.bocoo.dealer.operations.domain.vo.OperationsOrderLifecycleVo;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PlatformOperationsOverviewMapper {

    OperationsOrderLifecycleVo selectOrderLifecycle(@Param("start") LocalDateTime start);

    List<OperationsCurrencyAmountVo> selectCurrencyAmounts(@Param("start") LocalDateTime start);

    List<OperationsMerchantVo> selectMerchantOrderCounts(@Param("tenantIds") List<Long> tenantIds);

    List<OperationsMerchantLevelOptionVo> selectMerchantLevelOptions(@Param("status") String status);

    Page<OperationsMerchantVo> selectPendingApplications(@Param("page") Page<OperationsMerchantVo> page,
                                                         @Param("query") OperationsMerchantQueryBo query);
}
