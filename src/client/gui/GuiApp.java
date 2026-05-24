package client.gui;

import client.logic.*;
import common.CommandResponse;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GuiApp extends Application {

    private NetworkService networkService;
    private AllCommands allCommands;
    private LocalizationManager localization;
    private String currentUserLogin;
    private String currentUserPassword;

    @Override
    public void start(Stage primaryStage) {
        localization = new LocalizationManager();

        // Временная сцена загрузки
        StackPane loadingRoot = new StackPane();
        Label statusLabel = new Label(localization.get("app.status.initializing"));
        loadingRoot.getChildren().add(statusLabel);
        Scene loadingScene = new Scene(loadingRoot, 900, 600);
        primaryStage.setTitle(localization.get("app.title"));
        primaryStage.setScene(loadingScene);
        primaryStage.setResizable(true);
        primaryStage.show();

        // Фоновая инициализация сети
        Task<Void> initTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    updateMessage(localization.get("app.status.connecting"));
                    ClientConfig config = ClientConfig.defaultConfig();
                    networkService = new NetworkService(config.host(), config.port());
                    ConnectionInitializer initializer = new ConnectionInitializer(networkService, "connected");
                    CommandResponse initResponse = initializer.initialize();
                    if (initResponse == null) throw new RuntimeException(localization.get("error.handshake"));
                    updateMessage(localization.get("app.status.loading_commands"));
                    CommandRegistryLoader loader = new CommandRegistryLoader(networkService);
                    allCommands = loader.loadCommands(initResponse);
                    if (allCommands == null) throw new RuntimeException(localization.get("error.commands_load"));
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        initTask.setOnSucceeded(e -> showAuthScene(primaryStage));
        initTask.setOnFailed(e -> {
            String error = initTask.getException() != null ? initTask.getException().getMessage() : localization.get("error.unknown");
            statusLabel.setText(localization.get("app.status.error") + ": " + error);
        });

        Thread thread = new Thread(initTask, "Client-Init-Thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void showAuthScene(Stage stage) {
        AuthScene authScene = new AuthScene(stage, networkService, localization);
        authScene.setOnLoginSuccess(() -> {
            this.currentUserLogin = authScene.getLoginText();
            this.currentUserPassword = authScene.getPasswordText();
            System.out.println("Переход на главную сцену для пользователя: " + currentUserLogin);
            showMainScene(stage);
        });
        Scene scene = authScene.createScene();
        stage.setScene(scene);
        stage.sizeToScene();
    }

    private void showMainScene(Stage stage) {
        MainScene mainScene = new MainScene(stage, localization, networkService,
                currentUserLogin, currentUserPassword);
        Scene scene = mainScene.createScene();
        stage.setScene(scene);
        stage.setTitle(localization.get("app.title") + " - " + currentUserLogin);
        stage.setMinWidth(1200);
        stage.setMinHeight(800);


    }

    private void cleanup() {
        if (networkService != null && networkService.isConnected()) {
            System.out.println(localization.get("app.status.disconnecting"));
            networkService.disconnect();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}