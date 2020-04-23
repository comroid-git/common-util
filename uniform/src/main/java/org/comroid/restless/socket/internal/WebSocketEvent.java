package org.comroid.restless.socket.internal;

import org.comroid.listnr.Event;
import org.comroid.restless.socket.WebSocket;
import org.comroid.uniform.node.UniNode;

public class WebSocketEvent extends Event.Support.Abstract implements Event {
    public final WebSocket getWebSocket() {
        return webSocket;
    }
    private final WebSocket webSocket;
    private final UniNode   data;

    public WebSocketEvent(WebSocket webSocket, UniNode data) {
        super();
        this.webSocket = webSocket;
        this.data      = data;
    }
}
