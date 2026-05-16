package server.service;
import common.CommandRequest;
import common.CommandResponse;
import common.CommandType;
import common.Serializer;
import server.commands.Command;
import server.commands.CommandsList;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class ServerNetworkService {
    private ServerSocketChannel serverChannel;
    private final Selector selector;
    private final int port;
    private final CommandsList commandsList;
    private final Map<SocketChannel, ClientData> clients = new ConcurrentHashMap<>();

    // Пулы потоков
    private final ExecutorService readPool = Executors.newFixedThreadPool(4);
    private final ForkJoinPool processPool = new ForkJoinPool();
    private final ForkJoinPool sendPool = new ForkJoinPool();

    public ForkJoinPool getProcessPool() { return processPool; }

    public static class ClientData {
        public ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
        public ByteBuffer dataBuffer;
        public int expectedSize = -1;
        public boolean readingSize = true;
        public boolean initialized = false;
        private final int maxpack = 1000;
        public final Queue<ByteBuffer> writeQueue = new ArrayDeque<>();
        public ByteBuffer currentWriteBuffer = null;
        public void reset() {
            sizeBuffer.clear(); dataBuffer = null; expectedSize = -1; readingSize = true;
        }
    }

    public ServerNetworkService(int port, CommandsList commandsList) {
        this.port = port;
        this.commandsList = commandsList;
        try { selector = Selector.open(); }
        catch (IOException e) { throw new RuntimeException("Не удалось создать селектор", e); }
    }

    public void queueResponse(SocketChannel clientChannel, CommandResponse response) {
        try {
            byte[] data = Serializer.serialize(response);
            ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
            sizeBuffer.putInt(data.length); sizeBuffer.flip();
            ByteBuffer message = ByteBuffer.allocate(4 + data.length);
            message.put(sizeBuffer); message.put(data); message.flip();

            ClientData client = clients.get(clientChannel);
            if (client != null) {
                synchronized (client.writeQueue) {
                    client.writeQueue.offer(message);
                }
                SelectionKey key = clientChannel.keyFor(selector);
                if (key != null && key.isValid()) {
                    key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                    selector.wakeup(); // Будим селектор, чтобы он сразу отработал OP_WRITE
                }
            }
        } catch (IOException e) {
            removeClient(clientChannel);
        }
    }

    public boolean start() {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Сервер запущен на порту " + port + ", ожидание подключений...");
            return true;
        } catch (IOException e) {
            System.out.println("Не удалось запустить сервер: " + e.getMessage());
            return false;
        }
    }

    public List<SelectionKey> processEvents() {
        try {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            List<SelectionKey> readyKeys = new ArrayList<>();
            while (keys.hasNext()) {
                SelectionKey key = keys.next(); keys.remove();
                if (!key.isValid()) continue;
                if (key.isAcceptable()) handleAccept(key);
                else if (key.isReadable()) handleRead(key);
                else if (key.isWritable()) handleWrite(key);
                if (key.attachment() instanceof CommandRequest) readyKeys.add(key);
            }
            return readyKeys;
        } catch (IOException e) {
            System.out.println("Ошибка обработки событий: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            clients.put(clientChannel, new ClientData());
            System.out.println("Клиент подключён: " + clientChannel.getRemoteAddress());
            sendCommandsMap(clientChannel);
        }
    }

    private void sendCommandsMap(SocketChannel clientChannel) {
        Map<String, CommandType> commandsMap = new HashMap<>();
        for (Map.Entry<String, Command> entry : commandsList.getCommandList().entrySet())
            commandsMap.put(entry.getKey(), entry.getValue().getType());
        CommandResponse initResponse = new CommandResponse(true, "connected", commandsMap);
        queueResponse(clientChannel, initResponse);
        ClientData data = clients.get(clientChannel);
        if (data != null) data.initialized = true;
    }

    public CommandRequest readFromClient(SocketChannel clientChannel) {
        ClientData data = clients.get(clientChannel);
        if (data == null) return null;
        try {
            if (data.readingSize) {
                while (data.sizeBuffer.hasRemaining()) {
                    int bytesRead = clientChannel.read(data.sizeBuffer);
                    if (bytesRead == -1) { removeClient(clientChannel); return null; }
                    if (bytesRead == 0) return null; // Данные не готовы, вернёмся при следующем OP_READ
                }
                data.sizeBuffer.flip();
                data.expectedSize = data.sizeBuffer.getInt();
                data.sizeBuffer.clear();

                if (data.expectedSize <= 0 || data.expectedSize > data.maxpack) {
                    System.out.println("Некорректный размер сообщения: " + data.expectedSize);
                    removeClient(clientChannel); return null;
                }
                data.dataBuffer = ByteBuffer.allocate(data.expectedSize);
                data.readingSize = false;
            }

            while (data.dataBuffer.hasRemaining()) {
                int bytesRead = clientChannel.read(data.dataBuffer);
                if (bytesRead == -1) { removeClient(clientChannel); return null; }
                if (bytesRead == 0) return null; // Защита от бесконечного цикла
            }

            data.dataBuffer.flip();
            byte[] bytes = new byte[data.expectedSize];
            data.dataBuffer.get(bytes);
            data.reset();
            return (CommandRequest) Serializer.deserialize(bytes);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка чтения от клиента: " + e.getMessage());
            removeClient(clientChannel);
            return null;
        }
    }

    // Чтение делегируется в FixedThreadPool

    private void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ClientData data = clients.get(clientChannel);
        if (data == null) return;

        // Читаем В ТОМ ЖЕ ПОТОКЕ, чтобы request был готов до проверки в ServerLoop
        try {
            CommandRequest request = readFromClient(clientChannel);
            if (request != null) {
                key.attach(request);
            }
        } catch (Exception e) {
            System.err.println("Ошибка чтения: " + e.getMessage());
        }
    }

    // Запись делегируется в ForkJoinPool
    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ClientData client = clients.get(clientChannel);
        if (client == null) { key.cancel(); return; }
        sendPool.submit(() -> {
            try {
                synchronized (client.writeQueue) {
                    if (client.currentWriteBuffer == null) client.currentWriteBuffer = client.writeQueue.poll();
                    while (client.currentWriteBuffer != null && client.currentWriteBuffer.hasRemaining()) {
                        if (clientChannel.write(client.currentWriteBuffer) == -1) { removeClient(clientChannel); return; }
                    }
                    if (client.currentWriteBuffer != null) client.currentWriteBuffer = null;
                    if (client.writeQueue.isEmpty() && key.isValid())
                        key.interestOps(SelectionKey.OP_READ);
                }
            } catch (Exception e) { System.err.println("Ошибка отправки: " + e.getMessage()); }
        });
    }

    public void removeClient(SocketChannel clientChannel) {
        if (clientChannel != null) {
            clients.remove(clientChannel);
            try { clientChannel.close(); } catch (IOException ignored) {}
            System.out.println("Клиент отключён (осталось: " + clients.size() + ")");
        }
    }

    public void stop() {
        readPool.shutdownNow();
        processPool.shutdownNow();
        sendPool.shutdownNow();
        for (SocketChannel client : clients.keySet()) {
            try { client.close(); } catch (IOException ignored) {}
        }
        clients.clear();
        try {
            if (serverChannel != null && serverChannel.isOpen()) serverChannel.close();
            if (selector != null && selector.isOpen()) selector.close();
            System.out.println("Сервер остановлен");
        } catch (IOException e) { System.out.println("Ошибка при остановке сервера: " + e.getMessage()); }
    }
}