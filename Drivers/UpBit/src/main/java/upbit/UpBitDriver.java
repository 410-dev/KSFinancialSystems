package upbit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.kynesys.foundation.v1.interfaces.KSJournalingService;
import org.kynesys.foundation.v1.utils.SIDKit;
import org.kynesys.kstraderapi.v1.driver.TraderDriver;
import org.kynesys.kstraderapi.v1.objects.*;
import org.kynesys.kstraderapi.v1.utils.CurlEmulator;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpBitDriver implements TraderDriver {

    private final String endpointUrl = new UpBitDriverManifest().getDriverAPIEndpoint();
    private final String exchangeName = new UpBitDriverManifest().getDriverExchangeName();

    private static ArrayList<String> getMissingParams(HashMap<String, Object> params, String[] requiredParams) {
        ArrayList<String> missingParams = new ArrayList<>();
        for (String param : requiredParams) {
            if (!params.containsKey(param)) {
                missingParams.add(param);
            }
        }
        return missingParams;
    }

    @Getter
    private static class UpBitCandleDataStructure {
        String market;
        String candleDateTimeUtc;
        String candleDateTimeKst;
        long openingTime;
        double openingPrice;
        double highPrice;
        double lowPrice;
        double tradePrice;
        long timestamp;
        double candleAccTradePrice;
        double candleAccTradeVolume;
        int unit;

        public UpBitCandleDataStructure(JsonObject jsonObject) {
            this.market = jsonObject.get("market").getAsString();
            this.candleDateTimeUtc = jsonObject.get("candle_date_time_utc").getAsString();
            this.candleDateTimeKst = jsonObject.get("candle_date_time_kst").getAsString();
            this.openingPrice = jsonObject.get("opening_price").getAsDouble();
            this.highPrice = jsonObject.get("high_price").getAsDouble();
            this.lowPrice = jsonObject.get("low_price").getAsDouble();
            this.tradePrice = jsonObject.get("trade_price").getAsDouble();
            this.timestamp = jsonObject.get("timestamp").getAsLong();
            this.candleAccTradePrice = jsonObject.get("candle_acc_trade_price").getAsDouble();
            this.candleAccTradeVolume = jsonObject.get("candle_acc_trade_volume").getAsDouble();
            this.unit = jsonObject.get("unit").getAsInt();
            OffsetDateTime odtUtc = OffsetDateTime.parse(
                    candleDateTimeUtc + "Z",
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
            );
            Instant instant = odtUtc.toInstant();
            ZoneId localZone = ZoneId.systemDefault();
            OffsetDateTime odtLocal = instant.atZone(localZone).toOffsetDateTime();
            openingTime = odtLocal.toInstant().toEpochMilli();
        }
    }

    public UpBitDriver(KSJournalingService ksJournalingService) {

    }

    @Override
    public Chart getChart(Account account, HashMap<String, Object> params) throws Exception {

        // Check required parameters
        String[] requiredParams = {"symbol"};
        if (!getMissingParams(params, requiredParams).isEmpty()) {
            DriverState.addState(DriverExitCode.DRIVER_BAD_DATA_ERROR, "Missing required parameters: " + getMissingParams(params, requiredParams));
            return null;
        }

        // Get parameters
        String symbol = String.valueOf(params.get("symbol"));
        String intervalUnit = String.valueOf(params.getOrDefault("intervalUnit", "minutes"));
        String oldestCandlestick = String.valueOf(params.getOrDefault("oldestCandlestick", ""));
        int interval = Integer.parseInt(String.valueOf(params.getOrDefault("interval", 30)));
        int count = Integer.parseInt(String.valueOf(params.getOrDefault("count", 150)));

        if (intervalUnit.equals("seconds")) {
            System.out.println("WARNING: Interval unit 'seconds' requires interval to be 1. Setting interval to 1.");
            interval = 1;
        }

        // Send and parse response
        String response;
        try {
            response = CurlEmulator.curl(endpointUrl + "candles/" + intervalUnit + "/" + interval + "?market=" + symbol + "&count=" + count + "&to=" + oldestCandlestick, "GET", "accept: application/json", null);
        } catch (Exception e) {
            DriverState.addState(DriverExitCode.SERVER_CONNECTION_ERROR, "Failed to get response from UpBit API", e);
            return null;
        }
        JsonArray json;
        try {
            json = JsonParser.parseString(response).getAsJsonArray();
        } catch (Exception e) {
            DriverState.addState(DriverExitCode.DRIVER_BAD_DATA_ERROR, "Failed to parse response from UpBit API", e);
            return null;
        }

        // Expect array of json objects, where each object is a candlestick
        // Example:
        /*
        [
          {
            "market": "KRW-BTC",
            "candle_date_time_utc": "2018-04-18T10:16:00",
            "candle_date_time_kst": "2018-04-18T19:16:00",
            "opening_price": 8615000,
            "high_price": 8618000,
            "low_price": 8611000,
            "trade_price": 8616000,
            "timestamp": 1524046594584,
            "candle_acc_trade_price": 60018891.90054,
            "candle_acc_trade_volume": 6.96780929,
            "unit": 1
          }
        ]
         */


        // Convert to UpBitCandleDataStructure
        ArrayList<UpBitCandleDataStructure> candleData = new ArrayList<>();
        for (int i = 0; i < json.size(); i++) {
            candleData.add(new UpBitCandleDataStructure(json.get(i).getAsJsonObject()));
        }

        // Get last candle
        UpBitCandleDataStructure lastCandle = candleData.getLast();
        UpBitCandleDataStructure firstCandle = candleData.getFirst();

        // Create chart object
        Chart chart = new Chart(symbol, interval + intervalUnit.substring(0, 1), firstCandle.getOpeningTime(), lastCandle.getOpeningTime(), MarketTypes.SPOT, exchangeName, account.getUniqueID());

        // Add data to chart
        // Calculate close time based on unit and interval
        long secondsMultiplier = 1;
        switch (intervalUnit) {
            case "minutes" -> secondsMultiplier = 60;
            case "hours" -> secondsMultiplier = 3600;
            case "days" -> secondsMultiplier = 86400;
            case "weeks" -> secondsMultiplier = 604800;
            case "months" -> secondsMultiplier = 2592000;
            case "years" -> secondsMultiplier = 31536000;
        }
        long closeTime = (secondsMultiplier * interval) - 1;
        closeTime *= 1000; // Convert to milliseconds
        for (UpBitCandleDataStructure candle : candleData) {
            Candlestick candlestick = new Candlestick(exchangeName, symbol, candle.getOpeningTime(), candle.getOpeningTime() + closeTime, candle.getOpeningPrice(), candle.getHighPrice(), candle.getLowPrice(), candle.getTradePrice(), candle.getCandleAccTradeVolume());
            chart.addCandlestick(candlestick);
        }
        return chart;
    }

    @Override
    public ArrayList<Order> getOpenOrders(Account account, HashMap<String, Object> inputParams) throws Exception {
        String accessKey = account.getCredentials().getOrDefault(Account.CREDENTIAL_KEY_PK, "").toString();
        String secretKey = account.getCredentials().getOrDefault(Account.CREDENTIAL_KEY_SK, "").toString();

        // Translate symbol to market (Standard)
        HashMap<String, Object> params = new HashMap<>(inputParams);
        params.put("market", params.remove("symbol"));

        String[] states = {
                "wait",
                "watch"
        };

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, Object> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }
        for(String state : states) {
            queryElements.add("states[]=" + state);
        }

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes(StandardCharsets.UTF_8));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;

        ArrayList<Order> orders = new ArrayList<>();
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(endpointUrl + "/orders/open?" + queryString);
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String responseString = EntityUtils.toString(entity, "UTF-8");
            JsonArray jsonArray = JsonParser.parseString(responseString).getAsJsonArray();

            System.out.println(responseString);

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jo = jsonArray.get(i).getAsJsonObject();

                Order o = new Order();
                o.setExchange(exchangeName);
                o.setOrderId(jo.get("uuid").getAsString());
                o.setOwnerId(account.getUniqueID());
                o.setBuySide("bid".equals(jo.get("side").getAsString()));
                o.setType(jo.get("ord_type").toString().toUpperCase());
                o.setSymbol(jo.get("market").toString());
                o.setNumberOfTradeCounts(jo.get("trades_count").getAsInt());
                o.setClosed(false);
                o.setOpen(true);
                o.setFilled(o.getNumberOfTradeCounts() > 0);
                o.setExpired(o.getNumberOfTradeCounts() == 0);
                o.setFee(jo.get("paid_fee").getAsDouble() + jo.get("remaining_fee").getAsDouble());
                o.setAmount(jo.get("executed_volume").getAsDouble());
                if (jo.has("price")) {
                    o.setPrice(jo.get("price").getAsDouble());
                } else {
                    o.setPrice(jo.get("executed_funds").getAsDouble() / jo.get("executed_volume").getAsDouble());
                }
                OffsetDateTime odt = OffsetDateTime.parse(
                        jo.get("created_at").getAsString(),
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME
                );
                o.setTime(odt.toInstant().toEpochMilli());
                o.setMarketType("SPOT");
                o.setStatus(o.getNumberOfTradeCounts() > 0 ? "CLOSED" : "CANCELED");

                orders.add(o);
            }

            System.out.println(orders);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return orders;
    }


    // REFER TO
    //
    //    https://docs.upbit.com/kr/reference/%EC%A2%85%EB%A3%8C-%EC%A3%BC%EB%AC%B8-%EC%A1%B0%ED%9A%8C
    //
    @Override
    public ArrayList<Order> getClosedOrders(Account account, HashMap<String, Object> inputParams) throws Exception {
        String accessKey = account.getCredentials().getOrDefault(Account.CREDENTIAL_KEY_PK, "").toString();
        String secretKey = account.getCredentials().getOrDefault(Account.CREDENTIAL_KEY_SK, "").toString();

        // Translate symbol to market (Standard 'symbol' to proprietary 'market')
        HashMap<String, Object> params = new HashMap<>(inputParams);
        params.put("market", params.remove("symbol"));

        String[] states = {
                "done",
                "cancel"
        };

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, Object> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }
        for(String state : states) {
            queryElements.add("states[]=" + state);
        }

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes(StandardCharsets.UTF_8));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;

        ArrayList<Order> orders = new ArrayList<>();
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(endpointUrl + "/orders/closed?" + queryString);
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String responseString = EntityUtils.toString(entity, "UTF-8");
            JsonArray jsonArray = JsonParser.parseString(responseString).getAsJsonArray();

            System.out.println(responseString);

            /*
            {
                "uuid": "056ef64e-72db-46f6-876d-acc06d09b2cf",
                "side": "ask",
                "ord_type": "market",
                "state": "done",
                "market": "KRW-BTC",
                "created_at": "2025-06-25T11:39:29+09:00",
                "volume": "0.04448701",
                "remaining_volume": "0",
                "reserved_fee": "0",
                "remaining_fee": "0",
                "paid_fee": "3254.93657366",
                "locked": "0",
                "executed_volume": "0.04448701",
                "executed_funds": "6509873.14732",
                "trades_count": 1
              }
             */

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jo = jsonArray.get(i).getAsJsonObject();

                Order o = new Order();
                o.setExchange(exchangeName);
                o.setOrderId(jo.get("uuid").getAsString());
                o.setOwnerId(account.getUniqueID());
                o.setBuySide("bid".equals(jo.get("side").getAsString()));
                o.setType(jo.get("ord_type").toString().toUpperCase());
                o.setSymbol(jo.get("market").toString());
                o.setNumberOfTradeCounts(jo.get("trades_count").getAsInt());
                o.setClosed(true);
                o.setOpen(false);
                o.setFilled(o.getNumberOfTradeCounts() > 0);
                o.setExpired(o.getNumberOfTradeCounts() == 0);
                o.setFee(jo.get("paid_fee").getAsDouble() + jo.get("remaining_fee").getAsDouble());
                o.setAmount(jo.get("executed_volume").getAsDouble());
                if (jo.has("price")) {
                    o.setPrice(jo.get("price").getAsDouble());
                } else {
                    o.setPrice(jo.get("executed_funds").getAsDouble() / jo.get("executed_volume").getAsDouble());
                }
                OffsetDateTime odt = OffsetDateTime.parse(
                        jo.get("created_at").getAsString(),
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME
                );
                o.setTime(odt.toInstant().toEpochMilli());
                o.setMarketType("SPOT");
                o.setStatus(o.getNumberOfTradeCounts() > 0 ? "CLOSED" : "CANCELED");

                orders.add(o);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return orders;
    }



    @Override
    public Account getAccount(Account account) throws Exception {
        return null;
    }


    // REFER TO
    //
     //   https://docs.upbit.com/kr/reference/%EC%A3%BC%EB%AC%B8%ED%95%98%EA%B8%B0
    @Override
    public Order placeOrder(Account account, Order order, HashMap<String, Object> params) throws Exception {

        // Get parameters
        String symbol = order.getSymbol();
        String side = order.isBuySide() ? "bid" : "ask";
        String price = String.valueOf(order.getPrice());
        String volume = String.valueOf(order.getAmount());
        String orderType;
        if (params.containsKey("order_type")) {
            orderType = String.valueOf(params.get("order_type"));
        } else if (order.getType().equalsIgnoreCase("market")) {
            if (order.isBuySide()) {
                orderType = "market";
            } else {
                orderType = "limit";
            }
        } else if (order.getType().equalsIgnoreCase("limit")) {
            orderType = "limit";
        } else {
            DriverState.addState(DriverExitCode.DRIVER_BAD_DATA_ERROR, "Unknown order type: " + order.getType());
            return null;
        }

        // Get optional parameters
        String identifier = String.valueOf(params.getOrDefault("identifier", SIDKit.generateSID(SIDKit.SIDType.RECORD_DATA)));
        String timeInForce = String.valueOf(params.getOrDefault("time_in_force", "")).toLowerCase();

        // Check side and order_type constraints
        if (!orderType.equals("limit")
                && !(orderType.equals("price") && order.isBuySide()) // UpBit's "price" order is a buy order
                && !(orderType.equals("market") && !order.isBuySide())  // UpBit's "market" order is a sell order
                && !(orderType.equals("best") && (timeInForce.equals("ioc") || timeInForce.equals("fok")))) { // UpBit's "best" order is a market order with IOC or FOK
            DriverState.addState(DriverExitCode.DRIVER_BAD_DATA_ERROR, "Invalid order_type: " + orderType + ". Expected any of ('limit', 'price', 'market'), or 'best' with IOC/FOK for time_in_force.");
            return null;
        }


        // Add Xattr
        order.getXattr().put("side", side);
        order.getXattr().put("identifier", identifier);
        order.getXattr().put("time_in_force", timeInForce);

        String accessKey = account.getCredentials().get(Account.CREDENTIAL_KEY_PK).toString();
        String secretKey = account.getCredentials().get(Account.CREDENTIAL_KEY_SK).toString();
        String serverUrl = endpointUrl + "orders";

        HashMap<String, String> apiParams = new HashMap<>();
        apiParams.put("market", symbol);
        apiParams.put("side", side);
        apiParams.put("volume", volume);
        apiParams.put("price", price);
        apiParams.put("ord_type", orderType);
        if (!timeInForce.isEmpty()) {
            apiParams.put("time_in_force", timeInForce);
        }
        if (params.containsKey("identifier")) {
            apiParams.put("identifier", identifier);
        }

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : apiParams.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes(StandardCharsets.UTF_8));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(serverUrl);
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);
            request.setEntity(new StringEntity(new Gson().toJson(apiParams)));

            System.out.println("Request URL: " + serverUrl);
            System.out.println("Request Headers: " + request.getAllHeaders());
            System.out.println("Request Body: " + new Gson().toJson(apiParams));
            System.out.println("Request Query: " + queryString);
            System.out.println("Request Hash: " + queryHash);

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String responseString = EntityUtils.toString(entity, "UTF-8");

            System.out.println(responseString);

            // Test parsing to JSON and update order object
            JsonObject jsonResponse = JsonParser.parseString(responseString).getAsJsonObject();
            order.setExchange(exchangeName);
            order.setOrderId(jsonResponse.get("uuid").getAsString());
            order.setStatus(jsonResponse.get("state").getAsString());
            order.setOpen("wait".equalsIgnoreCase(jsonResponse.get("state").getAsString()));
            order.setFee(Double.parseDouble(jsonResponse.get("reserved_fee").getAsString()));
            OffsetDateTime odt = OffsetDateTime.parse(jsonResponse.get("created_at").getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            long epochMilli = odt.toInstant().toEpochMilli();
            order.setTime(epochMilli);

            // Set raw object to xattr
            order.getXattr().put("jsonResponse", jsonResponse.toString());
            order.getXattr().put("identifier", identifier);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return order;
    }

    @Override
    public ArrayList<Order> placeOrders(Account account, ArrayList<Order> orders, HashMap<String, Object> params) throws Exception {
        return null;
    }

    @Override
    public Order cancelOrder(Account account, String orderId, HashMap<String, Object> params) throws Exception {
        String accessKey = account.getCredentials().get(Account.CREDENTIAL_KEY_PK).toString();
        String secretKey = account.getCredentials().get(Account.CREDENTIAL_KEY_SK).toString();

        HashMap<String, String> apiParams = new HashMap<>();
//        apiParams.put("uuid", "cdd92199-2897-4e14-9448-f923320408ad");
        apiParams.put("uuid", orderId);

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : apiParams.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes(StandardCharsets.UTF_8));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpDelete request = new HttpDelete(endpointUrl + "/order?" + queryString);
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String responseString = EntityUtils.toString(entity, "UTF-8");
            System.out.println("Response: " + responseString);

            // Parsing to JSON
            JsonObject jsonObject = JsonParser.parseString(responseString).getAsJsonObject();
            Order order = new Order();

            order.setOrderId(jsonObject.get("uuid").getAsString());
            order.setExchange(exchangeName);
