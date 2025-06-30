package acadia.lwcardano.tools;

import me.hysong.files.ConfigurationFile;

public class ActionHooks {

    public static boolean onLowerLimitBreak(ConfigurationFile cfgFile) {
        return true;
    }

    public static boolean onUpperLimitBreak(ConfigurationFile cfgFile) {
        return true;
    }

    public static boolean onTerminate(ConfigurationFile cfgFile) {
        return true;
    }

    public static boolean onError(ConfigurationFile cfgFile) {
        return true;
    }

    public static boolean onLimitBreak(ConfigurationFile cfgFile) {
        return true;
    }

    public static boolean onStart(ConfigurationFile cfgFile) {
        return true;
    }
}
