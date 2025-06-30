package acadia.lwcardano;


import me.hysong.files.File2;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Logger {
    // Formatter for the timestamp including milliseconds
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
    private static final DateTimeFormatter logFileTime =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Private constructor to prevent instantiation of this utility class
    private Logger() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void log(String message) {
        log("INFO", message);
    }

    /**
     * Logs a message with a given status, automatically including timestamp and caller class name.
     *
     * The immediate caller of this log method will be used for the class name.
     *
     * @param status  A String representing the status of the log entry (e.g., "INFO", "ERROR", "DEBUG"). Cannot be null.
     * @param message The log message content. Cannot be null.
     */
    public static void log(String status, String message) {
        Objects.requireNonNull(status, "Status cannot be null");
        Objects.requireNonNull(message, "Message cannot be null");

        // Get the current time
        String timestamp = LocalDateTime.now().format(dateTimeFormatter);
        String logTimestamp = LocalDateTime.now().format(logFileTime);

        // Get the stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // Determine the caller class name
        String callerClassName = "<unknown class>";
        // Index 0: Thread.getStackTrace
        // Index 1: Logger.log (this method)
        // Index 2: The method that called Logger.log
        if (stackTrace.length > 2) {
            callerClassName = stackTrace[2].getClassName(); // Get canonical class name
        }

        callerClassName = "";

        // Format and print the log message
        String output = String.format("[%s] [%s] [%s] %s",
                timestamp,
                callerClassName,
                status,
                message);
        System.out.println(output);

        File2 logFile = new File2("logs/" + logTimestamp + ".log");
        try {
            logFile.appendString(true, output + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void log(String status, String message, Throwable throwable) {
        log(status, message);
        throwable.printStackTrace();
    }


    /**
     * Gets the canonical class name and method name of the caller of the method that called this getCaller().
     * This is equivalent to calling getCaller(0).
     *
     * @return A string in the format "com.example.ClassName.methodName", or "Unknown Caller" if the stack is not deep enough.
     */
    public static String getCaller() {
        return getCaller(0); // Default offset is 0
    }

    /**
     * Gets the canonical class name and method name of the caller at a specific offset in the call stack.
     *
     * An offset of 0 refers to the caller of the method that called getCaller.
     * An offset of 1 refers to the caller of that caller, and so on.
     *
     * @param offset The desired level up the call stack (0 means the caller of the caller of this method, 1 means the caller's caller's caller, etc.). Must be 0 or greater.
     * @return A string in the format "com.example.ClassName.methodName", or "Unknown Caller" if the stack is not deep enough for the given offset.
     * @throws IllegalArgumentException if offset is negative.
     */
    public static String getCaller(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative.");
        }

        // Get the stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // Calculate the index in the stack trace array
        // Index 0: Thread.getStackTrace
        // Index 1: Logger.getCaller(int) (this method)
        // Index 2: The method that called Logger.getCaller (e.g., the other getCaller() or external code)
        // Index 3: The caller of the method that called Logger.getCaller (this is offset 0)
        // Index 3 + offset: The caller 'offset' levels up from the caller's caller.
        int callerIndex = 3 + offset;

        if (callerIndex >= 0 && callerIndex < stackTrace.length) {
            StackTraceElement callerElement = stackTrace[callerIndex];
            // Return canonical class name + method name
            return callerElement.getClassName() + "." + callerElement.getMethodName();
        } else {
            // Stack wasn't deep enough for the requested offset
            return "Unknown Caller";
        }
    }
}
