package org.comroid.common.info;

import java.util.function.Supplier;

@FunctionalInterface
public interface MessageSupplier extends Supplier<String> {
    static MessageSupplier format(String format, Object... args) {
        return new OfFunc(() -> String.format(format, args));
    }

    final class OfFunc implements MessageSupplier {
        public OfFunc(Supplier<String> supplier) {
            this.supplier = supplier;
        }

        @Override
        public String toString() {
            return get();
        }

        @Override
        public String get() {
            return supplier.get();
        }
        private final Supplier<String> supplier;
    }
}
