package me.hysong.files;

import lombok.Setter;

import java.io.IOException;
import java.util.*;

public class ConfigurationFile extends File2 {

    @Setter private boolean useAutoSaveAfterSet = true;
    @Setter private boolean useAsyncWhenAutoSave = false;
    private final Map<String, String> configFile = new LinkedHashMap<>();
    private final List<String> originalLines = new ArrayList<>();
    private final Set<String> originalKeys = new LinkedHashSet<>();

    public ConfigurationFile(String pathname) {
        super(pathname);
    }

    public ConfigurationFile load() {
        String val = readStringNullable();
        if (val == null) {
            return this;
        }

        originalLines.clear();
        originalKeys.clear();
        configFile.clear();

        // Split with -1 to preserve trailing empty lines
        String[] lines = val.split("\n", -1);
        for (String line : lines) {
            originalLines.add(line);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                continue;
            }
            String[] components = line.split("=", 2);
            String key = components[0];
            String value = components.length > 1 ? components[1] : null;
            configFile.put(key, value);
            originalKeys.add(key);
        }
//
//        System.out.println(configFile);
//        System.out.println(originalKeys);
//        System.out.println(originalLines);

        return this;
    }

    public ConfigurationFile save() throws IOException {
        StringBuilder sb = new StringBuilder();

        // Rebuild existing lines, updating key=values
        for (String line : originalLines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                sb.append(line);
            } else {
                String[] components = line.split("=", 2);
                String key = components[0];
                if (configFile.containsKey(key)) {
                    sb.append(key).append("=").append(configFile.get(key));
                } else {
                    // Key was removed or not changed, write original
                    sb.append(line);
                }
            }
            sb.append(System.lineSeparator());
        }

        // Append any new keys that weren't in the original file
        for (Map.Entry<String, String> entry : configFile.entrySet()) {
            if (!originalKeys.contains(entry.getKey())) {
                sb.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append(System.lineSeparator());
            }
        }

        writeString(sb.toString());
        return this;
    }

    public String get(String key, String defaultValue) {
        return configFile.getOrDefault(key, defaultValue);
    }

    public String get(String key) {
        return get(key, null);
    }

    public ConfigurationFile set(String key, String value) {
        configFile.put(key, value);
        if (useAutoSaveAfterSet) {
            Runnable save = () -> {
                try {
                    save();
                } catch (Exception e) {
                    System.err.println("ERROR: Failed to save configuration file.");
                    throw new RuntimeException(e);
                }
            };
            if (useAsyncWhenAutoSave) {
                new Thread(save).start();
            } else {
                save.run();
            }
        }
        return this;
    }

    public boolean has(String key) {
        return configFile.containsKey(key);
    }

    public boolean isKeyNullOrEmpty(String key) {
        return get(key, null) == null || get(key, "").isEmpty();
    }
}
