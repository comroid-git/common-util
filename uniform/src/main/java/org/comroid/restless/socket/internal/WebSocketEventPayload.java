package org.comroid.restless.socket.internal;

import org.comroid.listnr.EventPayload;
import org.comroid.restless.socket.WebSocket;
import org.comroid.uniform.node.UniNode;

public class WebSocketEventPayload extends EventPayload.Support.Abstract implements EventPayload {
    private final WebSocket webSocket;
    private final UniNode   data;

    public WebSocketEventPayload(WebSocket webSocket, UniNode data) {
        super();
        this.webSocket = webSocket;
        this.data      = data;
    }

    public final WebSocket getWebSocket() {
        return webSocket;
    }
}
