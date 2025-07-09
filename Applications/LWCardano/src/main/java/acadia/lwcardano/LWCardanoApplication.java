package acadia.lwcardano;

import acadia.lwcardano.internalization.bybit.Market;
import acadia.lwcardano.internalization.bybit.Orders;
import acadia.lwcardano.internalization.bybit.objects.ByBitCredentials;
import acadia.lwcardano.internalization.bybit.objects.GridLine;
import acadia.lwcardano.internalization.bybit.objects.OrderObject;
import acadia.lwcardano.internalization.bybit.objects.PositionObject;
import acadia.lwcardano.internalization.objects.ConfigurationFile;
import acadia.lwcardano.internalization.objects.File2;
import acadia.lwcardano.tools.ActionHooks;
import acadia.lwcardano.tools.AutoGridBuilder;
import acadia.lwcardano.tools.HeadlessDialogs;
import acadia.lwcardano.tools.ProgramGridBuilder;
import com.bybit.api.client.domain.trade.Side;
import lombok.extern.java.Log;

import javax.swing.*;
import java.util.*;

import static acadia.lwcardano.tools.UnpreciseFloatComparer.isAlmostEqual;

public class LWCardanoApplication {

    public static LinkedHashMap<String, GridLine> grid = new LinkedHashMap<>();
    public static double lowerLimit = Double.MAX_VALUE;
    public static double upperLimit = Double.MIN_VALUE;
    public static ConfigurationFile cfg;
    public static ArrayList<OrderObject> orderHistory = new ArrayList<>();
    public static int MAX_ORDER_HISTORY_LENGTH = 100;
    public static int CLEAN_ORDER_HISTORY = 50;
    public static OrderObject resetTriggerOrder;
    private static ByBitCredentials credentials = null;

    public static boolean debugMode = false;
    public static boolean verbose = false;
    public static String build = "2 5 J 0 9 B 1"; // 앞 2글자: 연도, 다음 1글자: 월의 첫 글자, 다음 2글자: 날짜, 다음 1글자: 월이 겹치면 알파벳 하나 증가 (April, August 등), 다음 1글자: 리비젼 16진수 (1~F)

