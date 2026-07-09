package org.craftcord.craftcordplugin.protocol;

public enum ErrorCode {
    AUTH_FAILED("auth_failed"),
    BAD_REQUEST("bad_request"),
    UNSUPPORTED_ACTION("unsupported_action"),
    PLAYER_NOT_FOUND("player_not_found"),
    INTERNAL_ERROR("internal_error");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}

