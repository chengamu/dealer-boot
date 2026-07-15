package com.bocoo.product.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.vo.ProductFormulaVo;

/**
 * 产品配方数据Mapper
 */
public interface ProductFormulaMapper extends BaseMapperPlus<ProductFormula, ProductFormulaVo> {

    @Select("""
        select *
        from pc_formula
        where formula_id = #{formulaId}
          and del_flag = '0'
        for update
        """)
    ProductFormula selectActiveByIdForUpdate(@Param("formulaId") Long formulaId);
}
