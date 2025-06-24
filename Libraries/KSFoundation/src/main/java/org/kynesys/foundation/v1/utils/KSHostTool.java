package org.kynesys.foundation.v1.utils;

import org.kynesys.foundation.v1.drivers.KSDarwinCommunicator;
import org.kynesys.foundation.v1.drivers.KSLinuxCommunicator;
import org.kynesys.foundation.v1.drivers.KSNTCommunicator;
import org.kynesys.foundation.v1.enums.OSKernelDistro;
import org.kynesys.foundation.v1.interfaces.KSDeepSystemCommunicator;

public class KSHostTool {

    public static OSKernelDistro getOSKernelDistro() {
        // This method should return the current OS kernel distribution.
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("linux")) {
            return OSKernelDistro.LINUX;
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            return OSKernelDistro.DARWIN;
        } else if (osName.contains("windows")) {
            return OSKernelDistro.WINDOWS;
        } else {
            return OSKernelDistro.UNKNOWN;
        }
    }

    public static KSDeepSystemCommunicator getSystemCommunicator() {
        OSKernelDistro osKernelDistro = getOSKernelDistro();
        return switch (osKernelDistro) {
            case LINUX -> new KSLinuxCommunicator();
            case DARWIN -> new KSDarwinCommunicator();
            case WINDOWS -> new KSNTCommunicator();
            default -> throw new UnsupportedOperationException("Unsupported OS kernel distribution: " + osKernelDistro);
        };
    }
}
