package org.comroid.restless;

import java.net.URL;
import java.util.regex.Pattern;

import org.comroid.common.Polyfill;
import org.comroid.common.func.StringPattern;

public interface RestEndpoint {
    String getUrlBase();

    String getUrlExtension();

    StringPattern getPattern();

    default int getParameterCount() {
        return getUrlExtension().split("%s").length - 1;
    }

    default URL create(Object... args) throws IllegalArgumentException {
        if (args.length != getParameterCount())
            throw new IllegalArgumentException("Invalid argument count");

        return Polyfill.url(String.format(getUrlBase() + getUrlExtension(), args));
    }
}
