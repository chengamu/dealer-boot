package com.bocoo.common.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "bocoo.tenant")
public class TenantProperties {

    /**
     * Keep disabled until tenant_id columns are added to the target schema.
     */
    private Boolean enabled = false;

    private List<String> ignoreUrls = new ArrayList<>();

    private List<String> ignoreTables = new ArrayList<>();
}
