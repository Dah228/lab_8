package client.gui;

import client.logic.NetworkService;
import common.CommandRequest;
import common.CommandResponse;
import common.Vehicle;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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

        // Создаем обработчик команд
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

        hbox.getChildren().addAll(userLabel, spacer, langLabel, langComboBox);
        return hbox;
    }

    private VBox createCenterBox() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.TOP_CENTER);

        Label tableTitle = new Label(localization.get("main.table.title"));
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        tableController = new VehicleTableController(localization);
        VBox tablePane = tableController.createTablePane();

        // Canvas пока заглушка
        StackPane canvasPlaceholder = new StackPane();
        canvasPlaceholder.setMinHeight(200);
        canvasPlaceholder.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: white;");
        Label canvasText = new Label("[Canvas с визуализацией - будет добавлен]");
        canvasPlaceholder.getChildren().add(canvasText);

        vbox.getChildren().addAll(tableTitle, tablePane, canvasPlaceholder);
        VBox.setVgrow(tablePane, Priority.ALWAYS);

        return vbox;
    }

    private HBox createBottomPanel() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10));
        hbox.setAlignment(Pos.CENTER);
        hbox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

        // Создаем кнопки с локализованными названиями
        Button btnShow = new Button(localization.get("table.refresh"));
        Button btnAdd = new Button("add");
        Button btnUpdate = new Button("update");
        Button btnRemove = new Button("remove_by_id");
        Button btnClear = new Button("clear");

        // Дополнительные команды
        Button btnInfo = new Button("info");
        Button btnSort = new Button("sort");
        Button btnPrintDesc = new Button("print_descending");
        Button btnShuffle = new Button("shuffle");
        Button btnHelp = new Button("help");
        Button btnFilterEngine = new Button("filter_engine");
        Button btnBuy = new Button("buy");
        Button btnBalance = new Button("balance");
        Button btnDeposit = new Button("deposit");
        Button btnExit = new Button("exit");

        // Обработчики
        btnShow.setOnAction(e -> requestShowFromServer());
        btnAdd.setOnAction(e -> commandHandler.executeAdd());
        btnUpdate.setOnAction(e -> commandHandler.executeUpdate());
        btnRemove.setOnAction(e -> commandHandler.executeRemoveById());
        btnClear.setOnAction(e -> commandHandler.executeClear());
        btnInfo.setOnAction(e -> commandHandler.executeInfo());
        btnSort.setOnAction(e -> commandHandler.executeSort());
        btnPrintDesc.setOnAction(e -> commandHandler.executePrintDescending());
        btnShuffle.setOnAction(e -> commandHandler.executeShuffle());
        btnHelp.setOnAction(e -> commandHandler.executeHelp());
        btnFilterEngine.setOnAction(e -> commandHandler.executeFilterByEnginePower());
        btnBuy.setOnAction(e -> commandHandler.executeBuy());
        btnBalance.setOnAction(e -> commandHandler.executeShowBalance());
        btnDeposit.setOnAction(e -> commandHandler.executeDeposit());

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
    }

    public void requestShowFromServer() {
        new Thread(() -> {
            try {
                CommandRequest request = new CommandRequest("show", List.of("show"), null, true,
                        currentUserLogin, currentUserPassword);
                networkService.send(request);
                CommandResponse response = networkService.receive();

                if (response != null && response.isSuccess()) {
                    // Получаем данные из response
                    Object data = response.getData();
                    if (data instanceof List) {
                        List<?> dataList = (List<?>) data;
                        // Преобразуем в список Vehicle
                        List<Vehicle> vehicles = dataList.stream()
                                .filter(obj -> obj instanceof Vehicle)
                                .map(obj -> (Vehicle) obj)
                                .toList();

                        javafx.application.Platform.runLater(() -> {
                            if (tableController != null) {
                                tableController.updateData(vehicles);
                            }
                        });
                    }

                    // Показываем сообщение если есть
                    String message = response.getMessage();
                    if (message != null && !message.trim().isEmpty()) {
                        javafx.application.Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle(localization.get("app.title"));
                            alert.setHeaderText(null);
                            alert.setContentText(message);
                            alert.showAndWait();
                        });
                    }
                } else if (response != null) {
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle(localization.get("app.title"));
                        alert.setHeaderText(null);
                        alert.setContentText(response.getMessage());
                        alert.showAndWait();
                    });
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(localization.get("app.title"));
                    alert.setHeaderText(null);
                    alert.setContentText("Ошибка сети: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    // Метод для обновления данных извне (если нужно)
    public void updateTableData(List<Vehicle> vehicles) {
        if (tableController != null) {
            tableController.updateData(vehicles);
        }
    }
}