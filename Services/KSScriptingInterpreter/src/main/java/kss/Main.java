package kss;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // Check if opening as socket mode or direct terminal interpreter or script running mode

        // Usage:
        //   Host mode: kss --host=127.0.0.1 --port=3080
        //   Script mode: kss <script>
        //   Interpreter mode: kss

        boolean hostmode = Arrays.stream(args).anyMatch(s -> s.startsWith("--port="));
        if (hostmode) {
            System.out.println("Running host mode...");
            String portDef = Arrays.stream(args).filter(s -> s.startsWith("--port=")).findFirst().orElse("--port=3080");
            String hostDef = Arrays.stream(args).filter(s -> s.startsWith("--host=")).findFirst().orElse("--host=127.0.0.1");
            portDef = portDef.substring(portDef.indexOf("="));
            hostDef = hostDef.substring(hostDef.indexOf("="));
            if (portDef.length() < 2) {
                System.out.println("Invalid port configuration (Less than 2 digit port), ");
            }
        }
    }
}
