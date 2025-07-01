package upbit;

import com.google.gson.JsonObject;
import lombok.Getter;
import org.kynesys.foundation.v1.interfaces.KSJournalingService;
import org.kynesys.kstraderapi.v1.driver.TraderDriverManifest;
import org.kynesys.kstraderapi.v1.driver.TraderDriverSettings;
import org.kynesys.kstraderapi.v1.driver.TraderDriver;
import org.kynesys.kstraderapi.v1.objects.Account;
import org.kynesys.kstraderapi.v1.objects.DriverExitCode;


@Getter
public class UpBitDriverManifest implements TraderDriverManifest {
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

    public DriverExitCode testConnection() {
        return DriverExitCode.DRIVER_TEST_OK;
    }

    @Override
    public TraderDriver getDriver(KSJournalingService logger) {
        return new UpBitDriver(logger);
    }

    @Override
    public TraderDriverSettings getPreferenceObject(String driverCfgPath) {
        return new UpBitPreference(driverCfgPath);
    }

    @Override
    public Account getAccount(String type, JsonObject preferenceFile) {
        return new Account(type, driverExchange, preferenceFile.get("auth.apiAK").getAsString(), preferenceFile.get("auth.apiSK").getAsString());
    }
}
