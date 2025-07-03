package ksmanualtrader.windows;

import me.hysong.files.ConfigurationFile;
import org.kynesys.kstraderapi.v1.driver.KSExchangeDriverManifest;
import org.kynesys.kstraderapi.v1.objects.KSGenericAuthorizationObject;

import javax.swing.*;

public class MakeOrders extends JFrame {

    public MakeOrders(ConfigurationFile cfg, KSExchangeDriverManifest manifest) {
        if (manifest == null) {
            dispose();
            return;
        }
    }
}
