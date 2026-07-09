package org.craftcord.craftcordplugin.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class AuthService {
    private final byte[] tokenBytes;

    public AuthService(String apiToken) {
        this.tokenBytes = apiToken.getBytes(StandardCharsets.UTF_8);
    }

    public boolean isValidToken(String token) {
        if (token == null) {
            return false;
        }
        byte[] provided = token.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(tokenBytes, provided);
    }

    public boolean isValidAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }
        return isValidToken(authorizationHeader.substring("Bearer ".length()).trim());
    }
}

