package acadia.lwcardano.tools;

public class UnpreciseFloatComparer {
//    public static boolean almostEqual(float a, float b, double threshold) {
//        return Math.abs(a - b) < threshold;
//    }
//
    public static boolean isAlmostEqual(double a, double b, double threshold) {
        return Math.abs(a - b) < threshold;
    }
//
//    public static boolean almostEqual(float a, double b, double threshold) {
//        return Math.abs(a - b) < threshold;
//    }
//
//    public static boolean almostEqual(double a, float b, double threshold) {
//        return Math.abs(a - b) < threshold;
//    }

    public static boolean isAlmostEqual(Number a, Number b, double threshold) {
        return isAlmostEqual(a.doubleValue(), b.doubleValue(), threshold);
    }
}
