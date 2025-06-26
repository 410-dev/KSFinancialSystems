package acadia.lwcardano.internalization.bybit;

import acadia.lwcardano.Logger;
import acadia.lwcardano.internalization.utils.SynchronousCallback;
import ch.qos.logback.core.net.server.Client;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.restApi.BybitApiAsyncTradeRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import me.hysong.files.ConfigurationFile;

import java.util.Map;
import java.util.UUID;

public class Orders {



    public static boolean placeOrder(ConfigurationFile cfg, double price, Side side, String orderSID) {
        BybitApiAsyncTradeRestClient client = ClientFactory.getAuthorizedClient(cfg).newAsyncTradeRestClient();

        String category = cfg.get("market");
        String symbol = cfg.get("symbol");
        String fundStr = cfg.get("fund");
        double fund = Double.parseDouble(fundStr);
        double amountBTC = Market.calculateExchange(cfg, symbol, true, fund);

        TradeOrderRequest order = TradeOrderRequest.builder()
                .category(category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT)
                .symbol(symbol)
                .side(side)
                .orderType(TradeOrderType.LIMIT)
                .qty(String.valueOf(amountBTC).length() > "0.000".length() ? String.valueOf(amountBTC).substring(0, "0.000".length()) : String.valueOf(amountBTC))
                .orderLinkId(orderSID)
                .price(String.valueOf(price))
                .build();

        Logger.log("INFO", "[" + orderSID + "] Placing order...");
        SynchronousCallback callback = new SynchronousCallback();
        callback.setTimeout(60); // Set timeout to 60 seconds
        client.createOrder(order, callback::callbackCapture);
        Object response = callback.sync().getLastResponse();
        Logger.log("INFO", "[" + orderSID + "] Place order response: " + response);
        return response.toString().contains("\"retMsg\": \"OK\"");
    }

    public static boolean cancelOrder(ConfigurationFile cfg, String orderSID) {
        BybitApiAsyncTradeRestClient client = ClientFactory.getAuthorizedClient(cfg).newAsyncTradeRestClient();

        String category = cfg.get("market");
        String symbol = cfg.get("symbol");


        TradeOrderRequest order = TradeOrderRequest.builder()
                .category(category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT)
                .symbol(symbol)
                .orderLinkId(orderSID)
                .build();

        Logger.log("INFO", "[" + orderSID + "] Placing order...");
        SynchronousCallback callback = new SynchronousCallback();
        callback.setTimeout(60); // Set timeout to 60 seconds
        client.cancelOrder(order, callback::callbackCapture);
        Object response = callback.sync().getLastResponse();
        Logger.log("INFO", "[" + orderSID + "] Place order response: " + response);
        return response.toString().contains("\"retMsg\": \"OK\"");
    }
}
