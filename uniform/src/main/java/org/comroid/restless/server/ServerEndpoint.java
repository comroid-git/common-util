package org.comroid.restless.server;

import org.comroid.restless.endpoint.RestEndpoint;

import java.util.regex.Pattern;

public interface ServerEndpoint extends RestEndpoint {
    @Override
    Pattern getPattern();

    EndpointHandler getHandler();

    interface Underlying extends ServerEndpoint {
        RestEndpoint getUnderlyingEndpoint();

        @Override
        default Pattern getPattern() {
            return getUnderlyingEndpoint().getPattern();
        }
    }
}
