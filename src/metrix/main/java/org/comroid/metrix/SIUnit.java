package org.comroid.metrix;

import org.comroid.api.DoubleEnum;
import org.comroid.common.ref.Named;

import static java.lang.Math.max;
import static java.lang.Math.min;

public enum SIUnit implements DoubleEnum, Named {
    YOTTA("Y", 24),
    ZETTA("Z", 21),
    EXA("E", 18),
    PETA("P", 15),
    TERA("T", 12),
    GIGA("G", 9),
    MEGA("M", 6),
    KILO("k", 3),
    HEKTO("h", 2),
    DEKA("da", 1),

    ONE("", 0),

    DECI("d", -1),
    CENTI("c", -2),
    MILLI("m", -3),
    MICRO("µ", -6),
    NANO("n", -9),
    PIKO("p", -12),
    FEMTO("f", -15),
    ATTO("a", -18),
    ZEPTO("z", -21),
    YOKTO("y", -24);

    private final double multiplier;
    private final String shorthand;
    private final int power;

    public int getPower() {
        return power;
    }

    @Override
    public double getValue() {
        return multiplier;
    }

    @Override
    public String getName() {
        final String name = name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    @Override
    public String getAlternateFormattedName() {
        return shorthand;
    }

    SIUnit(String shorthand, int pow) {
        this.shorthand = shorthand;
        this.power = pow;
        this.multiplier = Math.pow(10, pow);
    }

    @Override
    public String toString() {
        return String.format("SIUnit{multiplier=0x%s}", Double.toHexString(multiplier));
    }

    public double convertTo(SIUnit si, double input) {
        final double siValue = si.getValue();

        if (siValue == multiplier)
            return input;
        return (max(siValue, multiplier) / min(siValue, multiplier)) * input;
    }
}
