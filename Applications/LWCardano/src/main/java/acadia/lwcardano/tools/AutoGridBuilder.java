package acadia.lwcardano.tools;

import acadia.lwcardano.LWCardanoApplication;
import acadia.lwcardano.Logger;
import acadia.lwcardano.internalization.objects.ConfigurationFile;

import javax.swing.*;
import java.util.Arrays;

public class AutoGridBuilder {
    public static void make(ConfigurationFile cfgFile, double currentPrice) {
        Logger.log("INFO", "Generating grid from autogrid configuration section..");
        // Check if keys exist and is not empty
        String[] keys = new String[]{"autogrid-start", "autogrid-steps", "autogrid-count"};
        for (String key : keys) {
            if (!cfgFile.has(key) || cfgFile.isKeyNullOrEmpty(key)) {
                System.err.println("Error: Autogrid mode requires " + Arrays.toString(keys) + " but missed " + key);
                HeadlessDialogs.showMessage("Error: Autogrid mode requires " + Arrays.toString(keys) + " but missed " + key);
                System.exit(0);
                return;
            }
        }

        // Cast to integers
        int autoStart, autoSteps, autoCount;
        try {
            autoStart = Integer.parseInt(cfgFile.get("autogrid-start"));
            autoSteps = Integer.parseInt(cfgFile.get("autogrid-steps"));
            autoCount = Integer.parseInt(cfgFile.get("autogrid-count"));
        } catch (Exception e) {
            System.err.println("Error: Failed to cast autogrid values to integer");
            HeadlessDialogs.showMessage("Error: Failed to cast autogrid values to integer");
            System.exit(0);
            return;
        }

        // Make
        String[] grids = new String[autoCount];
        for (int i = 0; i < autoCount; i++) {
            grids[i] = String.valueOf(autoStart + autoSteps * i);
        }

        cfgFile.set("grid", String.join(",", grids));
        Logger.log("DEBUG", "Grid generated: " + cfgFile.get("grid"));
    }
}
