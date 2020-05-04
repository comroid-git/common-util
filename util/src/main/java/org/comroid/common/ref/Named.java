package org.comroid.common.ref;

import java.io.IOException;
import java.util.Formattable;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.comroid.common.util.Bitmask;

public interface Named extends Formattable {
    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision) {
        String base = Bitmask.isFlagSet(flags, FormattableFlags.ALTERNATE) ? toString() : getName();

        if (precision != -1 && precision > base.length() && width < precision) {
            int precisionLeft = base.length() - precision;

            if (Bitmask.isFlagSet(flags, FormattableFlags.LEFT_JUSTIFY)) {
                base = base + IntStream.range(0, precisionLeft)
                        .mapToObj(x -> " ")
                        .collect(Collectors.joining());
            } else {
                base = IntStream.range(0, precisionLeft)
                        .mapToObj(x -> " ")
                        .collect(Collectors.joining()) + base;
            }
        }

        if (Bitmask.isFlagSet(flags, FormattableFlags.UPPERCASE)) {
            base = base.toUpperCase();
        }

        try {
            Appendable out = formatter.out();

            if (width != -1) {
                for (int i = 0; i < width; i++) {
                    out.append(base.charAt(i));
                }
            } else {
                out.append(base);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to append to formatter", e);
        }
    }

    String getName();

    class Base implements Named {
        private final String name;

        protected Base(String name) {
            this.name = name;
        }

        @Override
        public final String getName() {
            return name;
        }
    }
}
