package me.hysong.kynesys.application.cryptohedger;

import me.hysong.files.ConfigurationFile;
import me.hysong.files.File2;
import me.hysong.utils.security.SingleInstanceEnforcer;
import me.hysong.utils.security.SingleInstanceEnforcer.PromptMode;
import me.hysong.utils.security.SingleInstanceEnforcer.Scope;
import me.hysong.utils.strings.ArgParser;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Application {

    public static File2 storage = null;
    public static ConfigurationFile config = null;

    public static void init() throws IOException {
        storage.child("logs").mkdirs();
        storage.child("config.cfg").writeIfNotExist(
                """
                        # KSCryptoHedger Config
                        
                        # Authorization
                        ak=
                        pk=
                        
                        # Trade configuration
                        side-accept-ratio=1
                        """);
    }

    public static void main(String[] args) throws SingleInstanceEnforcer.InstanceAlreadyRunningException, IOException {
        try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());} catch (Exception ignored) {}
        SingleInstanceEnforcer.enforcePrompt(PromptMode.ADAPTIVE);
        SingleInstanceEnforcer.markRunning(Scope.PUBLIC, Application.class);
        String storagePath = ArgParser.getValue(args, "--storage", "Storage");
        storage = new File2(storagePath);
        init();
    }
}
