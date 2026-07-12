package com.bocoo.dealer.fulfillment.mapper;

import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentPackageMetricsVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShipmentMapper extends BaseMapperPlus<Shipment, ShipmentVo> {
    List<ShipmentPackageMetricsVo> selectPackageMetrics(@Param("tenantIds") List<Long> tenantIds,
                                                        @Param("documentIds") List<Long> documentIds);
}
