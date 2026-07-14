package com.bocoo.dealer.dashboard.scope;

import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SalesDashboardCapabilitiesResolver {
    private static final String ALL_PERMISSION = "*:*:*";

    public SalesDashboardVo.Capabilities resolve(boolean platformView) {
        LoginUser user = LoginHelper.getLoginUser();
        if (user == null) throw ServiceException.ofMessageKey("auth.unauthorized");
        return resolve(user, platformView);
    }

    SalesDashboardVo.Capabilities resolve(LoginUser user, boolean platformView) {
        Set<String> permissions = user.getMenuPermission() == null ? Set.of() : user.getMenuPermission();
        boolean admin = LoginHelper.isAdmin(user.getUserId()) || permissions.contains(ALL_PERMISSION);
        if (platformView) {
            return new SalesDashboardVo.Capabilities(
                admin || permissions.contains("platform:sales:quote:list"),
                admin || permissions.contains("platform:sales:order:list"),
                false, false, false, false, false, false);
        }
        return new SalesDashboardVo.Capabilities(
            admin || permissions.contains("customer:quote:list"),
            admin || permissions.contains("dealer:sales:list"),
            admin || permissions.contains("pay:order:list"),
            admin || permissions.contains("dealer:fulfillment:production:list"),
            admin || permissions.contains("dealer:fulfillment:shipment:list"),
            admin || permissions.contains("customer:quote:add"),
            admin || permissions.contains("customer:profile:add"),
            admin || permissions.contains("dealer:quick-order:add")
        );
    }
}
