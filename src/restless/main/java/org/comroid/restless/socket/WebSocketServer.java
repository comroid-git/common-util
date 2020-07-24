package org.comroid.restless.socket;

import com.google.common.flogger.FluentLogger;
import org.comroid.dreadpool.loop.Infinite;
import org.comroid.dreadpool.loop.manager.LoopManager;
import org.comroid.mutatio.span.Span;
import org.comroid.restless.REST;
import org.comroid.restless.REST.Header.List;
import org.comroid.restless.socket.event.WebSocketEvent;
import org.comroid.restless.socket.event.WebSocketPayload;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniNode;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class WebSocketServer extends AbstractEventManager<WebSocketData, WebSocketEvent<WebSocketPayload.Data>, WebSocketPayload.Data> {
    public static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final SerializationAdapter<?, ?, ?> seriLib;
    private final Executor executor;
    private final BiFunction<WebSocketServer, Socket, ? extends SocketHandler> handlerCreator;
    private final ServerSocket socket;
    private final Span<SocketHandler> handlers = new Span<>();

    public WebSocketServer(
            SerializationAdapter<?, ?, ?> serializationAdapter,
            Executor executor,
            InetAddress adress,
            int port
    ) throws IOException {
        this(serializationAdapter, executor, SocketHandler::new, adress, port);
    }

    public WebSocketServer(
            SerializationAdapter<?, ?, ?> serializationAdapter,
            Executor executor,
            BiFunction<WebSocketServer, Socket, ? extends SocketHandler> socketHandlerCreator,
            InetAddress adress,
            int port
    ) throws IOException {
        super(new ListnrCore(executor));

        this.seriLib = serializationAdapter;
        this.executor = executor;
        this.handlerCreator = socketHandlerCreator;
        this.socket = new ServerSocket(port, 0, adress);

        executor.execute(new SocketAcceptor());
    }

    public void broadcast(UniNode data) {
        handlers.forEach(handler -> handler.enqueue(data.toString()));
    }

    @SuppressWarnings("FieldCanBeLocal")
    public static class SocketHandler
            extends AbstractEventManager<WebSocketData, WebSocketEvent<WebSocketPayload.Data>, WebSocketPayload.Data> {
        private final WebSocketServer socketServer;
        private final Socket socket;
        private final LoopManager loopManager;
        private final Queue<String> messageQueue = new LinkedBlockingQueue<>();
        private final Infinite<Void> reader = new Infinite<Void>(0, null) {
            @Override
            protected void execute() {
                try (
                        InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                        BufferedReader br = new BufferedReader(isr)
                ) {
                    final String message = br.lines().collect(Collectors.joining("\n"));
                    System.out.println("message = " + message);
                    final UniNode node = socketServer.seriLib.createUniNode(message);
                    final WebSocketData data = WebSocketData.ofNode(null, node);

                    publish(WebSocketEvent.DATA, data);
                } catch (IOException e) {
                    logger.at(Level.SEVERE).withCause(e).log();
                    loopManager.close();
                }
            }
        };
        private final Infinite<Void> writer = new Infinite<Void>(0, null) {
            @Override
            protected void execute() {
                synchronized (messageQueue) {
                    while (messageQueue.isEmpty()) {
                        try {
                            messageQueue.wait();
                        } catch (InterruptedException e) {
                            logger.at(Level.SEVERE)
                                    .withCause(e)
                                    .log("Could not wait for new messages");
                        }
                    }

                    final String message = messageQueue.poll();

                    try (
                            OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
                            PrintWriter pw = new PrintWriter(osw)
                    ) {
                        pw.println(message);
                    } catch (IOException e) {
                        logger.at(Level.SEVERE).withCause(e).log();
                        loopManager.close();
                    }
                }
            }
        };

        public Socket getSocket() {
            return socket;
        }

        private final CompletableFuture<REST.Header.List> initialHeaders;

        public CompletableFuture<List> getInitialHeaders() {
            return initialHeaders;
        }

        public SocketHandler(WebSocketServer socketServer, Socket socket) {
            super(socketServer);

            this.socketServer = socketServer;
            this.socket = socket;
            this.loopManager = LoopManager.start(2);

            this.initialHeaders = waitForHeaders()
                    .thenApply(headers -> {
                        loopManager.queue(reader);
                        loopManager.queue(writer);
                        return headers;
                    });
        }

        private CompletableFuture<REST.Header.List> waitForHeaders() {
            return CompletableFuture.supplyAsync(() -> {
                try (
                        InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                        BufferedReader br = new BufferedReader(isr)
                ) {
                    final List headers = new List();
                    br.lines()
                            .skip(1)
                            .map(str -> str.split(": "))
                            .filter(box -> !box[0].isEmpty())
                            .forEach(box -> headers.add(box[0], String.join(": ", Arrays.copyOfRange(box, 1, box.length))));
                    return headers;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public void enqueue(String message) {
            synchronized (messageQueue) {
                messageQueue.add(message);
                messageQueue.notify();
            }
        }
    }

    private final class SocketAcceptor implements Runnable {
        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    final Socket socket = WebSocketServer.this.socket.accept();
                    final SocketHandler handler = handlerCreator.apply(WebSocketServer.this, socket);
                    handlers.add(handler);
                } catch (IOException e) {
                    logger.at(Level.SEVERE).withCause(e).log();
                }
            }
        }
    }
}
