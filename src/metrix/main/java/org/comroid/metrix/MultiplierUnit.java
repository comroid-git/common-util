package org.comroid.metrix;

import org.comroid.api.DoubleEnum;
import org.comroid.common.ref.Named;

import static java.lang.Math.max;
import static java.lang.Math.min;

public interface MultiplierUnit extends DoubleEnum, Named {
    default double convertTo(MultiplierUnit unit, double input) {
        final double multiplier = getValue();
        final double siValue = unit.getValue();

        if (siValue == multiplier)
            return input;
        return (max(siValue, multiplier) / min(siValue, multiplier)) * input;
    }
}
