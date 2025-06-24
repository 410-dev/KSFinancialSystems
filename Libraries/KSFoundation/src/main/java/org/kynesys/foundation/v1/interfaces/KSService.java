package org.kynesys.foundation.v1.interfaces;

import org.kynesys.foundation.v1.sharedobj.KSEnvironment;

public interface KSService {
    int serviceMain(KSEnvironment environment, String execLocation, String[] args);
    int stop(int code);
}
