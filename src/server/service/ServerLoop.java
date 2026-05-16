package server.service;
import common.CommandRequest;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

public class ServerLoop {
    private final ServerContext context;
    private final NetworkRequestHandler requestHandler;
    private volatile boolean running = true;

    public ServerLoop(ServerContext context, NetworkRequestHandler requestHandler) {
        this.context = context;
        this.requestHandler = requestHandler;
    }


    public void run() {
        ServerNetworkService network = context.getNetworkService();
        while (running) {
            List<SelectionKey> readyKeys = network.processEvents();
            for (SelectionKey key : readyKeys) {
                CommandRequest request = (CommandRequest) key.attachment();
                if (request == null) continue;
                key.attach(null);
                SocketChannel channel = (SocketChannel) key.channel();

                // Многопоточная обработка запроса через ForkJoinPool
                network.getProcessPool().submit(() -> {
                    try {
                        requestHandler.processRequest(channel, request);
                    } catch (Exception e) {
                        System.err.println("Ошибка обработки запроса: " + e.getMessage());
                    }
                });
            }
        }
    }
}