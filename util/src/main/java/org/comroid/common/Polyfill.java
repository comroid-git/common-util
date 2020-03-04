package org.comroid.common;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.BiFunction;
import java.util.regex.Matcher;

import org.jetbrains.annotations.Nullable;

public final class Polyfill {
    public static String regexGroupOrDefault(Matcher matcher, String groupName, @Nullable String orDefault) {
        String cont;

        if (matcher.matches() && (cont = matcher.group(groupName)) != null) return cont;
        else if (orDefault != null) return orDefault;
        else throw new NullPointerException("Group cannot be matched!");
    }

    public static <T extends Throwable> URL url(String spec, @Nullable BiFunction<String, MalformedURLException, T> throwableReconfigurator) throws T {
        if (throwableReconfigurator == null)
            //noinspection unchecked
            throwableReconfigurator = (msg, cause) -> (T) new AssertionError(msg, cause);

        try {
            return new URL(spec);
        } catch (MalformedURLException e) {
            throw throwableReconfigurator.apply("Unexpected MalformedURLException", e);
        }
    }

    public static <T extends Throwable> URI uri(String spec, @Nullable BiFunction<String, URISyntaxException, T> throwableReconfigurator) throws T {
        if (throwableReconfigurator == null)
            //noinspection unchecked
            throwableReconfigurator = (msg, cause) -> (T) new AssertionError(msg, cause);

        try {
            return new URI(spec);
        } catch (URISyntaxException e) {
            throw throwableReconfigurator.apply("Unexpected URISyntaxException", e);
        }
    }
}
