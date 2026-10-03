package com.hrishabh.problemservice.helper;

import com.hrishabh.problemservice.exceptions.ForbiddenException;
import com.hrishabh.problemservice.exceptions.UnauthorizedException;

/**
 * Resolves the caller's identity from the {@code X-User-Id} header, which the API gateway sets from
 * a validated JWT (and strips from inbound client requests). Never accept a user id from the body,
 * path or query for user-owned data.
 */
public final class CurrentUser {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String ROLE_HEADER = "X-User-Role";
    public static final String ADMIN_ROLE = "ADMIN";

    private static final int MAX_USER_ID_LENGTH = 255;

    private CurrentUser() {
    }

    public static String require(String headerValue) {
        if (headerValue == null || headerValue.isBlank() || headerValue.length() > MAX_USER_ID_LENGTH) {
            throw new UnauthorizedException("Authentication required");
        }
        return headerValue.trim();
    }

    public static String requireAdmin(String userIdHeader, String roleHeader) {
        String userId = require(userIdHeader);
        if (roleHeader == null || !ADMIN_ROLE.equalsIgnoreCase(roleHeader.trim())) {
            throw new ForbiddenException("Admin role required");
        }
        return userId;
    }
}
