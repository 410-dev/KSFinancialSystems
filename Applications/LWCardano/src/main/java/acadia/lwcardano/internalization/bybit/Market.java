package acadia.lwcardano.internalization.bybit;

import acadia.lwcardano.Logger;
import acadia.lwcardano.internalization.bybit.objects.ByBitCredentials;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.position.request.PositionDataRequest;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;

import java.util.LinkedHashMap;

import static acadia.lwcardano.internalization.bybit.APIResponseConverter.asListOfObj;
import static acadia.lwcardano.internalization.bybit.APIResponseConverter.asObj;


public class Market {

    public static double getCurrentPrice(ByBitCredentials credentials, String category, String symbol) {
        BybitApiMarketRestClient client = ClientFactory.getAuthorizedTradeClient(credentials).newMarketDataRestClient();

        CategoryType ct = category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT;

        MarketDataRequest market = MarketDataRequest.builder()
                .category(ct)
                .symbol(symbol)
                .marketInterval(MarketInterval.HOURLY).build();

        LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) client.getMarketPriceLinesData(market);
        Logger.log("DEBUG", "getCurrentPrice() got response: " + response.toString().substring(0, Math.min(response.toString().length(), 300)));
        String lastTradedPrice = String.valueOf(
                asListOfObj(
                        asListOfObj(
                                asObj(
                                        asObj(response).get("result")
                                ).get("list")
                        ).getFirst()
                ).getLast());
        return Double.parseDouble(lastTradedPrice);
    }

    public static double calculateExchange(ByBitCredentials credentials, String category, String symbol, boolean fromTether, double amount) {
        double exchangeRate = getCurrentPrice(credentials, category, symbol);

        if (fromTether) {
            return amount / exchangeRate; // Tether to Crypto (1k usd / 100k btcusd = 0.01 btc)
        } else {
            return exchangeRate / amount; // Crypto to Tether (100k btcusd / 0.01 btc = 1k usd)
        }
    }

}
