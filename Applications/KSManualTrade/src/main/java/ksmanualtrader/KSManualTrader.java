package ksmanualtrader;


import ksmanualtrader.objects.JournalingObject;
import ksmanualtrader.windows.EditDriverSettings;
import ksmanualtrader.windows.MakeOrders;
import ksmanualtrader.windows.OrderList;
import lombok.Getter;
import me.hysong.files.ConfigurationFile;
import me.hysong.files.File2;
import org.kynesys.foundation.v1.interfaces.KSApplication;
import org.kynesys.foundation.v1.interfaces.KSJournalingService;
import org.kynesys.foundation.v1.sharedobj.KSEnvironment;
import org.kynesys.foundation.v1.utils.LanguageKit;
import org.kynesys.foundation.v1.utils.StorageSetupTool;
import org.kynesys.graphite.v1.GPSplashWindow;
import org.kynesys.graphite.v1.GraphiteProgramLauncher;
import org.kynesys.graphite.v1.KSGraphicalApplication;
import org.kynesys.kstraderapi.v1.driver.KSExchangeDriverManifest;
import org.kynesys.kstraderapi.v1.objects.KSGenericAuthorizationObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;

@Getter
public class KSManualTrader extends KSGraphicalApplication implements KSApplication {

    private final String appDisplayName = "Kyne Systems Manual Trader";
    private final int windowWidth = 800;
    private final int windowHeight = 600;

    public static String storagePath = "Storage";



    // --- Global Component Declarations ---

    // Top Panel Components
    private JPanel topPanel;
    private JButton refreshButton;
    private JComboBox<String> exchangeComboBox;

    // Main content panel that holds all sections
    private JPanel mainContentPanel;

    // General Section Components
    private JPanel generalPanel;
    private JButton generalSettingsButton;
    private JButton logsButton;

    // Orders Section Components
    private JPanel ordersPanel;
    private JButton makeGridOrderButton;
    private JButton manualSellWindowButton;

    // History Section Components
    private JPanel historyPanel;
    private JButton profitHistoryButton;

    // Bottom Panel Components
    private JLabel versionLabel;
    private JLabel connectionLabel;

    // Current selection
    private ConfigurationFile cfg;
    private File2 cfgFile;
    private File2 storage;

    // Static data
    public static final String logsPath = "logs";
    public static final String cfgPath = "configs";

    private File2 root;

