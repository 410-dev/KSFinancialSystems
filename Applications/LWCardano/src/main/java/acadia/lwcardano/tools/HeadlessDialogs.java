package acadia.lwcardano.tools;

import javax.swing.*;
import java.awt.*;

public final class HeadlessDialogs {
    private static final boolean HEADLESS = GraphicsEnvironment.isHeadless();

    private HeadlessDialogs() { /* no instances */ }

    public static void showMessage(String message) {
        if (!HEADLESS) {
            JOptionPane.showMessageDialog(null, message);
        } else {
            // optionally log or ignore
            System.err.println("GUI skipped (headless): " + message);
        }
    }

    public static String showInput(String prompt) {
        if (!HEADLESS) {
            return JOptionPane.showInputDialog(null, prompt);
        } else {
            // return a safe default or null
            System.err.println("Input dialog skipped (headless): " + prompt);
            return null;
        }
    }

    // add other wrappers as you need, e.g. showConfirm, showOption...
}
