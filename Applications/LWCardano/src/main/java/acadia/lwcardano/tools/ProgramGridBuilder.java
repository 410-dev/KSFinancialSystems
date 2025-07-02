package acadia.lwcardano.tools;

import acadia.lwcardano.Logger;
import acadia.lwcardano.internalization.bybit.objects.GridLine;
import acadia.lwcardano.internalization.bybit.objects.OrderObject;
import acadia.lwcardano.internalization.objects.ConfigurationFile;
import acadia.lwcardano.internalization.utils.OrderLinkIDGen;
import com.bybit.api.client.domain.trade.Side;

import javax.swing.*;
import java.util.LinkedHashMap;

public class ProgramGridBuilder {
    public static LinkedHashMap<String, GridLine> buildGrid(ConfigurationFile cfgFile, int leverage, double currentPrice) {
        LinkedHashMap<String, GridLine> grid = new LinkedHashMap<>();

        // 설정에서 그리드 정보 가져오기
        String[] gridRaw = cfgFile.get("grid", "").split(",");
        if (gridRaw.length < 3) {
            HeadlessDialogs.showMessage("Error: Grid must have at least 3 elements");
            System.exit(0);
        }


        // 설정 그리드 값을 모두 포맷함
        String oLinkIDPrefix = OrderLinkIDGen.generate();
        for (int i = 0; i < gridRaw.length; i++) {
            double gridValue;
            try {
                gridValue = Double.parseDouble(gridRaw[i]);
            } catch (Exception e) {
                e.printStackTrace();
                HeadlessDialogs.showMessage("Error: Grid value must be a real number, but got: [" + gridRaw[i] + "]. For safety, program will terminate.");
                System.exit(0);
                return null;
            }

            // 현재가에 따라 방향 결정
            // 현재가보다 높은 위치의 그리드라면 SHORT 포지션
            // 현재가보다 낮은 위치의 그리드라면 LONG 포지션
            String linkId = oLinkIDPrefix + "-" + i;
            if (i == 0) {
                linkId += "-LOWLIM";
            } else if (i == gridRaw.length - 1) {
                linkId += "-UPLIM";
            }
            GridLine gl = new GridLine(gridRaw[i], linkId, currentPrice < gridValue ? Side.SELL : Side.BUY, Double.parseDouble(cfgFile.get("fund")) / gridValue * leverage, currentPrice < gridValue ? OrderObject.DIRECTION_RISES_TO : OrderObject.DIRECTION_FALLS_TO);
            grid.put(gridRaw[i], gl);
            Logger.log("DEBUG", "Grid data generated: " + gl.getQuantity() + "@" + gl.getPrice());
        }

        // 반환
        return grid;
    }

    public static boolean reconstructGrid(ConfigurationFile cfgFile) {
        return true;
    }
}
