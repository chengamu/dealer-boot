package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductFormulaBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductFormulaVersionVo;
import com.bocoo.product.domain.vo.ProductFormulaVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductFormulaService {

    TableDataInfo<ProductFormulaVo> queryPageList(ProductFormulaBo bo, PageQuery pageQuery);

    List<ProductFormulaVo> queryList(ProductFormulaBo bo);

    ProductFormulaVo queryById(Long id);

    Boolean insertByBo(ProductFormulaBo bo);

    Boolean updateByBo(ProductFormulaBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean submitReview(Long id);

    Boolean approve(Long id);

    Boolean reject(Long id, String rejectReason);

    Boolean stop(Long id);

    Boolean validateFormula(Long id);

    List<ProductFormulaVersionVo> queryVersions(Long formulaId);

    ProductFormulaVersionVo queryVersionById(Long formulaId, Long versionId);

    BaseEditCheckResultVo checkEditAllowed(Long id);

    ReferenceCheckResultVo checkReferences(Long formulaId);
}
