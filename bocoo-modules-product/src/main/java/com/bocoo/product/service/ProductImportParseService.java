package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ProductImportBatchBo;
import com.bocoo.product.domain.vo.ProductImportBatchVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductImportParseService {

    ProductImportBatchVo parseImportExcel(MultipartFile file, ProductImportBatchBo bo) throws IOException;
}
