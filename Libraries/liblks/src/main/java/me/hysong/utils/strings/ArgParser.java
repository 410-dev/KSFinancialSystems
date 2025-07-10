package me.hysong.utils.strings;

import java.util.Arrays;

public class ArgParser {

    public static String getValue(String[] args, String key, String fallback) {
        return Arrays.stream(args).filter(e -> e.startsWith(key)).findFirst().orElse(fallback);
    }
}
