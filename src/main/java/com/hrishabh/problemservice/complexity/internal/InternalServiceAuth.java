package com.hrishabh.problemservice.complexity.internal;

import com.hrishabh.problemservice.complexity.config.ComplexityProfileProperties;
import com.hrishabh.problemservice.exceptions.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalServiceAuth {

    public static final String INTERNAL_CALL_HEADER = "X-Internal-Call";

    private final ComplexityProfileProperties properties;

    public InternalServiceAuth(ComplexityProfileProperties properties) {
        this.properties = properties;
    }

    public void requireInternal(HttpServletRequest request) {
        String configuredToken = properties.getInternalServiceToken();
        boolean tokenConfigured = StringUtils.hasText(configuredToken);
        if (properties.isInternalAuthRequireToken() || tokenConfigured) {
            if (!tokenConfigured) {
                throw new ForbiddenException("Internal service token is not configured");
            }
            String provided = request.getHeader(ComplexityProfileProperties.INTERNAL_SERVICE_TOKEN_HEADER);
            if (provided == null || !constantTimeEquals(configuredToken, provided)) {
                throw new ForbiddenException("Internal service authentication required");
            }
            return;
        }
        String internalCall = request.getHeader(INTERNAL_CALL_HEADER);
        if (!"true".equalsIgnoreCase(internalCall)) {
            throw new ForbiddenException("Internal call header required");
        }
    }

    private static boolean constantTimeEquals(String expected, String provided) {
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = provided.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }
}
