package acadia.lwcardano.internalization.bybit;

import acadia.lwcardano.Logger;
import acadia.lwcardano.internalization.bybit.objects.ByBitCredentials;
import acadia.lwcardano.internalization.bybit.objects.OrderObject;
import acadia.lwcardano.internalization.bybit.objects.PositionObject;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.position.request.PositionDataRequest;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.request.BatchOrderRequest;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.restApi.BybitApiPositionRestClient;
import com.bybit.api.client.restApi.BybitApiTradeRestClient;

import java.util.*;

import static acadia.lwcardano.internalization.bybit.APIResponseConverter.asListOfObj;
import static acadia.lwcardano.internalization.bybit.APIResponseConverter.asObj;

public class Orders {

    public static boolean placeOrder(ByBitCredentials credentials, String market, String fundStr, String symbol, double price, Side side, String orderSID) {
        double fund = Double.parseDouble(fundStr);
        double amountBTC = Market.calculateExchange(credentials, market, symbol, true, fund);

        return placeOrder(credentials, price, side, market, symbol, amountBTC, orderSID);
    }

    public static boolean placeOrder(ByBitCredentials credentials, double price, Side side, String category, String symbol, double amountBTC, String orderSID) {
        BybitApiTradeRestClient client = ClientFactory.getAuthorizedTradeClient(credentials).newTradeRestClient();

        TradeOrderRequest order = TradeOrderRequest.builder()
                .category(category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT)
                .symbol(symbol)
                .side(side)
                .orderType(TradeOrderType.LIMIT)
                .qty(String.format("%.3f", amountBTC))
                .orderLinkId(orderSID.replace("-RST-RST", "-RST"))
                .price(String.valueOf(price))
                .build();

        Logger.log("INFO", "[" + orderSID + "] 주문 요청중...");
        LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) client.createOrder(order);
        Logger.log("DEBUG", "[" + orderSID + "] 서버 응답: " + response);
        return (int) response.get("retCode") == 0;
    }

