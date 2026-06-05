package client.gui;

import client.logic.NetworkService;
import common.Vehicle;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//координатор всей сцены
public class MainScene {
    private final Stage stage;
    private final LocalizationManager localization;
    private final String currentUserLogin;
    private final String currentUserPassword;
    private final NetworkService networkService;

    //контроллеры и обработчики
    private VehicleTableController tableController;
    private VehicleCanvasController canvasController;
    private CommandDialogHandler commandHandler;

    //менеджеры
    private ThemeManager themeManager;
    private UserProfilePanel profilePanel;

    //основные контейнеры
    private BorderPane root;
    private HBox topPanel;
    private VBox tableContainer;
    private VBox canvasContainer;
    private HBox bottomPanel;
    private Label visualTitle;

    //кнопки и элементы управления
    private List<Button> themeAwareButtons = new ArrayList<>();
    private Label userLabel;
    private Button balanceButton;
    private Button depositButton;
    private Button themeToggleButton;
    private ImageView themeIconView;
    private ComboBox<Locale> langComboBox;

    //уведомления и данные
    private VBox notificationContainer;
    private List<Vehicle> lastCanvasVehicles = List.of();
    private ScheduledExecutorService refreshScheduler;

    public MainScene(Stage stage, LocalizationManager localization, NetworkService networkService,
                     String currentUserLogin, String currentUserPassword) {
        this.stage = stage;
        this.localization = localization;
        this.networkService = networkService;
        this.currentUserLogin = currentUserLogin;
        this.currentUserPassword = currentUserPassword;
        this.commandHandler = new CommandDialogHandler(networkService, localization, currentUserLogin, currentUserPassword);
        this.themeManager = new ThemeManager();
    }

    //создаём главную сцену
    public Scene createScene() {
        root = new BorderPane();
        root.setPadding(new Insets(15));

        //контейнер для уведомлений
        notificationContainer = new VBox(10);
        notificationContainer.setAlignment(Pos.TOP_RIGHT);
        notificationContainer.setPadding(new Insets(15));
        notificationContainer.setMouseTransparent(true);
        notificationContainer.setStyle("-fx-background-color: transparent;");

        //верхняя панель
        topPanel = createTopPanel();
        root.setTop(topPanel);

        //центральная часть с таблицей и холстом
        SplitPane centerSplit = createCenterSplit();
        centerSplit.setDividerPositions(0.65);
        root.setCenter(centerSplit);

        //нижняя панель с кнопками
        bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);
        BorderPane.setMargin(bottomPanel, new Insets(15, 0, 0, 0));

        //панель профиля
        profilePanel = new UserProfilePanel(stage, currentUserLogin, localization,
                networkService, themeManager, this::stopAutoRefresh);
        profilePanel.getPanel().setVisible(false);
        profilePanel.getPanel().setOpacity(0);
        profilePanel.getPanel().setTranslateY(-20);
        profilePanel.setUserLabel(userLabel);

        //главный контейнер
        StackPane mainContainer = new StackPane(root, notificationContainer, profilePanel.getPanel());
        Scene scene = new Scene(mainContainer, 1500, 850);

        //горячая клавиша для выхода
        KeyCombination exitKey = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(exitKey, () -> handleExit());