    public static void main(String[] args) {

        boolean stable = false;
        System.out.println("╔═══════════════════════════════╗");
        System.out.println("║       L W C A R D A N O       ║");
        System.out.println("║         " + build + "         ║");
        if (!stable) {
            System.out.println("║         Expm. Release         ║");
        } else {
            System.out.println("║        Stable  Release        ║");
        }
        System.out.println("╚═══════════════════════════════╝");

        // Check startup parameters
        // - Config file check
        String cfgPath = Arrays.stream(args)
                .filter(s -> s.startsWith("--cfg=")).findAny().orElse("");

        // - Grid build mode check
        boolean notGridBuildMode = Arrays.stream(args)
                .filter(s -> s.equals("--gridgen")).findAny().orElse("").isEmpty();

        // - Debug mode
        debugMode = Arrays.asList(args).contains("--debug");
        verbose = Arrays.asList(args).contains("--verbose");

        // help
        if (Arrays.asList(args).contains("help") || Arrays.asList(args).contains("-h") ||  Arrays.asList(args).contains("--help")) {
            System.out.println();
            System.out.println("Required parameters:");
            System.out.println("    --cfg=<configuration file>");
            System.out.println();
            System.out.println("Optional parameters:");
            System.out.println("    --debug   : Launch in debug mode: This shows API responses to console.");
            System.out.println("    --verbose : Enable verbose flag. This will show all DEEP-DEBUG flagged logs, which will make console dirty.");
            System.out.println("    --gridgen : Generate grid from autogrid configuration in cfg file");
            System.out.println("    --about   : Print about this program");
            System.out.println();
            return;
        }

        // about
        if (Arrays.asList(args).contains("--about")) {
            System.out.println();
            System.out.println("LWCardano " + build.replace(" ", "") + " " + (stable ? "Release" : "Experimental"));
            System.out.println("Development Codename LAUPECTRA - Lightweight Automated Perpetual Cryptocurrency Trader");
            System.out.println();
            return;
        }


        if (cfgPath.isEmpty()) {
            System.err.println("Error: Configuration file not passed in as argument: Requires --cfg=<config file>");
            HeadlessDialogs.showMessage("Error: Configuration file not passed in as argument: Requires --cfg=<config file>");
            System.exit(0);
            return;
        }

        Logger.log("");
        Logger.log("LWCARDANO PROGRAM START!!!!");

        // Load settings
        cfgPath = cfgPath.substring(cfgPath.indexOf("=") + 1);
        ConfigurationFile cfgFile = new File2(cfgPath).configFileMode().load();

        String endpoint = cfgFile.get("url");
        String ak = cfgFile.get("ak");
        String sk = cfgFile.get("sk");
        credentials = new ByBitCredentials(endpoint, ak, sk);
        cfg = cfgFile;


        // Grid build mode
        if (!notGridBuildMode) {
            Logger.log("INFO", "현재 가격 가져오는중...");
            double currentPrice = Market.getCurrentPrice(credentials, cfg.get("market", "FUTURE"), cfg.get("symbol", "BTCUSDT"));
            Logger.log("INFO", "현재가: " + currentPrice);
            AutoGridBuilder.make(cfgFile, currentPrice);
        }

        boolean actionHookSuccess = ActionHooks.onStart(credentials, cfgFile);
        if (!actionHookSuccess) {
            HeadlessDialogs.showMessage("Error: Action Hook Failed");
            System.exit(0);
            return;
        }


        /*
        테스트 코드
        */
        boolean TESTMODE = false;
        if (TESTMODE) {
            System.out.println(Orders.enumerateOrderHistory(credentials, "FUTURE", "BTCUSDT", 200));
            return;
        }

        /*
        초기 셋팅
        1. buildGrid(currentPrice) 로 현재 적정 그리드 데이터를 계산한다.
            이 때, 리턴값은 <String, GridLine> 해시맵이다. (GridLine: 가격, OrderLinkID, Side 를 포함)
        2. 현재 걸려있는 예약을 모두 취소한다. (계정에서 불러온 후 iterative 로 전량 취소)
        3. 해시맵에 따라 전부 그리드를 예약한다
        (OK)
        */
        reset();
        boolean firstRun = true;

        /*
        프로그램 주기능 무한 반복.
         */
        while (true) {

            /*
            대기용 반복 루프
            1. 대기 조건: 그리드 하나가 돌파되어 거래가 체결 될 때 까지 대기.
             -> 대기 종료시: 체결된 해당 그리드는 없어짐을 보장한다.
            */
            int wait = 500;
            Logger.log("INFO", "대기 시작...");
            ArrayList<OrderObject> filledOrders = new ArrayList<>();
            while (filledOrders.isEmpty()) {
                long start = System.currentTimeMillis();
                filledOrders = getOrdersFilledDelta();
                long taken = System.currentTimeMillis() - start;
                Logger.log("DEEP-DEBUG", "Took " + taken + " ms to scan.");
                start = System.currentTimeMillis();
                try {Thread.sleep(wait);} catch (Exception ignored) {}
                taken = System.currentTimeMillis() - start;
                Logger.log("DEEP-DEBUG", "Took " + taken + " ms to sleep.");
            }
            Logger.log("거래 체결 감지... filledOrder 리스트 길이: " + filledOrders.size());

            for (int i = 0; i < filledOrders.size(); i++) {
                Logger.log("DEBUG", "Filled order [" + i + "]: " + filledOrders.get(i));
            }

            /*
            2. 기존 해시맵을 모두 스캔하며 체결된 그리드를 확인한다
            ->  만약 체결된 그리드가 Upper / Lower 리미트에 해당한다면 전용 작업 실행
            ->  그렇지 않다면 계속 진행
            */
            boolean isUpperLimitBroken = false;
            boolean isLowerLimitBroken = false;
            for (OrderObject orderObject : filledOrders) {
                if (orderObject.getOrderLinkId().contains("-UPLIM")) {
                    isUpperLimitBroken = true;
                }
                else if (orderObject.getOrderLinkId().contains("-LOWLIM")) {
                    isLowerLimitBroken = true;
                }
            }
            Logger.log("상한선 돌파: " + isUpperLimitBroken);
            Logger.log("하한선 돌파: " + isLowerLimitBroken);
            boolean isAnyLimitBroken = isUpperLimitBroken || isLowerLimitBroken;
            if (isAnyLimitBroken) { // 리미트 해당 확인
                boolean specificActionHookSuccess = true;
                if (isUpperLimitBroken) {
                    Logger.log("상한선 돌파 액션 후크 실행...");
                    specificActionHookSuccess = ActionHooks.onUpperLimitBreak(credentials, cfgFile);
                }
                if (isLowerLimitBroken) {
                    Logger.log("하한선 돌파 액션 후크 실행...");
                    specificActionHookSuccess = ActionHooks.onLowerLimitBreak(credentials, cfgFile);
                }
                if (!specificActionHookSuccess) {
                    HeadlessDialogs.showMessage("Error: Action Hook for onLower/UpperLimit Failed");
                    Logger.log("ERROR", "Error: Action Hook for onLower/UpperLimit Failed");
                    System.exit(0);
                    return;
                }
                Logger.log("상/하한선 돌파 액션 후크 실행...");
                specificActionHookSuccess = ActionHooks.onLimitBreak(credentials, cfgFile);
                if (!specificActionHookSuccess) {
                    HeadlessDialogs.showMessage("Error: Action Hook for onLower/UpperLimit Failed");
                    Logger.log("ERROR", "Error: Action Hook for onLower/UpperLimit Failed");
                    System.exit(0);
                    return;
                }
                return; // continue; TODO 대체하기
            } else {
                Logger.log("상/하한선 돌파 감지 없음...");
            }

            /*
            현재 조건: 체결된 예약은 Upper/lower 리미트가 아니다
                     filledOrders 값이 비어있지 않다

            작업: 방향 전환 인식
            1. resetTriggerOrder 와 동일한 오더가 체결됐다면 reset() 트리거 후 반복 재개 -> 방향 전환 인식됨
            2. orders 에 체결된 거래를 모두 기록함
            3. resetTriggerOrder 에 취소 명령 처리
            4. 리셋 처리
             */
            boolean triggeredResetOrder = false;
            for (OrderObject orderObject : filledOrders) {
                if (resetTriggerOrder == null) break;
                if (orderObject.equals(resetTriggerOrder) || isAlmostEqual(orderObject.getPrice(), resetTriggerOrder.getPrice(), 0.0001)) {
                    triggeredResetOrder = true;
                    break;
                }
            }
            if (triggeredResetOrder) {
                Logger.log("리셋 트리거 감지... reset() 호출");
                reset();
                continue; // 리셋 트리거 정리 완료
            } else {
                Logger.log("리셋 트리거 감지 없음...");
            }

            /*
            현재 조건: 체결된 거래는 Upper/lower limit 이 아니고
                    filledOrder 값이 비어있지 않으며 (거래가 0.4초 (+-오차) 이내에 한번 이상 발생했고)
                    방향 전환이 발생하지 않았으므로
                    리셋 트리거가 발동되지 않았다

            작업: 리셋 트리거 재설정
            1. 리셋 트리거의 방향을 가져옴
            2. orders 의 마지막에서 두번째 값을 resetTrigger 로 설정, 만약 길이가 부족하다면 리셋 트리거를 설정하지 않음. 이 때 위에서 찾은 방향을 이용하며 getCurrentPosition 으로 현재 qty 를 거래 총량으로 설정
             */
            orderHistory.addAll(filledOrders);
            if (resetTriggerOrder != null) {
                Logger.log("기존 리셋 트리거 취소 요청 전송");
                String response = "";
                if (!firstRun) {
                    if (resetTriggerOrder.getOrderLinkId().endsWith("-RST")) {
                        response = resetTriggerOrder.cancel();
                    } else {
                        Logger.log("WARNING", "리셋 트리거의 링크 ID (SID) 값이 RST 로 끝나지 않습니다. 정식 리셋 트리거가 아니므로 리셋이 트리거 되지 않았습니다.");
                    }
                } else {
                    Logger.log("최초 실행 감지, 트리거 취소 요청 취하");
                    firstRun = false;
                }
                if (!response.isEmpty()) {
                    // 리셋 트리거 취소 실패.
                    if (response.contains("order not exists or too late to cancel")) {
                        Logger.log("WARNING", "레이턴시로 인해 리셋 트리거가 체결되었음에도 감지되지 않은것으로 인식되었습니다. 거래가 만료된 것으로 간주합니다.");
                        Logger.log("WARNING", "패턴에서 벗어난 reset() 호출이 강제됩니다!");
                        reset();
                        continue;
                    }
                    Logger.log("ERROR", "리셋 트리거 취소 실패: " + response);
                }
            } else {
                // 기존의 리셋 트리거 정보가 존재하지 않으므로 리셋 트리거를 과거의 마지막 거래로 임의 설정
                Logger.log("리셋 트리거 미확인... 과거 마지막 거래로 임의 설정...");
                resetTriggerOrder = filledOrders.getLast();
            }
            int newRstTriggerDirection = resetTriggerOrder.getTriggerDirection();
            Logger.log("유지할 기존 리셋 트리거 돌파 방향: " + (newRstTriggerDirection == OrderObject.DIRECTION_FALLS_TO ? "하방 돌파" : "상방 돌파"));

            if (orderHistory.size() >= 2) { // 길이가 충분할 경우 트리거 설정
                Logger.log("새로운 리셋 트리거 준비중...");
                OrderObject secondLastOrder = orderHistory.get(orderHistory.size() - 2);
                String category = cfgFile.get("market", "FUTURE");
                String symbol = cfgFile.get("symbol", "BTCUSDT");
                String orderLinkId = secondLastOrder.getOrderLinkId();
                if (!orderLinkId.endsWith("-RST")) orderLinkId += "-RST";
                double price = secondLastOrder.getPrice().doubleValue();
                Side side = secondLastOrder.getInvertedSide();
                HashMap<String, PositionObject> pos = Orders.getCurrentPosition(credentials, category, symbol);
                PositionObject currentPosition = pos.get(symbol);
                double quantity = currentPosition.getQty();
                resetTriggerOrder = new OrderObject(credentials, category, price, side, symbol, quantity, orderLinkId, newRstTriggerDirection);
                Logger.log("DEBUG", "리셋 트리거 주문 데이터: " + resetTriggerOrder);
                boolean success = resetTriggerOrder.placeOrder();
                if (success) {
                    Logger.log("리셋 트리거 설정 완료: " + resetTriggerOrder.getOrderLinkId() + "@" + resetTriggerOrder.getPrice() + " (d=" + resetTriggerOrder.getSide() + ", q=" + resetTriggerOrder.getQty() + ")");
                } else {
                    Logger.log("ERROR", "리셋 트리거 주문 실패!");
                }
            } else {
                Logger.log("거래 기록 불충분, 리셋 트리거 설정 불가 (현재 누적 거래 기록 수: " + orderHistory.size() + ")");
            }

            if (orderHistory.size() >= MAX_ORDER_HISTORY_LENGTH) {
                Logger.log("거래 기록 과포화, 오래된 거래 기록 절삭 시작.");
                for (int i = 0; i < CLEAN_ORDER_HISTORY; i++) {
                    orderHistory.removeFirst();
                }
                Logger.log("거래 기록 절삭 완료: 거래 기록 총 " + orderHistory.size() + "개 남음.");
            }

            Logger.log("주 반복 작업 완료===========================================");
        }

    }

