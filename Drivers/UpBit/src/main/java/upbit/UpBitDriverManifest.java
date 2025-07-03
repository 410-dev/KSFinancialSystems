package upbit;

import com.google.gson.JsonObject;
import lombok.Getter;
import me.hysong.files.ConfigurationFile;
import org.kynesys.foundation.v1.interfaces.KSJournalingService;
import org.kynesys.kstraderapi.v1.driver.KSExchangeDriverManifest;
import org.kynesys.kstraderapi.v1.driver.KSExchangeDriverSettings;
import org.kynesys.kstraderapi.v1.driver.KSExchangeDriver;
import org.kynesys.kstraderapi.v1.objects.KSGenericAuthorizationObject;
import org.kynesys.kstraderapi.v1.objects.KSExchangeDriverExitCode;


@Getter
public class UpBitDriverManifest implements KSExchangeDriverManifest {
    private final String driverName = "UpBit";
    private final String driverExchangeName = "UpBit";
    private final String driverExchange = "upbit.com[spot]";
    private final String driverAPIEndpoint = "https://api.upbit.com/v1/";
    private final String driverVersion = "1.0.0";
    private final boolean supportFuture = false;
    private final boolean supportOption = false;
    private final boolean supportPerpetual = false;
    private final boolean supportSpot = true;
    private final boolean supportWS = true;
    private final boolean supportREST = true;
    private final boolean supportOrderAsMarket = true;
    private final boolean supportOrderAsLimit = true;
    private final String driverUpdateDate = "2025-05-30";
    private final String[] supportedSymbols = new String[] {
            "KRW-BTC",
            "KRW-ETH",
            "KRW-XRP",
            "KRW-LTC",
            "KRW-BCH"
    };

    public KSExchangeDriverExitCode testConnection() {
        return KSExchangeDriverExitCode.DRIVER_TEST_OK;
    }

    @Override
    public KSExchangeDriver getDriver(KSJournalingService logger) {
        return new UpBitDriver(logger);
    }

    @Override
    public KSExchangeDriverSettings getPreferenceObject(String driverCfgPath) {
        return new UpBitPreference(driverCfgPath);
    }

    @Override
    public KSGenericAuthorizationObject getAccount(String type, ConfigurationFile preferenceFile) {
        return new KSGenericAuthorizationObject(type, driverExchange, preferenceFile.get("auth.apiAK", String.class, ""), preferenceFile.get("auth.apiSK", String.class, ""));
    }
}
