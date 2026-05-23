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
    private ScheduledExecutorService refreshScheduler;

    // Мягкая зелёная тема (Material 3 style)
    private static final String BTN_BASE_STYLE = "-fx-background-color: linear-gradient(to bottom, #C8E6C9, #A5D6A7); " +
            "-fx-text-fill: #2E7D32; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8; " +
            "-fx-border-color: #81C784; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(129,199,132,0.3), 5, 0, 0, 2); " +
            "-fx-cursor: hand;";
    private static final String BTN_HOVER_STYLE = "-fx-background-color: linear-gradient(to bottom, #A5D6A7, #81C784); " +
            "-fx-text-fill: #1B5E20; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8; " +
            "-fx-border-color: #66BB6A; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(102,187,106,0.5), 8, 0, 0, 3); " +
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
        hbox.setStyle("-fx-background-color: linear-gradient(to bottom, #E8F5E9, #C8E6C9); " +
                "-fx-border-color: #81C784; -fx-border-width: 0 0 2 0; " +
                "-fx-effect: dropshadow(gaussian, rgba(129,199,132,0.2), 10, 0, 0, 2);");

        userLabel = new Label(localization.get("main.user.label") + " " + currentUserLogin);
        userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-text-fill: #2E7D32; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnVisualize = new Button(localization.get("main.visual.title"));
        btnVisualize.setStyle(BTN_BASE_STYLE);
        btnVisualize.setOnMouseEntered(e -> btnVisualize.setStyle(BTN_HOVER_STYLE));
        btnVisualize.setOnMouseExited(e -> btnVisualize.setStyle(BTN_BASE_STYLE));
        btnVisualize.setOnAction(e -> openVisualization());

        Label langLabel = new Label(localization.get("main.lang.label"));
        langLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");

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
        canvasController.setOnVehicleClicked(vehicle -> {
            if (vehicle != null) {
                Platform.runLater(() -> commandHandler.executeEdit(vehicle));
            }
        });

        StackPane pane = new StackPane(canvas);
        pane.setStyle("-fx-background-color: #F1F8E9;");
        Scene scene = new Scene(pane, 1000, 600);
        vizStage.setScene(scene);
        vizStage.show();
    }

    private VBox createCenterBox() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setStyle("-fx-background-color: linear-gradient(to bottom, #F8FDF9, #E8F5E9); " +
                "-fx-border-color: #C8E6C9; -fx-border-width: 1;");

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

        hbox.setStyle("-fx-background-color: linear-gradient(to bottom, #E8F5E9, #C8E6C9); " +
                "-fx-border-color: #81C784; -fx-border-width: 2 0 0 0; " +
                "-fx-effect: dropshadow(gaussian, rgba(129,199,132,0.3), 8, 0, 0, -2);");

        Button btnAdd = new Button(localization.get("btn.add"));
        Button btnRemove = new Button(localization.get("btn.remove"));
        Button btnShuffle = new Button(localization.get("btn.shuffle"));
        Button btnBuy = new Button(localization.get("btn.buy"));
        Button btnBalance = new Button(localization.get("btn.balance"));
        Button btnExit = new Button(localization.get("btn.exit"));

        List<Button> buttons = List.of(btnAdd, btnRemove, btnShuffle, btnBuy, btnBalance, btnExit);

        for (Button btn : buttons) {
            btn.setStyle(BTN_BASE_STYLE);
            btn.setOnMouseEntered(e -> btn.setStyle(BTN_HOVER_STYLE));
            btn.setOnMouseExited(e -> btn.setStyle(BTN_BASE_STYLE));
            HBox.setHgrow(btn, Priority.ALWAYS);
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        btnAdd.setOnAction(e -> commandHandler.executeAdd());

        // === ИЗМЕНЕНИЕ ЗДЕСЬ: Удаляем выделенный элемент ===
        btnRemove.setOnAction(e -> {
            Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
            if (selected != null) {
                commandHandler.executeRemoveById(selected.getId());
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(localization.get("app.title"));
                alert.setHeaderText(null);
                alert.setContentText("Выберите элемент в таблице для удаления!");
                alert.showAndWait();
            }
        });

        btnShuffle.setOnAction(e -> commandHandler.executeShuffle());
        btnBuy.setOnAction(e -> commandHandler.executeBuy());
        btnBalance.setOnAction(e -> commandHandler.executeShowBalance());
        btnExit.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(localization.get("app.title"));
            alert.setHeaderText(null);
            alert.setContentText(localization.get("confirm.exit"));
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #F1F8E9;");
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

        hbox.getChildren().addAll(btnAdd, btnRemove, btnShuffle, btnBuy, btnBalance, btnExit);
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
                        case 0: btn.setText(localization.get("btn.add")); break;
                        case 1: btn.setText(localization.get("btn.remove")); break;
                        case 2: btn.setText(localization.get("btn.shuffle")); break;
                        case 3: btn.setText(localization.get("btn.buy")); break;
                        case 4: btn.setText(localization.get("btn.balance")); break;
                        case 5: btn.setText(localization.get("btn.exit")); break;
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