package org.comroid.restless;

import org.comroid.common.Polyfill;
import org.comroid.common.util.ArrayUtil;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface RestEndpoint {
    String getUrlBase();

    String getUrlExtension();

    Pattern getPattern();

    IntUnaryOperator getGroupFx();

    default int getParameterCount() {
        return getUrlExtension().split("%s").length - 1;
    }

    default String getFullUrl() {
        return getUrlBase() + getUrlExtension();
    }

    default URL url(Object... args) throws IllegalArgumentException {
        return makeAndValidateUrl(Polyfill::url, args);
    }

    default URI uri(Object... args) throws IllegalArgumentException {
        return makeAndValidateUrl(Polyfill::uri, args);
    }

    default boolean testURL(URL url) {
        return checkURL(url) != null;
    }

    default boolean testURI(URI uri) {
        return checkURI(uri) != null;
    }

    default @Nullable Object[] checkURL(URL url) {
        return extractArgs(getPattern().matcher(url.toExternalForm()), url.toExternalForm());
    }

    default @Nullable Object[] checkURI(URI uri) {
        return extractArgs(getPattern().matcher(uri.toString()), uri.toString());
    }

    default String processEndpointUrl(IntUnaryOperator groupFx) {
        String yield = getFullUrl();
        int c = groupFx.applyAsInt(-1);

        while (yield.contains("%s")) {
            if (c == -1) {
                return yield;
            }

            int fi = yield.indexOf("%s");
            yield = String.format("%s$%d%s", yield.substring(0, fi), c, yield.substring(fi + 2));
            c = groupFx.applyAsInt(c);
        }

        return yield;
    }

    default @Nullable Object[] extractArgs(Matcher matcher, String expected) {
        if (matcher.matches() && matcher.replaceAll(processEndpointUrl(getGroupFx()))
                .equals(expected)) {
            final IntUnaryOperator fx = getGroupFx();
            int x = -1;
            List<Object> yields = new ArrayList<>();

            while ((x = fx.applyAsInt(x)) != -1 && matcher.matches())
                yields.add(matcher.group(x));

            return yields.toArray();
        }
        return null;
    }

    default <T> T makeAndValidateUrl(Function<String, T> maker, Object... args) {
        if (args.length != getParameterCount()) {
            throw new IllegalArgumentException("Invalid argument count");
        }

        T url = maker.apply(String.format(getFullUrl(), args));
        if ((url instanceof URL && testURL((URL) url)) || (url instanceof URI && testURI((URI) url))) {
            return url;
        }

        throw new IllegalArgumentException("Generated URL is invalid");
    }
}
