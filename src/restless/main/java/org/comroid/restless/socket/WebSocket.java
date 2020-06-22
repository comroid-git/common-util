package org.comroid.restless.socket;

import org.comroid.listnr.EventManager;
import org.comroid.restless.socket.event.WebSocketEvent;

public interface WebSocket extends EventManager<WebSocketEvent, WebSocketEventPayload> {

}
