package com.bocoo.system.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.system.domain.bo.SalesStoreBo;
import com.bocoo.system.domain.entity.SalesStore;
import com.bocoo.system.domain.vo.SysDeptVo;
import com.bocoo.system.mapper.SalesStoreMapper;
import com.bocoo.system.mapper.SysDeptMapper;
import com.bocoo.system.service.impl.SalesStoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesStoreServiceTest {

    @Mock
    private SalesStoreMapper storeMapper;
    @Mock
    private SysDeptMapper deptMapper;
    @Mock
    private ObjectProvider<SalesStoreReferenceChecker> referenceCheckers;
    @Mock
    private SalesStoreReferenceChecker referenceChecker;

    private SalesStoreServiceImpl service;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SalesStore.class);
        service = new SalesStoreServiceImpl(deptMapper, referenceCheckers);
        ReflectionTestUtils.setField(service, "baseMapper", storeMapper);
    }

    @Test
    void merchantTenantCannotCreateStore() {
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 2001L, 9L, "merchant");

        assertThatThrownBy(() -> service.insertByBo(validBo()))
            .isInstanceOf(com.bocoo.common.core.exception.ServiceException.class);
    }

    @Test
    void disabledDepartmentCannotBeLinked() {
        SysDeptVo dept = new SysDeptVo();
        dept.setDeptId(10L);
        dept.setStatus("0");
        when(deptMapper.selectVoById(10L)).thenReturn(dept);
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9L, "admin");

        assertThatThrownBy(() -> service.insertByBo(validBo()))
            .isInstanceOf(com.bocoo.common.core.exception.ServiceException.class);
    }

    @Test
    void insertForcesPlatformTenantAndUsd() {
        SysDeptVo dept = new SysDeptVo();
        dept.setDeptId(10L);
        dept.setStatus("1");
        when(deptMapper.selectVoById(10L)).thenReturn(dept);
        when(storeMapper.selectCount(any())).thenReturn(0L);
        when(storeMapper.insert(any(SalesStore.class))).thenReturn(1);
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9L, "admin");

        assertThat(service.insertByBo(validBo())).isTrue();

        ArgumentCaptor<SalesStore> captor = ArgumentCaptor.forClass(SalesStore.class);
        verify(storeMapper).insert(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(1L);
        assertThat(captor.getValue().getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void duplicateStoreCodeIsRejected() {
        SysDeptVo dept = new SysDeptVo();
        dept.setDeptId(10L);
        dept.setStatus("1");
        when(deptMapper.selectVoById(10L)).thenReturn(dept);
        when(storeMapper.selectCount(any())).thenReturn(1L);
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9L, "admin");

        assertThatThrownBy(() -> service.insertByBo(validBo()))
            .isInstanceOf(com.bocoo.common.core.exception.ServiceException.class);
    }

    @Test
    void duplicateDepartmentLinkIsRejected() {
        SysDeptVo dept = new SysDeptVo();
        dept.setDeptId(10L);
        dept.setStatus("1");
        when(deptMapper.selectVoById(10L)).thenReturn(dept);
        when(storeMapper.selectCount(any())).thenReturn(0L, 1L);
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9L, "admin");

        assertThatThrownBy(() -> service.insertByBo(validBo()))
            .isInstanceOf(com.bocoo.common.core.exception.ServiceException.class);
    }

    @Test
    void enabledStoreCannotChangeCodeOrDepartment() {
        SalesStore current = currentStore();
        when(storeMapper.selectById(1L)).thenReturn(current);
        SysDeptVo dept = new SysDeptVo();
        dept.setDeptId(10L);
        dept.setStatus("1");
        when(deptMapper.selectVoById(10L)).thenReturn(dept);
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9L, "admin");
        SalesStoreBo bo = validBo();
        bo.setSalesStoreId(1L);
        bo.setStoreCode("CHANGED");

        assertThatThrownBy(() -> service.updateByBo(bo))
            .isInstanceOf(com.bocoo.common.core.exception.ServiceException.class);
    }

    @Test
    void historicalReferencesDoNotBlockDisable() {
        when(storeMapper.selectById(1L)).thenReturn(currentStore());
        when(storeMapper.update(any(), any())).thenReturn(1);
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9L, "admin");

        assertThat(service.updateStatus(1L, "0")).isEqualTo(1);
        verify(referenceChecker, never()).check(1L);
    }

    private SalesStoreBo validBo() {
        SalesStoreBo bo = new SalesStoreBo();
        bo.setStoreCode("STORE-01");
        bo.setStoreName("Main Store");
        bo.setDeptId(10L);
        bo.setCreditLimit(BigDecimal.TEN);
        bo.setPaymentTermDays(30);
        bo.setStatus("1");
        return bo;
    }

    private SalesStore currentStore() {
        SalesStore store = new SalesStore();
        store.setSalesStoreId(1L);
        store.setStoreCode("STORE-01");
        store.setDeptId(10L);
        store.setStatus("1");
        return store;
    }
}
