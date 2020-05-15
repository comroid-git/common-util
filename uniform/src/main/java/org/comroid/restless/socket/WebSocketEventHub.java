package org.comroid.restless.socket;

import org.comroid.listnr.Listnr;
import org.comroid.listnr.ListnrCore;
import org.comroid.listnr.model.EventContainer;
import org.comroid.restless.socket.event.*;
import org.comroid.restless.socket.event.multipart.WebSocketEventPayload;
import org.comroid.restless.socket.event.multipart.WebSocketEventType;
import org.comroid.uniform.node.UniObjectNode;

public final class WebSocketEventHub extends ListnrCore<UniObjectNode, WebSocket,
        WebSocketEventType<? extends WebSocketEventPayload<?>>,
        WebSocketEventPayload<? extends WebSocketEventType<?>>
        > {
    public final EventContainer<UniObjectNode, WebSocket, OpenEvent.Type, OpenEvent.Payload> Open;
    public final EventContainer<UniObjectNode, WebSocket, DataEvent.Type, DataEvent.Payload> Data;
    public final EventContainer<UniObjectNode, WebSocket, PingEvent.Type, PingEvent.Payload> Ping;
    public final EventContainer<UniObjectNode, WebSocket, PongEvent.Type, PongEvent.Payload> Pong;
    public final EventContainer<UniObjectNode, WebSocket, CloseEvent.Type, CloseEvent.Payload> Close;

    public WebSocketEventHub(WebSocket webSocket) {
        super(UniObjectNode.class, webSocket);

        this.Open = OpenEvent.container(webSocket).registerAt(this);
        this.Data = DataEvent.container(webSocket).registerAt(this);
        this.Ping = PingEvent.container(webSocket).registerAt(this);
        this.Pong = PongEvent.container(webSocket).registerAt(this);
        this.Close = CloseEvent.container(webSocket).registerAt(this);
    }

    public interface Attachable extends Listnr.Attachable<UniObjectNode, WebSocket, WebSocketEventType<?>, WebSocketEventPayload<?>> {
    }
}
