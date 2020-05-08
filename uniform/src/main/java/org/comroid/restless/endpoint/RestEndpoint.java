package org.comroid.restless.endpoint;

import org.comroid.common.Polyfill;
import org.comroid.common.info.NamedGroup;
import org.comroid.common.ref.StaticCache;
import org.comroid.common.util.ArrayUtil;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface RestEndpoint extends RatelimitedEndpoint, Predicate<String> {
    String getUrlBase();

    String getUrlExtension();

    default Pattern getPattern() {
        // todo: Inspect overhead
        return StaticCache.access(this, Pattern.class,
                () -> Pattern.compile(getFullUrl().replace("%s", ".*")));
    }

    List<? extends NamedGroup> getGroups();

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
                .replaceAll(processExtractionUrl(getGroups()))
                .equals(url);
    }

    default String[] extractArgs(URL url) {
        return extractArgs(url.toExternalForm());
    }

    default String[] extractArgs(URI uri) {
        return extractArgs(uri.toString());
    }

    default String[] extractArgs(String requestUrl) {
        final Matcher matcher = getPattern().matcher(requestUrl);
        final List<? extends NamedGroup> groups = getGroups();

        if (matcher.matches() && test(requestUrl)) {
            List<String> yields = new ArrayList<>();

            int i = 0;
            while (groups.size() > i && matcher.matches())
                yields.add(matcher.group(groups.get(i++).getName()));

            return yields.toArray(new String[0]);
        }

        return ArrayUtil.empty();
    }

    @Override
    default int getRatePerSecond() {
        return -1;
    }

    @Override
    default int getGlobalRatelimit() {
        return -1;
    }

    default String processExtractionUrl(List<? extends NamedGroup> groups) {
        // todo: Inspect overhead
        return StaticCache.access(this, String.class, () -> {
            String yield = getFullUrl();

            int i = 0;
            while (yield.contains("%s") && groups.size() > i) {
                int fi = yield.indexOf("%s");
                yield = String.format("%s$%d%s",
                        yield.substring(0, fi),
                        groups.get(i++).getValue(),
                        yield.substring(fi + 2)
                );
            }

            return yield;
        });
    }

    default <T> T makeAndValidateUrl(Function<String, T> maker, Object... args) {
        if (args.length != getParameterCount()) {
            throw new IllegalArgumentException("Invalid argument count");
        }

        final String format = String.format(getFullUrl(), args);
        final T made = maker.apply(format);

        if ((made instanceof URL && test((URL) made)) || (made instanceof URI && test((URI) made))) {
            return made;
        } else if (test(format)) return made;

        throw new IllegalArgumentException("Generated URL is invalid");
    }
}
