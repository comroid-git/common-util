package org.comroid.restless.endpoint;

import org.comroid.common.Polyfill;
import org.comroid.common.info.NamedGroup;
import org.comroid.common.ref.StaticCache;
import org.comroid.common.util.ArrayUtil;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface RestEndpoint extends RatelimitedEndpoint, Predicate<String> {
    String getUrlBase();

    String getUrlExtension();

    String[] getRegExpGroups();

    default Pattern getPattern() {
        // todo: Inspect overhead
        return StaticCache.access(this, "pattern", this::buildUrlPattern);
    }

    default int getParameterCount() {
        return getUrlExtension().split("%s").length - 1;
    }

    /**
     * @return The complete, unformatted URL.
     */
    default String getFullUrl() {
        return getUrlBase() + getUrlExtension();
    }

    @Override
    default int getRatePerSecond() {
        return -1;
    }

    @Override
    default int getGlobalRatelimit() {
        return -1;
    }

    default CompleteEndpoint complete(Object... args) throws IllegalArgumentException {
        return CompleteEndpoint.of(this, string(args));
    }

    default String string(Object... args) throws IllegalArgumentException {
        if (args.length != getParameterCount()) {
            throw new IllegalArgumentException("Invalid argument count");
        }

        final String format = String.format(getFullUrl(), args);

        if (test(format))
            return format;

        throw new IllegalArgumentException("Generated spec is invalid");
    }

    default URL url(Object... args) throws IllegalArgumentException {
        return Polyfill.url(string(args));
    }

    default URI uri(Object... args) throws IllegalArgumentException {
        return Polyfill.uri(string(args));
    }

    default boolean test(URL url) {
        return test(url.toExternalForm());
    }

    default boolean test(URI uri) {
        return test(uri.toString());
    }

    @Override
    default boolean test(String url) {
        final String[] regExpGroups = getRegExpGroups();
        final String replacer = replacer(regExpGroups);
        
        if (regExpGroups.length == 0)
            return replacer.equals(url);
        else return getPattern()
                .matcher(url)
                .replaceAll(replacer)
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
        final String[] groups = getRegExpGroups();

        if (matcher.matches() && test(requestUrl)) {
            List<String> yields = new ArrayList<>();

            int i = 0;
            while (groups.length > i && matcher.matches())
                yields.add(matcher.group(i));

            return yields.toArray(new String[0]);
        }

        return ArrayUtil.empty();
    }

    @Internal
    default String replacer(String[] groups) {
        // todo: Inspect overhead
        return StaticCache.access(this, "replacer", () -> {
            String yield = getFullUrl();

            int i = 0;
            while (yield.contains("%s") && groups.length > i) {
                int fi = yield.indexOf("%s");
                yield = String.format("%s$%d%s",
                        yield.substring(0, fi),
                        ++i,
                        yield.substring(fi + 2)
                );
            }

            return yield;
        });
    }

    default Pattern buildUrlPattern() {
        final String[] regExpGroups = getRegExpGroups();

        if (regExpGroups != null && regExpGroups.length > 0)
            return Pattern.compile(String.format(getFullUrl(), Arrays.stream(regExpGroups)
                    .map(str -> String.format("(%s)", str))
                    .toArray()));
        return Pattern.compile(getFullUrl().replace("%s", "(.*)"));
    }
}
