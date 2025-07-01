package acadia.lwcardano.internalization.bybit.objects;

import acadia.lwcardano.internalization.bybit.Orders;
import com.bybit.api.client.domain.trade.Side;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class OrderObject {

    private ByBitCredentials credentials;

    private String category;

    private String orderId;
    private String orderLinkId;
    private String blockTradeId;
    private String symbol;

    private BigDecimal price;
    private BigDecimal qty;
    private String side;
    private boolean isLeverage;
    private int positionIdx;

    private String orderStatus;
    private String cancelType;
    private String rejectReason;

    private BigDecimal avgPrice;
    private BigDecimal leavesQty;
    private BigDecimal leavesValue;
    private BigDecimal cumExecQty;
    private BigDecimal cumExecValue;
    private BigDecimal cumExecFee;

    private String timeInForce;
    private String orderType;
    private String stopOrderType;

    private BigDecimal triggerPrice;
    private BigDecimal takeProfit;
    private BigDecimal stopLoss;
    private String tpTriggerBy;
    private String slTriggerBy;

    private int triggerDirection;
    private String triggerBy;
    private String lastPriceOnCreated;

    private boolean reduceOnly;
    private boolean closeOnTrigger;

    private String smpType;
    private int smpGroup;
    private String smpOrderId;

    private String tpslMode;
    private String tpLimitPrice;
    private String slLimitPrice;
    private String placeType;

    private Instant createdTime;
    private Instant updatedTime;

    private boolean isResponse;

    public static int DIRECTION_RISES_TO = 1;
    public static int DIRECTION_FALLS_TO = 2;

    public OrderObject(ByBitCredentials c, HashMap<String, Object> o) {
        this.credentials = c;

        this.orderId           = getString(o, "orderId");
        this.orderLinkId       = getString(o, "orderLinkId").replace("-RST-RST", "-RST");
        this.blockTradeId      = getString(o, "blockTradeId");
        this.symbol            = getString(o, "symbol");
        this.category          = getString(o, "category");

        this.price             = getBigDecimal(o, "price");
        this.qty               = getBigDecimal(o, "qty");
        this.side              = getString(o, "side");
        this.isLeverage        = getBoolean(o, "isLeverage");
        this.positionIdx       = getInt(o, "positionIdx");

        this.orderStatus       = getString(o, "orderStatus");
        this.cancelType        = getString(o, "cancelType");
        this.rejectReason      = getString(o, "rejectReason");

        this.avgPrice          = getBigDecimal(o, "avgPrice");
        this.leavesQty         = getBigDecimal(o, "leavesQty");
        this.leavesValue       = getBigDecimal(o, "leavesValue");
        this.cumExecQty        = getBigDecimal(o, "cumExecQty");
        this.cumExecValue      = getBigDecimal(o, "cumExecValue");
        this.cumExecFee        = getBigDecimal(o, "cumExecFee");

        this.timeInForce       = getString(o, "timeInForce");
        this.orderType         = getString(o, "orderType");
        this.stopOrderType     = getString(o, "stopOrderType");

        this.triggerPrice      = getBigDecimal(o, "triggerPrice");
        this.takeProfit        = getBigDecimal(o, "takeProfit");
        this.stopLoss          = getBigDecimal(o, "stopLoss");
        this.tpTriggerBy       = getString(o, "tpTriggerBy");
        this.slTriggerBy       = getString(o, "slTriggerBy");

        this.triggerDirection  = getInt(o, "triggerDirection");
        this.triggerBy         = getString(o, "triggerBy");
        this.lastPriceOnCreated= getString(o, "lastPriceOnCreated");

        this.reduceOnly        = getBoolean(o, "reduceOnly");
        this.closeOnTrigger    = getBoolean(o, "closeOnTrigger");

        this.smpType           = getString(o, "smpType");
        this.smpGroup          = getInt(o, "smpGroup");
        this.smpOrderId        = getString(o, "smpOrderId");

        this.tpslMode          = getString(o, "tpslMode");
        this.tpLimitPrice      = getString(o, "tpLimitPrice");
        this.slLimitPrice      = getString(o, "slLimitPrice");
        this.placeType         = getString(o, "placeType");

        this.createdTime       = getInstant(o, "createdTime");
        this.updatedTime       = getInstant(o, "updatedTime");

        this.isResponse        = true;
    }

    public OrderObject(ByBitCredentials c, String category, double price, Side side, String symbol, double quantity, String orderLinkId, int triggerDirection) {
        this.credentials = c;

        this.price = new BigDecimal(price);
        this.side = side.toString();
        this.symbol = symbol;
        this.qty = new BigDecimal(quantity);
        this.orderLinkId = orderLinkId.replace("-RST-RST", "-RST");
        this.category = category;


        this.orderId           = orderLinkId;
        this.blockTradeId      = null;

        this.isLeverage        = false;
        this.positionIdx       = -1;

        this.orderStatus       = null;
        this.cancelType        = null;
        this.rejectReason      = null;

        this.avgPrice          = null;
        this.leavesQty         = null;
        this.leavesValue       = null;
        this.cumExecQty        = null;
        this.cumExecValue      = null;
        this.cumExecFee        = null;

        this.timeInForce       = null;
        this.orderType         = null;
        this.stopOrderType     = null;

        this.triggerPrice      = null;
        this.takeProfit        = null;
        this.stopLoss          = null;
        this.tpTriggerBy       = null;
        this.slTriggerBy       = null;

        this.triggerDirection  = triggerDirection;
        this.triggerBy         = null;
        this.lastPriceOnCreated= null;

        this.reduceOnly        = false;
        this.closeOnTrigger    = false;

        this.smpType           = null;
        this.smpGroup          = -1;
        this.smpOrderId        = null;

        this.tpslMode          = null;
        this.tpLimitPrice      = null;
        this.slLimitPrice      = null;
        this.placeType         = null;

        this.createdTime       = null;
        this.updatedTime       = null;

        this.isResponse        = false;
    }

    public boolean canOpen() {
        return !isResponse();
    }

    public boolean isOpen() {
        if (isResponse()) {
            return (orderStatus.equalsIgnoreCase("New") || orderStatus.equalsIgnoreCase("PartiallyFilled") || orderStatus.equalsIgnoreCase("Untriggered"));
        }
        return false;
    }


    public boolean placeOrder() {
        if (!this.canOpen()) {
            throw new RuntimeException("Unable to create order - current object is from response.");
        }
        return Orders.placeOrder(credentials, price.doubleValue(), Side.valueOf(side), category, symbol, qty.doubleValue(), orderLinkId);
    }

    public String cancel() {
        return Orders.cancelOrder(credentials, category, symbol, this.orderId);
    }

    public long getCreatedTimeAsLong() {
        return Long.parseLong(String.valueOf(getCreatedTime()));
    }

    public int getInvertedTriggerDirection() {
        return getTriggerDirection() == DIRECTION_FALLS_TO ? DIRECTION_RISES_TO : DIRECTION_FALLS_TO;
    }

    public Side getInvertedSide() {
        return getSide().equalsIgnoreCase(Side.SELL.toString()) ? Side.BUY : Side.SELL;
    }

    private static String getString(Map<String, Object> o, String key) {
        Object val = o.get(key);
        return val != null ? val.toString() : "";
    }

    private static BigDecimal getBigDecimal(Map<String, Object> o, String key) {
        String str = getString(o, key);
        try {
            return str.isEmpty() ? BigDecimal.ZERO : new BigDecimal(str);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static boolean getBoolean(Map<String, Object> o, String key) {
        Object val = o.get(key);
        if (val == null) return false;
        String str = val.toString();
        return !str.isEmpty() && Boolean.parseBoolean(str);
    }

    private static int getInt(Map<String, Object> o, String key) {
        Object val = o.get(key);
        if (val == null) return 0;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Instant getInstant(Map<String, Object> o, String key) {
        String str = getString(o, key);
        try {
            long epoch = str.isEmpty() ? 0L : Long.parseLong(str);
            return Instant.ofEpochMilli(epoch);
        } catch (NumberFormatException e) {
            return Instant.EPOCH;
        }
    }

    @Override
    public String toString() {
        return "OrderObject{" +
                "orderLinkId=" + orderLinkId +
                ", orderId='" + orderId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", side='" + side + '\'' +
                ", price=" + price +
                ", qty=" + qty +
                ", orderStatus='" + orderStatus + '\'' +
                ", createdTime=" + createdTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderObject that = (OrderObject) o;
        return orderId.equals(that.orderId) || orderLinkId.equals(that.orderLinkId);
    }
}
