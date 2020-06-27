package org.comroid.test.metrix;

import org.comroid.metrix.SIUnit;
import org.comroid.metrix.length.Meter;
import org.comroid.mutatio.pipe.Pipe;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class SITest {
    @Test
    public void test() {
        Pipe.of(Arrays.asList(SIUnit.values()))
                .bi(si -> Meter.of(5, si))
                .peek((si, len) -> System.out.printf("Testing unit %s with %s\n", si, len))
                .forEach((si, meter) -> {
                    si.above()
                            .map(unit -> {
                                System.out.printf("Converting %s -> %s...\n", si, unit);
                                return si.convertTo(unit, 5);
                            })
                            .ifPresent(expected -> Assert.assertEquals(
                                    String.format("Converting %s -> %s", si, si.above().orElse(null)),
                                    expected,
                                    meter.getAs(si.above().orElseThrow(AssertionError::new)),
                                    0
                            ));
                    si.below()
                            .map(unit -> {
                                System.out.printf("Converting %s -> %s...\n", si, unit);
                                return si.convertTo(unit, 5);
                            })
                            .ifPresent(expected -> Assert.assertEquals(
                                    String.format("Converting %s -> %s", si, si.below().orElse(null)),
                                    expected,
                                    meter.getAs(si.below().orElseThrow(AssertionError::new)),
                                    0
                            ));
                });
    }
}
