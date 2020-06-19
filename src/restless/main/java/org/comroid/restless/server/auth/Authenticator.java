package org.comroid.restless.server.auth;

import com.sun.net.httpserver.Headers;
import org.comroid.restless.CommonHeaderNames;
import org.comroid.restless.REST;

import java.util.Arrays;
import java.util.function.BiPredicate;

public interface Authenticator<M extends AuthenticationMethod<S>, S> extends BiPredicate<S, REST.Header> {
    String[] DEFAULT_HEADER_NAMES = new String[]{CommonHeaderNames.AUTHORIZATION};

    default String[] getHeaderNames() {
        return DEFAULT_HEADER_NAMES;
    }

    M getMethod();

    @Override
    default boolean test(S secure, REST.Header header) {
        return getMethod().apply(secure).equals(header.getValue());
    }

    default boolean test(S secure, Headers headers) {
        return headers.entrySet()
                .stream()
                .filter(entry -> Arrays.binarySearch(getHeaderNames(), entry.getKey()) != -1)
                .map(entry -> new REST.Header(entry.getKey(), entry.getValue().get(0)))
                .findFirst()
                .filter(header -> test(secure, header))
                .isPresent();
    }
}
