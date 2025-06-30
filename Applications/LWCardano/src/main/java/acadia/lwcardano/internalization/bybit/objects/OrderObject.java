package acadia.lwcardano.internalization.bybit.objects;

import acadia.lwcardano.internalization.bybit.Orders;
import com.bybit.api.client.domain.trade.Side;
import lombok.Getter;
import lombok.Setter;
import me.hysong.files.ConfigurationFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class OrderObject {

    private final ByBitCredentials credentials;

    private final String category;

    private final String orderId;
    private final String orderLinkId;
    private final String blockTradeId;
    private final String symbol;

    private final BigDecimal price;
    private final BigDecimal qty;
    private final String side;
    private final boolean isLeverage;
    private final int positionIdx;

    private final String orderStatus;
    private final String cancelType;
    private final String rejectReason;

    private final BigDecimal avgPrice;
    private final BigDecimal leavesQty;
    private final BigDecimal leavesValue;
    private final BigDecimal cumExecQty;
    private final BigDecimal cumExecValue;
    private final BigDecimal cumExecFee;

    private final String timeInForce;
    private final String orderType;
    private final String stopOrderType;

    private final BigDecimal triggerPrice;
    private final BigDecimal takeProfit;
    private final BigDecimal stopLoss;
    private final String tpTriggerBy;
    private final String slTriggerBy;

    private final int triggerDirection;
    private final String triggerBy;
    private final String lastPriceOnCreated;

    private final boolean reduceOnly;
    private final boolean closeOnTrigger;

    private final String smpType;
    private final int smpGroup;
    private final String smpOrderId;

    private final String tpslMode;
    private final String tpLimitPrice;
    private final String slLimitPrice;
    private final String placeType;

    private final Instant createdTime;
    private final Instant updatedTime;

    private final boolean isResponse;

    public static final int DIRECTION_RISES_TO = 1;
    public static final int DIRECTION_FALLS_TO = 2;

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


    public boolean open() {
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
                "orderId='" + orderId + '\'' +
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
