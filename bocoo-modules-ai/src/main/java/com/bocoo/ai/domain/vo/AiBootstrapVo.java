package com.bocoo.ai.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiBootstrapVo {
    private boolean enabled;
    private String model;
    private String pageAgentBaseUrl;
    private String apiKey;
    private List<String> features = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private AiQuotaVo quota;
}
