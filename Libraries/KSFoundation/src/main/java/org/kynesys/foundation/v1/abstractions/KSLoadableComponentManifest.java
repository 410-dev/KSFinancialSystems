package org.kynesys.foundation.v1.abstractions;

import org.kynesys.foundation.v1.sharedobj.KSLoadableComponentDependencyRecord;

import java.util.ArrayList;

public interface KSLoadableComponentManifest {
    String getComponentID();
    String getComponentName();
    String getComponentVersion();
    int getBuild();
    int getRequiredFoundationSDKVersion();
    String getRequiredFoundationSDKIdentifier();
    String getSourceRepositoryUrl();     // e.g. “https://github.com/thirdparty/indicator”
    String getLicenseIdentifier();       // e.g. “MIT” or any SPDX identifier
    ArrayList<KSLoadableComponentDependencyRecord> getDependencies();
}
