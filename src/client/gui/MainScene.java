package client.gui;

import client.logic.NetworkService;
import common.Vehicle;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainScene {
    private final Stage stage;
    private final LocalizationManager localization;
    private final String currentUserLogin;
    private final String currentUserPassword;
    private final NetworkService networkService;
    private VehicleTableController tableController;
    private VehicleCanvasController canvasController;
    private CommandDialogHandler commandHandler;
    private Button depositButton;

    // Элементы верхней панели
    private Label userLabel;
    private Button balanceButton;
    private ComboBox<Locale> langComboBox;
    private VBox notificationContainer;

    private ScheduledExecutorService refreshScheduler;

    // Стили
    private static final String BG_COLOR = "-fx-background-color: #F4F6F8;";
    private static final String CARD_STYLE = "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 2);";
    private static final String BTN_PRIMARY = "-fx-background-color: #2979FF; " +
            "-fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand;";
    private static final String BTN_PRIMARY_HOVER = "-fx-background-color: #1565C0;";
    private static final String BTN_SECONDARY = "-fx-background-color: white; " +
            "-fx-text-fill: #424242; " +
            "-fx-border-color: #E0E0E0; -fx-border-radius: 8; " +
            "-fx-background-radius: 8; -fx-cursor: hand;";
    private static final String BTN_SECONDARY_HOVER = "-fx-background-color: #FAFAFA; -fx-border-color: #BDBDBD;";
    private static final String BTN_DANGER = "-fx-background-color: white; " +
            "-fx-text-fill: #D32F2F; " +
            "-fx-border-color: #EF9A9A; -fx-border-radius: 8; " +
            "-fx-background-radius: 8; -fx-cursor: hand;";
    private static final String BTN_DANGER_HOVER = "-fx-background-color: #FFEBEE; -fx-border-color: #D32F2F;";

    private static final String MODERN_CARD = "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 5);";

    private static final String MODERN_INPUT = "-fx-background-color: #F9FAFB; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #E5E7EB; " +
            "-fx-border-radius: 10; " +
            "-fx-border-width: 1.5; " +
            "-fx-padding: 12 16; " +
            "-fx-font-size: 14px;";

    public MainScene(Stage stage, LocalizationManager localization, NetworkService networkService,
                     String currentUserLogin, String currentUserPassword) {
        this.stage = stage;
        this.localization = localization;
        this.networkService = networkService;
        this.currentUserLogin = currentUserLogin;
        this.currentUserPassword = currentUserPassword;
        this.commandHandler = new CommandDialogHandler(networkService, localization,
                currentUserLogin, currentUserPassword);
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle(BG_COLOR);
        root.setPadding(new Insets(15));

        // Контейнер для уведомлений (поверх всего)
        notificationContainer = new VBox(10);
        notificationContainer.setAlignment(Pos.TOP_RIGHT);
        notificationContainer.setPadding(new Insets(15));
        notificationContainer.setMouseTransparent(true); // Пропускаем клики
        notificationContainer.setStyle("-fx-background-color: transparent;");

        HBox topPanel = createTopPanel();
        root.setTop(topPanel);

        SplitPane centerSplit = createCenterSplit();
        centerSplit.setDividerPositions(0.65);
        root.setCenter(centerSplit);

        HBox bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);
        BorderPane.setMargin(bottomPanel, new Insets(15, 0, 0, 0));

        // StackPane для наложения уведомлений
        StackPane mainContainer = new StackPane(root, notificationContainer);

        Scene scene = new Scene(mainContainer, 1300, 850);
        KeyCombination exitKey = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(exitKey, () -> handleExit());

        Platform.runLater(() -> commandHandler.executeShow());
        startAutoRefresh();

        return scene;
    }

    private void startAutoRefresh() {
        refreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AutoRefresh-" + currentUserLogin);
            t.setDaemon(true);
            return t;
        });
        refreshScheduler.scheduleAtFixedRate(() -> {
            if (stage.isShowing()) {
                Platform.runLater(() -> {
                    commandHandler.executeShowSilent();
                    updateVisualization();
                });
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void stopAutoRefresh() {
        if (refreshScheduler != null && !refreshScheduler.isShutdown()) {
            refreshScheduler.shutdownNow();
        }
    }

    private HBox createTopPanel() {
        HBox hbox = new HBox(20);
        hbox.setPadding(new Insets(12, 20, 12, 20));
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setStyle(CARD_STYLE);
        userLabel = new Label(localization.get("main.user.label") + " " + currentUserLogin);
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2979FF;");
// Кнопка баланса сверху
        balanceButton = new Button(localization.get("btn.balance"));
        balanceButton.setStyle(BTN_SECONDARY);
        balanceButton.setOnMouseEntered(e -> balanceButton.setStyle(BTN_SECONDARY_HOVER));
        balanceButton.setOnMouseExited(e -> balanceButton.setStyle(BTN_SECONDARY));
        balanceButton.setOnAction(e -> {
            String originalText = balanceButton.getText();
            balanceButton.setText("...");
            balanceButton.setDisable(true);
            new Thread(() -> {
                try {
                    common.CommandRequest req = new common.CommandRequest(
                            "show_balance",
                            java.util.List.of("show_balance"),
                            null,
                            true,
                            currentUserLogin,
                            currentUserPassword
                    );
                    networkService.send(req);
                    common.CommandResponse resp = networkService.receive();
                    Platform.runLater(() -> {
                        if (resp != null && resp.isSuccess()) {
                            String msg = resp.getMessage();
                            String balanceStr = msg.replace("Ваш баланс: ", "").trim();
                            balanceButton.setText(balanceStr + " ₽");
                        } else {
                            balanceButton.setText("Ошибка");
                        }
                        PauseTransition pause = new PauseTransition(Duration.seconds(3));
                        pause.setOnFinished(event -> {
                            balanceButton.setText(originalText);
                            balanceButton.setDisable(false);
                        });
                        pause.play();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        balanceButton.setText("Ошибка сети");
                        PauseTransition pause = new PauseTransition(Duration.seconds(3));
                        pause.setOnFinished(event -> {
                            balanceButton.setText(originalText);
                            balanceButton.setDisable(false);
                        });
                        pause.play();
                    });
                }
            }).start();
        });
// === НОВАЯ КНОПКА ПОПОЛНИТЬ ===
        depositButton = new Button(localization.get("btn.deposit"));
        depositButton.setStyle(BTN_SECONDARY);
        depositButton.setOnMouseEntered(e -> depositButton.setStyle(BTN_SECONDARY_HOVER));
        depositButton.setOnMouseExited(e -> depositButton.setStyle(BTN_SECONDARY));
        depositButton.setOnAction(e -> {
            if (commandHandler != null) {
                commandHandler.executeDeposit();
            }
        });
// ==============================
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label langLabel = new Label(localization.get("main.lang.label"));
        langLabel.setStyle("-fx-text-fill: #757575; -fx-font-weight: 500;");
        langComboBox = new ComboBox<>();
        langComboBox.getItems().setAll(localization.getAvailableLocales());
        langComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : localization.getLocaleDisplayName(item));
            }
        });
        langComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : localization.getLocaleDisplayName(item));
            }
        });
        langComboBox.setValue(localization.getCurrentLocale());
        langComboBox.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 6; -fx-background-radius: 6;");
        langComboBox.setOnAction(e -> {
            Locale selected = langComboBox.getValue();
            if (selected != null) {
                localization.setLocale(selected);
                updateUITexts();
            }
        });
        hbox.getChildren().addAll(userLabel, balanceButton, depositButton, spacer, langLabel, langComboBox);
        return hbox;
    }


    private SplitPane createCenterSplit() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        splitPane.setStyle("-fx-background-color: transparent;");

        // ЛЕВАЯ ЧАСТЬ: Таблица
        tableController = new VehicleTableController(localization);
        VBox tableContainer = tableController.createTablePane();
        tableContainer.setStyle(CARD_STYLE + "-fx-padding: 15;");
        if (commandHandler != null) commandHandler.setTableController(tableController);

        tableController.getTable().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
                if (selected != null) commandHandler.executeEdit(selected);
            }
        });

        // ПРАВАЯ ЧАСТЬ: Визуализация
        if (canvasController == null) {
            canvasController = new VehicleCanvasController(localization);
        }
        javafx.scene.canvas.Canvas canvas = canvasController.createCanvas(600, 600);

        canvasController.setOnVehicleClicked(vehicle -> {
            if (vehicle != null) {
                Platform.runLater(() -> {
                    tableController.getTable().getSelectionModel().select(vehicle);
                    commandHandler.executeEdit(vehicle);
                });
            }
        });

        VBox canvasContainer = new VBox(10);
        canvasContainer.setStyle(CARD_STYLE + "-fx-padding: 15;");
        Label visualTitle = new Label("Визуализация координат");
        visualTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #424242;");

        Pane canvasPane = new Pane(canvas);
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());

        canvasContainer.getChildren().addAll(visualTitle, canvasPane);
        VBox.setVgrow(canvasPane, Priority.ALWAYS);

        splitPane.getItems().addAll(tableContainer, canvasContainer);
        return splitPane;
    }

    private HBox createBottomPanel() {
        HBox hbox = new HBox(15);
        hbox.setPadding(new Insets(5));
        hbox.setAlignment(Pos.CENTER);
        hbox.setStyle("-fx-background-color: transparent;");

        Button btnAdd = createStyledButton(localization.get("btn.add"), true);
        Button btnRemove = createStyledButton(localization.get("btn.remove"), false);
        Button btnShuffle = createStyledButton(localization.get("btn.shuffle"), false);
        Button btnBuy = createStyledButton(localization.get("btn.buy"), false);
        Button btnClear = createStyledButton("Очистить", "danger");

        List<Button> buttons = List.of(btnAdd, btnRemove, btnShuffle, btnBuy, btnClear);
        for (Button btn : buttons) {
            HBox.setHgrow(btn, Priority.ALWAYS);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(45);
            btn.setFont(javafx.scene.text.Font.font(14));
        }

        btnAdd.setOnAction(e -> commandHandler.executeAdd());
        btnRemove.setOnAction(e -> {
            Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
            if (selected != null) {
                commandHandler.executeRemoveById(selected.getId());
            } else {
                showWarning("Выберите элемент в таблице для удаления!");
            }
        });

        // ИЗМЕНЕНИЕ 1: Перемешивание без алерта
        btnShuffle.setOnAction(e -> {
            // Просто отправляем команду, которая обновит таблицу
            commandHandler.executeShuffle();
        });

        btnBuy.setOnAction(e -> {
            Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
            if (selected != null) {
                commandHandler.executeBuy(selected.getId());
            } else {
                showWarning("Выберите элемент в таблице для покупки!");
            }
        });

        btnClear.setOnAction(e -> commandHandler.executeClear());

        hbox.getChildren().addAll(buttons);
        return hbox;
    }

    private Button createStyledButton(String text, boolean isPrimary) {
        Button btn = new Button(text);
        if (isPrimary) {
            btn.setStyle(BTN_PRIMARY);
            btn.setOnMouseEntered(e -> btn.setStyle(BTN_PRIMARY_HOVER));
            btn.setOnMouseExited(e -> btn.setStyle(BTN_PRIMARY));
        } else {
            btn.setStyle(BTN_SECONDARY);
            btn.setOnMouseEntered(e -> btn.setStyle(BTN_SECONDARY_HOVER));
            btn.setOnMouseExited(e -> btn.setStyle(BTN_SECONDARY));
        }
        return btn;
    }

    private Button createStyledButton(String text, String type) {
        Button btn = new Button(text);
        btn.setStyle(BTN_DANGER);
        btn.setOnMouseEntered(e -> btn.setStyle(BTN_DANGER_HOVER));
        btn.setOnMouseExited(e -> btn.setStyle(BTN_DANGER));
        return btn;
    }

    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(localization.get("app.title"));
        alert.setHeaderText(null);
        alert.setContentText(localization.get("confirm.exit"));
        alert.getDialogPane().setStyle("-fx-background-color: white;");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            stopAutoRefresh();
            if (networkService != null && networkService.isConnected()) networkService.disconnect();
            stage.close();
        }
    }

    private void showWarning(String msg) {
        ModernNotifications.showWarning(notificationContainer, msg);
    }

    public void showSuccessNotification(String msg) {
        ModernNotifications.showSuccess(notificationContainer, msg);
    }

    public void showErrorNotification(String msg) {
        ModernNotifications.showError(notificationContainer, msg);
    }

    public void showInfoNotification(String msg) {
        ModernNotifications.showInfo(notificationContainer, msg);
    }

    private void updateUITexts() {
        userLabel.setText(localization.get("main.user.label") + " " + currentUserLogin);
        stage.setTitle(localization.get("app.title") + " - " + currentUserLogin);
        if (tableController != null) tableController.updateLocalization();
        if (balanceButton != null) {
            if (!balanceButton.getText().contains("₽") && !balanceButton.getText().equals("...") && !balanceButton.getText().equals("Ошибка")) {
                balanceButton.setText(localization.get("btn.balance"));
            }
        }
    }

    public void updateVisualization() {
        if (tableController != null && canvasController != null) {
            canvasController.updateData(tableController.getAllVehicles());
        }
    }
}