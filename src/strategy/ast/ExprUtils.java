package strategy.ast;

public final class ExprUtils {

    public static boolean isTrue(long v) {
        return v != 0;
    }

    public static long bool(boolean b) {
        return b ? 1 : 0;
    }

}
