package org.kynesys.foundation.v1.interfaces;

import org.kynesys.foundation.v1.sharedobj.KSEnvironment;

public interface KSApplication {
    default boolean isMultipleInstancesAllowed() {return false;}
    String getAppDisplayName();
    int appMain(KSEnvironment environment, String execLocation, String[] args, KSJournalingService journaling);
}
