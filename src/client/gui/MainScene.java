package client.gui;

import client.logic.NetworkService;
import common.Vehicle;
import javafx.animation.FadeTransition;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.util.ArrayList;
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

    // Ссылки на основные контейнеры для обновления тем
    private BorderPane root;
    private HBox topPanel;
    private VBox tableContainer;
    private VBox canvasContainer;
    private HBox bottomPanel;
    private Label visualTitle;
    private List<Button> themeAwareButtons = new ArrayList<>();

    // Элементы верхней панели
    private Label userLabel;
    private Button balanceButton;
    private Button depositButton;
    private Button themeToggleButton;
    private ComboBox<Locale> langComboBox;
    private VBox notificationContainer;
    private List<Vehicle> lastCanvasVehicles = List.of();
    private ScheduledExecutorService refreshScheduler;

    // Состояние темы
    private boolean isDarkMode = false;

    // === СТИЛИ СВЕТЛОЙ ТЕМЫ ===
    private static final String L_BG = "-fx-background-color: #F4F6F8;";
    private static final String L_CARD = "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 2);";
    private static final String L_BTN_P = "-fx-background-color: #2979FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String L_BTN_P_H = "-fx-background-color: #1565C0;";
    private static final String L_BTN_S = "-fx-background-color: white; -fx-text-fill: #424242; -fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String L_BTN_S_H = "-fx-background-color: #FAFAFA; -fx-border-color: #BDBDBD;";
    private static final String L_BTN_D = "-fx-background-color: white; -fx-text-fill: #D32F2F; -fx-border-color: #EF9A9A; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String L_BTN_D_H = "-fx-background-color: #FFEBEE; -fx-border-color: #D32F2F;";

    // === СТИЛИ ТЕМНОЙ ТЕМЫ ===
    private static final String D_BG = "-fx-background-color: #121212;";
    private static final String D_CARD = "-fx-background-color: #1E1E1E; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 2);";
    private static final String D_BTN_P = "-fx-background-color: #BB86FC; -fx-text-fill: #121212; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String D_BTN_P_H = "-fx-background-color: #9C64E6;";
    private static final String D_BTN_S = "-fx-background-color: #2C2C2C; -fx-text-fill: #E0E0E0; -fx-border-color: #444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String D_BTN_S_H = "-fx-background-color: #383838; -fx-border-color: #666;";
    private static final String D_BTN_D = "-fx-background-color: #2C2C2C; -fx-text-fill: #FF5252; -fx-border-color: #D32F2F; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String D_BTN_D_H = "-fx-background-color: #3A0000; -fx-border-color: #FF5252;";


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
        root = new BorderPane();
        root.setPadding(new Insets(15));

        // Контейнер для уведомлений
        notificationContainer = new VBox(10);
        notificationContainer.setAlignment(Pos.TOP_RIGHT);
        notificationContainer.setPadding(new Insets(15));
        notificationContainer.setMouseTransparent(true);
        notificationContainer.setStyle("-fx-background-color: transparent;");

        topPanel = createTopPanel();
        root.setTop(topPanel);

        SplitPane centerSplit = createCenterSplit();
        centerSplit.setDividerPositions(0.65);
        root.setCenter(centerSplit);

        bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);
        BorderPane.setMargin(bottomPanel, new Insets(15, 0, 0, 0));

        StackPane mainContainer = new StackPane(root, notificationContainer);
        Scene scene = new Scene(mainContainer, 1300, 850);

        KeyCombination exitKey = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(exitKey, () -> handleExit());

        // Применяем начальную тему
        applyThemeStyles();

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

        // === КНОПКА ПЕРЕКЛЮЧЕНИЯ ТЕМЫ ===
        themeToggleButton = new Button("🌙");
        themeToggleButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 18px; -fx-padding: 5;");
        themeToggleButton.setOnAction(e -> {
            isDarkMode = !isDarkMode;
            themeToggleButton.setText(isDarkMode ? "☀️" : "🌙");
            applyThemeStyles();
        });
        // ==============================

        userLabel = new Label(localization.get("main.user.label") + " " + currentUserLogin);

        balanceButton = new Button(localization.get("btn.balance"));
        themeAwareButtons.add(balanceButton);
        setupButtonHover(balanceButton, false);
        balanceButton.setOnAction(e -> {
            String originalText = balanceButton.getText();
            balanceButton.setText("...");
            balanceButton.setDisable(true);
            new Thread(() -> {
                try {
                    common.CommandRequest req = new common.CommandRequest("show_balance",
                            java.util.List.of("show_balance"), null, true, currentUserLogin, currentUserPassword);
                    networkService.send(req);
                    common.CommandResponse resp = networkService.receive();
                    Platform.runLater(() -> {
                        if (resp != null && resp.isSuccess()) {
                            String msg = resp.getMessage();
                            balanceButton.setText(msg.replace("Ваш баланс: ", "").trim() + " ₽");
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

        depositButton = new Button(localization.get("btn.deposit"));
        themeAwareButtons.add(depositButton);
        setupButtonHover(depositButton, false);
        depositButton.setOnAction(e -> {
            if (commandHandler != null) commandHandler.executeDeposit();
        });

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
        langComboBox.setOnAction(e -> {
            Locale selected = langComboBox.getValue();
            if (selected != null) {
                localization.setLocale(selected);
                updateUITexts();
            }
        });

        // Порядок добавления: Тема -> Имя -> Кнопки -> Пробел -> Язык
        hbox.getChildren().addAll(themeToggleButton, userLabel, balanceButton, depositButton, spacer, langLabel, langComboBox);
        return hbox;
    }

    private SplitPane createCenterSplit() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        splitPane.setStyle("-fx-background-color: transparent;");

        // 1. ИНИЦИАЛИЗИРУЕМ КОНТРОЛЛЕР ТАБЛИЦЫ ПЕРВЫМ ДЕЛОМ
        tableController = new VehicleTableController(localization);

        // 2. Теперь можно безопасно вызывать методы
        VBox tableContainer = tableController.createTablePane();
        tableContainer.setStyle(CARD_STYLE + "-fx-padding: 15;");

        if (commandHandler != null) {
            commandHandler.setTableController(tableController);
        }

        // Обработчик кликов по таблице
        tableController.getTable().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
                if (selected != null) commandHandler.executeEdit(selected);
            } else if (event.getClickCount() == 1) {
                // При одиночном клике по таблице -> плавно фокусируемся на объекте
                Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
                if (selected != null && canvasController != null) {
                    canvasController.focusOnVehicle(selected);
                }
            }
        });

        // ПРАВАЯ ЧАСТЬ: Визуализация
        if (canvasController == null) {
            canvasController = new VehicleCanvasController(localization);
        }
        javafx.scene.canvas.Canvas canvas = canvasController.createCanvas(600, 600);

        // Обработчик клика по канвасу
        canvasController.setOnVehicleClicked(vehicle -> {
            if (vehicle != null) {
                Platform.runLater(() -> {
                    tableController.getTable().getSelectionModel().select(vehicle);
                    canvasController.focusOnVehicle(vehicle); // Фокус при клике на канвас
                    commandHandler.executeEdit(vehicle);
                });
            } else {
                // Клик в пустое место -> сброс вида
                Platform.runLater(() -> canvasController.resetView());
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
            themeAwareButtons.add(btn);
        }

        btnAdd.setOnAction(e -> commandHandler.executeAdd());
        btnRemove.setOnAction(e -> {
            Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
            if (selected != null) commandHandler.executeRemoveById(selected.getId());
            else showWarning("Выберите элемент в таблице для удаления!");
        });
        btnShuffle.setOnAction(e -> commandHandler.executeShuffle());
        btnBuy.setOnAction(e -> {
            Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
            if (selected != null) commandHandler.executeBuy(selected.getId());
            else showWarning("Выберите элемент в таблице для покупки!");
        });
        btnClear.setOnAction(e -> commandHandler.executeClear());

        hbox.getChildren().addAll(buttons);
        return hbox;
    }

    // === УПРАВЛЕНИЕ СТИЛЯМИ КНОПОК С УЧЕТОМ ТЕМЫ ===
    private Button createStyledButton(String text, boolean isPrimary) {
        Button btn = new Button(text);
        btn.setStyle(getBaseStyle(isPrimary));
        setupButtonHover(btn, isPrimary);
        return btn;
    }

    private Button createStyledButton(String text, String type) {
        Button btn = new Button(text);
        btn.setStyle(getBaseStyle("danger"));
        setupButtonHover(btn, "danger");
        return btn;
    }

    private String getBaseStyle(boolean isPrimary) {
        return isDarkMode ? (isPrimary ? D_BTN_P : D_BTN_S) : (isPrimary ? L_BTN_P : L_BTN_S);
    }

    private String getHoverStyle(boolean isPrimary) {
        return isDarkMode ? (isPrimary ? D_BTN_P_H : D_BTN_S_H) : (isPrimary ? L_BTN_P_H : L_BTN_S_H);
    }

    private String getBaseStyle(String type) {
        return isDarkMode ? (D_BTN_D) : (L_BTN_D);
    }

    private String getHoverStyle(String type) {
        return isDarkMode ? (D_BTN_D_H) : (L_BTN_D_H);
    }

    private void setupButtonHover(Button btn, boolean isPrimary) {
        btn.setOnMouseEntered(e -> btn.setStyle(getHoverStyle(isPrimary)));
        btn.setOnMouseExited(e -> btn.setStyle(getBaseStyle(isPrimary)));
    }

    private void setupButtonHover(Button btn, String type) {
        btn.setOnMouseEntered(e -> btn.setStyle(getHoverStyle(type)));
        btn.setOnMouseExited(e -> btn.setStyle(getBaseStyle(type)));
    }

    // === ПРИМЕНЕНИЕ ТЕМЫ КО ВСЕМ КОНТЕЙНЕРАМ ===
    private void applyThemeStyles() {
        if (root == null) return;

        // Фоновые стили
        root.setStyle(isDarkMode ? D_BG : L_BG);
        if (topPanel != null) topPanel.setStyle(isDarkMode ? D_CARD : L_CARD);
        if (tableContainer != null) tableContainer.setStyle((isDarkMode ? D_CARD : L_CARD) + " -fx-padding: 15;");
        if (canvasContainer != null) canvasContainer.setStyle((isDarkMode ? D_CARD : L_CARD) + " -fx-padding: 15;");
        if (bottomPanel != null) bottomPanel.setStyle("-fx-background-color: transparent;");

        // Цвета текста
        String txtColor = isDarkMode ? "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #BB86FC;"
                : "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2979FF;";
        if (userLabel != null) userLabel.setStyle(txtColor);
        if (visualTitle != null) visualTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + (isDarkMode ? "#E0E0E0" : "#424242") + ";");

        // Обновляем кнопки
        for (Button btn : themeAwareButtons) {
            if (btn.getStyle().contains(D_BTN_P) || btn.getStyle().contains(L_BTN_P) || btn.getStyle().contains(D_BTN_S) || btn.getStyle().contains(L_BTN_S)) {
                // Определяем, является ли кнопка "Primary" по текущему тексту или стилю
                // Для простоты, просто сбрасываем базовый стиль. Hover-листнеры подхватят тему динамически.
                btn.setStyle(btn.getText().equals("Очистить") ?
                        getBaseStyle("danger") :
                        (btn.getText().equals(localization.get("btn.add")) ? getBaseStyle(true) : getBaseStyle(false)));
            }
        }
        // Явно обновляем кнопки баланса и депозита
        if (balanceButton != null) balanceButton.setStyle(getBaseStyle(false));
        if (depositButton != null) depositButton.setStyle(getBaseStyle(false));

        // Стиль кнопки темы
        if (themeToggleButton != null) {
            themeToggleButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 18px; -fx-padding: 5; -fx-text-fill: " + (isDarkMode ? "#E0E0E0" : "#424242") + ";");
        }

        // Комбобокс языка
        if (langComboBox != null) {
            langComboBox.setStyle("-fx-background-color: " + (isDarkMode ? "#2C2C2C" : "white") +
                    "; -fx-border-color: " + (isDarkMode ? "#444" : "#E0E0E0") +
                    "; -fx-border-radius: 6; -fx-background-radius: 6;");
        }
    }

    private void handleExit() {
        stopAutoRefresh();
        if (networkService != null && networkService.isConnected()) networkService.disconnect();
        stage.close();
    }

    private void showWarning(String msg) { ModernNotifications.showWarning(notificationContainer, msg); }
    public void showSuccessNotification(String msg) { ModernNotifications.showSuccess(notificationContainer, msg); }
    public void showErrorNotification(String msg) { ModernNotifications.showError(notificationContainer, msg); }
    public void showInfoNotification(String msg) { ModernNotifications.showInfo(notificationContainer, msg); }

    private void updateUITexts() {
        userLabel.setText(localization.get("main.user.label") + " " + currentUserLogin);
        stage.setTitle(localization.get("app.title") + " - " + currentUserLogin);
        if (tableController != null) tableController.updateLocalization();
        if (balanceButton != null) {
            if (!balanceButton.getText().contains("₽") && !balanceButton.getText().equals("...") && !balanceButton.getText().equals("Ошибка")) {
                balanceButton.setText(localization.get("btn.balance"));
            }
        }
        // При смене языка иногда нужно обновить кнопки
        applyThemeStyles();
    }

    public void updateVisualization() {
        if (tableController != null && canvasController != null) {
            List<Vehicle> currentVehicles = tableController.getAllVehicles();
            if (hasDataChanged(lastCanvasVehicles, currentVehicles)) {
                canvasController.updateData(currentVehicles);
                canvasController.resetView();
                lastCanvasVehicles = new ArrayList<>(currentVehicles);
            } else {
                canvasController.updateData(currentVehicles);
            }
        }
    }

    private boolean hasDataChanged(List<Vehicle> oldList, List<Vehicle> newList) {
        if (oldList == null && newList == null) return false;
        if (oldList == null || newList == null) return true;
        if (oldList.size() != newList.size()) return true;
        for (int i = 0; i < oldList.size(); i++) {
            Vehicle ov = oldList.get(i);
            Vehicle nv = newList.get(i);
            if (ov.getId() != nv.getId() ||
                    ov.getCoordinates().getX() != nv.getCoordinates().getX() ||
                    ov.getCoordinates().getY() != nv.getCoordinates().getY()) {
                return true;
            }
        }
        return false;
    }
}