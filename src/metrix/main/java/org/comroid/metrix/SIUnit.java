package org.comroid.metrix;

import org.comroid.api.DoubleEnum;
import org.comroid.common.ref.Named;

import static java.lang.Math.max;
import static java.lang.Math.min;

public enum SIUnit implements DoubleEnum, Named {
    YOTTA(24),
    ZETTA(21),
    EXA(18),
    PETA(15),
    TERA(12),
    GIGA(9),
    MEGA(6),
    KILO(3),
    HEKTO(2),
    DEKA(1),

    ONE(0),

    DECI(-1),
    CENTI(-2),
    MILLI(-3),
    MICRO(-6),
    NANO(-9),
    PIKO(-12),
    FEMTO(-15),
    ATTO(-18),
    ZEPTO(-21),
    YOKTO(-24);

    private final double multiplier;
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

    SIUnit(int pow) {
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
