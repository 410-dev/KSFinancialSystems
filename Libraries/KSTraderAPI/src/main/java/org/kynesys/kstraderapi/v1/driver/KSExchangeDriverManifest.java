package org.kynesys.kstraderapi.v1.driver;

import com.google.gson.JsonObject;
import me.hysong.files.ConfigurationFile;
import org.kynesys.kstraderapi.v1.objects.KSGenericAuthorizationObject;
import org.kynesys.kstraderapi.v1.objects.KSExchangeDriverExitCode;
import org.kynesys.foundation.v1.interfaces.KSJournalingService;

public interface KSExchangeDriverManifest {

    String getDriverName();          // Driver name, ex. "UpBit Generic Driver"
    String getDriverExchangeName();  // Exchange name, ex. "UpBit"
    String getDriverExchange();      // Exchange with supported types. Format: "<url>[support]", ex. "upbit.com[spot,future,option]"
    String getDriverAPIEndpoint();   // API endpoint, ex. "https://api.upbit.com/v1/"
    String getDriverVersion();
    boolean isSupportREST();
    boolean isSupportWS();
    boolean isSupportFuture();
    boolean isSupportOption();
    boolean isSupportPerpetual();
    boolean isSupportSpot();
    boolean isSupportOrderAsLimit();
    boolean isSupportOrderAsMarket();
    String getDriverUpdateDate();
    String[] getSupportedSymbols(); // Supported symbols, ex. ["KRW-BTC", "BTC-USDT"]
    KSExchangeDriverExitCode testConnection();
    KSExchangeDriver getDriver(KSJournalingService logger);
    KSExchangeDriverSettings getPreferenceObject(String driverCfgPath);
    KSGenericAuthorizationObject getAccount(String type, ConfigurationFile preferenceFile);

    default String getFileSystemIdentifier() {
        return getDriverExchange() + "@" + getDriverAPIEndpoint().replace("/", "_").replace(":", "_");
    }
}
