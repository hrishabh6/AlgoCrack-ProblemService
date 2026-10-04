package com.hrishabh.problemservice.complexity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "complexity")
public class ComplexityProfileProperties {

    /**
     * Registers internal complexity profile endpoints. Default off until orchestration is enabled.
     */
    private boolean profileApiEnabled = false;

    /**
     * Shared secret for internal service calls. Empty disables token enforcement (header-only era).
     */
    private String internalServiceToken = "";

    public static final String INTERNAL_SERVICE_TOKEN_HEADER = "X-Internal-Service-Token";
}
