package org.kynesys.kstraderapi.v1.strategy;

import org.kynesys.kstraderapi.v1.driver.KSExchangeDriverManifest;
import org.kynesys.kstraderapi.v1.driver.KSExchangeDriver;
import org.kynesys.kstraderapi.v1.objects.KSGenericAuthorizationObject;
import org.kynesys.foundation.v1.interfaces.KSJournalingService;

public interface KSStrategyForWebSocketInterface {
    void loop(KSGenericAuthorizationObject KSGenericAuthorizationObject, String[] symbols, KSExchangeDriverManifest driverManifest, KSExchangeDriver driver, KSJournalingService logger) throws Exception;
    default double getPreferredLatency() {
        return 0.5;
    }
}
