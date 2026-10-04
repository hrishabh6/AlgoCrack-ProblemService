package com.hrishabh.problemservice.complexity.internal;

import com.hrishabh.problemservice.complexity.config.ComplexityProfileProperties;
import com.hrishabh.problemservice.exceptions.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalServiceAuthTest {

    @Mock
    private HttpServletRequest request;

    private ComplexityProfileProperties properties;
    private InternalServiceAuth auth;

    @BeforeEach
    void setUp() {
        properties = new ComplexityProfileProperties();
        auth = new InternalServiceAuth(properties);
    }

    @Test
    void developmentAllowsInternalCallHeaderWhenTokenNotRequired() {
        when(request.getHeader(InternalServiceAuth.INTERNAL_CALL_HEADER)).thenReturn("true");
        assertDoesNotThrow(() -> auth.requireInternal(request));
    }

    @Test
    void rejectsMissingInternalCallWhenTokenNotConfigured() {
        assertThrows(ForbiddenException.class, () -> auth.requireInternal(request));
    }

    @Test
    void productionRequireTokenRejectsHeaderOnlyTrust() {
        properties.setInternalAuthRequireToken(true);
        properties.setInternalServiceToken("secret-token");
        when(request.getHeader(ComplexityProfileProperties.INTERNAL_SERVICE_TOKEN_HEADER)).thenReturn(null);
        assertThrows(ForbiddenException.class, () -> auth.requireInternal(request));
    }

    @Test
    void productionRequireTokenAcceptsMatchingServiceToken() {
        properties.setInternalAuthRequireToken(true);
        properties.setInternalServiceToken("secret-token");
        when(request.getHeader(ComplexityProfileProperties.INTERNAL_SERVICE_TOKEN_HEADER)).thenReturn("secret-token");
        assertDoesNotThrow(() -> auth.requireInternal(request));
    }

    @Test
    void configuredTokenRejectsWrongTokenEvenWhenRequireFlagFalse() {
        properties.setInternalServiceToken("secret-token");
        when(request.getHeader(ComplexityProfileProperties.INTERNAL_SERVICE_TOKEN_HEADER)).thenReturn("wrong");
        assertThrows(ForbiddenException.class, () -> auth.requireInternal(request));
    }

    @Test
    void requireTokenWithoutConfiguredSecretFailsClosed() {
        properties.setInternalAuthRequireToken(true);
        assertThrows(ForbiddenException.class, () -> auth.requireInternal(request));
    }
}