//    public static LinkedHashMap<OrderObject, Boolean> placeBatchOrder(ByBitCredentials credentials, String category, ArrayList<OrderObject> orders) {
//        BybitApiTradeRestClient client = ClientFactory.getAuthorizedTradeClient(credentials).newTradeRestClient();
//        List<TradeOrderRequest> requests = new ArrayList<>();
//        CategoryType categoryType = category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT;
//        for (OrderObject order : orders) {
//            TradeOrderRequest tr = TradeOrderRequest.builder()
//                    .category(categoryType)
//                    .symbol(order.getSymbol())
//                    .side(Side.valueOf(order.getSide()))
//                    .orderType(TradeOrderType.LIMIT)
//                    .qty(String.valueOf(order.getQty()))
//                    .price(String.valueOf(order.getPrice()))
//                    .orderLinkId(order.getOrderLinkId())
//                    .triggerDirection(order.getTriggerDirection())
//                    .build();
//            requests.add(tr);
//        }
//        BatchOrderRequest br = BatchOrderRequest.builder().category(categoryType).request(requests).build();
//        LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) client.createBatchOrder(br);
//
//        // 반환값 체크
//        ArrayList<LinkedHashMap<String, Object>> retExtInfoList = asListOfObj(asObj(response.get("retExtInfo")).get("list"));
//        ArrayList<LinkedHashMap<String, Object>> resultList = asListOfObj(asObj(response.get("result")).get("list"));
//
//        // 반환 데이터 준비
//        LinkedHashMap<OrderObject, Boolean> result = new LinkedHashMap<>();
//        for (int i = 0; i < retExtInfoList.size(); i++) {
//            LinkedHashMap<String, Object> retExtInfo = retExtInfoList.get(i);
//            LinkedHashMap<String, Object> resultInfo = resultList.get(i);
//
//            // retExtInfo 에 주로 category 값이 없으나 오브젝트 생성시 참조함. 따라서 이는 추가 되어야 함
//            retExtInfo.put("category", categoryType);
//
//            // 상태 체크
//            // code 값이 0이거나, msg 값이 "OK" 여야 함
//            OrderObject o = new OrderObject(credentials, retExtInfo);
//            if (((int) resultInfo.get("code")) == 0 || resultInfo.getOrDefault("msg", "NOT_OK").equals("OK")) {
//                result.put(o, true);
//            } else {
//                result.put(o, false);
//            }
//        }
//
//        return result;
//    }

    public static LinkedHashMap<OrderObject, Boolean> placeBatchOrder(ByBitCredentials credentials, String category, ArrayList<OrderObject> orders) {
        BybitApiTradeRestClient client = ClientFactory.getAuthorizedTradeClient(credentials).newTradeRestClient();

        CategoryType categoryType = category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT;

        // 한 번에 보낼 최대 주문 수
        int ordersPerRequest = 10;

        LinkedHashMap<OrderObject, Boolean> result = new LinkedHashMap<>();
        boolean allSuccess = true;

        for (int i = 0; i < orders.size(); i += ordersPerRequest) {
            // 현재 배치에 포함될 주문 리스트 준비
            List<TradeOrderRequest> batchReqs = new ArrayList<>();
            List<OrderObject> subOrders = orders.subList(i, Math.min(i + ordersPerRequest, orders.size()));

            for (OrderObject order : subOrders) {
                TradeOrderRequest tr = TradeOrderRequest.builder()
                        .category(categoryType)
                        .symbol(order.getSymbol())
                        .side(Side.valueOf(order.getSide()))
                        .orderType(TradeOrderType.LIMIT)
                        .qty(String.format("%.3f", order.getQty()))
                        .price(String.valueOf(order.getPrice()))
                        .orderLinkId(order.getOrderLinkId().replace("-RST-RST", "-RST"))
                        .triggerDirection(order.getTriggerDirection())
                        .build();
                batchReqs.add(tr);
            }

            // Batch 주문 실행
            BatchOrderRequest br = BatchOrderRequest.builder()
                    .category(categoryType)
                    .request(batchReqs)
                    .build();
            LinkedHashMap<String, Object> resp = (LinkedHashMap<String, Object>) client.createBatchOrder(br);
            Logger.log("DEBUG", "placeBatchOrder() got response: " + resp);

            // 반환값 체크
            ArrayList<LinkedHashMap<String, Object>> retExtInfoList = asListOfObj(asObj(resp.get("retExtInfo")).get("list"));
            ArrayList<LinkedHashMap<String, Object>> resultList = asListOfObj(asObj(resp.get("result")).get("list"));

            // 각 주문별 성공 여부 기록
            for (int j = 0; j < retExtInfoList.size(); j++) {
                LinkedHashMap<String, Object> ext = retExtInfoList.get(j);
                LinkedHashMap<String, Object> info = resultList.get(j);

                // OrderObject 생성 시 category 추가 필요
                ext.put("category", categoryType);

                OrderObject o = new OrderObject(credentials, ext);
                boolean success = ((int) ext.get("code") == 0) || "OK".equals(ext.getOrDefault("msg", ""));
                result.put(o, success);

                if (!success) {
                    allSuccess = false;
                }
            }

            // 다음 배치 전 속도 제한 적용
            if (i + ordersPerRequest < orders.size()) {
                Logger.log("INFO", "API 콜 제한 대기중: 1000 밀리초... (opr:" + ordersPerRequest + ", i:" + i + ", ods:" + orders.size() + ")");
                try {Thread.sleep(1000);} catch (Exception ignored) {}
            }

            // 하나라도 실패 시 더 이상 배치 진행하지 않음
            if (!allSuccess) {
                break;
            }
        }

        return result;
    }


    public static String cancelOrder(ByBitCredentials credentials, String category, String symbol, String orderSID) {
        BybitApiTradeRestClient client = ClientFactory.getAuthorizedTradeClient(credentials).newTradeRestClient();

        TradeOrderRequest order = TradeOrderRequest.builder()
                .category(category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT)
                .symbol(symbol)
                .orderLinkId(orderSID.replace("-RST-RST", "-RST"))
                .build();

        Logger.log("INFO", "[" + orderSID + "] 주문 요청중...");
        LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) client.cancelOrder(order);
        Logger.log("DEBUG", "[" + orderSID + "] 서버 응답: " + response);
        return (int) response.get("retCode") == 0 ? "" : response.get("retMsg").toString();
    }

    public static boolean cancelBatchOrder(ByBitCredentials credentials, String category, ArrayList<OrderObject> orders) {
        BybitApiTradeRestClient client = ClientFactory.getAuthorizedTradeClient(credentials).newTradeRestClient();
        CategoryType categoryType = category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT;

        boolean allSuccess = true;
        int ordersPerRequest = 10;
        int requestsPerSecond = 5;

        for (int i = 0; i < orders.size(); i += ordersPerRequest) {
            List<TradeOrderRequest> requests = new ArrayList<>();
            for (OrderObject order : orders) {
                TradeOrderRequest tr = TradeOrderRequest.builder()
                        .category(categoryType)
                        .symbol(order.getSymbol())
                        .orderLinkId(order.getOrderLinkId().replace("-RST-RST", "-RST"))
                        .build();
                requests.add(tr);
            }
            BatchOrderRequest br = BatchOrderRequest.builder().category(categoryType).request(requests).build();
            LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) client.cancelBatchOrder(br);

            // Check if all orders were cancelled successfully
            ArrayList<LinkedHashMap<String, Object>> retExtInfoList = asListOfObj(asObj(response.get("retExtInfo")).get("list"));
            ArrayList<LinkedHashMap<String, Object>> resultList = asListOfObj(asObj(response.get("result")).get("list"));

            for (int j = 0; j < retExtInfoList.size(); j++) {
                LinkedHashMap<String, Object> resultInfo = resultList.get(j);
                if (((int) resultInfo.get("code")) != 0 && !resultInfo.getOrDefault("msg", "NOT_OK").equals("OK")) {
                    allSuccess = false;
                    break;
                }
            }
            if (!allSuccess) break;

            Logger.log("INFO", "Making delay... (i:" + i + "  opr:" + ordersPerRequest + "  ods:" + orders.size() + ")");
            if (i + ordersPerRequest < orders.size()) {
                Logger.log("INFO", "Making delay... (i:" + i + "  opr:" + ordersPerRequest + "  ods:" + orders.size() + ")");
                try {Thread.sleep(1000/requestsPerSecond);} catch (Exception ignored) {}
            }
        }
        return allSuccess;
    }

    public static boolean cancelAllOrders(ByBitCredentials credentials, String category, String symbol) {
        BybitApiTradeRestClient client = ClientFactory.getAuthorizedTradeClient(credentials).newTradeRestClient();

        TradeOrderRequest request = TradeOrderRequest.builder()
                .category(category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT)
                .symbol(symbol)
                .build();

        LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) client.cancelAllOrder(request);
        Logger.log("DEBUG", "Cancel all orders response: " + response);
        return (int) response.get("retCode") == 0 || response.getOrDefault("retMsg", "NOT_OK").equals("OK");
    }

