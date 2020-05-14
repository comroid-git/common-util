package org.comroid.restless.socket;

import org.comroid.listnr.Listnr;
import org.comroid.listnr.ListnrCore;
import org.comroid.listnr.model.EventContainer;
import org.comroid.restless.socket.event.*;
import org.comroid.restless.socket.event.multipart.WebSocketEventPayload;
import org.comroid.restless.socket.event.multipart.WebSocketEventType;
import org.comroid.uniform.node.UniObjectNode;

public final class WebSocketEventHub extends ListnrCore<UniObjectNode, WebSocket, WebSocketEventType<? extends WebSocketEventPayload>, WebSocketEventPayload> {
    public final EventContainer<UniObjectNode, WebSocket, WebSocketEvent.Type<OpenEvent.Payload>, OpenEvent.Payload> Open;
    public final EventContainer<UniObjectNode, WebSocket, WebSocketEvent.Type<DataEvent.Payload>, DataEvent.Payload> Data;
    public final EventContainer<UniObjectNode, WebSocket, WebSocketEvent.Type<PingEvent.Payload>, PingEvent.Payload> Ping;
    public final EventContainer<UniObjectNode, WebSocket, WebSocketEvent.Type<PongEvent.Payload>, PongEvent.Payload> Pong;
    public final EventContainer<UniObjectNode, WebSocket, WebSocketEvent.Type<CloseEvent.Payload>, CloseEvent.Payload> Close;

    public WebSocketEventHub(WebSocket webSocket) {
        super(UniObjectNode.class, webSocket);

        this.Open = new OpenEvent.Container(webSocket).registerAt(this);
        this.Data = new DataEvent.Container(webSocket).registerAt(this);
        this.Ping = new PingEvent.Container(webSocket).registerAt(this);
        this.Pong = new PongEvent.Container(webSocket).registerAt(this);
        this.Close = new CloseEvent.Container(webSocket).registerAt(this);
    }

    public interface Attachable extends Listnr.Attachable<UniObjectNode, WebSocket, WebSocketEvent.Type<?>, WebSocketEvent.Payload<?>> {
    }
}
