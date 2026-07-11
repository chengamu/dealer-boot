package com.bocoo.product.service;

public interface ProductQuoteReferenceProvider {

    long countSaleProductReferences(Long saleProductId);

    long countFormulaVersionReferences(Long formulaVersionId);

    long countShippingTemplateReferences(Long shippingTemplateId);
}
