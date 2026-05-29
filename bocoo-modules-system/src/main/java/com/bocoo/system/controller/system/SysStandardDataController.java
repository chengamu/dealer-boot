package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import com.bocoo.common.core.domain.R;
import com.bocoo.system.domain.vo.SysCountryVo;
import com.bocoo.system.domain.vo.SysCurrencyVo;
import com.bocoo.system.domain.vo.SysLanguageVo;
import com.bocoo.system.service.SysStandardDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class SysStandardDataController {

    private final SysStandardDataService standardDataService;

    @SaIgnore
    @GetMapping("/system/standard/countries")
    public R<List<SysCountryVo>> countries(@RequestParam(required = false) String keyword) {
        return R.ok(standardDataService.listCountries(keyword));
    }

    @SaIgnore
    @GetMapping("/system/standard/currencies")
    public R<List<SysCurrencyVo>> currencies(@RequestParam(required = false) String keyword) {
        return R.ok(standardDataService.listCurrencies(keyword));
    }

    @SaIgnore
    @GetMapping("/system/standard/languages")
    public R<List<SysLanguageVo>> languages(@RequestParam(required = false) String keyword) {
        return R.ok(standardDataService.listLanguages(keyword));
    }
}
