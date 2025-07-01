package org.kynesys.kstraderapi.v1.strategy;

import org.kynesys.foundation.v1.interfaces.KSJournalingService;

import java.util.HashMap;

public interface KSStrategyManifest {

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
    KSStrategySettings parseSettings(HashMap<String, Object> settings);

    KSStrategyForRepresentationalStateTransferInterface getRESTStrategy(KSJournalingService logger);
    KSStrategyForWebSocketInterface getWSStrategy(KSJournalingService logger);


}
