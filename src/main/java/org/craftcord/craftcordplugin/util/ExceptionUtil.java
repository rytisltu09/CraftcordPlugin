package org.craftcord.craftcordplugin.util;

import java.util.concurrent.CompletionException;

public final class ExceptionUtil {
    private ExceptionUtil() {
    }

    public static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            return completionException.getCause();
        }
        return throwable;
    }
}

