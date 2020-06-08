package org.comroid.restless.server;

import com.sun.net.httpserver.Headers;
import org.comroid.restless.HTTPStatusCodes;
import org.comroid.restless.REST;
import org.comroid.uniform.node.UniNode;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface EndpointHandler {
    default boolean supports(REST.Method method) {
        switch (method) {
            case GET:
                return isReimplemented("executeGET");
            case PUT:
                return isReimplemented("executePUT");
            case POST:
                return isReimplemented("executePOST");
            case PATCH:
                return isReimplemented("executePATCH");
            case DELETE:
                return isReimplemented("executeDELETE");
            case HEAD:
                return isReimplemented("executeHEAD");
        }

        throw new AssertionError("No such method: " + method);
    }

    default REST.Response executeMethod(
            REST.Method method,
            Headers headers,
            String[] urlParams,
            UniNode body
    ) throws RestEndpointException {
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

    @Internal
    default boolean isReimplemented(String methodName) {
        try {
            return !getClass().getMethod(methodName, Headers.class, String[].class, UniNode.class)
                    .getDeclaringClass()
                    .equals(EndpointHandler.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }
}
