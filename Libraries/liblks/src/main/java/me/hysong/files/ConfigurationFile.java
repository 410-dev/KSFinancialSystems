package me.hysong.files;

import java.io.IOException;
import java.util.HashMap;

public class ConfigurationFile extends File2 {

    private HashMap<String, String> configFile = new HashMap<>();

    public ConfigurationFile(String pathname) {
        super(pathname);
    }

    public ConfigurationFile load() {
        String val = readStringNullable();
        if (val == null) return this;

        String[] lines = val.split("\n");
        for (String l : lines) {
            if (l.trim().startsWith("#") || l.trim().startsWith("//")) {
                continue;
            }
            String[] components = l.split("=", 2);
            configFile.put(components[0], components[1]);
        }

        return this;
    }

    public ConfigurationFile save() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String key : configFile.keySet()) {
            sb.append(key).append("=").append(configFile.get(key)).append("\n");
        }
        writeString(sb.toString());
        return this;
    }

    public String get(String key, String defaultValue) {
        return configFile.getOrDefault(key, defaultValue);
    }

    public ConfigurationFile set(String key, String value) {
        configFile.put(key, value);
        return this;
    }
}
