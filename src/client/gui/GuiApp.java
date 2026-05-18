package client.gui;

import client.*;
import client.logic.ClientConfig;
import client.logic.CommandRegistryLoader;
import client.logic.ConnectionInitializer;
import client.logic.NetworkService;
import common.CommandResponse;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Точка входа в GUI-клиент.
 * Заменяет консольный Main.java для графического режима.
 */
public class GuiApp extends Application {

    private NetworkService networkService;
    private AllCommands allCommands;

    @Override
    public void start(Stage primaryStage) {
        // 1. Базовая сцена-заглушка
        StackPane root = new StackPane();
        Label statusLabel = new Label("Инициализация подключения...");
        statusLabel.setStyle("-fx-font-size: 18px;");
        root.getChildren().add(statusLabel);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("Vehicle Manager Client");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();

        // 2. Сетевая инициализация в фоновом потоке
        Task<Void> initTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Подключение к серверу...");
                ClientConfig config = ClientConfig.defaultConfig();
                networkService = new NetworkService(config.host(), config.port());

                ConnectionInitializer initializer = new ConnectionInitializer(networkService, "connected");
                CommandResponse initResponse = initializer.initialize();

                if (initResponse == null) {
                    throw new RuntimeException("Сервер не ответил на рукопожатие");
                }

                updateMessage("Загрузка команд...");
                CommandRegistryLoader registryLoader = new CommandRegistryLoader(networkService);
                allCommands = registryLoader.loadCommands(initResponse);

                if (allCommands == null) {
                    throw new RuntimeException("Ошибка загрузки карты команд");
                }

                updateMessage("Готово! Переход к авторизации...");
                return null;
            }
        };

        // 3. Обработка успешного завершения (выполняется в JavaFX Thread)
        initTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("✅ Подключено. Загружено команд: " + allCommands.getCommandType("help"));
                // В следующих этапах здесь будет вызов AuthScene
            });
        });

        // 4. Обработка ошибок
        initTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                String error = initTask.getException() != null ? initTask.getException().getMessage() : "Неизвестная ошибка";
                statusLabel.setText("❌ Ошибка подключения: " + error);
                // Можно добавить кнопку "Повторить" или завершить приложение
            });
        });

        // 5. Запуск в отдельном потоке
        Thread thread = new Thread(initTask, "Client-Init-Thread");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() {
        // Корректное закрытие сети при выходе из приложения
        if (networkService != null && networkService.isConnected()) {
            System.out.println("Закрытие сетевого соединения...");
            networkService.disconnect();
        }
    }

    // Запуск через JavaFX Application Thread
    public static void main(String[] args) {
        launch(args);
    }
}