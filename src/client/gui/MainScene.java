package client.gui;

import client.logic.NetworkService;
import common.CommandRequest;
import common.CommandResponse;
import common.Vehicle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Locale;

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

        return new Scene(root, 1200, 800);
    }

    private HBox createTopPanel() {
        HBox hbox = new HBox(15);
        hbox.setPadding(new Insets(10));
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        userLabel = new Label(localization.get("main.user.label") + " " + currentUserLogin);
        userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnVisualize = new Button("🎨 Визуализация");
        btnVisualize.setOnAction(e -> openVisualization());

        Label langLabel = new Label(localization.get("main.lang.label"));
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

        // Берём текущие данные из таблицы
        List<Vehicle> currentVehicles = tableController.getAllVehicles();

        Stage vizStage = new Stage();
        vizStage.setTitle("Визуализация объектов");

        javafx.scene.canvas.Canvas canvas = canvasController.createCanvas(800, 600);
        canvasController.updateData(currentVehicles);

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
        vbox.setStyle("-fx-background-color: #f5f5f5;");

        // Создаём таблицу — КАК БЫЛО РАНЬШЕ
        tableController = new VehicleTableController(localization);
        VBox tablePane = tableController.createTablePane();

        // Связываем с обработчиком команд
        if (commandHandler != null) {
            commandHandler.setTableController(tableController);
        }

        vbox.getChildren().add(tablePane);
        VBox.setVgrow(tablePane, Priority.ALWAYS);

        return vbox;
    }

    private HBox createBottomPanel() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10));
        hbox.setAlignment(Pos.CENTER);
        hbox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

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
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
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

        // Обновляем тексты кнопок
        HBox bottomPanel = (HBox) ((BorderPane) stage.getScene().getRoot()).getBottom();
        if (bottomPanel != null) {
            int i = 0;
            for (javafx.scene.Node node : bottomPanel.getChildren()) {
                if (node instanceof Button) {
                    Button btn = (Button) node;
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

        // Обновляем таблицу
        if (tableController != null) {
            tableController.updateLocalization();
        }
    }

    // Метод для обновления данных извне (если нужно)
    public void updateTableData(List<Vehicle> vehicles) {
        if (tableController != null) {
            tableController.updateData(vehicles);
        }
    }
}