    /**
     * 체결된 포지션을 찾은 후 반환함. 찾는 방식은 체결 기록을 바탕으로 set 처리를 통해 교집합이 "아닌것" 을 찾아 반환하는 방식임.
     * @return 체결된 계약 목록 반환, 체결 시간으로 Epoch 이 가장 낮은 것 부터 반환함 (오름차순)
     */
    public static ArrayList<OrderObject> getOrdersFilledDelta() {
//        Logger.log("DEEP-DEBUG", "Orders.enumerateOrderHistory()");
        ArrayList<OrderObject> orderHistory = Orders.enumerateOrderHistory(credentials, cfg.get("market", "FUTURE"), cfg.get("symbol", "BTCUSDT"), (int) (cfg.get("grid", "").split(",").length * 1.5));
        ArrayList<OrderObject> closedOrders = new ArrayList<>();
        ArrayList<String> gridLineLinkedIDs = new ArrayList<>();

//        Logger.log("DEEP-DEBUG", "for (GridLine g : grid.values()){}");
        for (GridLine g : grid.values()) {
            gridLineLinkedIDs.add(g.getOrderLinkId());
            gridLineLinkedIDs.add(g.getOrderLinkId() + "-RST"); // 리셋 주문도 포함
        }

//        Logger.log("DEEP-DEBUG", "for (OrderObject o : orderHistory) {}");
        for (OrderObject o : orderHistory) {
            if (gridLineLinkedIDs.contains(o.getOrderLinkId())) {
                if (!o.isOpen()) {
                    closedOrders.add(o);
                }
            }
        }
//        Logger.log("OH: " + orderHistory.size() + "개 항목, CO: " + closedOrders.size() + "개 항목, GLLID: " + gridLineLinkedIDs.size() + "개 항목");
        // 이 시점에서 openOrders 와 closedOrders 는 현재 세션의 그리드만 해당되는 오더다.

        // 거래 기록을 비교하여 LWCardanoApplication.orderHistory 에 없는 closedOrder 가 있다면 이는 새롭게 filled 된 order 이다
        ArrayList<OrderObject> filledOrders = new ArrayList<>();

//        Logger.log("DEEP-DEBUG", "for (OrderObject o : closedOrders) {}");
        for (OrderObject o : closedOrders) {
            if (!LWCardanoApplication.orderHistory.contains(o)) {
                filledOrders.add(o);
            }
        }

        // 시간차순으로 정렬
//        Logger.log("DEEP-DEBUG", "filledOrders.sort(Comparator.comparing(OrderObject::getCreatedTime));");
        filledOrders.sort(Comparator.comparing(OrderObject::getCreatedTime));
        return filledOrders;
    }

