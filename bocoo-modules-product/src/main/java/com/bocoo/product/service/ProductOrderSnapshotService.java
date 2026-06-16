package com.bocoo.product.service;

import com.bocoo.product.domain.bo.OrderSnapshotBuildBo;
import com.bocoo.product.domain.vo.OrderProductSnapshotVo;

public interface ProductOrderSnapshotService {

    OrderProductSnapshotVo buildSnapshot(OrderSnapshotBuildBo bo);

    OrderProductSnapshotVo buildAndSaveSnapshot(OrderSnapshotBuildBo bo);
}
