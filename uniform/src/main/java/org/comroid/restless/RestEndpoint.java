package org.comroid.restless;

import org.comroid.common.Polyfill;

import java.net.URI;
import java.net.URL;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
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
        return getPattern().matcher(url.toExternalForm())
                .replaceAll(processEndpointUrl(getGroupFx()))
                .equals(url.toExternalForm());
    }

    default boolean testURI(URI uri) {
        return getPattern().matcher(uri.toString())
                .replaceAll(processEndpointUrl(getGroupFx()))
                .equals(uri.toString());
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
