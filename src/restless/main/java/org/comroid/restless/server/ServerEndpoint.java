package org.comroid.restless.server;

import com.sun.net.httpserver.Headers;
import org.comroid.restless.HTTPStatusCodes;
import org.comroid.restless.REST;
import org.comroid.restless.endpoint.AccessibleEndpoint;
import org.comroid.uniform.node.UniNode;

import java.util.regex.Pattern;

public interface ServerEndpoint extends AccessibleEndpoint {
    AccessibleEndpoint getEndpointBase();

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

    default REST.Response executeMethod(
            REST.Method method,
            Headers headers,
            String[] urlParams,
            UniNode body
    ) {
        switch (method) {
            case GET:
                return executeGET(headers, urlParams, body);
            case PUT:
                return executePUT(headers, urlParams, body);
            case POST:
                return executePOST(headers, urlParams, body);
            case PATCH:
                return executePATCH(headers, urlParams, body);
            case DELETE:
                return executeDELETE(headers, urlParams, body);
            case HEAD:
                return executeHEAD(headers, urlParams, body);
        }

        throw new AssertionError("No such method: " + method);
    }

    default REST.Response executeGET(
            Headers headers,
            String[] urlParams,
            UniNode body
    ) throws RestEndpointException {
        throw new RestEndpointException(HTTPStatusCodes.METHOD_NOT_ALLOWED, "Method not supported: GET");
    }

    default REST.Response executePUT(
            Headers headers,
            String[] urlParams,
            UniNode body
    ) throws RestEndpointException {
        throw new RestEndpointException(HTTPStatusCodes.METHOD_NOT_ALLOWED, "Method not supported: PUT");
    }

    default REST.Response executePOST(
            Headers headers,
            String[] urlParams,
            UniNode body
    ) throws RestEndpointException {
        throw new RestEndpointException(HTTPStatusCodes.METHOD_NOT_ALLOWED, "Method not supported: POST");
    }

    default REST.Response executePATCH(
            Headers headers,
            String[] urlParams,
            UniNode body
    ) throws RestEndpointException {
        throw new RestEndpointException(HTTPStatusCodes.METHOD_NOT_ALLOWED, "Method not supported: PATCH");
    }

    default REST.Response executeDELETE(
            Headers headers,
            String[] urlParams,
            UniNode body
    ) throws RestEndpointException {
        throw new RestEndpointException(HTTPStatusCodes.METHOD_NOT_ALLOWED, "Method not supported: DELETE");
    }

    default REST.Response executeHEAD(
            Headers headers,
            String[] urlParams,
            UniNode body
    ) throws RestEndpointException {
        throw new RestEndpointException(HTTPStatusCodes.METHOD_NOT_ALLOWED, "Method not supported: HEAD");
    }
}
