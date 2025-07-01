package acadia.lwcardano.tools;

import acadia.lwcardano.Logger;
import acadia.lwcardano.internalization.bybit.Orders;
import acadia.lwcardano.internalization.bybit.objects.ByBitCredentials;
import acadia.lwcardano.internalization.bybit.objects.OrderObject;
import acadia.lwcardano.internalization.bybit.objects.PositionObject;
import com.bybit.api.client.domain.trade.Side;
import me.hysong.files.ConfigurationFile;

import java.math.BigDecimal;
import java.util.HashMap;

public class ActionHooks {

    public static boolean onLowerLimitBreak(ByBitCredentials cred, ConfigurationFile cfgFile) {
        return true;
    }

    public static boolean onUpperLimitBreak(ByBitCredentials cred, ConfigurationFile cfgFile) {
        return true;
    }

    public static boolean onTerminate(ByBitCredentials cred, ConfigurationFile cfgFile) {
        return true;
    }

    public static boolean onError(ByBitCredentials cred, ConfigurationFile cfgFile) {
        return true;
    }

    public static boolean onLimitBreak(ByBitCredentials cred, ConfigurationFile cfgFile) {
        Logger.log("상/하방 한계점 돌파 작업 후크 실행...");
        Logger.log("주문 전량 취소중...");
        String category = cfgFile.get("market", "FUTURES");
        String symbol = cfgFile.get("symbol", "BTCUSDT");
        Orders.cancelAllOrders(cred, category, symbol);
        Logger.log("포지션 불러오는중...");
        HashMap<String, PositionObject> positions = Orders.getCurrentPosition(cred, category, symbol);
        PositionObject currentPosition = positions.get(symbol);
        OrderObject o = currentPosition.constructOrderObject(cred, category);
        Logger.log("포지션 정리중...");
        o.setSide(currentPosition.getSide().equals(Side.SELL.toString()) ? Side.BUY.toString() : Side.SELL.toString()); // Reverse side
        o.setPrice(new BigDecimal(-1)); // Throw at market
        return o.placeOrder();
    }

    public static boolean onStart(ByBitCredentials cred, ConfigurationFile cfgFile) {
        return true;
    }
}
