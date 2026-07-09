package org.craftcord.craftcordplugin.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    @Test
    void validatesTokenAndAuthorizationHeader() {
        AuthService authService = new AuthService("secret-token");

        assertTrue(authService.isValidToken("secret-token"));
        assertFalse(authService.isValidToken("wrong"));

        assertTrue(authService.isValidAuthorizationHeader("Bearer secret-token"));
        assertFalse(authService.isValidAuthorizationHeader("Bearer wrong"));
        assertFalse(authService.isValidAuthorizationHeader("secret-token"));
    }
}

