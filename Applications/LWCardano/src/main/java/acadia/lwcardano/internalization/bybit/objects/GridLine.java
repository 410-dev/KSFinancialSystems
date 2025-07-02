package acadia.lwcardano.internalization.bybit.objects;

import acadia.lwcardano.internalization.objects.ConfigurationFile;
import com.bybit.api.client.domain.trade.Side;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Getter
public class GridLine implements Serializable {
    private final String price;
    private final String orderLinkId;
    private double quantity;
    private final int triggerDirection;
    private Side side;
    private final LinkedHashMap<Long, Side> events = new LinkedHashMap<>(); // 돌파 당시의 방향을 기록
    private int lastTriggeredIndex = Integer.MIN_VALUE;

    public GridLine(String price, String orderLinkId, Side side, double quantity, int triggerDirection) {
        this.price = price;
        this.orderLinkId = orderLinkId;
        this.side = side;
        this.quantity = quantity;
        this.triggerDirection = triggerDirection;
    }

    public OrderObject constructOrderObject(ByBitCredentials c, ConfigurationFile config) {
        return new OrderObject(c, config.get("market"), Double.parseDouble(price), side, config.get("symbol"), quantity, orderLinkId, triggerDirection);
    }

    public void flip(long time, int triggerIndex) {
        events.put(time, side); // 플립이 일어나는 이유는 돌파 되었다는 뜻이므로 돌파를 기록
        lastTriggeredIndex = triggerIndex;
        if (side == Side.BUY) {
            side = Side.SELL;
        } else {
            side = Side.BUY;
        }
    }

    public GridLine makeCopy() {
        return new GridLine(price, orderLinkId, side, quantity, triggerDirection);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GridLine) {
            return ((GridLine) o).price.equals(price) || ((GridLine) o).orderLinkId.equals(orderLinkId);
        }
        return false;
    }
}
