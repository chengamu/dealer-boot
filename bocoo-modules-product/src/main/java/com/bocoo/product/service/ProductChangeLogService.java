package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductChangeLogBo;
import com.bocoo.product.domain.vo.ProductChangeLogVo;

/**
 * 产品业务变更流水服务
 */
public interface ProductChangeLogService {

    TableDataInfo<ProductChangeLogVo> queryPageList(ProductChangeLogBo bo, PageQuery pageQuery);

    void record(String bizModule, String bizType, Long bizId, String bizCode, String actionType,
                Object beforeValue, Object afterValue, String remark);
}