//            order.setOwnerId(account.getAccountId());
            order.setStatus(jsonObject.get("state").getAsString());
            order.setCanceled("cancel".equalsIgnoreCase(order.getStatus()));
            order.setOpen("wait".equalsIgnoreCase(order.getStatus()));
            order.setClosed("done".equalsIgnoreCase(order.getStatus()));
            order.setBuySide("bid".equalsIgnoreCase(jsonObject.get("side").getAsString()));
            order.setType(jsonObject.get("ord_type").getAsString());
            order.setSymbol(jsonObject.get("market").getAsString());
            order.setPrice(Double.parseDouble(jsonObject.get("price").getAsString()));
            order.setAmount(Double.parseDouble(jsonObject.get("volume").getAsString()));
            order.setTime(OffsetDateTime.parse(jsonObject.get("created_at").getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli());
            order.setFee(Double.parseDouble(jsonObject.get("reserved_fee").getAsString()));
            order.getXattr().put("jsonResponse", jsonObject.toString());

            return order;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ArrayList<Order> cancelOrders(Account account, ArrayList<String> orderIds, HashMap<String, Object> params) throws Exception {
        return null;
    }

    @Override
    public HashMap<String, Double> getPrice(Account account, String[] tickers) throws Exception {
        OkHttpClient client = new OkHttpClient();

        String markets = String.join(", ", tickers);

        Request request = new Request.Builder()
                .url(endpointUrl + "/ticker?markets=" + markets)
                .get()
                .addHeader("accept", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        String responseStr = response.body().string();
        JsonArray jar = JsonParser.parseString(responseStr).getAsJsonArray();
        HashMap<String, Double> listings = new HashMap<>();
        for (int i = 0; i < jar.size(); i++) {
            JsonObject jo = jar.get(i).getAsJsonObject();
            listings.put(jo.get("market").getAsString(), jo.get("trade_price").getAsDouble());
        }

        return listings;
    }


}
