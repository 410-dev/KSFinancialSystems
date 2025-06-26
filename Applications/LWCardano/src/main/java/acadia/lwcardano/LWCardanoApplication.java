package acadia.lwcardano;

import acadia.lwcardano.tools.AutoGridBuilder;
import me.hysong.files.ConfigurationFile;
import me.hysong.files.File2;

import javax.swing.*;
import java.util.Arrays;

public class LWCardanoApplication {
    public static void main(String[] args) {

        // Check startup parameters
        // - Config file check
        String cfgPath = Arrays.stream(args)
                .filter(s -> s.startsWith("--cfg=")).findAny().orElse("");

        // - Grid build mode check
        boolean notGridBuildMode = Arrays.stream(args)
                .filter(s -> s.equals("--gridgen")).findAny().orElse("").isEmpty();


        if (cfgPath.isEmpty()) {
            System.err.println("Error: Configuration file not passed in as argument: Requires --cfg=<config file>");
            JOptionPane.showMessageDialog(null, "Error: Configuration file not passed in as argument: Requires --cfg=<config file>");
            System.exit(0);
            return;
        }

        // Load settings
        cfgPath = cfgPath.substring(cfgPath.indexOf("=") + 1);
        ConfigurationFile cfgFile = new File2(cfgPath).configFileMode().load();

        // Grid build mode
        if (!notGridBuildMode) {
            AutoGridBuilder.make(cfgFile);
        }


        /*
        작동 알고리즘 메모

        초기 셋팅
        1. buildGrid(currentPrice) 로 현재 적정 그리드 데이터를 계산한다.
            이 때, 리턴값은 <String, GridLine> 해시맵이다. (GridLine: 가격, OrderLinkID, Side 를 포함)
        2. 현재 걸려있는 예약을 모두 취소한다. (계정에서 불러온 후 iterative 로 전량 취소)
        3. 해시맵에 따라 전부 그리드를 예약한다

        반복 루프
        1. 대기 조건: 그리드 하나가 돌파되어 거래가 체결 될 때 까지 대기.
         -> 대기 종료시: 체결된 해당 그리드는 없어짐을 보장했다.

        2. 기존 해시맵을 모두 스캔하며 체결된 그리드를 확인한다
        ->  만약 트리거 인덱스가 0 이상인 그리드가 체결됐다면 (10번으로 점프)
        ->  만약 체결된 그리드가 Upper / Lower 리미트에 해당한다면 (n 번으로 점프)
        ->  그렇지 않다면 3번으로 점프

        3. 트리거 인덱스를 1 증가시킨다
        4. 체결된 그리드 라인에 flip() 을 트리거 한다 (데이터 기록)
        ->  플립 트리거시 트리거 인덱스가 0 이상이 된다 (!!!중요!!!)
        5. 주문 양을 현재 누적 체결량 + 일반 주문양 으로 전환한다
        // Scene complete, loop repeat


        10. 트리거 인덱스를 0으로 초기화 한다
        11. 체결된 그리드 데이터를 기억한다
         */
    }
}
