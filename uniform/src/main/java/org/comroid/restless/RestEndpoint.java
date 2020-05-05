package org.comroid.restless;

import org.comroid.common.Polyfill;
import org.comroid.common.ref.StaticCache;
import org.comroid.common.util.ArrayUtil;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface RestEndpoint extends Predicate<String> {
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

    default boolean test(URL url) {
        return test(url.toExternalForm());
    }

    default boolean test(URI uri) {
        return test(uri.toString());
    }

    @Override
    default boolean test(String url) {
        return getPattern().matcher(url)
                .replaceAll(processExtractionUrl(getGroupFx()))
                .equals(url);
    }

    default Object[] extractArgs(URL url) {
        return extractArgs(url.toExternalForm());
    }

    default Object[] extractArgs(URI uri) {
        return extractArgs(uri.toString());
    }

    default Object[] extractArgs(String requestUrl) {
        final Matcher matcher = getPattern().matcher(requestUrl);
        final IntUnaryOperator fx = getGroupFx();

        if (matcher.matches() && test(requestUrl)) {
            int x = -1;
            List<Object> yields = new ArrayList<>();

            while ((x = fx.applyAsInt(x)) != -1 && matcher.matches())
                yields.add(matcher.group(x));

            return yields.toArray();
        }

        return ArrayUtil.empty();
    }

    default String processExtractionUrl(IntUnaryOperator groupFx) {
        // todo: Inspect overhead
        return StaticCache.access(this, String.class, () -> {
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
        });
    }

    default <T> T makeAndValidateUrl(Function<String, T> maker, Object... args) {
        if (args.length != getParameterCount()) {
            throw new IllegalArgumentException("Invalid argument count");
        }

        T url = maker.apply(String.format(getFullUrl(), args));
        if ((url instanceof URL && test((URL) url))
                || (url instanceof URI && test((URI) url))) {
            return url;
        }

        throw new IllegalArgumentException("Generated URL is invalid");
    }
}
