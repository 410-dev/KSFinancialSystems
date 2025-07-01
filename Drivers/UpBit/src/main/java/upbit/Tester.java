package upbit;


import me.hysong.files.ConfigurationFile;
import me.hysong.files.File2;
import org.kynesys.kstraderapi.v1.objects.KSGenericAuthorizationObject;

import java.util.HashMap;

public class Tester {
    public static void main(String[] args) throws Exception {
        File2 fc = new File2("env.cfg.local");
        ConfigurationFile cfg = fc.configFileMode().load();
        String ak = cfg.get("UPBIT_AK", "");
        String sk = cfg.get("UPBIT_SK", "");
        KSGenericAuthorizationObject ac = new KSGenericAuthorizationObject("spot", "upbit", ak, sk);

        System.out.println("AK: " + ak);
        System.out.println("SK: " + sk);

        UpBitDriverManifest drvm = new UpBitDriverManifest();
        UpBitDriver drv = (UpBitDriver) drvm.getDriver((status, message) -> System.out.println("[ " + status + " ] " + message));

        HashMap<String, Object> param = new HashMap<>();
        param.put("symbol", "KRW-BTC");
        param.put("limit", "100");
//        param.put("start_time", "1733720213791");

        // TODO 이더리움이나 리플 혹은 소형 코인들의 거래 볼륨 및 price 가 double 인지 int 인지 구분할 필요가 있음
        // 그러나 현재 BTC 이외 닫힌 거래 기록 조회가 안됨. 고침 필요.

        drv.getOpenOrders(ac, param);
        Thread.sleep(100);
        drv.getClosedOrders(ac, param);
        System.out.println(drv.getPrice(ac, new String[]{"KRW-BTC"}));
    }

//
//    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
//        String accessKey = "m65q0jcrOdDpjP2SVDqnludfuwzlgPmgD4s2Ruwd";
//        String secretKey = "xnnq4EAKQmVkYCAAagoCTLmO2xRIYK9BoGT25iNu";
//        String serverUrl = "https://api.upbit.com";
//
//        HashMap<String, String> params = new HashMap<>();
//        params.put("market", "KRW-XRP");
//        params.put("limit", "100");
//
//        String[] states = {
//                "wait",
//                "watch"
//        };
//
//        ArrayList<String> queryElements = new ArrayList<>();
//        for(Map.Entry<String, String> entity : params.entrySet()) {
//            queryElements.add(entity.getKey() + "=" + entity.getValue());
//        }
//        for(String state : states) {
//            queryElements.add("states[]=" + state);
//        }
//
//        String queryString = String.join("&", queryElements.toArray(new String[0]));
//
//        MessageDigest md = MessageDigest.getInstance("SHA-512");
//        md.update(queryString.getBytes(StandardCharsets.UTF_8));
//
//        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));
//
//        Algorithm algorithm = Algorithm.HMAC256(secretKey);
//        String jwtToken = JWT.create()
//                .withClaim("access_key", accessKey)
//                .withClaim("nonce", UUID.randomUUID().toString())
//                .withClaim("query_hash", queryHash)
//                .withClaim("query_hash_alg", "SHA512")
//                .sign(algorithm);
//
//        String authenticationToken = "Bearer " + jwtToken;
//
//        try {
//            HttpClient client = HttpClientBuilder.create().build();
//            HttpGet request = new HttpGet(serverUrl + "/v1/orders/open?" + queryString);
//            request.setHeader("Content-Type", "application/json");
//            request.addHeader("Authorization", authenticationToken);
//
//            HttpResponse response = client.execute(request);
//            HttpEntity entity = response.getEntity();
//
//            System.out.println(EntityUtils.toString(entity, "UTF-8"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//    }
}
