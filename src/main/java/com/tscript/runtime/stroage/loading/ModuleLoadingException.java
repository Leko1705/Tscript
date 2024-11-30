package com.tscript.runtime.stroage.loading;

import java.io.IOException;

public class ModuleLoadingException extends IOException {

    public ModuleLoadingException(String message) {
        super(message);
    }

    public ModuleLoadingException(Throwable cause) {
        super(cause);
    }
}
