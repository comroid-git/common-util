package org.comroid.restless.socket;

import org.comroid.listnr.EventManager;
import org.comroid.restless.socket.event.WebSocketEvent;
import org.comroid.restless.socket.event.WebSocketPayload;

public interface WebSocket extends EventManager<WebSocketData, WebSocketEvent, WebSocketPayload> {
}
