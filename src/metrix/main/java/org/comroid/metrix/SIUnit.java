package org.comroid.metrix;

import java.util.Optional;

public enum SIUnit implements MultiplierUnit {
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
        return String.format("SIUnit{multiplier=%s}", Double.toHexString(multiplier));
    }

    public Optional<SIUnit> above() {
        final int ordinal = ordinal();
        return ordinal > 0 ? Optional.of(values()[ordinal - 1]) : Optional.empty();
    }

    public Optional<SIUnit> below() {
        final int ordinal = ordinal();
        final SIUnit[] values = values();

        return ordinal < values.length ? Optional.of(values[ordinal + 1]) : Optional.empty();
    }
}
