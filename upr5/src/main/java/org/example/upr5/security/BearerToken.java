package org.example.upr5.security;

public final class BearerToken {
    private static final String PREFIX = "Bearer ";

    private BearerToken() {
    }

    public static String fromAuthorizationHeader(String authorization) {
        if (authorization == null
                || authorization.length() <= PREFIX.length()
                || !authorization.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            throw new IllegalArgumentException("Authorization must use the Bearer scheme");
        }

        String token = authorization.substring(PREFIX.length()).trim();
        if (token.isEmpty() || token.indexOf(' ') >= 0) {
            throw new IllegalArgumentException("Invalid Bearer token");
        }
        return token;
    }
}
