package org.comroid.common.func.pattern;

import java.util.function.Predicate;

import org.comroid.common.ref.Named;

import org.intellij.lang.annotations.Language;

public interface Block extends Named, Predicate<String> {
    @Override
    boolean test(String s);

    static Block chars(String name, CharacterCase charCase, int count) {
        return range(name, charCase.range, count);
    }

    static Block decimal(String name, int count) {
        return range(name, "0-9", count);
    }

    static Block hex(String name, int count) {
        return range(name, "0-9a-fA-F", count);
    }

    static Block range(String name, String range, int count) {
        return regex(name, String.format("[%s]{,%d}", range, count));
    }

    static Block regex(String name, @Language("RegExp") String regex) {
        return new Support.OfRegex(name, regex);
    }

    final class Support {
        public static class OfRegex extends Named.Base implements Block {
            private final String regex;

            public OfRegex(String name, @Language("RegExp") String regex) {
                super(name);

                this.regex = String.format("(%s)", regex);
            }

            @Override
            public boolean test(String str) {
                return str.matches(regex);
            }
        }
    }

    enum CharacterCase {
        LOWERCASE("a-z"),
        UPPERCASE("A-Z"),
        MIXED_CASE("a-zA-Z");

        private final String range;

        CharacterCase(String range) {
            this.range = range;
        }
    }
}
