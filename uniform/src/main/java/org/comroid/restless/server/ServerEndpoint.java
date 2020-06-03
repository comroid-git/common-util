package org.comroid.restless.server;

import org.comroid.restless.REST;
import org.comroid.restless.endpoint.RestEndpoint;

import java.util.regex.Pattern;

public interface ServerEndpoint extends RestEndpoint {
    @Override
    Pattern getPattern();

    EndpointHandler getHandler();

    REST.Method[] allowedMethods();

    interface Underlying extends ServerEndpoint {
        RestEndpoint getUnderlyingEndpoint();

        @Override
        default Pattern getPattern() {
            return getUnderlyingEndpoint().getPattern();
        }

        @Override
        default String getUrlBase() {
            return getUnderlyingEndpoint().getUrlBase();
        }

        @Override
        default String getUrlExtension() {
            return getUnderlyingEndpoint().getUrlExtension();
        }
    }
}
