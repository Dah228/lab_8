package client.logic;

import common.CommandRequest;
import common.CommandResponse;
import common.Serializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public class NetworkService {
    private SocketChannel channel;
    private final String host;
    private final int port;

    private final Object lock = new Object();

    public NetworkService(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(true); // Блокирующий режим
            channel.connect(new InetSocketAddress(host, port));
            System.out.println("Подключено к серверу " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.out.println("Не удалось подключиться к серверу: " + e.getMessage());
            return false;
        }
    }

    // Добавляем synchronized, чтобы два потока не писали в сокет одновременно
    public boolean send(CommandRequest request) {
        synchronized (lock) {
            try {
                byte[] data = Serializer.serialize(request);
                // Отправляем размер данных (4 байта) + сами данные
                ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
                sizeBuffer.order(ByteOrder.BIG_ENDIAN);
                sizeBuffer.putInt(data.length);
                sizeBuffer.flip();
                while (sizeBuffer.hasRemaining()) {
                    channel.write(sizeBuffer);
                }
                ByteBuffer buffer = ByteBuffer.wrap(data);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                return true;
            } catch (IOException e) {
                System.out.println("Ошибка отправки: " + e.getMessage());
                return false;
            }
        }
    }

    // Добавляем synchronized, чтобы два потока не читали из сокета одновременно
    public CommandResponse receive() {
        synchronized (lock) {
            try {
                // Читаем размер ответа (4 байта)
                ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
                while (sizeBuffer.hasRemaining()) {
                    if (channel.read(sizeBuffer) == -1) {
                        System.out.println("Сервер закрыл соединение");
                        return null;
                    }
                }
                sizeBuffer.flip();
                int dataSize = sizeBuffer.getInt();

                // Защита от рассинхронизации (если пришел "мусор" вместо размера)
                if (dataSize < 0 || dataSize > 10_000_000) {
                    System.out.println("Ошибка сети: рассинхронизация (size=" + dataSize + "). Переподключитесь.");
                    return null;
                }

                // Читаем данные
                ByteBuffer dataBuffer = ByteBuffer.allocate(dataSize);
                while (dataBuffer.hasRemaining()) {
                    if (channel.read(dataBuffer) == -1) {
                        System.out.println("Сервер закрыл соединение при чтении данных");
                        return null;
                    }
                }
                dataBuffer.flip();
                byte[] data = new byte[dataSize];
                dataBuffer.get(data);
                return (CommandResponse) Serializer.deserialize(data);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Ошибка получения ответа: " + e.getMessage());
                return null;
            }
        }
    }

    public void disconnect() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
                System.out.println("Отключено от сервера");
            }
        } catch (IOException e) {
            System.out.println("Ошибка при отключении: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return channel != null && channel.isOpen() && channel.isConnected();
    }
}