//    public static ArrayList<OrderObject> enumerateOrderHistory(ByBitCredentials credentials, String category, String symbol, int limit) {
//        BybitApiTradeRestClient client = ClientFactory.getAuthorizedTradeClient(credentials).newTradeRestClient();
//
//        String nextPageCursor = "";
//        ArrayList<OrderObject> orders = new ArrayList<>();
//
//        while (orders.size() < limit) {
//
//            CategoryType categoryType = category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT;
//            TradeOrderRequest order = TradeOrderRequest.builder()
//                    .category(categoryType)
//                    .symbol(symbol)
//                    .limit(Math.max(50, limit))
//                    .cursor(nextPageCursor)
//                    .build();
//
//            LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) client.getOpenOrders(order);
//
//            ArrayList<LinkedHashMap<String, Object>> trades = asListOfObj(asObj(response.get("result")).get("list"));
//            for (LinkedHashMap<String, Object> trade : trades) {
//                trade.put("category", categoryType);
//                orders.add(new OrderObject(credentials, trade));
//            }
//
//            nextPageCursor = asObj(response.get("result")).get("nextPageCursor").toString();
//
//        }
//
//        return orders;
//    }

    public static ArrayList<OrderObject> enumerateOrderHistory(ByBitCredentials credentials, String category, String symbol, int limit) {
        BybitApiTradeRestClient client = ClientFactory.getAuthorizedTradeClient(credentials).newTradeRestClient();

        String nextPageCursor = "";
        ArrayList<OrderObject> orders = new ArrayList<>();

        while (orders.size() < limit) {

            CategoryType categoryType = category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT;
            TradeOrderRequest order = TradeOrderRequest.builder()
                    .category(categoryType)
                    .symbol(symbol)
                    .limit(Math.min(50, limit))
                    .cursor(nextPageCursor)
                    .build();

            LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) client.getOrderHistory(order);

            ArrayList<LinkedHashMap<String, Object>> trades = asListOfObj(asObj(response.get("result")).get("list"));
            for (LinkedHashMap<String, Object> trade : trades) {
                trade.put("category", categoryType);
                orders.add(new OrderObject(credentials, trade));
            }

            nextPageCursor = asObj(response.get("result")).get("nextPageCursor").toString();

        }

        return orders;
    }

    public static HashMap<String, PositionObject> getCurrentPosition(ByBitCredentials credentials, String category, String symbol) {
        BybitApiPositionRestClient client = ClientFactory.getAuthorizedTradeClient(credentials).newPositionRestClient();

        CategoryType ct = category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT;
        PositionDataRequest pdr = PositionDataRequest.builder()
                .category(ct)
                .symbol(symbol)
                .build();
        LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) client.getPositionInfo(pdr);
        ArrayList<LinkedHashMap<String, Object>> positionsListRaw = asListOfObj(asObj(result.get("result")).get("list"));
        HashMap<String, PositionObject> positions = new HashMap<>();

        for (LinkedHashMap<String, Object> positionRaw : positionsListRaw) {
            positions.put((String) positionRaw.get("symbol"), new PositionObject(positionRaw));
        }

        return positions;
    }

    public static boolean setRemoteLeverage(ByBitCredentials credentials, String category, String symbol, int leverage) {
        BybitApiPositionRestClient client = ClientFactory.getAuthorizedTradeClient(credentials).newPositionRestClient();
        PositionDataRequest drq = PositionDataRequest.builder()
                .category(category.equals("FUTURE") ? CategoryType.LINEAR : CategoryType.SPOT)
                .symbol(symbol)
                .buyLeverage(String.valueOf(leverage))
                .sellLeverage(String.valueOf(leverage))
                .build();
        LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) client.setPositionLeverage(drq);
        Logger.log("DEBUG", "setRemoteLeverage() accepted API response: " + result);
        return "OK".equals(result.get("retMsg")) || 0 == Integer.parseInt(result.get("retCode").toString())
                || "leverage not modified".equals(result.get("retMsg")) || 110043 == Integer.parseInt(result.get("retCode").toString());

    }
}
