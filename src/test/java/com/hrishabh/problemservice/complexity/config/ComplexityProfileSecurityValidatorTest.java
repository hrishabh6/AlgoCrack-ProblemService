package com.hrishabh.problemservice.complexity.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComplexityProfileSecurityValidatorTest {

    @Test
    void skipsValidationWhenProfileApiDisabled() {
        ComplexityProfileProperties properties = new ComplexityProfileProperties();
        properties.setInternalAuthRequireToken(true);
        ComplexityProfileSecurityValidator validator = new ComplexityProfileSecurityValidator(properties);
        assertDoesNotThrow(validator::validateProductionInternalAuth);
    }

    @Test
    void failsStartupWhenRequireTokenWithoutSecret() {
        ComplexityProfileProperties properties = new ComplexityProfileProperties();
        properties.setProfileApiEnabled(true);
        properties.setInternalAuthRequireToken(true);
        ComplexityProfileSecurityValidator validator = new ComplexityProfileSecurityValidator(properties);
        assertThrows(IllegalStateException.class, validator::validateProductionInternalAuth);
    }

    @Test
    void passesWhenRequireTokenAndSecretConfigured() {
        ComplexityProfileProperties properties = new ComplexityProfileProperties();
        properties.setProfileApiEnabled(true);
        properties.setInternalAuthRequireToken(true);
        properties.setInternalServiceToken("configured");
        ComplexityProfileSecurityValidator validator = new ComplexityProfileSecurityValidator(properties);
        assertDoesNotThrow(validator::validateProductionInternalAuth);
    }
}
