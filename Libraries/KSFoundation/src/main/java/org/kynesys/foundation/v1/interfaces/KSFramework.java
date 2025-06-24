package org.kynesys.foundation.v1.interfaces;

import org.kynesys.foundation.v1.sharedobj.KSEnvironment;

public interface KSFramework {
    int frameworkMain(KSEnvironment environment, String execLocation, String[] args);
}
