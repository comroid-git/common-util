package org.comroid.metrix.length;

import org.comroid.common.ref.Named;
import org.comroid.metrix.MultiplierUnit;
import org.comroid.metrix.SIUnit;
import org.comroid.trie.TrieMap;

import java.util.Map;

public class Meter implements Length, Named {
    private static final Map<String, Meter> cache = TrieMap.ofString();
    private final double value;
    private final SIUnit siUnit;

    @Override
    public String getName() {
        return String.format("%f %#sm", value, siUnit);
    }

    private Meter(double value, SIUnit siUnit) {
        this.value = value;
        this.siUnit = siUnit;
    }

    public static Meter of(double value, SIUnit rate) {
        return cache.computeIfAbsent(
                Double.toHexString(rate.convertTo(SIUnit.ONE, value)),
                key -> new Meter(value, rate)
        );
    }

    @Override
    public String toString() {
        return String.format("Meter{len=%s}", this);
    }

    @Override
    public double getAs(MultiplierUnit unit) {
        return siUnit.convertTo(unit, value);
    }
}
