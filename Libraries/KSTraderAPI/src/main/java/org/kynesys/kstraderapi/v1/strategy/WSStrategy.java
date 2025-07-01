package org.kynesys.kstraderapi.v1.strategy;

import org.kynesys.kstraderapi.v1.driver.TraderDriverManifest;
import org.kynesys.kstraderapi.v1.driver.TraderDriver;
import org.kynesys.kstraderapi.v1.objects.Account;
import org.kynesys.foundation.v1.interfaces.KSJournalingService;

public interface WSStrategy {
    void loop(Account account, String[] symbols, TraderDriverManifest driverManifest, TraderDriver driver, KSJournalingService logger) throws Exception;
    default double getPreferredLatency() {
        return 0.5;
    }
}