    public static void reset() {
        // 걸린 주문 모두 취소
        Logger.log("reset()");
        Logger.log("오더 목록 정리...");
        orderHistory.clear();
        Logger.log("리셋 트리거 Nullify...");
        resetTriggerOrder = null;
        Logger.log("예약된 오더 전량 취소중...");
        Orders.cancelAllOrders(credentials, cfg.get("market", "FUTURE"), cfg.get("symbol", "BTCUSDT"));

        // 포지션 전부 닫기
        Orders.ensureCloseAllInMarket(credentials, cfg.get("market", "FUTURE"), cfg.get("symbol", "BTCUSDT"));

        // 설정에서 레버리지 가져오기 및 원격에 설정
        int leverage = Integer.parseInt(cfg.get("leverage", "1"));
        Logger.log("레버리지 설정중: " + leverage + "배수");
        boolean leverageSet = Orders.setRemoteLeverage(credentials, cfg.get("market", "FUTURE"), cfg.get("symbol", "BTCUSDT"), leverage);
        if (!leverageSet) {
            Logger.log("ERROR", "레버리지 설정 실패: " + leverage);
            HeadlessDialogs.showMessage("레버리지 설정 실패: " + leverage);
            System.exit(9);
            return;
        }

        // 현재가를 불러와 그리드 설정
        Logger.log("INFO", "현재 가격 가져오는중...");
        double currentPrice = Market.getCurrentPrice(credentials, cfg.get("market", "FUTURE"), cfg.get("symbol", "BTCUSDT"));
        Logger.log("INFO", "현재가: " + currentPrice);
        grid.clear();
        grid = ProgramGridBuilder.buildGrid(cfg, leverage, currentPrice);
        Logger.log("INFO", "총 " + grid.size() + " 개의 그리드 데이터화 됨.");
        Logger.log("INFO", "전량 예약중...");
        ArrayList<OrderObject> failedOrders = makeOrderAll();
        if (!failedOrders.isEmpty()) {
            String failedOrdersString = Arrays.toString(failedOrders.toArray());
            Logger.log("ERROR", "예약 실패: " + failedOrdersString);
            HeadlessDialogs.showMessage("오류: 예약 실패. 실패한 예약 목록: " + failedOrdersString);
            System.exit(9);
            return;
        }
        Logger.log("INFO", "예약 완료.");
    }

    /**
     * 생성된 그리드 데이터를 바탕으로 order place 명령을 송신.
     * @return 주문 실패한 OrderObject 리스트를 반환함
     */
    public static ArrayList<OrderObject> makeOrderAll() {
        ArrayList<OrderObject> orders = new ArrayList<>();
        for (GridLine line : grid.values()) {
            orders.add(line.constructOrderObject(credentials, cfg));
        }
        LinkedHashMap<OrderObject, Boolean> result = Orders.placeBatchOrder(credentials, cfg.get("market", "FUTURE"), orders);
        ArrayList<OrderObject> failedOrders = new ArrayList<>();
        for (OrderObject order : result.keySet()) {
            if (!result.get(order)) {
                failedOrders.add(order);
            }
        }
        return failedOrders;
    }
}
