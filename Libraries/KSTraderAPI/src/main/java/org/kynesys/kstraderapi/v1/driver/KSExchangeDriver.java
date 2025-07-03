package org.kynesys.kstraderapi.v1.driver;

import org.kynesys.kstraderapi.v1.objects.KSGenericAuthorizationObject;
import org.kynesys.kstraderapi.v1.objects.Chart;
import org.kynesys.kstraderapi.v1.objects.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface KSExchangeDriver {

    /**
     * Get chart data
     * @param KSGenericAuthorizationObject Account that contains credentials
     * @param params Parameters for the chart data.
     *               Keys may require:
     *               - symbol: The symbol to get chart data for.
     *               - interval: The interval for thechart data.
     *               - startTime: The start time for the chart data.
     *               - endTime: The end time for the chart data.
     *               - limit: The limit for the chart data.
     * @return A chart object representing the chart data.
     * @throws Exception if an error occurs while getting chart data
     */
    Chart getChart(KSGenericAuthorizationObject KSGenericAuthorizationObject, HashMap<String, Object> params) throws Exception;

    /**
     * Get open orders
     * @param auth Account that contains credentials
     * @param params Parameters for the orders.
     *               Keys may require:
     *               - symbol: The symbol to get orders for.
     *               - limit: The limit for the orders.
     * @return An Array of Order objects representing the orders.
     * @throws Exception if an error occurs while getting orders
     */
    ArrayList<Order> getOpenOrders(KSGenericAuthorizationObject auth, HashMap<String, Object> params) throws Exception;

    default ArrayList<Order> getOpenOrders(KSGenericAuthorizationObject auth, String symbol, int limit) throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        params.put("symbol", symbol);
        params.put("limit", limit);
        return getOpenOrders(auth, params);
    }

    /**
     * Get closed orders
     * @param KSGenericAuthorizationObject Account that contains credentials
     * @param params Parameters for the orders.
     *               Keys may require:
     *               - symbol: The symbol to get orders for.
     *               - limit: The limit for the orders.
     * @return An Array of Order objects representing the orders.
     * @throws Exception if an error occurs while getting orders
     */
    ArrayList<Order> getClosedOrders(KSGenericAuthorizationObject KSGenericAuthorizationObject, HashMap<String, Object> params) throws Exception;

    /**
     * Get account information
     * @param KSGenericAuthorizationObject Account that contains credentials
     * @return An Account object representing the account information.
     * @throws Exception if an error occurs while getting account information
     */
    KSGenericAuthorizationObject getAccount(KSGenericAuthorizationObject KSGenericAuthorizationObject) throws Exception;

    /**
     * Make a trade
     * @param KSGenericAuthorizationObject Account that contains credentials
     * @param order Orders to be placed.
     * @param params Parameters for the trade.
     * @return An Array of Order objects representing the orders.
     * @throws Exception if an error occurs while making a trade
     */
    Order placeOrder(KSGenericAuthorizationObject KSGenericAuthorizationObject, Order order, HashMap<String, Object> params) throws Exception;

    /**
     * Place orders as batch
     * @param KSGenericAuthorizationObject Account that contains credentials
     * @param orders Orders to be placed.
     * @param params Parameters for the trade.
     * @return An Array of Order objects representing the orders.
     * @throws Exception if an error occurs while placing orders
     */
    ArrayList<Order> placeOrders(KSGenericAuthorizationObject KSGenericAuthorizationObject, ArrayList<Order> orders, HashMap<String, Object> params) throws Exception;

    /**
     * Cancel an order
     * @param KSGenericAuthorizationObject Account that contains credentials
     * @param orderId The ID of the order to be canceled.
     * @param params Parameters for the cancelation.
     * @return An Order object representing the canceled order.
     * @throws Exception if an error occurs while canceling an order
     */
    Order cancelOrder(KSGenericAuthorizationObject KSGenericAuthorizationObject, String orderId, HashMap<String, Object> params) throws Exception;

    /**
     * Cancel order as batch
     * @param KSGenericAuthorizationObject Account that contains credentials
     * @param orderIds The IDs of the orders to be canceled.
     * @param params Parameters for the cancelation.
     * @return An Array of Order objects representing the canceled orders.
     * @throws Exception if an error occurs while canceling orders
     */
    ArrayList<Order> cancelOrders(KSGenericAuthorizationObject KSGenericAuthorizationObject, ArrayList<String> orderIds, HashMap<String, Object> params) throws Exception;

    /**
     * Get price snapshot
     * @param KSGenericAuthorizationObject Account that contains credentials
     * @param tickers Containing tickers to check
     */
    HashMap<String, Double> getPrice(KSGenericAuthorizationObject KSGenericAuthorizationObject, String[] tickers) throws Exception;
}

