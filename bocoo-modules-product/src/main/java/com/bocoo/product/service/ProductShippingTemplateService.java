package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductShippingTemplateBo;
import com.bocoo.product.domain.vo.ProductShippingTemplateVo;

import java.util.List;

public interface ProductShippingTemplateService {
    TableDataInfo<ProductShippingTemplateVo> queryPageList(ProductShippingTemplateBo bo, PageQuery pageQuery);

    List<ProductShippingTemplateVo> queryList(ProductShippingTemplateBo bo);

    ProductShippingTemplateVo queryById(Long id);

    ProductShippingTemplateVo queryDefaultEnabled();

    Boolean insertByBo(ProductShippingTemplateBo bo);

    Boolean updateByBo(ProductShippingTemplateBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);
}
