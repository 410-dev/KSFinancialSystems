package org.kynesys.kstraderapi.v1.objects;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public class KSExchangeDriverState {

    @Getter
    private static final ArrayList<KSExchangeDriverState> KS_EXCHANGE_DRIVER_STATES = new ArrayList<>();

    private final KSExchangeDriverExitCode exitCode;
    private final String message;
    private final Exception exception;

    private KSExchangeDriverState(KSExchangeDriverExitCode exitCode, String message, Exception exception) {
        this.exitCode = exitCode;
        this.message = message;
        this.exception = exception;
    }

    public static void addState(KSExchangeDriverExitCode exitCode, String message) {
        addState(exitCode, message, null);
    }

    public static void addState(KSExchangeDriverExitCode exitCode, String message, Exception exception) {
        KS_EXCHANGE_DRIVER_STATES.add(new KSExchangeDriverState(exitCode, message, exception));

        if (exitCode != KSExchangeDriverExitCode.OK && exitCode != KSExchangeDriverExitCode.DRIVER_TERMINATED_INTENTIONALLY && exitCode != KSExchangeDriverExitCode.DRIVER_TEST_OK) {
            System.out.println("Driver state: " + exitCode + " - " + message);
            if (exception != null) {
                exception.printStackTrace();
            }
        } else if (exception != null) {
            System.out.println("Driver state: " + exitCode + " - " + message);
            exception.printStackTrace();
        }
    }

}
