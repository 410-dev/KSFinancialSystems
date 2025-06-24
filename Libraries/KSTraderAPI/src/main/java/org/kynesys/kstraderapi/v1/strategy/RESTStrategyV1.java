package org.kynesys.kstraderapi.v1.strategy;

import org.kynesys.kstraderapi.v1.driver.TraderDriverManifestV1;
import org.kynesys.kstraderapi.v1.driver.TraderDriverV1;
import org.kynesys.kstraderapi.v1.objects.Account;
import org.kynesys.foundation.v1.interfaces.KSJournalingService;

public interface RESTStrategyV1 {

    void loop(Account account, String[] symbols, TraderDriverManifestV1 driverManifest, TraderDriverV1 driver, KSJournalingService logger) throws Exception;
    default double getPreferredLatency() {
        return 0.5;
    }

}
