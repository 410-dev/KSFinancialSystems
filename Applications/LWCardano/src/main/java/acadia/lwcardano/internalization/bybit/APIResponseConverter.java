package acadia.lwcardano.internalization.bybit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class APIResponseConverter {
    public static LinkedHashMap<String, Object> asObj(Object o) {
        if (o instanceof HashMap<?, ?>) {
            return (LinkedHashMap<String, Object>) o;
        } else {
            return new LinkedHashMap<>();
        }
    }

    public static ArrayList<LinkedHashMap<String, Object>> asListOfObj(Object o) {
        if (o instanceof ArrayList<?>) {
            return (ArrayList<LinkedHashMap<String, Object>>) o;
        } else {
            return new ArrayList<>();
        }
    }

    public static ArrayList<Object> asListPrimitive(Object o) {
        if (o instanceof ArrayList<?>) {
            return (ArrayList<Object>) o;
        } else {
            return new ArrayList<>();
        }
    }
}
