package com.bocoo.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.mybatis.core.service.impl.BaseServiceImpl;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.bo.SalesStoreBo;
import com.bocoo.system.domain.entity.SalesStore;
import com.bocoo.system.domain.entity.SysDept;
import com.bocoo.system.domain.vo.SalesStoreDeptOptionVo;
import com.bocoo.system.domain.vo.SalesStoreReferenceVo;
import com.bocoo.system.domain.vo.SalesStoreOptionVo;
import com.bocoo.system.domain.vo.SalesStoreVo;
import com.bocoo.system.domain.vo.SysDeptVo;
import com.bocoo.system.mapper.SalesStoreMapper;
import com.bocoo.system.mapper.SysDeptMapper;
import com.bocoo.system.service.SalesStoreManagementService;
import com.bocoo.system.service.SalesStoreReferenceChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesStoreServiceImpl
    extends BaseServiceImpl<SalesStoreMapper, SalesStore, SalesStoreVo>
    implements SalesStoreManagementService {

    private static final String CURRENCY_USD = "USD";
    private final SysDeptMapper deptMapper;
    private final ObjectProvider<SalesStoreReferenceChecker> referenceCheckers;

    @Override
    public TableDataInfo<SalesStoreVo> queryPageList(SalesStoreBo bo, PageQuery pageQuery) {
        requirePlatformTenant();
        return platform(() -> {
            Page<SalesStoreVo> page = pageVo(pageQuery.build(), buildQueryWrapper(bo));
            enrich(page.getRecords());
            return TableDataInfo.build(page);
        });
    }

    @Override
    public List<SalesStoreOptionVo> queryOptions() {
        requirePlatformTenant();
        return platform(() -> list(Wrappers.<SalesStore>lambdaQuery()
            .eq(SalesStore::getStatus, UserConstants.ROLE_NORMAL)
            .orderByAsc(SalesStore::getStoreName)).stream()
            .map(row -> new SalesStoreOptionVo(
                row.getSalesStoreId(), row.getStoreCode(), row.getStoreName()))
            .toList());
    }

    @Override
    public List<SalesStoreDeptOptionVo> queryDeptOptions() {
        requirePlatformTenant();
        return platform(() -> deptMapper.selectVoList(Wrappers.<SysDept>lambdaQuery()
                .eq(SysDept::getStatus, UserConstants.DEPT_NORMAL)
                .orderByAsc(SysDept::getOrderNum))
            .stream()
            .map(dept -> new SalesStoreDeptOptionVo(
                dept.getDeptId(), dept.getDeptName(), isDeptLinkedInPlatform(dept.getDeptId())))
            .toList());
    }

    @Override
    public SalesStoreVo queryManagementById(Long salesStoreId) {
        requirePlatformTenant();
        SalesStoreVo store = queryById(salesStoreId);
        if (store == null) throw ServiceException.ofMessageKey("sales.store.not.found");
        return store;
    }

    @Override
    public SalesStoreVo resolveEnabledByDeptId(Long deptId) {
        if (deptId == null) throw ServiceException.ofMessageKey("sales.store.dept.required");
        SalesStoreVo store = platform(() -> getVoOne(Wrappers.<SalesStore>lambdaQuery()
            .eq(SalesStore::getDeptId, deptId)
            .eq(SalesStore::getStatus, UserConstants.ROLE_NORMAL)));
        if (store == null) throw ServiceException.ofMessageKey("sales.store.enabled.not.found");
        return store;
    }

    @Override
    public SalesStoreVo queryById(Long salesStoreId) {
        if (salesStoreId == null) return null;
        SalesStoreVo store = platform(() -> getVoById(salesStoreId));
        if (store != null) store.setAddress(joinAddress(store));
        return store;
    }

    @Override
    public boolean isDeptLinked(Long deptId) {
        return deptId != null && platform(() -> isDeptLinkedInPlatform(deptId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertByBo(SalesStoreBo bo) {
        requirePlatformTenant();
        validateDepartment(bo.getDeptId());
        SalesStore entity = toEntity(bo);
        entity.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
        entity.setCurrencyCode(CURRENCY_USD);
        entity.setStatus(validStatusOrDefault(entity.getStatus()));
        entity.setCreditLimit(entity.getCreditLimit() == null ? BigDecimal.ZERO : entity.getCreditLimit());
        entity.setPaymentTermDays(entity.getPaymentTermDays() == null ? 0 : entity.getPaymentTermDays());
        validateUnique(entity);
        return platform(() -> save(entity));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByBo(SalesStoreBo bo) {
        requirePlatformTenant();
        SalesStore current = requireStore(bo.getSalesStoreId());
        validateDepartment(bo.getDeptId());
        if (UserConstants.ROLE_NORMAL.equals(current.getStatus())
            && (!java.util.Objects.equals(current.getStoreCode(), bo.getStoreCode())
            || !java.util.Objects.equals(current.getDeptId(), bo.getDeptId()))) {
            throw ServiceException.ofMessageKey("sales.store.enabled.identity.immutable");
        }
        SalesStore entity = toEntity(bo);
        entity.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
        entity.setCurrencyCode(CURRENCY_USD);
        entity.setStatus(current.getStatus());
        validateUnique(entity);
        return platform(() -> updateById(entity));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(Long salesStoreId, String status) {
        requirePlatformTenant();
        SalesStore current = requireStore(salesStoreId);
        if (!UserConstants.ROLE_NORMAL.equals(status) && !UserConstants.ROLE_DISABLE.equals(status)) {
            throw ServiceException.ofMessageKey("sales.store.status.invalid");
        }
        if (UserConstants.ROLE_NORMAL.equals(status)) {
            validateDepartment(current.getDeptId());
            validateUnique(current);
        }
        return platform(() -> baseMapper.update(null, new LambdaUpdateWrapper<SalesStore>()
            .eq(SalesStore::getSalesStoreId, salesStoreId)
            .set(SalesStore::getStatus, status)));
    }

    @Override
    public SalesStoreReferenceVo checkDisableReferences(Long salesStoreId) {
        requirePlatformTenant();
        requireStore(salesStoreId);
        long customers = 0;
        long unfinishedOrders = 0;
        for (SalesStoreReferenceChecker checker : referenceCheckers.orderedStream().toList()) {
            SalesStoreReferenceVo result = checker.check(salesStoreId);
            if (result != null) {
                customers += result.getCustomerCount();
                unfinishedOrders += result.getUnfinishedOrderCount();
            }
        }
        return new SalesStoreReferenceVo(customers, unfinishedOrders);
    }

    private LambdaQueryWrapper<SalesStore> buildQueryWrapper(SalesStoreBo bo) {
        LambdaQueryWrapper<SalesStore> query = Wrappers.lambdaQuery();
        if (bo != null) {
            query.like(StringUtils.isNotBlank(bo.getStoreCode()), SalesStore::getStoreCode, bo.getStoreCode())
                .like(StringUtils.isNotBlank(bo.getStoreName()), SalesStore::getStoreName, bo.getStoreName())
                .eq(bo.getDeptId() != null, SalesStore::getDeptId, bo.getDeptId())
                .eq(StringUtils.isNotBlank(bo.getStatus()), SalesStore::getStatus, bo.getStatus());
        }
        return query.orderByDesc(SalesStore::getUpdateTime).orderByAsc(SalesStore::getStoreCode);
    }

    private void validateUnique(SalesStore store) {
        Long id = store.getSalesStoreId() == null ? -1L : store.getSalesStoreId();
        if (platform(() -> count(Wrappers.<SalesStore>lambdaQuery()
            .eq(SalesStore::getStoreCode, store.getStoreCode()).ne(SalesStore::getSalesStoreId, id))) > 0) {
            throw ServiceException.ofMessageKey("sales.store.code.exists");
        }
        if (platform(() -> count(Wrappers.<SalesStore>lambdaQuery()
            .eq(SalesStore::getDeptId, store.getDeptId()).ne(SalesStore::getSalesStoreId, id))) > 0) {
            throw ServiceException.ofMessageKey("sales.store.dept.exists");
        }
    }

    private void validateDepartment(Long deptId) {
        SysDeptVo dept = platform(() -> deptMapper.selectVoById(deptId));
        if (dept == null) throw ServiceException.ofMessageKey("sales.store.dept.not.found");
        if (!UserConstants.DEPT_NORMAL.equals(dept.getStatus())) {
            throw ServiceException.ofMessageKey("sales.store.dept.disabled");
        }
    }

    private SalesStore requireStore(Long id) {
        SalesStore store = platform(() -> getById(id));
        if (store == null) throw ServiceException.ofMessageKey("sales.store.not.found");
        return store;
    }

    private boolean isDeptLinkedInPlatform(Long deptId) {
        return count(Wrappers.<SalesStore>lambdaQuery().eq(SalesStore::getDeptId, deptId)) > 0;
    }

    private void enrich(List<SalesStoreVo> stores) {
        for (SalesStoreVo store : stores) {
            SysDeptVo dept = deptMapper.selectVoById(store.getDeptId());
            store.setDeptName(dept == null ? null : dept.getDeptName());
            store.setAddress(joinAddress(store));
            SalesStoreReferenceVo references = checkReferencesWithoutStoreLookup(store.getSalesStoreId());
            store.setCustomerCount(references.getCustomerCount());
            store.setUnfinishedOrderCount(references.getUnfinishedOrderCount());
        }
    }

    private SalesStoreReferenceVo checkReferencesWithoutStoreLookup(Long id) {
        long customers = 0;
        long unfinishedOrders = 0;
        for (SalesStoreReferenceChecker checker : referenceCheckers.orderedStream().toList()) {
            SalesStoreReferenceVo result = checker.check(id);
            if (result != null) {
                customers += result.getCustomerCount();
                unfinishedOrders += result.getUnfinishedOrderCount();
            }
        }
        return new SalesStoreReferenceVo(customers, unfinishedOrders);
    }

    private String validStatusOrDefault(String status) {
        if (StringUtils.isBlank(status)) return UserConstants.ROLE_NORMAL;
        if (UserConstants.ROLE_NORMAL.equals(status) || UserConstants.ROLE_DISABLE.equals(status)) return status;
        throw ServiceException.ofMessageKey("sales.store.status.invalid");
    }

    private SalesStore toEntity(SalesStoreBo bo) {
        if (bo == null) throw ServiceException.ofMessageKey("sales.store.request.invalid");
        SalesStore entity = new SalesStore();
        entity.setSalesStoreId(bo.getSalesStoreId());
        entity.setStoreCode(bo.getStoreCode());
        entity.setStoreName(bo.getStoreName());
        entity.setDeptId(bo.getDeptId());
        entity.setContactName(bo.getContactName());
        entity.setContactPhone(bo.getContactPhone());
        entity.setCountry(bo.getCountry());
        entity.setState(bo.getState());
        entity.setCity(bo.getCity());
        entity.setAddressLine1(bo.getAddressLine1());
        entity.setAddressLine2(bo.getAddressLine2());
        entity.setPostalCode(bo.getPostalCode());
        entity.setCreditLimit(bo.getCreditLimit());
        entity.setPaymentTermDays(bo.getPaymentTermDays());
        entity.setStatus(bo.getStatus());
        entity.setRemark(bo.getRemark());
        return entity;
    }

    private String joinAddress(SalesStoreVo store) {
        return java.util.stream.Stream.of(store.getAddressLine1(), store.getAddressLine2(), store.getCity(),
                store.getState(), store.getPostalCode(), store.getCountry())
            .filter(StringUtils::isNotBlank)
            .collect(java.util.stream.Collectors.joining(", "));
    }

    private void requirePlatformTenant() {
        if (!LoginHelper.isPlatformTenant()
            || !TenantConstants.PLATFORM_TENANT_ID.equals(LoginHelper.getTenantId())) {
            throw ServiceException.ofMessageKey("sales.store.platform.only");
        }
    }

    private <T> T platform(java.util.function.Supplier<T> action) {
        return TenantContextHolder.callWithTenant(TenantConstants.PLATFORM_TENANT_ID, action);
    }
}
