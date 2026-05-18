package client.gui;

import client.logic.*;
import common.CommandResponse;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Locale;

public class GuiApp extends Application {

    private NetworkService networkService;
    private AllCommands allCommands;
    private LocalizationManager localization;
    private ComboBox<Locale> langComboBox;
    private Label statusLabel;  // ← ДОБАВИЛИ: поле для статуса
    private Label langLabel;    // ← ДОБАВИЛИ: поле для метки языка

    @Override
    public void start(Stage primaryStage) {
        localization = new LocalizationManager();

        BorderPane root = new BorderPane();
        HBox topPanel = createTopPanel();
        root.setTop(topPanel);

        StackPane center = new StackPane();
        statusLabel = createStatusLabel(localization.get("app.status.initializing")); // ← СОХРАНЯЕМ
        center.getChildren().add(statusLabel);
        root.setCenter(center);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle(localization.get("app.title"));
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setOnCloseRequest(e -> cleanup());
        primaryStage.show();

        startInitialization();
    }

    private HBox createTopPanel() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10));
        hbox.setAlignment(Pos.CENTER_RIGHT);

        langLabel = new Label(localization.get("main.lang.label")); // ← СОХРАНЯЕМ

        langComboBox = new ComboBox<>();
        langComboBox.getItems().setAll(localization.getAvailableLocales());
        langComboBox.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : localization.getLocaleDisplayName(item));
            }
        });
        langComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : localization.getLocaleDisplayName(item));
            }
        });

        langComboBox.setValue(localization.getCurrentLocale());

        langComboBox.setOnAction(e -> {
            Locale selected = langComboBox.getValue();
            if (selected != null) {
                localization.setLocale(selected);
                updateUITexts();
            }
        });

        hbox.getChildren().addAll(langLabel, langComboBox);
        return hbox;
    }

    private void updateUITexts() {
        // Обновляем ВСЕ тексты в UI при смене языка
        Stage stage = (Stage) langComboBox.getScene().getWindow();
        stage.setTitle(localization.get("app.title"));

        // Обновляем метку языка
        if (langLabel != null) {
            langLabel.setText(localization.get("main.lang.label"));
        }

        // Обновляем статус (если уже инициализирован)
        if (statusLabel != null && allCommands != null) {
            statusLabel.setText(localization.get("app.status.connected") +
                    " | " + localization.get("app.commands_loaded") + ": " +
                    allCommands.getCommandType("help"));
        }
    }

    private Label createStatusLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 18px;");
        return label;
    }

    // ← УБРАЛИ параметр statusLabel, теперь используем поле класса
    private void startInitialization() {
        Task<Void> initTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    updateMessage(localization.get("app.status.connecting"));

                    ClientConfig config = ClientConfig.defaultConfig();
                    networkService = new NetworkService(config.host(), config.port());

                    ConnectionInitializer initializer = new ConnectionInitializer(networkService, "connected");
                    CommandResponse initResponse = initializer.initialize();
                    if (initResponse == null) {
                        throw new RuntimeException(localization.get("error.handshake"));
                    }

                    updateMessage(localization.get("app.status.loading_commands"));

                    CommandRegistryLoader loader = new CommandRegistryLoader(networkService);
                    allCommands = loader.loadCommands(initResponse);
                    if (allCommands == null) {
                        throw new RuntimeException(localization.get("error.commands_load"));
                    }

                    updateMessage(localization.get("app.status.ready"));
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        };

        initTask.setOnSucceeded(e -> Platform.runLater(() -> {
            // ← Используем поле класса statusLabel
            statusLabel.setText(localization.get("app.status.connected") +
                    " | " + localization.get("app.commands_loaded") + ": " +
                    allCommands.getCommandType("help"));
        }));

        initTask.setOnFailed(e -> Platform.runLater(() -> {
            String error = initTask.getException() != null
                    ? initTask.getException().getMessage()
                    : localization.get("error.unknown");
            statusLabel.setText(localization.get("app.status.error") + ": " + error);
        }));

        Thread thread = new Thread(initTask, "Client-Init-Thread");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() {
        cleanup();
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