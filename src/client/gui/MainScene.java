package client.gui;
import client.logic.NetworkService;
import common.Vehicle;
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
    private Label userLabel;
    private ComboBox<Locale> langComboBox;
    private ScheduledExecutorService refreshScheduler;

    // Modern Clean Styles
    private static final String BG_COLOR = "-fx-background-color: #F4F6F8;";
    private static final String CARD_STYLE = "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 2);";

    // Buttons
    private static final String BTN_PRIMARY = "-fx-background-color: #2979FF; " +
            "-fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand;";
    private static final String BTN_PRIMARY_HOVER = "-fx-background-color: #1565C0;";
    private static final String BTN_SECONDARY = "-fx-background-color: white; " +
            "-fx-text-fill: #424242; " +
            "-fx-border-color: #E0E0E0; -fx-border-radius: 8; " +
            "-fx-background-radius: 8; -fx-cursor: hand;";
    private static final String BTN_SECONDARY_HOVER = "-fx-background-color: #FAFAFA; -fx-border-color: #BDBDBD;";

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

        HBox topPanel = createTopPanel();
        root.setTop(topPanel);

        // Центральная часть с разделителем
        SplitPane centerSplit = createCenterSplit();
        // По умолчанию таблица занимает 65%, график 35%
        centerSplit.setDividerPositions(0.65);
        root.setCenter(centerSplit);

        HBox bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);
        BorderPane.setMargin(bottomPanel, new Insets(15, 0, 0, 0));

        Scene scene = new Scene(root, 1300, 850);

        // === ГОРЯЧАЯ КЛАВИША ВЫХОДА (Ctrl+W) ===
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
                    // Автоматическое обновление графика вместе с таблицей
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

        hbox.getChildren().addAll(userLabel, spacer, langLabel, langComboBox);
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

        // Canvas теперь поддерживает зум и пан внутри контроллера
        javafx.scene.canvas.Canvas canvas = canvasController.createCanvas(600, 600);
        canvasController.setOnVehicleClicked(vehicle -> {
            if (vehicle != null) Platform.runLater(() -> commandHandler.executeEdit(vehicle));
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

        // Кнопки (Выход удален)
        Button btnAdd = createStyledButton(localization.get("btn.add"), true);
        Button btnRemove = createStyledButton(localization.get("btn.remove"), false);
        Button btnShuffle = createStyledButton(localization.get("btn.shuffle"), false);
        Button btnBuy = createStyledButton(localization.get("btn.buy"), false);
        Button btnBalance = createStyledButton(localization.get("btn.balance"), false);

        List<Button> buttons = List.of(btnAdd, btnRemove, btnShuffle, btnBuy, btnBalance);
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

        btnShuffle.setOnAction(e -> commandHandler.executeShuffle());
        btnBuy.setOnAction(e -> commandHandler.executeBuy());
        btnBalance.setOnAction(e -> commandHandler.executeShowBalance());

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
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.getDialogPane().setStyle("-fx-background-color: white;");
        alert.showAndWait();
    }

    private void updateUITexts() {
        userLabel.setText(localization.get("main.user.label") + " " + currentUserLogin);
        stage.setTitle(localization.get("app.title") + " - " + currentUserLogin);

        HBox bottomPanel = (HBox) ((BorderPane) stage.getScene().getRoot()).getBottom();
        if (bottomPanel != null) {
            int i = 0;
            for (javafx.scene.Node node : bottomPanel.getChildren()) {
                if (node instanceof Button btn) {
                    switch (i) {
                        case 0: btn.setText(localization.get("btn.add")); break;
                        case 1: btn.setText(localization.get("btn.remove")); break;
                        case 2: btn.setText(localization.get("btn.shuffle")); break;
                        case 3: btn.setText(localization.get("btn.buy")); break;
                        case 4: btn.setText(localization.get("btn.balance")); break;
                    }
                    i++;
                }
            }
        }
        if (tableController != null) tableController.updateLocalization();
    }

    // Метод для синхронизации графика с данными таблицы
    public void updateVisualization() {
        if (tableController != null && canvasController != null) {
            canvasController.updateData(tableController.getAllVehicles());
        }
    }
}