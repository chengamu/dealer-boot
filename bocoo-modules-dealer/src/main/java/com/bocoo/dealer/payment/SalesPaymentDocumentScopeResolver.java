package com.bocoo.dealer.payment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.core.domain.vo.RoleVO;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.mapper.SalesDocumentQueryMapper;
import com.bocoo.dealer.scope.SalesBusinessScope;
import com.bocoo.pay.api.PaymentDocumentFacts;
import com.bocoo.pay.api.PaymentDocumentFilter;
import com.bocoo.pay.api.PaymentDocumentScopeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class SalesPaymentDocumentScopeResolver implements PaymentDocumentScopeResolver {
    private static final String PLATFORM_FINANCE_ROLE = "platform_finance";
    private static final String PLATFORM_FINANCE_PERMISSION = "platform:finance:";
    private static final String ALL_PERMISSION = "*:*:*";

    private final SalesDocumentQueryMapper mapper;

    @Override
    public List<Long> accessibleDocumentIds(PaymentDocumentFilter filter) {
        return selectList(query(filter)).stream().map(SalesDocument::getSalesDocumentId).toList();
    }

    @Override
    public PaymentDocumentFacts requireAccessible(Long salesDocumentId) {
        SalesDocument document = findAccessibleDocument(salesDocumentId);
        if (document == null) throw ServiceException.ofMessageKey("dealer.sales.notFound");
        return facts(document);
    }

    @Override
    public Map<Long, PaymentDocumentFacts> resolveFacts(Collection<Long> salesDocumentIds) {
        if (salesDocumentIds == null || salesDocumentIds.isEmpty()) return Map.of();
        QueryWrapper<SalesDocument> query = scopedQuery().in("sales_document_id", salesDocumentIds);
        Map<Long, PaymentDocumentFacts> result = new LinkedHashMap<>();
        selectList(query).forEach(row -> result.put(row.getSalesDocumentId(), facts(row)));
        return result;
    }

    SalesDocument findAccessibleDocument(Long salesDocumentId) {
        if (salesDocumentId == null) return null;
        QueryWrapper<SalesDocument> query = scopedQuery().eq("sales_document_id", salesDocumentId);
        return select(() -> mapper.selectOne(query, false));
    }

    private QueryWrapper<SalesDocument> query(PaymentDocumentFilter filter) {
        QueryWrapper<SalesDocument> query = scopedQuery();
        if (filter == null) return query;
        String origin = filter.getBusinessOrigin();
        query.eq(StringUtils.isNotBlank(origin), "business_origin", origin);
        applySubject(query, origin, filter.getSubjectId());
        if (StringUtils.isNotBlank(filter.getKeyword())) {
            query.and(row -> row.like("order_no", filter.getKeyword())
                .or().like("source_no", filter.getKeyword())
                .or().like("customer_name", filter.getKeyword())
                .or().like("merchant_name", filter.getKeyword()));
        }
        return query;
    }

    private QueryWrapper<SalesDocument> scopedQuery() {
        QueryWrapper<SalesDocument> query = new QueryWrapper<SalesDocument>().eq("del_flag", "0");
        if (!canCrossTenant()) {
            SalesBusinessScope scope = SalesBusinessScope.current();
            query.eq("tenant_id", scope.tenantId()).eq("business_origin", scope.businessOrigin());
        }
        return query;
    }

    private void applySubject(QueryWrapper<SalesDocument> query, String origin, Long subjectId) {
        if (subjectId == null) return;
        if ("MERCHANT".equals(origin)) {
            query.eq("tenant_id", subjectId);
        } else if ("INTERNAL".equals(origin)) {
            query.eq("sales_store_id", subjectId);
        } else {
            query.and(row -> row.nested(merchant -> merchant.eq("business_origin", "MERCHANT")
                    .eq("tenant_id", subjectId))
                .or(internal -> internal.eq("business_origin", "INTERNAL")
                    .eq("sales_store_id", subjectId)));
        }
    }

    private List<SalesDocument> selectList(QueryWrapper<SalesDocument> query) {
        return select(() -> mapper.selectList(query));
    }

    private <T> T select(Supplier<T> supplier) {
        return canCrossTenant() ? TenantContextHolder.callWithIgnore(supplier) : supplier.get();
    }

    boolean canCrossTenant() {
        LoginUser user = LoginHelper.getLoginUser();
        if (user == null || !LoginHelper.isPlatformTenant()) return false;
        if (LoginHelper.isAdmin(user.getUserId())) return true;
        if (contains(user.getRolePermission(), PLATFORM_FINANCE_ROLE)) return true;
        if (user.getRoles() != null && user.getRoles().stream().map(RoleVO::getRoleKey)
            .anyMatch(PLATFORM_FINANCE_ROLE::equals)) return true;
        return user.getMenuPermission() != null && user.getMenuPermission().stream()
            .anyMatch(permission -> ALL_PERMISSION.equals(permission)
                || permission.startsWith(PLATFORM_FINANCE_PERMISSION));
    }

    private boolean contains(Set<String> values, String value) {
        return values != null && values.contains(value);
    }

    private PaymentDocumentFacts facts(SalesDocument document) {
        String subjectName = "MERCHANT".equals(document.getBusinessOrigin())
            ? document.getMerchantName() : document.getOwnerName();
        return PaymentDocumentFacts.builder().salesDocumentId(document.getSalesDocumentId())
            .businessOrigin(document.getBusinessOrigin()).tenantId(document.getTenantId())
            .salesStoreId(document.getSalesStoreId()).deptId(document.getDeptId())
            .ownerUserId(document.getOwnerUserId()).subjectName(subjectName).build();
    }
}
