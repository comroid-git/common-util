package org.comroid.restless.server;

import org.comroid.restless.REST;
import org.comroid.restless.endpoint.AccessibleEndpoint;

import java.util.regex.Pattern;

public interface ServerEndpoint extends AccessibleEndpoint {
    AccessibleEndpoint getUnderlyingEndpoint();

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

    @Override
    default String[] getRegExpGroups() {
        return getUnderlyingEndpoint().getRegExpGroups();
    }

    default REST.Response executeGET() throws RestEndpointException {
        throw new UnsupportedOperationException();
    }

    default REST.Response executePUT() throws RestEndpointException {
        throw new UnsupportedOperationException();
    }

    default REST.Response executePOST() throws RestEndpointException {
        throw new UnsupportedOperationException();
    }

    default REST.Response executePATCH() throws RestEndpointException {
        throw new UnsupportedOperationException();
    }

    default REST.Response executeDELETE() throws RestEndpointException {
        throw new UnsupportedOperationException();
    }

    default REST.Response executeHEAD() throws RestEndpointException {
        throw new UnsupportedOperationException();
    }
}
