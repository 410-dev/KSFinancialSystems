package me.hysong.files;

import lombok.Setter;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enhanced ConfigurationFile supporting typed getters and list/array parsing.
 */
public class ConfigurationFile extends File2 {

    @Setter private boolean useAutoSaveAfterSet = true;
    @Setter private boolean useAsyncWhenAutoSave = false;
    private final Map<String, String> configFile = new LinkedHashMap<>();
    private final List<String> originalLines = new ArrayList<>();
    private final Set<String> originalKeys = new LinkedHashSet<>();

    // Parsers for primitives and String
    private final Map<Class<?>, Function<String, ?>> parsers = new HashMap<>();

    public ConfigurationFile() {
        super("");
        initParsers();
    }

    public ConfigurationFile(String pathname) {
        super(pathname);
        initParsers();
    }

    private void initParsers() {
        parsers.put(String.class,    Function.identity());
        parsers.put(Integer.class,   Integer::parseInt);
        parsers.put(Long.class,      Long::parseLong);
        parsers.put(Float.class,     Float::parseFloat);
        parsers.put(Double.class,    Double::parseDouble);
        parsers.put(Boolean.class,   Boolean::parseBoolean);
        parsers.put(Byte.class,      Byte::parseByte);
        parsers.put(Short.class,     Short::parseShort);
        parsers.put(Character.class, s -> s.isEmpty() ? '\0' : s.charAt(0));
    }


    public ConfigurationFile load() {
        String val = readStringNullable();
        if (val == null) return this;

        originalLines.clear();
        originalKeys.clear();
        configFile.clear();

        String[] lines = val.split("\n", -1);
        for (String line : lines) {
            originalLines.add(line);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                continue;
            }
            String[] comp = line.split("=", 2);
            String key = comp[0];
            String value = comp.length > 1 ? comp[1] : null;
            configFile.put(key, value);
            originalKeys.add(key);
        }
        return this;
    }

    public ConfigurationFile extend(ConfigurationFile newConfigFile) {
        configFile.putAll(newConfigFile.configFile);
        return this;
    }

    public ConfigurationFile save(ConfigurationFile newConfigFile) throws IOException {
        // Copy configuration file to current
        configFile.clear();
        configFile.putAll(newConfigFile.configFile);
        return save();
    }


    public ConfigurationFile save() throws IOException {
        if (super.getPath().isEmpty()) {
            throw new IOException("Path is not specified, cannot save.");
        }
        StringBuilder sb = new StringBuilder();
        for (String line : originalLines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                sb.append(line);
            } else {
                String[] comp = line.split("=", 2);
                String key = comp[0];
                if (configFile.containsKey(key)) {
                    sb.append(key).append("=").append(configFile.get(key));
                } else {
                    sb.append(line);
                }
            }
            sb.append(System.lineSeparator());
        }
        for (Map.Entry<String, String> e : configFile.entrySet()) {
            if (!originalKeys.contains(e.getKey())) {
                sb.append(e.getKey()).append("=").append(e.getValue()).append(System.lineSeparator());
            }
        }
        writeString(sb.toString());
        return this;
    }

    /**
     * Generic getter for primitives, wrappers, and String.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type, T defaultValue) {
        String raw = configFile.get(key);
        if (raw == null) {
            System.out.printf("WARNING: Missing key '%s', defaulting to %s%n", key, defaultValue);
            return defaultValue;
        }

        Function<String, T> parser = (Function<String, T>) parsers.get(
                type.isPrimitive() ? primitiveToWrapper(type) : type
        );
        if (parser == null) {
            throw new IllegalArgumentException("No parser for type " + type.getName());
        }
        try {
            return parser.apply(raw);
        } catch (Exception e) {
            System.out.printf("ERROR: Parsing key '%s' as %s failed, defaulting to %s%n",
                    key, type.getSimpleName(), defaultValue);
            return defaultValue;
        }
    }

    /**
     * Retrieve comma-delimited array of elements.
     */
    @SuppressWarnings("unchecked")
    public <T> T[] getArray(String key, Class<T> elementType, T[] defaultValue) {
        String raw = configFile.get(key);
        if (raw == null) {
            System.out.printf("WARNING: Missing key '%s', defaulting to %s%n",
                    key, Arrays.toString(defaultValue));
            return defaultValue;
        }
        String[] parts = raw.split(",");
        T[] result = (T[]) java.lang.reflect.Array
                .newInstance(elementType, parts.length);
        Function<String, T> parser = (Function<String, T>) parsers.get(
                primitiveToWrapper(elementType));
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = parser.apply(parts[i].trim());
            } catch (Exception e) {
                System.out.printf(
                        "ERROR: Element %d of '%s' invalid, defaulting to %s%n",
                        i, key, defaultValue.length>i?defaultValue[i]:"null"
                );
                result[i] = defaultValue.length>i?defaultValue[i]:null;
            }
        }
        return result;
    }

    /**
     * Retrieve comma-delimited List of elements.
     */
    public <T> List<T> getList(String key, Class<T> elementType, List<T> defaultValue) {
        T[] arr = getArray(key, elementType,
                defaultValue.toArray((T[])java.lang.reflect.Array.newInstance(elementType, 0)));
        return new ArrayList<>(Arrays.asList(arr));
    }

    /**
     * Simple setter with optional autosave.
     */
    public ConfigurationFile set(String key, Object value) {
        configFile.put(key, value.toString());
        if (useAutoSaveAfterSet) {
            Runnable saveTask = () -> {
                try { save(); }
                catch (Exception e) {
                    System.err.println("ERROR: Failed to save configuration file.");
                    throw new RuntimeException(e);
                }
            };
            if (useAsyncWhenAutoSave) new Thread(saveTask).start();
            else saveTask.run();
        }
        return this;
    }

    public boolean has(String key) {
        return configFile.containsKey(key);
    }

    public boolean isKeyNullOrEmpty(String key) {
        String v = get(key, String.class, null);
        return v == null || v.isEmpty();
    }

    // Map primitive class to wrapper
    private static Class<?> primitiveToWrapper(Class<?> cls) {
        if      (cls == int.class)     return Integer.class;
        else if (cls == long.class)    return Long.class;
        else if (cls == float.class)   return Float.class;
        else if (cls == double.class)  return Double.class;
        else if (cls == boolean.class) return Boolean.class;
        else if (cls == byte.class)    return Byte.class;
        else if (cls == short.class)   return Short.class;
        else if (cls == char.class)    return Character.class;
        else                           return cls;
    }
}
