package org.kynesys.foundation.v1.async;

import java.io.Serializable;

@FunctionalInterface
public interface ParameteredRunnable extends Serializable {
    void run(Object... args);
}

