package com.bocoo.product.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.domain.vo.ProductSaleProductVo;

public interface ProductSaleProductMapper extends BaseMapperPlus<ProductSaleProduct, ProductSaleProductVo> {

    @Select("""
        select *
        from pc_sale_product
        where sale_product_id = #{saleProductId}
          and del_flag = '0'
        for update
        """)
    ProductSaleProduct selectActiveByIdForUpdate(@Param("saleProductId") Long saleProductId);
}
