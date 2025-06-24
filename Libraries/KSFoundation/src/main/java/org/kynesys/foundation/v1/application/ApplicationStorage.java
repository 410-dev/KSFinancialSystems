package org.kynesys.foundation.v1.application;

import me.hysong.files.File2;
import org.kynesys.foundation.v1.interfaces.KSDeepSystemCommunicator;
import org.kynesys.foundation.v1.utils.KSHostTool;

public class ApplicationStorage {

    public static String appName = "ATLAS_DEFAULT_APP_NAME";
    public static KSDeepSystemCommunicator dsc = KSHostTool.getSystemCommunicator();

    public static boolean mkdirs(String virtualPath) {
        return new File2(dsc.getApplicationDataPath() + "/" + appName + "/" + virtualPath).mkdirs();
    }
}
