package client.gui;
import client.logic.NetworkService;
import common.Vehicle;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

    // Фоновый планировщик для автообновления (требование 3 и 4)
    private ScheduledExecutorService refreshScheduler;

    // Стили для розовой темы
    private static final String BTN_BASE_STYLE = "-fx-background-color: linear-gradient(to bottom, #ffc0cb, #ff69b4); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8; " +
            "-fx-border-color: #ff1493; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(255,105,180,0.4), 5, 0, 0, 2); " +
            "-fx-cursor: hand;";

    private static final String BTN_HOVER_STYLE = "-fx-background-color: linear-gradient(to bottom, #ff69b4, #ff1493); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8; " +
            "-fx-border-color: #c71585; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(255,20,147,0.6), 8, 0, 0, 3); " +
            "-fx-cursor: hand;";

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
        root.setPadding(new Insets(10));

        HBox topPanel = createTopPanel();
        root.setTop(topPanel);

        VBox centerBox = createCenterBox();
        root.setCenter(centerBox);

        HBox bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 1200, 800);

        // Требование 4: загрузка данных сразу при открытии окна
        Platform.runLater(() -> commandHandler.executeShow());

        // Требование 3: оповещение клиентов об изменениях (авто-поллинг каждые 5 сек)
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
                    // Тихий запрос данных без показа диалоговых окон
                    commandHandler.executeShowSilent();
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
        HBox hbox = new HBox(15);
        hbox.setPadding(new Insets(10));
        hbox.setAlignment(Pos.CENTER_LEFT);
        // Розовый градиент для верхней панели
        hbox.setStyle("-fx-background-color: linear-gradient(to bottom, #ffb6c1, #ff69b4); " +
                "-fx-border-color: #ff1493; -fx-border-width: 0 0 2 0; " +
                "-fx-effect: dropshadow(gaussian, rgba(255,105,180,0.3), 10, 0, 0, 2);");

        userLabel = new Label(localization.get("main.user.label") + " " + currentUserLogin);
        userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnVisualize = new Button(localization.get("main.visual.title"));
        btnVisualize.setStyle(BTN_BASE_STYLE);
        btnVisualize.setOnMouseEntered(e -> btnVisualize.setStyle(BTN_HOVER_STYLE));
        btnVisualize.setOnMouseExited(e -> btnVisualize.setStyle(BTN_BASE_STYLE));
        btnVisualize.setOnAction(e -> openVisualization());

        Label langLabel = new Label(localization.get("main.lang.label"));
        langLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

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
        langComboBox.setOnAction(e -> {
            Locale selected = langComboBox.getValue();
            if (selected != null) {
                localization.setLocale(selected);
                updateUITexts();
            }
        });

        hbox.getChildren().addAll(userLabel, spacer, btnVisualize, langLabel, langComboBox);
        return hbox;
    }

    private void openVisualization() {
        if (canvasController == null) {
            canvasController = new VehicleCanvasController(localization);
        }
        List<Vehicle> currentVehicles = tableController != null ? tableController.getAllVehicles() : List.of();
        Stage vizStage = new Stage();
        vizStage.setTitle(localization.get("main.visual.title"));
        javafx.scene.canvas.Canvas canvas = canvasController.createCanvas(800, 600);
        canvasController.updateData(currentVehicles);

        // Требование 2: редактирование прямо из окна визуализации
        canvasController.setOnVehicleClicked(vehicle -> {
            if (vehicle != null) {
                Platform.runLater(() -> commandHandler.executeEdit(vehicle));
            }
        });

        StackPane pane = new StackPane(canvas);
        pane.setStyle("-fx-background-color: white;");
        Scene scene = new Scene(pane, 800, 600);
        vizStage.setScene(scene);
        vizStage.show();
    }

    private VBox createCenterBox() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.TOP_CENTER);
        // Нежно-розовый фон
        vbox.setStyle("-fx-background-color: linear-gradient(to bottom, #fff0f5, #ffe4e8); " +
                "-fx-border-color: #ffb6c1; -fx-border-width: 1;");

        tableController = new VehicleTableController(localization);
        VBox tablePane = tableController.createTablePane();

        if (commandHandler != null) {
            commandHandler.setTableController(tableController);
        }

        //редактирование прямо из таблицы (двойной клик)
        tableController.getTable().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
                if (selected != null) {
                    commandHandler.executeEdit(selected);
                }
            }
        });

        vbox.getChildren().add(tablePane);
        VBox.setVgrow(tablePane, Priority.ALWAYS);
        return vbox;
    }

    private HBox createBottomPanel() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10));
        hbox.setAlignment(Pos.CENTER);
        // Розовая нижняя панель
        hbox.setStyle("-fx-background-color: linear-gradient(to bottom, #ffb6c1, #ff69b4); " +
                "-fx-border-color: #ff1493; -fx-border-width: 2 0 0 0; " +
                "-fx-effect: dropshadow(gaussian, rgba(255,20,147,0.4), 8, 0, 0, -2);");

        Button btnShow = new Button(localization.get("btn.show"));
        Button btnAdd = new Button(localization.get("btn.add"));
        Button btnUpdate = new Button(localization.get("btn.update"));
        Button btnRemove = new Button(localization.get("btn.remove"));
        Button btnClear = new Button(localization.get("btn.clear"));
        Button btnInfo = new Button(localization.get("btn.info"));
        Button btnSort = new Button(localization.get("btn.sort"));
        Button btnPrintDesc = new Button(localization.get("btn.print_desc"));
        Button btnShuffle = new Button(localization.get("btn.shuffle"));
        Button btnFilterEngine = new Button(localization.get("btn.filter_engine"));
        Button btnBuy = new Button(localization.get("btn.buy"));
        Button btnBalance = new Button(localization.get("btn.balance"));
        Button btnDeposit = new Button(localization.get("btn.deposit"));
        Button btnHelp = new Button(localization.get("btn.help"));
        Button btnExit = new Button(localization.get("btn.exit"));

        // Применяем стиль ко всем кнопкам
        List<Button> buttons = List.of(btnShow, btnAdd, btnUpdate, btnRemove, btnClear,
                btnInfo, btnSort, btnPrintDesc, btnShuffle,
                btnFilterEngine, btnBuy, btnBalance, btnDeposit,
                btnHelp, btnExit);

        for (Button btn : buttons) {
            btn.setStyle(BTN_BASE_STYLE);
            btn.setOnMouseEntered(e -> btn.setStyle(BTN_HOVER_STYLE));
            btn.setOnMouseExited(e -> btn.setStyle(BTN_BASE_STYLE));
        }

        btnShow.setOnAction(e -> commandHandler.executeShow());
        btnAdd.setOnAction(e -> commandHandler.executeAdd());
        btnUpdate.setOnAction(e -> commandHandler.executeUpdate());
        btnRemove.setOnAction(e -> commandHandler.executeRemoveById());
        btnClear.setOnAction(e -> commandHandler.executeClear());
        btnInfo.setOnAction(e -> commandHandler.executeInfo());
        btnSort.setOnAction(e -> commandHandler.executeSort());
        btnPrintDesc.setOnAction(e -> commandHandler.executePrintDescending());
        btnShuffle.setOnAction(e -> commandHandler.executeShuffle());
        btnFilterEngine.setOnAction(e -> commandHandler.executeFilterByEnginePower());
        btnBuy.setOnAction(e -> commandHandler.executeBuy());
        btnBalance.setOnAction(e -> commandHandler.executeShowBalance());
        btnDeposit.setOnAction(e -> commandHandler.executeDeposit());
        btnHelp.setOnAction(e -> commandHandler.executeHelp());
        btnExit.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(localization.get("app.title"));
            alert.setHeaderText(null);
            alert.setContentText(localization.get("confirm.exit"));
            // Стилизация алерта подтверждения
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #fff0f5;");
            dialogPane.lookupButton(ButtonType.OK).setStyle(BTN_BASE_STYLE);
            dialogPane.lookupButton(ButtonType.CANCEL).setStyle(BTN_BASE_STYLE);

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                stopAutoRefresh();
                if (networkService != null && networkService.isConnected()) {
                    networkService.disconnect();
                }
                stage.close();
            }
        });

        hbox.getChildren().addAll(
                btnShow, btnAdd, btnUpdate, btnRemove, btnClear,
                btnInfo, btnSort, btnPrintDesc, btnShuffle,
                btnFilterEngine, btnBuy, btnBalance, btnDeposit,
                btnHelp, btnExit
        );
        return hbox;
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
                        case 0: btn.setText(localization.get("btn.show")); break;
                        case 1: btn.setText(localization.get("btn.add")); break;
                        case 2: btn.setText(localization.get("btn.update")); break;
                        case 3: btn.setText(localization.get("btn.remove")); break;
                        case 4: btn.setText(localization.get("btn.clear")); break;
                        case 5: btn.setText(localization.get("btn.info")); break;
                        case 6: btn.setText(localization.get("btn.sort")); break;
                        case 7: btn.setText(localization.get("btn.print_desc")); break;
                        case 8: btn.setText(localization.get("btn.shuffle")); break;
                        case 9: btn.setText(localization.get("btn.filter_engine")); break;
                        case 10: btn.setText(localization.get("btn.buy")); break;
                        case 11: btn.setText(localization.get("btn.balance")); break;
                        case 12: btn.setText(localization.get("btn.deposit")); break;
                        case 13: btn.setText(localization.get("btn.help")); break;
                        case 14: btn.setText(localization.get("btn.exit")); break;
                    }
                    i++;
                }
            }
        }

        if (tableController != null) {
            tableController.updateLocalization();
        }
    }

    public void updateTableData(List<Vehicle> vehicles) {
        if (tableController != null) {
            tableController.updateData(vehicles);
        }
    }
}