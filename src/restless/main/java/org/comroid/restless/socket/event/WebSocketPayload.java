package org.comroid.restless.socket.event;

import org.comroid.listnr.EventPayload;
import org.comroid.restless.socket.WebSocket;
import org.comroid.restless.socket.WebSocketData;
import org.comroid.uniform.node.UniNode;

public interface WebSocketPayload extends EventPayload {
    WebSocket getWebSocket();

    abstract class Abstract implements WebSocketPayload {
        protected final WebSocket webSocket;

        public final WebSocket getWebSocket() {
            return webSocket;
        }

        protected Abstract(WebSocket webSocket) {
            this.webSocket = webSocket;
        }
    }

    final class Open extends Abstract {
        protected Open(WebSocketData data) {
            super(data.getWebSocket());
        }
    }

    final class Data extends Abstract {
        private final UniNode node;

        public UniNode getBody() {
            return node;
        }

        protected Data(WebSocketData data) {
            super(data.getWebSocket());

            this.node = data.getBody();
        }
    }

    final class Ping extends Abstract {
        protected Ping(WebSocketData data) {
            super(data.getWebSocket());
        }
    }

    final class Pong extends Abstract {
        protected Pong(WebSocketData data) {
            super(data.getWebSocket());
        }
    }

    final class Error extends Abstract {
        private final Throwable throwable;

        public Throwable getThrowable() {
            return throwable;
        }

        protected Error(WebSocketData data) {
            super(data.getWebSocket());

            this.throwable = data.getError();
        }
    }

    final class Close extends Abstract {
        private final int closeCode;
        private final String reason;

        public int getCloseCode() {
            return closeCode;
        }

        public String getReason() {
            return reason;
        }

        protected Close(WebSocketData data) {
            super(data.getWebSocket());

            this.closeCode = data.getCloseCode();
            this.reason = data.getCloseReason();
        }
    }
}
