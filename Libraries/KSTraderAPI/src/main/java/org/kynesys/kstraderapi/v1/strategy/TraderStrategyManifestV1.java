package org.kynesys.kstraderapi.v1.strategy;

import org.kynesys.foundation.v1.interfaces.KSJournalingService;

import java.util.HashMap;

public interface TraderStrategyManifestV1 {

    String getStrategyName();
    String getStrategyVersion();
    boolean isForWS();
    boolean isForREST();
    boolean isSupportSpot();
    boolean isSupportFuture();
    boolean isSupportOption();
    boolean isSupportPerpetual();
    boolean isSupportOrderAsLimit();
    boolean isSupportOrderAsMarket();
    StrategySettingsV1 parseSettings(HashMap<String, Object> settings);

    RESTStrategyV1 getRESTStrategy(KSJournalingService logger);
    WSStrategyV1 getWSStrategy(KSJournalingService logger);


}