    public KSExchangeDriverManifest getManifest() {
        String id = null;
        if (exchangeComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Unable to open setting: ID is not selected.", "ID not selected", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        id = exchangeComboBox.getSelectedItem().toString();
        String[] comp = id.split(" \\(");
        if (comp.length < 2) {
            JOptionPane.showMessageDialog(null, "Unable to retrieve driver ID from selection.", "Internal Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        id = comp[comp.length - 1];
        id = id.substring(0, id.length() - 1); // This is key of drivers instantiated
        return Drivers.driversInstantiated.get(id);
    }

    private void updateConfig(String id) {
        if (id == null) return;

        try {
            cfgFile = storage.child(cfgPath).child(id.split(": ")[1].split(" \\(")[0] + ".cfg");
            cfgFile.parent().mkdirs();
            cfgFile.writeIfNotExist();
            cfg = cfgFile.configFileMode().load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes all the Swing components.
     */
    private void initComponents() {
        // Top Panel
        refreshButton = new JButton("Refresh");
//        String[] exchanges = {"UpBit", "Binance", "Coinbase"};

        // Make combo box from drivers
        String[] exchanges = new String[Drivers.driversInstantiated.size()];
        int index = 0;
        for (String driverId : Drivers.driversInstantiated.keySet()) {
            String exchangeName = Drivers.driversInstantiated.get(driverId).getDriverExchangeName() + ": " + Drivers.driversInstantiated.get(driverId).getDriverExchange() + " (" + driverId + ")";
            exchanges[index] = exchangeName;
            index += 1;
        }
        exchangeComboBox = new JComboBox<>(exchanges);
        exchangeComboBox.addActionListener(e -> {
            System.out.println("Exchange selected: " + exchangeComboBox.getSelectedItem());
            updateConfig(exchangeComboBox.getSelectedItem() != null ? exchangeComboBox.getSelectedItem().toString() : null);
        });

        // General Section
        generalSettingsButton = new JButton("Settings");
        logsButton = new JButton("Logs");

        // Orders Section
        makeGridOrderButton = new JButton("Make Grid Order");
        manualSellWindowButton = new JButton("Manual Sell");

        // History Section
        profitHistoryButton = new JButton("Profit History");

        // Bottom Panel
        versionLabel = new JLabel("Version 1.0");
        connectionLabel = new JLabel("Not connected");

        // Button actions
        generalSettingsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    new EditDriverSettings(cfg, getManifest()).setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        logsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "Getting things ready...");
            }
        });
        makeGridOrderButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    new MakeOrders(cfg, getManifest()).setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        manualSellWindowButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    new OrderList(cfg, getManifest()).setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        profitHistoryButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "Getting things ready...");
            }
        });
    }

    /**
     * Lays out all the initialized components within various panels.
     */
    private void layoutComponents() {
        // --- Top Panel Construction ---
        topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(refreshButton, BorderLayout.WEST);
        topPanel.add(exchangeComboBox, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH); // Add top panel to the frame

        // --- Main Content Panel Construction (for sections) ---
        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));

        // --- General Panel ---
        generalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        generalPanel.setBorder(new TitledBorder("General"));
        generalPanel.add(generalSettingsButton);
        generalPanel.add(logsButton);
        mainContentPanel.add(generalPanel);

        // --- Orders Panel ---
        ordersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        ordersPanel.setBorder(new TitledBorder("Orders"));
        ordersPanel.add(makeGridOrderButton);
        ordersPanel.add(manualSellWindowButton);
        mainContentPanel.add(ordersPanel);

        // --- History Panel ---
        historyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        historyPanel.setBorder(new TitledBorder("History"));
        historyPanel.add(profitHistoryButton);
        mainContentPanel.add(historyPanel);

        // Add the central container panel to the frame's center
        add(mainContentPanel, BorderLayout.CENTER);

        // --- Bottom Panel (Version Label) ---
        // We wrap the label in a panel to provide some padding
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(versionLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }


    @Override
    public int appMain(KSEnvironment environment, String execLocation, String[] args, KSJournalingService logger) {
        return 0;
    }

    @Override
    public GPSplashWindow getSplashWindow(String[] args) {
        GPSplashWindow splashWindow = new GPSplashWindow(400, 300, JLabel.RIGHT);
        splashWindow.setSplashBackend(new Thread(() -> {
            // Setup storage path
            storagePath = StorageSetupTool.init("KSManualTrader", args);

            storage = new File2(storagePath);
            root = storage.parent().parent();
            File2 sharedlib = root.child("Library");

            // Load libraries
            try {
                splashWindow.setCurrentStatus("Loading libraries...");
                Drivers.loadJarsIn(sharedlib);
                Drivers.loadJarsIn(new File2(storagePath + "/Libraries"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(splashWindow, "Failed to load libraries", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            // Load languages
            String[] nonDefaultLanguages = new File2(storagePath + "/languages").childrenRecursive(true).toArray(new String[0]);
            String[] defaultLanguages = new File2(storagePath + "/defaults/languages").childrenRecursive(true).toArray(new String[0]);
            for (String file : defaultLanguages) {
                // Filter .lang.txt files only
                if (!file.endsWith(".lang.txt")) continue;

                // Load
                try {
                    LanguageKit.loadLanguageFromFile(storagePath + "/defaults/languages/" + file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (String file : nonDefaultLanguages) {
                // Filter .lang.txt files only
                if (!file.endsWith(".lang.txt")) continue;

                // Load
                try {
                    LanguageKit.loadLanguageFromFile(storagePath + "/languages/" + file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Load drivers
            loadDrivers();

            // Prepare UI
            // --- Frame Setup ---
            setLayout(new BorderLayout(10, 10)); // Main layout with gaps

            // --- Initialize Components ---
            initComponents();

            // --- Layout Panels ---
            layoutComponents();

            // --- Finalize Frame ---
            setMinimumSize(getSize()); // Prevent resizing smaller than the packed size

            // Load configurations
            updateConfig(exchangeComboBox.getSelectedItem() == null ? null : exchangeComboBox.getSelectedItem().toString());

        }));
        JLabel titleLabel = new JLabel("Kyne Systems Trader Machine");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        titleLabel.setSize(splashWindow.getWidth(), splashWindow.getHeight());
        titleLabel.setLocation(0,0);
        splashWindow.add(titleLabel);
        splashWindow.setStatusSuffixSpacing("    ");
        splashWindow.setStatusSuffix("KSTraderMachine 1.0");
        splashWindow.setForegroundColor(Color.WHITE);
        splashWindow.setBackgroundColor(Color.BLACK);
        splashWindow.setImageLocation("path/to/splash/image.png"); // Set the path to your splash image
        return splashWindow;
    }

    public void loadDrivers() {

        // Load drivers
        try {
            System.out.println("Loading Drivers...");
            Drivers.loadJarsIn(root.child("Drivers"));
            Drivers.loadJarsIn(new File2(storagePath + "/Drivers"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to load drivers", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Index drivers
        try {
            HashMap<String, Class<?>> drivers = (HashMap<String, Class<?>>) Drivers.DriverIntrospection.findImplementations(KSExchangeDriverManifest.class);
            for (String key : drivers.keySet()) {
                Class<?> driverClass = drivers.get(key);
                GraphiteProgramLauncher.getJournalingObject().log("INFO", "Driver: " + key + " -> " + driverClass.getName());
                try {
                    KSExchangeDriverManifest manifest = (KSExchangeDriverManifest) driverClass.getDeclaredConstructor().newInstance();
                    Drivers.driversInstantiated.put(key, manifest);
                    Drivers.drivers.put(key, driverClass);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Drivers not instantiated!", "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                    throw new RuntimeException("Driver instantiation failed", ex);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Drivers are not loaded!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }

    public static void main(String[] args) {
        GraphiteProgramLauncher.sleekUIEnabled = true;
        GraphiteProgramLauncher.launch(KSManualTrader.class, args, JournalingObject.class);
    }
}