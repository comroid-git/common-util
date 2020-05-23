package org.comroid.restless.server;

import org.comroid.restless.REST;
import org.comroid.uniform.node.UniNode;

public interface EndpointHandler {
    REST.Response handle(UniNode body, String[] urlArgs);
}
