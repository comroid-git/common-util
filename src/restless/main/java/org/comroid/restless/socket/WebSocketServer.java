package org.comroid.restless.socket;

import com.google.common.flogger.FluentLogger;
import org.comroid.api.UUIDContainer;
import org.comroid.dreadpool.loop.Infinite;
import org.comroid.dreadpool.loop.manager.LoopManager;
import org.comroid.listnr.EventManager;
import org.comroid.listnr.ListnrCore;
import org.comroid.mutatio.span.Span;
import org.comroid.restless.socket.event.WebSocketEvent;
import org.comroid.restless.socket.event.WebSocketPayload;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniNode;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class WebSocketServer extends UUIDContainer
        implements EventManager<WebSocketData, WebSocketEvent<WebSocketPayload.Data>, WebSocketPayload.Data> {
    public static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final SerializationAdapter<?, ?, ?> seriLib;
    private final Executor executor;
    private final ListnrCore listnrCore;
    private final Consumer<SocketHandler> additionalSocketHandler;
    private final ServerSocket socket;
    private final Span<SocketHandler> handlers = new Span<>();

    public WebSocketServer(
            SerializationAdapter<?, ?, ?> serializationAdapter,
            Executor executor,
            InetAddress adress,
            int port
    ) throws IOException {
        this(serializationAdapter, executor, it -> {
        }, adress, port);
    }

    public WebSocketServer(
            SerializationAdapter<?, ?, ?> serializationAdapter,
            Executor executor,
            Consumer<SocketHandler> additionalSocketHandler,
            InetAddress adress,
            int port
    ) throws IOException {
        this.seriLib = serializationAdapter;
        this.executor = executor;
        this.listnrCore = new ListnrCore(executor);
        this.additionalSocketHandler = additionalSocketHandler;
        this.socket = new ServerSocket(port, 0, adress);

        executor.execute(new SocketAcceptor());
    }

    @Override
    public ListnrCore listnr() {
        return listnrCore;
    }

    public void broadcast(UniNode data) {
        handlers.forEach(handler -> handler.enqueue(data.toString()));
    }

    @SuppressWarnings("FieldCanBeLocal")
    public final class SocketHandler extends UUIDContainer {
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
                    final String message = br.lines().collect(Collectors.joining());
                    final UniNode node = seriLib.createUniNode(message);
                    final WebSocketData data = WebSocketData.ofNode(null, node);

                    publish(WebSocketEvent.DATA, data);
                } catch (IOException e) {
                    logger.at(Level.SEVERE).withCause(e).log();
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
                    }
                }
            }
        };

        public Socket getSocket() {
            return socket;
        }

        public SocketHandler(Socket socket) {
            this.socket = socket;
            this.loopManager = LoopManager.start(2);

            loopManager.queue(reader);
            loopManager.queue(writer);
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
                    final SocketHandler handler = new SocketHandler(socket);

                    additionalSocketHandler.accept(handler);
                    handlers.add(handler);
                } catch (IOException e) {
                    logger.at(Level.SEVERE).withCause(e).log();
                }
            }
        }
    }
}
