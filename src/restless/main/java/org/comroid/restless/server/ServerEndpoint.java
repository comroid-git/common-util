package org.comroid.restless.server;

import com.sun.net.httpserver.Headers;
import org.comroid.restless.HTTPStatusCodes;
import org.comroid.restless.REST;
import org.comroid.restless.endpoint.AccessibleEndpoint;
import org.comroid.uniform.node.UniNode;

import java.util.regex.Pattern;

public interface ServerEndpoint extends AccessibleEndpoint, EndpointHandler {
    AccessibleEndpoint getEndpointBase();

    default boolean allowMemberAccess() {
        return false;
    }

    @Override
    default Pattern getPattern() {
        return getEndpointBase().getPattern();
    }

    @Override
    default String getUrlBase() {
        return getEndpointBase().getUrlBase();
    }

    @Override
    default String getUrlExtension() {
        return getEndpointBase().getUrlExtension();
    }

    @Override
    default String[] getRegExpGroups() {
        return getEndpointBase().getRegExpGroups();
    }

    @Override
    default ServerEndpoint attachHandler(EndpointHandler handler) {
        // todo: implement a handler chain in Support.Combined to allow retrying with another handler if failed
        throw new UnsupportedOperationException("Cannot attach Handler to ServerEndpoint");
    }

    static ServerEndpoint combined(AccessibleEndpoint accessibleEndpoint, EndpointHandler handler) {
        return new Support.Combined(accessibleEndpoint, handler);
    }

    final class Support {
        private static final class Combined implements ServerEndpoint {
            private final AccessibleEndpoint accessibleEndpoint;
            private final EndpointHandler handler;

            @Override
            public AccessibleEndpoint getEndpointBase() {
                return accessibleEndpoint;
            }

            public Combined(AccessibleEndpoint accessibleEndpoint, EndpointHandler handler) {
                this.accessibleEndpoint = accessibleEndpoint;
                this.handler = handler;
            }

            @Override
            public REST.Response executeMethod(REST.Method method, Headers headers, String[] urlParams, UniNode body) {
                return handler.executeMethod(method, headers, urlParams, body);
            }
        }
    }
}
