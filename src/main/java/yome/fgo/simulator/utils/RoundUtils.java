package yome.fgo.simulator.utils;

import java.math.BigDecimal;

import static java.math.RoundingMode.DOWN;
import static java.math.RoundingMode.HALF_UP;

public class RoundUtils {
    public static double roundDown(final double num) {
        return BigDecimal.valueOf(num).setScale(2, DOWN).doubleValue();
    }

    public static double roundNearest(final double num) {
        return BigDecimal.valueOf(num).setScale(2, HALF_UP).doubleValue();
    }
}