        //закрытие профиля при клике вне его
        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (profilePanel.isOpen() && profilePanel.getPanel().isVisible()) {
                if (!profilePanel.getPanel().getBoundsInParent().contains(e.getSceneX(), e.getSceneY()) &&
                        !userLabel.getBoundsInParent().contains(e.getSceneX(), e.getSceneY())) {
                    profilePanel.closeProfile();
                }
            }
        });

        //применяем стили и загружаем данные
        applyThemeStyles();
        Platform.runLater(() -> commandHandler.executeShow());
        startAutoRefresh();

        return scene;
    }

    //запускаем автообновление данных
    private void startAutoRefresh() {
        refreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AutoRefresh-" + currentUserLogin);
            t.setDaemon(true);
            return t;
        });

        //обновляем каждые 5 секунд
        refreshScheduler.scheduleAtFixedRate(() -> {
            if (stage.isShowing()) {
                Platform.runLater(() -> {
                    commandHandler.executeShowSilent();
                    updateVisualization();
                });
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    //останавливаем автообновление
    public void stopAutoRefresh() {
        if (refreshScheduler != null && !refreshScheduler.isShutdown()) {
            refreshScheduler.shutdownNow();
        }
    }

    //создаём верхнюю панель
    private HBox createTopPanel() {
        HBox hbox = new HBox(20);
        hbox.setPadding(new Insets(12, 20, 12, 20));
        hbox.setAlignment(Pos.CENTER_LEFT);

        //загружаем иконки темы
        Image sunImage = null, moonImage = null;
        try {
            sunImage = new Image(getClass().getResourceAsStream("/sun.png"));
            moonImage = new Image(getClass().getResourceAsStream("/moon.png"));
            if (sunImage.isError() || moonImage.isError()) throw new Exception("Load error");
        } catch (Exception e) {
            System.out.println("Иконки темы не найдены, используется текстовый режим.");
        }

        //кнопка переключения темы
        themeToggleButton = createThemeToggleButton(sunImage, moonImage);

        //метка пользователя
        userLabel = createUserLabel();

        //кнопка баланса
        balanceButton = createBalanceButton();

        //кнопка пополнения
        depositButton = createDepositButton();

        //разделитель
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        //метка языка
        Label langLabel = new Label(localization.get("main.lang.label"));
        langLabel.setStyle("-fx-text-fill: #757575; -fx-font-weight: 500;");

        //выбор языка
        langComboBox = createLanguageComboBox();

        hbox.getChildren().addAll(themeToggleButton, userLabel, balanceButton, depositButton,
                spacer, langLabel, langComboBox);

        return hbox;
    }

    //создаём кнопку переключения темы
    private Button createThemeToggleButton(Image sunImage, Image moonImage) {
        Button themeToggleButton = new Button();
        themeIconView = new ImageView();

        if (sunImage != null && moonImage != null) {
            themeIconView.setFitWidth(24);
            themeIconView.setFitHeight(24);
            themeIconView.setPreserveRatio(true);
            themeIconView.setImage(moonImage);
            themeToggleButton.setGraphic(themeIconView);
        } else {
            //используем эмодзи если картинки не загрузились
            themeToggleButton.setText("🌙");
            themeToggleButton.setStyle("-fx-font-size: 20px; -fx-background-color: transparent; -fx-cursor: hand;");
        }

        themeToggleButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        themeToggleButton.setOnAction(e -> {
            themeManager.toggleTheme();
            updateThemeIcon();
            applyThemeStyles();
        });

        return themeToggleButton;
    }

    //создаём метку пользователя
    private Label createUserLabel() {
        Label userLabel = new Label(localization.get("main.user.label") + " " + currentUserLogin);
        String baseColor = themeManager.isDarkMode() ? "#8B5CF6" : "#2563EB";
        userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + baseColor +
                "; -fx-cursor: hand; -fx-underline: true;");

        userLabel.setOnMouseClicked(e -> profilePanel.toggleProfile());
        userLabel.setOnMouseEntered(e -> {
            String hoverColor = themeManager.isDarkMode() ? "#A78BFA" : "#1D4ED8";
            userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + hoverColor +
                    "; -fx-cursor: hand; -fx-underline: false;");
        });
        userLabel.setOnMouseExited(e -> {
            String baseCol = themeManager.isDarkMode() ? "#8B5CF6" : "#2563EB";
            userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + baseCol +
                    "; -fx-cursor: hand; -fx-underline: true;");
        });

        return userLabel;
    }

    //создаём кнопку баланса
    private Button createBalanceButton() {
        Button balanceButton = new Button(localization.get("btn.balance"));
        themeAwareButtons.add(balanceButton);
        themeManager.setupButtonHover(balanceButton, false);

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
                            balanceButton.setText(resp.getMessage()
                                    .replace(localization.get("error.balance_prefix", localization.getCurrentLocale()), "")
                                    .trim() + localization.get("error.currency_suffix", localization.getCurrentLocale()));
                        } else {
                            balanceButton.setText(localization.get("error.network"));
                        }

                        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                        pause.setOnFinished(ev -> {
                            balanceButton.setText(originalText);
                            balanceButton.setDisable(false);
                        });
                        pause.play();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        balanceButton.setText(localization.get("error.network"));
                        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                        pause.setOnFinished(ev -> {
                            balanceButton.setText(originalText);
                            balanceButton.setDisable(false);
                        });
                        pause.play();
                    });
                }
            }).start();
        });

        return balanceButton;
    }

    //создаём кнопку пополнения
    private Button createDepositButton() {
        Button depositButton = new Button(localization.get("btn.deposit"));
        themeAwareButtons.add(depositButton);
        themeManager.setupButtonHover(depositButton, false);
        depositButton.setOnAction(e -> {
            if (commandHandler != null) commandHandler.executeDeposit();
        });

        return depositButton;
    }

    //создаём ComboBox для выбора языка
    private ComboBox<Locale> createLanguageComboBox() {
        ComboBox<Locale> langComboBox = new ComboBox<>();
        langComboBox.getItems().setAll(localization.getAvailableLocales());

        langComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : localization.getLocaleDisplayName(item));
            }
        });

        langComboBox.setButtonCell(new ListCell<>() {
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

        return langComboBox;
    }

    //создаём центральную часть с таблицей и холстом
    private SplitPane createCenterSplit() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        splitPane.setStyle("-fx-background-color: transparent;");

        //таблица
        tableController = new VehicleTableController(localization);
        tableContainer = tableController.createTablePane();
        if (commandHandler != null) commandHandler.setTableController(tableController);

        //обработчики кликов по таблице
        tableController.getTable().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                //двойной клик - редактирование
                Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
                if (selected != null) commandHandler.executeEdit(selected);
            } else if (event.getClickCount() == 1) {
                //одиночный клик - фокус на холсте
                Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
                if (selected != null && canvasController != null) canvasController.focusOnVehicle(selected);
            }
        });

        //холст
        if (canvasController == null) canvasController = new VehicleCanvasController(localization);
        javafx.scene.canvas.Canvas canvas = canvasController.createCanvas(600, 600);

        //обработчик клика по объекту на холсте
        canvasController.setOnVehicleClicked(vehicle -> {
            if (vehicle != null) {
                Platform.runLater(() -> {
                    tableController.getTable().getSelectionModel().select(vehicle);
                    canvasController.focusOnVehicle(vehicle);
                    commandHandler.executeEdit(vehicle);
                });
            } else {
                Platform.runLater(() -> canvasController.resetView());
            }
        });

        //контейнер для холста
        canvasContainer = new VBox(10);
        visualTitle = new Label(localization.get("main.visual.title"));
        visualTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " +
                (themeManager.isDarkMode() ? "#E2E8F0" : "#334155") + ";");

        Pane canvasPane = new Pane(canvas);
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvasContainer.getChildren().addAll(visualTitle, canvasPane);
        VBox.setVgrow(canvasPane, Priority.ALWAYS);

        splitPane.getItems().addAll(tableContainer, canvasContainer);
        splitPane.setDividerPositions(0.4); //40% таблица, 60% визуализация

        return splitPane;
    }

    //создаём нижнюю панель с кнопками
    private HBox createBottomPanel() {
        HBox hbox = new HBox(15);
        hbox.setPadding(new Insets(5));
        hbox.setAlignment(Pos.CENTER);
        hbox.setStyle("-fx-background-color: transparent;");

        Button btnAdd = createStyledButton(localization.get("btn.add"), true);
        Button btnRemove = createStyledButton(localization.get("btn.remove"), false);
        Button btnShuffle = createStyledButton(localization.get("btn.shuffle"), false);
        Button btnBuy = createStyledButton(localization.get("btn.buy"), false);
        Button btnClear = createStyledButton(localization.get("btn.clear"), "danger");

        List<Button> buttons = List.of(btnAdd, btnRemove, btnShuffle, btnBuy, btnClear);
        for (Button btn : buttons) {
            HBox.setHgrow(btn, Priority.ALWAYS);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(45);
            btn.setFont(Font.font(14));
            themeAwareButtons.add(btn);
        }

        //обработчики кнопок
        btnAdd.setOnAction(e -> commandHandler.executeAdd());
        btnRemove.setOnAction(e -> {
            Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
            if (selected != null) {
                commandHandler.executeRemoveById(selected.getId());
            } else {
                showWarning(localization.get("table.warning.select_for_delete"));
            }
        });
        btnShuffle.setOnAction(e -> commandHandler.executeShuffle());
        btnBuy.setOnAction(e -> {
            Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
            if (selected != null) {
                commandHandler.executeBuy(selected.getId());
            } else {
                showWarning(localization.get("table.warning.select_for_buy"));
            }
        });
        btnClear.setOnAction(e -> commandHandler.executeClear());

        hbox.getChildren().addAll(buttons);

        return hbox;
    }

    //создаём стилизованную кнопку
    private Button createStyledButton(String text, boolean isPrimary) {
        Button btn = new Button(text);
        btn.setStyle(themeManager.getBaseStyle(isPrimary));
        themeManager.setupButtonHover(btn, isPrimary);
        return btn;
    }

    //создаём стилизованную кнопку опасного действия
    private Button createStyledButton(String text, String type) {
        Button btn = new Button(text);
        btn.setStyle(themeManager.getBaseStyle("danger"));
        themeManager.setupButtonHover(btn, "danger");
        return btn;
    }

    //обновляем иконку темы
    private void updateThemeIcon() {
        if (themeIconView != null && themeIconView.getImage() != null) {
            Image sunImage = new Image(getClass().getResourceAsStream("/sun.png"));
            Image moonImage = new Image(getClass().getResourceAsStream("/moon.png"));
            if (!sunImage.isError() && !moonImage.isError()) {
                themeIconView.setImage(themeManager.isDarkMode() ? sunImage : moonImage);
            }
        } else if (themeToggleButton != null) {
            //используем эмодзи
            themeToggleButton.setText(themeManager.isDarkMode() ? "☀️" : "🌙");
        }
    }

    //применяем стили темы ко всем элементам
    private void applyThemeStyles() {
        themeManager.applyThemeStyles(root, topPanel, tableContainer, canvasContainer,
                bottomPanel, userLabel, visualTitle, themeAwareButtons,
                balanceButton, depositButton, langComboBox,
                profilePanel.getPanel(), localization,
                tableController, canvasController, commandHandler);
        profilePanel.updateStyles();
        updateThemeIcon();
    }

    //обрабатываем выход из приложения
    private void handleExit() {
        stopAutoRefresh();
        if (networkService != null && networkService.isConnected()) networkService.disconnect();
        stage.close();
    }

    //показываем предупреждение
    private void showWarning(String msg) {
        if (notificationContainer != null) {
            ModernNotifications.showWarning(notificationContainer, msg, themeManager.isDarkMode());
        }
    }

    //показываем уведомление об успехе
    public void showSuccessNotification(String msg) {
        if (notificationContainer != null) {
            ModernNotifications.showSuccess(notificationContainer, msg, themeManager.isDarkMode());
        }
    }

    //показываем уведомление об ошибке
    public void showErrorNotification(String msg) {
        if (notificationContainer != null) {
            ModernNotifications.showError(notificationContainer, msg, themeManager.isDarkMode());
        }
    }

    //показываем информационное уведомление
    public void showInfoNotification(String msg) {
        if (notificationContainer != null) {
            ModernNotifications.showInfo(notificationContainer, msg, themeManager.isDarkMode());
        }
    }

    //обновляем все тексты при смене языка
    private void updateUITexts() {
        stage.setTitle(localization.get("app.title") + " - " + currentUserLogin);
        userLabel.setText(localization.get("main.user.label") + " " + currentUserLogin);
        visualTitle.setText(localization.get("main.visual.title"));

        balanceButton.setText(localization.get("btn.balance"));
        depositButton.setText(localization.get("btn.deposit"));

        //обновляем кнопки по ключам
        for (Button btn : themeAwareButtons) {
            String key = themeManager.getButtonKey(btn.getText(), localization);
            if (key != null) {
                btn.setText(localization.get(key));
            }
        }

        if (tableController != null) tableController.updateLocalization();
        applyThemeStyles();
    }

    //обновляем визуализацию на холсте
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

    //проверяем, изменились ли данные
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