package org.comroid.restless.socket;

import org.comroid.restless.socket.event.WebSocketEvent;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniValueNode;
import org.jetbrains.annotations.Nullable;

public interface WebSocketData {
    WebSocket getWebSocket();

    WebSocketEvent.Type getType();

    default UniNode getBody() {
        return UniValueNode.nullNode();
    }

    default @Nullable Throwable getError() {
        return null;
    }

    default int getCloseCode() {
        return 0;
    }

    default @Nullable String getCloseReason() {
        return null;
    }

    static WebSocketData empty(WebSocket socket, WebSocketEvent.Type type) {
        return new Support.Empty(socket, type);
    }

    static WebSocketData ofNode(WebSocket socket, UniNode node) {
        return new Support.OfNode(socket, node);
    }

    static WebSocketData error(WebSocket socket, Throwable throwable) {
        return new Support.OfError(socket, throwable);
    }

    static WebSocketData close(WebSocket socket, int statusCode, String reason) {
        return new Support.Close(socket, statusCode, reason);
    }

    final class Support {
        private static class Empty implements WebSocketData {
            private final WebSocketEvent.Type type;
            private final WebSocket webSocket;

            @Override
            public WebSocket getWebSocket() {
                return webSocket;
            }

            @Override
            public WebSocketEvent.Type getType() {
                return type;
            }

            private Empty(WebSocket webSocket, WebSocketEvent.Type type) {
                this.webSocket = webSocket;
                this.type = type;
            }
        }

        private static final class OfNode extends Empty {
            private final UniNode body;

            @Override
            public UniNode getBody() {
                return body;
            }

            private OfNode(WebSocket webSocket, UniNode body) {
                super(webSocket, WebSocketEvent.Type.DATA);

                this.body = body;
            }
        }

        public static class OfError extends Empty {
            private final Throwable throwable;

            @Override
            public @Nullable Throwable getError() {
                return throwable;
            }

            public OfError(WebSocket webSocket, Throwable throwable) {
                super(webSocket, WebSocketEvent.Type.ERROR);

                this.throwable = throwable;
            }
        }

        public static class Close extends Empty {
            private final int statusCode;
            private final String reason;

            public int getStatusCode() {
                return statusCode;
            }

            public String getReason() {
                return reason;
            }

            public Close(WebSocket webSocket, int statusCode, String reason) {
                super(webSocket, WebSocketEvent.Type.CLOSE);

                this.statusCode = statusCode;
                this.reason = reason;
            }
        }
    }
}
