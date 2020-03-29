package org.comroid.common.func;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Deprecated
public final class nFunction {
    @FunctionalInterface
    public interface I0O<O> extends Supplier<O> {
        static <O> I0O<O> of(Supplier<O> func) {
            return func::get;
        }

        @Override
        default O get() {
            return execute();
        }

        O execute();
    }

    @FunctionalInterface
    public interface I1O<I, O> extends Function<I, O> {
        static <I, O> I1O<I, O> of(Function<I, O> func) {
            return func::apply;
        }

        @Override
        default O apply(I in) {
            return execute(in);
        }

        O execute(I in);
    }

    @FunctionalInterface
    public interface I2O<I1, I2, O> extends BiFunction<I1, I2, O> {
        static <I1, I2, O> I2O<I1, I2, O> of(BiFunction<I1, I2, O> func) {
            return func::apply;
        }

        @Override
        default O apply(I1 in1, I2 in2) {
            return execute(in1, in2);
        }

        O execute(I1 in1, I2 in2);
    }

    @FunctionalInterface
    public interface I3O<I1, I2, I3, O> {
        O execute(I1 in1, I2 in2, I3 in3);
    }
}
