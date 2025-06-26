package acadia.lwcardano.internalization.bybit;

import acadia.lwcardano.Logger;
import com.bybit.api.client.service.BybitApiClientFactory;
import me.hysong.files.ConfigurationFile;

public class ClientFactory {

    public static BybitApiClientFactory getAuthorizedClient(ConfigurationFile cfg) {
        String pk = cfg.get("ak");
        String sk = cfg.get("sk");

        if (pk == null || sk == null || pk.isEmpty() || sk.isEmpty()) {
            throw new IllegalArgumentException("API Key or Secret is not set.");
        }

        // Get Preferred API endpoint
        String apiEndpoint = cfg.get("url", "https://api-demo.bybit.com");

        // If endpoint is demo, send log alert
        if (apiEndpoint.equals("https://api-demo.bybit.com")) {
            Logger.log("WARNING", "API Client factory is using demo endpoint: " + apiEndpoint);
            Logger.log("DEBUG", "API Key: " + pk + ", API Secret: " + sk);
        }

        return BybitApiClientFactory.newInstance(pk, sk, apiEndpoint);
    }

}
