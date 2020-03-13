package org.comroid.common.func;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class nFunction {
    @FunctionalInterface
    public interface I0O<O> extends Supplier<O> {
        O execute();

        @Override
        default O get() {
            return execute();
        }

        static <O> I0O<O> of(Supplier<O> func) {
            return func::get;
        }
    }

    @FunctionalInterface
    public interface I1O<I, O> extends Function<I, O> {
        O execute(I in);

        @Override
        default O apply(I in) {
            return execute(in);
        }

        static <I, O> I1O<I, O> of(Function<I, O> func) {
            return func::apply;
        }
    }

    @FunctionalInterface
    public interface I2O<I1, I2, O> extends BiFunction<I1, I2, O> {
        O execute(I1 in1, I2 in2);

        @Override
        default O apply(I1 in1, I2 in2) {
            return execute(in1, in2);
        }

        static <I1, I2, O> I2O<I1, I2, O> of(BiFunction<I1, I2, O> func) {
            return func::apply;
        }
    }

    @FunctionalInterface
    public interface I3O<I1, I2, I3, O> {
        O execute(I1 in1, I2 in2, I3 in3);
    }
}
