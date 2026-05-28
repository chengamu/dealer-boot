package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.system.domain.bo.SysLegalDocumentBo;
import com.bocoo.system.domain.vo.SysLegalDocumentVo;
import com.bocoo.system.service.SysLegalDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
public class SysLegalDocumentController extends BaseController {

    private final SysLegalDocumentService legalDocumentService;

    @SaIgnore
    @GetMapping("/legal/documents/{documentType}")
    public R<SysLegalDocumentVo> published(@PathVariable String documentType) {
        return R.ok(legalDocumentService.selectPublished(documentType));
    }

    @SaCheckPermission("system:legal:document:list")
    @GetMapping("/system/legal/documents")
    public TableDataInfo<SysLegalDocumentVo> list(SysLegalDocumentBo bo, PageQuery pageQuery) {
        return legalDocumentService.selectPage(bo, pageQuery);
    }

    @SaCheckPermission("system:legal:document:query")
    @GetMapping("/system/legal/documents/{id}")
    public R<SysLegalDocumentVo> get(@PathVariable Long id) {
        return R.ok(legalDocumentService.selectById(id));
    }

    @SaCheckPermission("system:legal:document:add")
    @PostMapping("/system/legal/documents")
    public R<Void> add(@Validated @RequestBody SysLegalDocumentBo bo) {
        legalDocumentService.insert(bo);
        return R.ok();
    }

    @SaCheckPermission("system:legal:document:edit")
    @PutMapping("/system/legal/documents")
    public R<Void> edit(@Validated @RequestBody SysLegalDocumentBo bo) {
        legalDocumentService.update(bo);
        return R.ok();
    }

    @SaCheckPermission("system:legal:document:remove")
    @DeleteMapping("/system/legal/documents/{id}")
    public R<Void> remove(@PathVariable Long id) {
        legalDocumentService.deleteById(id);
        return R.ok();
    }
}
