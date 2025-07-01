package org.kynesys.foundation.v1.sharedobj;

import lombok.Getter;
import org.kynesys.foundation.v1.abstractions.KSLoadableComponentManifest;

import java.util.function.Predicate;

public record KSLoadableComponentDependencyRecord(String componentIdentifier, String version,
                                                  Predicate<KSLoadableComponentManifest> compatibilityTest) {

}
