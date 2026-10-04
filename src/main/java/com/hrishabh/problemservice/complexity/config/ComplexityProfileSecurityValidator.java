package com.hrishabh.problemservice.complexity.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ComplexityProfileSecurityValidator {

    private final ComplexityProfileProperties properties;

    public ComplexityProfileSecurityValidator(ComplexityProfileProperties properties) {
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateProductionInternalAuth() {
        if (!properties.isProfileApiEnabled()) {
            return;
        }
        if (properties.isInternalAuthRequireToken()
                && !StringUtils.hasText(properties.getInternalServiceToken())) {
            throw new IllegalStateException(
                    "complexity.internal-auth-require-token is true but INTERNAL_SERVICE_TOKEN is not configured");
        }
    }
}
