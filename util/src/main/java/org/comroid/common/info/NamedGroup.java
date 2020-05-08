package org.comroid.common.info;

import org.comroid.common.ref.IntEnum;
import org.comroid.common.ref.Named;
import org.comroid.common.util.Bitmask;

public interface NamedGroup extends Named, IntEnum {
    static NamedGroup of(String name) {
        return of(name, Bitmask.nextFlag(1));
    }

    static NamedGroup of(String name, int value) {
        return new Support.Of(name, value);
    }

    @Override
    String getName();

    @Override
    int getValue();

    final class Support {
        private static final class Of extends Base implements NamedGroup {
            private final int value;

            private Of(String name, int value) {
                super(name);

                this.value = value;
            }

            @Override
            public int getValue() {
                return value;
            }
        }
    }
}