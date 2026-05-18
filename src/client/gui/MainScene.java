package client.gui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Locale;

public class MainScene {

    private final Stage stage;
    private final LocalizationManager localization;
    private final String currentUserLogin;
    private VehicleTableController tableController;
    private VehicleCanvasController canvasController; // Добавим позже

    private Label userLabel;
    private ComboBox<Locale> langComboBox;

    public MainScene(Stage stage, LocalizationManager localization, String currentUserLogin) {
        this.stage = stage;
        this.localization = localization;
        this.currentUserLogin = currentUserLogin;
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

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().add(tablePane);

        StackPane canvasPlaceholder = new StackPane();
        canvasPlaceholder.setMinHeight(200);
        canvasPlaceholder.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: white;");
        Label canvasText = new Label("[Здесь будет Canvas с визуализацией]");
        canvasPlaceholder.getChildren().add(canvasText);
        splitPane.getItems().add(canvasPlaceholder);
        splitPane.setDividerPositions(0.7);

        vbox.getChildren().addAll(tableTitle, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        return vbox;
    }

    private HBox createBottomPanel() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10));
        hbox.setAlignment(Pos.CENTER);
        hbox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

        Button btnShow = new Button("show");
        Button btnAdd = new Button("add");
        Button btnRemove = new Button("remove_by_id");
        Button btnClear = new Button("clear");
        Button btnExit = new Button("exit");

        btnExit.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(localization.get("app.title"));
            alert.setHeaderText(null);
            alert.setContentText(localization.get("confirm.exit"));
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                stage.close();
            }
        });

        // Подключаем кнопку Show к загрузке данных
        btnShow.setOnAction(e -> {
            if (tableController != null) {
                tableController.requestShowFromServer();
            }
        });

        hbox.getChildren().addAll(btnShow, btnAdd, btnRemove, btnClear, btnExit);
        return hbox;
    }

    private void updateUITexts() {
        userLabel.setText(localization.get("main.user.label") + " " + currentUserLogin);
        stage.setTitle(localization.get("app.title") + " - " + currentUserLogin);
    }

    // Метод для обновления данных таблицы извне
    public void updateTableData(java.util.List<common.Vehicle> vehicles) {
        if (tableController != null) {
            tableController.updateData(vehicles);
        }
    }

    // Метод для обновления Canvas (будет реализован на этапе 6)
    public void updateVisualData(java.util.List<common.Vehicle> vehicles) {
        if (canvasController != null) {
            canvasController.setData(vehicles);
        }
    }
}