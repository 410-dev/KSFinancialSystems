package acadia.lwcardano.internalization.bybit.objects;

import acadia.lwcardano.internalization.utils.OrderLinkIDGen;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.trade.Side;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Objects;

@Getter
public class PositionObject {
    private final String symbol;
    private final int leverage;
    private final int autoAddMargin;
    private final double avgPrice;
    private final double liqPrice;
    private final double bustPrice;
    private final long riskLimitValue;
    private final String takeProfit;
    private final double positionValue;
    private final boolean isReduceOnly;
    private final String tpslMode;
    private final int riskId;
    private final double trailingStop;
    private final double unrealisedPnl;
    private final double markPrice;
    private final int adlRankIndicator;
    private final double cumRealisedPnl;
    private final double positionMM;
    private final long createdTime;
    private final int positionIdx;
    private final double positionIM;
    private final long seq;
    private final long updatedTime;
    private final String side;
    private final double positionBalance;
    private final String leverageSysUpdatedTime;
    private final double curRealisedPnl;
    private final double qty;
    private final String positionStatus;
    private final String mmrSysUpdatedTime;
    private final String stopLoss;
    private final int tradeMode;
    private final String sessionAvgPrice;

    public PositionObject(LinkedHashMap<String, Object> map) {
        this.symbol = Objects.toString(map.get("symbol"), null);
        this.leverage = parseInt(map.get("leverage"));
        this.autoAddMargin = parseInt(map.get("autoAddMargin"));
        this.avgPrice = parseDouble(map.get("avgPrice"));
        this.liqPrice = parseDouble(map.get("liqPrice"));
        this.bustPrice = parseDouble(Objects.toString(map.get("bustPrice"), null));
        this.riskLimitValue = parseLong(map.get("riskLimitValue"));
        this.takeProfit = Objects.toString(map.get("takeProfit"), null);
        this.positionValue = parseDouble(map.get("positionValue"));
        this.isReduceOnly = parseBoolean(map.get("isReduceOnly"));
        this.tpslMode = Objects.toString(map.get("tpslMode"), null);
        this.riskId = parseInt(map.get("riskId"));
        this.trailingStop = parseDouble(map.get("trailingStop"));
        this.unrealisedPnl = parseDouble(map.get("unrealisedPnl"));
        this.markPrice = parseDouble(map.get("markPrice"));
        this.adlRankIndicator = parseInt(map.get("adlRankIndicator"));
        this.cumRealisedPnl = parseDouble(map.get("cumRealisedPnl"));
        this.positionMM = parseDouble(map.get("positionMM"));
        this.createdTime = parseLong(map.get("createdTime"));
        this.positionIdx = parseInt(map.get("positionIdx"));
        this.positionIM = parseDouble(map.get("positionIM"));
        this.seq = parseLong(map.get("seq"));
        this.updatedTime = parseLong(map.get("updatedTime"));
        this.side = Objects.toString(map.get("side"), null);
        this.positionBalance = parseDouble(map.get("positionBalance"));
        this.leverageSysUpdatedTime = Objects.toString(map.get("leverageSysUpdatedTime"), null);
        this.curRealisedPnl = parseDouble(map.get("curRealisedPnl"));
        this.qty = parseDouble(map.get("size"));
        this.positionStatus = Objects.toString(map.get("positionStatus"), null);
        this.mmrSysUpdatedTime = Objects.toString(map.get("mmrSysUpdatedTime"), null);
        this.stopLoss = Objects.toString(map.get("stopLoss"), null);
        this.tradeMode = parseInt(map.get("tradeMode"));
        this.sessionAvgPrice = Objects.toString(map.get("sessionAvgPrice"), null);
    }

    public OrderObject constructOrderObject(ByBitCredentials c, String category) {
        OrderObject o = new OrderObject(
                c,
                category,
                -1,
                side.equals(Side.SELL.toString()) ? Side.SELL : Side.BUY,
                symbol,
                qty,
                OrderLinkIDGen.generate() + "-CLS",
                0
        );
        return o;
    }

    private static int parseInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String s = Objects.toString(value, "");
        if (s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long parseLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        String s = Objects.toString(value, "");
        if (s.isEmpty()) return 0L;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static double parseDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        String s = Objects.toString(value, "");
        if (s.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static boolean parseBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String s = Objects.toString(value, "").toLowerCase();
        return "true".equals(s);
    }

    @Override
    public String toString() {
        return "PositionObject{symbol=" + symbol + ", leverage=" + leverage + ", avgPrice=" + avgPrice + ", positionValue=" + positionValue + ", qty=" + qty + ", side=" + side + ", updatedTime=" + updatedTime + "}";
    }
}

