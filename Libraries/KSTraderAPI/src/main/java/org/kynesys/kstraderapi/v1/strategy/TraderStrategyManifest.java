package org.kynesys.kstraderapi.v1.strategy;

import org.kynesys.foundation.v1.interfaces.KSJournalingService;

import java.util.HashMap;

public interface TraderStrategyManifest {

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
    StrategySettings parseSettings(HashMap<String, Object> settings);

    RESTStrategy getRESTStrategy(KSJournalingService logger);
    WSStrategy getWSStrategy(KSJournalingService logger);


}
