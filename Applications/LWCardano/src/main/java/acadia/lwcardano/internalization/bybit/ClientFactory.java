package acadia.lwcardano.internalization.bybit;

import acadia.lwcardano.Logger;
import acadia.lwcardano.internalization.bybit.objects.ByBitCredentials;
import com.bybit.api.client.service.BybitApiClientFactory;

public class ClientFactory {

    public static BybitApiClientFactory getAuthorizedTradeClient(ByBitCredentials c) {

        if (c.AK() == null || c.SK() == null || c.AK().isEmpty() || c.SK().isEmpty()) {
            throw new IllegalArgumentException("API Key or Secret is not set.");
        }

        // Get Preferred API endpoint
        String apiEndpoint = c.endpoint() != null ? c.endpoint() : "https://api-demo.bybit.com";

        // If endpoint is demo, send log alert
//        if (apiEndpoint.equals("https://api-demo.bybit.com")) {
//            Logger.log("WARNING", "API Client factory is using demo endpoint: " + apiEndpoint);
//            Logger.log("DEBUG", "API Key: " + c.AK() + ", API Secret: " + c.SK());
//        }

        return BybitApiClientFactory.newInstance(c.AK(), c.SK(), apiEndpoint);
    }
}
