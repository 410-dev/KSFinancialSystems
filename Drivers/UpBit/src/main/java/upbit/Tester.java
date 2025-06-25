package upbit;

import me.hysong.files.ConfigurationFile;
import me.hysong.files.File2;
import org.kynesys.foundation.v1.interfaces.KSJournalingService;
import org.kynesys.kstraderapi.v1.driver.TraderDriverV1;
import org.kynesys.kstraderapi.v1.objects.Account;

import java.util.HashMap;

public class Tester {
    public static void main(String[] args) throws Exception {
        File2 fc = new File2("env.cfg.local");
        ConfigurationFile cfg = fc.configFileMode().load();
        String ak = cfg.get("UPBIT_AK", "");
        String sk = cfg.get("UPBIT_SK", "");
        Account ac = new Account("spot", "upbit", ak, sk);

        System.out.println("AK: " + ak);
        System.out.println("SK: " + sk);

        UpBitDriverManifestV1 drvm = new UpBitDriverManifestV1();
        TraderDriverV1 drv = drvm.getDriver((status, message) -> System.out.println("[ " + status + " ] " + message));

        HashMap<String, Object> param = new HashMap<>();
        param.put("symbol", "KRW-XRP");
        param.put("limit", "100");
//        param.put("start_time", "1733720213791");

        // TODO 이더리움이나 리플 혹은 소형 코인들의 거래 볼륨 및 price 가 double 인지 int 인지 구분할 필요가 있음
        // 그러나 현재 BTC 이외 닫힌 거래 기록 조회가 안됨. 고침 필요.

//        drv.getOpenOrders(ac, param);
        drv.getPrice(ac, new String[]{"KRW-BTC"});
    }
}
