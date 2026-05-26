package client.gui;
import client.logic.AllCommands;
import client.logic.CommandRegistryLoader;
import client.logic.ConnectionInitializer;
import client.logic.NetworkService;
import common.CommandResponse;
import common.Vehicle;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.awt.*;
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
    private NetworkService networkService;
    private VehicleTableController tableController;
    private VehicleCanvasController canvasController;
    private CommandDialogHandler commandHandler;
    private BorderPane root;
    private HBox topPanel;
    private VBox tableContainer;
    private VBox canvasContainer;
    private HBox bottomPanel;
    private Label visualTitle;
    private List<Button> themeAwareButtons = new ArrayList<>();
    private Label userLabel;
    private Label langLabel; // <--- Вынесено в поле класса для доступа из applyThemeStyles
    private Button balanceButton;
    private Button depositButton;
    private Button themeToggleButton;
    private ComboBox<Locale> langComboBox;
    private VBox notificationContainer;
    private List<Vehicle> lastCanvasVehicles = List.of();
    private ScheduledExecutorService refreshScheduler;
    private boolean isDarkMode = false;
    // === Профиль ===
    private VBox profilePanel;
    private boolean isProfileOpen = false;
    // === СВЕТЛАЯ ТЕМА ===
    private static final String L_BG = "-fx-background-color: #F8FAFC;";
    private static final String L_CARD = "-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);";
    private static final String L_BTN_P = "-fx-background-color: linear-gradient(to right, #3B82F6, #2563EB); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String L_BTN_P_H = "-fx-background-color: linear-gradient(to right, #2563EB, #1D4ED8);";
    private static final String L_BTN_S = "-fx-background-color: #F1F5F9; -fx-text-fill: #334155; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String L_BTN_S_H = "-fx-background-color: #E2E8F0; -fx-border-color: #CBD5E1;";
    private static final String L_BTN_D = "-fx-background-color: #F1F5F9; -fx-text-fill: #EF4444; -fx-border-color: #FECACA; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String L_BTN_D_H = "-fx-background-color: #FEE2E2; -fx-border-color: #FCA5A5;";
    // === ТЁМНАЯ ТЕМА ===
    private static final String D_BG = "-fx-background-color: #0B1120;";
    private static final String D_CARD = "-fx-background-color: #1E293B; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 2);";
    private static final String D_BTN_P = "-fx-background-color: linear-gradient(to right, #8B5CF6, #7C3AED); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String D_BTN_P_H = "-fx-background-color: linear-gradient(to right, #A78BFA, #8B5CF6);";
    private static final String D_BTN_S = "-fx-background-color: #334155; -fx-text-fill: #E2E8F0; -fx-border-color: #475569; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String D_BTN_S_H = "-fx-background-color: #475569; -fx-border-color: #64748B;";
    private static final String D_BTN_D = "-fx-background-color: linear-gradient(to right, #DC2626, #B91C1C); -fx-text-fill: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String D_BTN_D_H = "-fx-background-color: linear-gradient(to right, #EF4444, #DC2626);";
    // === СТИЛИ ПРОФИЛЯ ===
    private static final String L_PROFILE_BG = "-fx-background-color: rgba(255,255,255,0.98); -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 30, 0, 0, 15);";
    private static final String D_PROFILE_BG = "-fx-background-color: rgba(30,41,59,0.98); -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 30, 0, 0, 15);";
    private static final String L_SEPARATOR = "-fx-background-color: #E5E7EB;";
    private static final String D_SEPARATOR = "-fx-background-color: #475569;";
    private static final String PROFILE_BTN_LOGOUT = "-fx-background-color: linear-gradient(to right, #EF4444, #DC2626); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 14 30; -fx-cursor: hand; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(239,68,68,0.3), 10, 0, 0, 3);";
    private static final String PROFILE_BTN_LOGOUT_H = "-fx-background-color: linear-gradient(to right, #F87171, #EF4444); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 14 30; -fx-cursor: hand; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(239,68,68,0.4), 15, 0, 0, 5);";
    private static final String PROFILE_BTN_BACK_L = "-fx-background-color: #F3F4F6; -fx-text-fill: #4B5563; -fx-font-weight: 600; -fx-background-radius: 12; -fx-padding: 12 30; -fx-cursor: hand; -fx-font-size: 14px;";
    private static final String PROFILE_BTN_BACK_H_L = "-fx-background-color: #E5E7EB; -fx-text-fill: #1F2937; -fx-font-weight: 600; -fx-background-radius: 12; -fx-padding: 12 30; -fx-cursor: hand; -fx-font-size: 14px;";
    private static final String PROFILE_BTN_BACK_D = "-fx-background-color: #334155; -fx-text-fill: #E2E8F0; -fx-font-weight: 600; -fx-background-radius: 12; -fx-padding: 12 30; -fx-cursor: hand; -fx-font-size: 14px;";
    private static final String PROFILE_BTN_BACK_H_D = "-fx-background-color: #475569; -fx-text-fill: #F8FAFC; -fx-font-weight: 600; -fx-background-radius: 12; -fx-padding: 12 30; -fx-cursor: hand; -fx-font-size: 14px;";
    private static final String TEXT_AREA_L = "-fx-background-color: #F9FAFB; -fx-text-fill: #1F2937; -fx-font-size: 13px; -fx-background-radius: 10; -fx-border-color: #E5E7EB; -fx-border-radius: 10; -fx-border-width: 1;";
    private static final String TEXT_AREA_D = "-fx-background-color: #334155; -fx-text-fill: #E2E8F0; -fx-font-size: 13px; -fx-background-radius: 10; -fx-border-color: #475569; -fx-border-radius: 10; -fx-border-width: 1;";
    public MainScene(Stage stage, LocalizationManager localization, NetworkService networkService,
                     String currentUserLogin, String currentUserPassword) {
        this.stage = stage;
        this.localization = localization;
        this.networkService = networkService;
        this.currentUserLogin = currentUserLogin;
        this.currentUserPassword = currentUserPassword;
        this.commandHandler = new CommandDialogHandler(networkService, localization, currentUserLogin, currentUserPassword);
    }
    public Scene createScene() {
        root = new BorderPane();
        root.setPadding(new Insets(15));
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
// === Создаём панель профиля ===
        profilePanel = createProfilePanel();
        profilePanel.setVisible(false);
        profilePanel.setOpacity(0);
        profilePanel.setTranslateY(-20);
        StackPane mainContainer = new StackPane(root, notificationContainer, profilePanel);
        Scene scene = new Scene(mainContainer, 1200, 800);
        KeyCombination exitKey = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(exitKey, () -> handleExit());
// Закрытие профиля по клику вне его
        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (isProfileOpen && profilePanel.isVisible()) {
                if (!profilePanel.getBoundsInParent().contains(e.getSceneX(), e.getSceneY()) &&
                        !userLabel.getBoundsInParent().contains(e.getSceneX(), e.getSceneY())) {
                    closeProfile();
                }
            }
        });
        applyThemeStyles();
        Platform.runLater(() -> commandHandler.executeShow());
        startAutoRefresh();
        return scene;
    }
    // === Создание панели профиля ===
    private VBox createProfilePanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(350);
        panel.setPrefHeight(520);
        panel.setStyle(isDarkMode ? D_PROFILE_BG : L_PROFILE_BG);
        panel.setMouseTransparent(false);
// Кнопка закрытия (крестик)
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: " + (isDarkMode ? "#94A3B8" : "#9CA3AF") + "; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5 10;"
        );
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
                "-fx-background-color: " + (isDarkMode ? "#334155" : "#F3F4F6") + "; " +
                        "-fx-background-radius: 20; " +
                        "-fx-text-fill: #EF4444; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5 10;"
        ));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: " + (isDarkMode ? "#94A3B8" : "#9CA3AF") + "; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5 10;"
        ));
        closeBtn.setOnAction(e -> closeProfile());
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.getChildren().add(closeBtn);
// Аватар
        Circle avatar = new Circle(60);
        avatar.setFill(new javafx.scene.paint.LinearGradient(
                0, 0, 1, 1, true,
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.rgb(102, 126, 234)),
                new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.rgb(118, 75, 162))
        ));
        avatar.setStroke(javafx.scene.paint.Color.WHITE);
        avatar.setStrokeWidth(4);
        Label avatarIcon = new Label("👤");
        avatarIcon.setStyle("-fx-font-size: 40px; -fx-text-fill: white;");
        StackPane avatarContainer = new StackPane(avatar, avatarIcon);
        avatarContainer.setAlignment(Pos.CENTER);
// Имя пользователя (теперь видно в обеих темах)
        Label userNameLabel = new Label(currentUserLogin);
        userNameLabel.setStyle(
                "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + (isDarkMode ? "#E2E8F0" : "#1F2937") + ";"
        );
// Статус онлайн
        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER);
        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px;");
        Label statusText = new Label("Онлайн");
        statusText.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: 500;");
        statusBox.getChildren().addAll(statusDot, statusText);
// Разделитель (адаптируется под тему)
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle(isDarkMode ? D_SEPARATOR : L_SEPARATOR);
        separator.setPadding(new Insets(10, 0, 10, 0));
// Поле "О себе"
        Label aboutLabel = new Label("Расскажите о себе:");
        aboutLabel.setStyle(
                "-fx-text-fill: " + (isDarkMode ? "#94A3B8" : "#6B7280") + "; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 500;"
        );
        TextArea aboutTextArea = new TextArea();
        aboutTextArea.setPromptText("Введите информацию о себе...");
        aboutTextArea.setWrapText(true);
        aboutTextArea.setPrefRowCount(3);
        aboutTextArea.setStyle(isDarkMode ? TEXT_AREA_D : TEXT_AREA_L);
// Кнопка выхода
        Button logoutBtn = new Button("Выйти из аккаунта");
        logoutBtn.setStyle(PROFILE_BTN_LOGOUT);
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> handleLogout());
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(PROFILE_BTN_LOGOUT_H));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(PROFILE_BTN_LOGOUT));
// Кнопка "Назад в меню"
        Button backBtn = new Button("← Назад в меню");
        backBtn.setStyle(isDarkMode ? PROFILE_BTN_BACK_D : PROFILE_BTN_BACK_L);
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> closeProfile());
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(isDarkMode ? PROFILE_BTN_BACK_H_D : PROFILE_BTN_BACK_H_L));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(isDarkMode ? PROFILE_BTN_BACK_D : PROFILE_BTN_BACK_L));
        panel.getChildren().addAll(
                topBar,
                avatarContainer,
                userNameLabel,
                statusBox,
                separator,
                aboutLabel,
                aboutTextArea,
                new Region(),
                logoutBtn,
                backBtn
        );
        VBox.setVgrow(panel.getChildren().get(panel.getChildren().size() - 3), Priority.ALWAYS);
        return panel;
    }
    // === Открытие/Закрытие профиля ===
    private void toggleProfile() {
        if (isProfileOpen) {
            closeProfile();
        } else {
            openProfile();
        }
    }
    private void openProfile() {
        isProfileOpen = true;
        profilePanel.setVisible(true);
        double labelX = userLabel.localToScene(userLabel.getBoundsInLocal()).getMinX();
        double labelY = userLabel.localToScene(userLabel.getBoundsInLocal()).getMaxY() + 10;
        profilePanel.setLayoutX(Math.max(15, labelX - 140));
        profilePanel.setLayoutY(labelY);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), profilePanel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(200), profilePanel);
        slideDown.setFromY(-20);
        slideDown.setToY(0);
        slideDown.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fadeIn, slideDown).play();
    }
    private void closeProfile() {
        if (!isProfileOpen) return;
        isProfileOpen = false;
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), profilePanel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(Interpolator.EASE_IN);
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(150), profilePanel);
        slideUp.setFromY(0);
        slideUp.setToY(-20);
        slideUp.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition exitAnim = new ParallelTransition(fadeOut, slideUp);
        exitAnim.setOnFinished(e -> profilePanel.setVisible(false));
        exitAnim.play();
    }
    // === Обработка выхода ===
    private void handleLogout() {
        closeProfile();
        PauseTransition pause = new PauseTransition(Duration.millis(200));
        pause.setOnFinished(e -> {
            stopAutoRefresh();
            if (networkService != null && networkService.isConnected()) {
                networkService.disconnect();
            }
            returnToAuth();
        });
        pause.play();
    }
    // === Переподключение и возврат к авторизации ===
    private void returnToAuth() {
        NetworkService newNetworkService = new NetworkService("localhost", 7301);
        javafx.concurrent.Task<Boolean> connectTask = new javafx.concurrent.Task<>() {
            @Override
            protected Boolean call() {
                return newNetworkService.connect();
            }
        };
        connectTask.setOnSucceeded(event -> {
            if (connectTask.getValue()) {
                try {
                    ConnectionInitializer initializer = new ConnectionInitializer(newNetworkService, "connected");
                    CommandResponse initResponse = initializer.initialize();
                    if (initResponse != null) {
                        CommandRegistryLoader loader = new CommandRegistryLoader(newNetworkService);
                        AllCommands allCommands = loader.loadCommands(initResponse);
                        AuthScene authScene = new AuthScene(stage, newNetworkService, localization);
                        authScene.setOnLoginSuccess(() -> {
                            MainScene newMainScene = new MainScene(stage, localization, newNetworkService,
                                    authScene.getLoginText(), authScene.getPasswordText());
                            stage.setScene(newMainScene.createScene());
                            stage.setTitle(localization.get("app.title") + " - " + authScene.getLoginText());
                            stage.setWidth(1200);
                            stage.setHeight(800);
                            stage.centerOnScreen();
                        });
                        stage.setScene(authScene.createScene());
// === ВОЗВРАЩАЕМ ИСХОДНЫЕ РАЗМЕРЫ ДЛЯ ОКНА АВТОРИЗАЦИИ ===
                        stage.setWidth(500);
                        stage.setHeight(600);
                        stage.centerOnScreen();
                        stage.setTitle(localization.get("app.title"));
                    } else {
                        showError("Не удалось инициализировать соединение");
                    }
                } catch (Exception ex) {
                    showError("Ошибка инициализации: " + ex.getMessage());
                }
            } else {
                showError("Не удалось подключиться к серверу");
            }
        });
        connectTask.setOnFailed(event -> {
            showError("Ошибка подключения: " + connectTask.getException().getMessage());
        });
        new Thread(connectTask).start();
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        if (refreshScheduler != null && !refreshScheduler.isShutdown()) refreshScheduler.shutdownNow();
    }
    private HBox createTopPanel() {
        HBox hbox = new HBox(20);
        hbox.setPadding(new Insets(12, 20, 12, 20));
        hbox.setAlignment(Pos.CENTER_LEFT);

        // === ЗАГРУЗКА ИЗОБРАЖЕНИЙ ===
        javafx.scene.image.Image sunImage;
        javafx.scene.image.Image moonImage;
        try {
            sunImage = new javafx.scene.image.Image(getClass().getResourceAsStream("/sun.png"));
            moonImage = new javafx.scene.image.Image(getClass().getResourceAsStream("/moon.png"));
            if (sunImage.isError() || moonImage.isError()) {
                throw new Exception("Images failed to load");
            }
        } catch (Exception e) {
            System.err.println("Ошибка: Не удалось загрузить картинки темы");
            sunImage = null;
            moonImage = null;
        }

        final javafx.scene.image.ImageView themeIcon = new javafx.scene.image.ImageView();
        themeIcon.setFitWidth(24);
        themeIcon.setFitHeight(24);
        themeIcon.setPreserveRatio(true);
        if (moonImage != null) themeIcon.setImage(moonImage);

        themeToggleButton = new Button();
        themeToggleButton.setGraphic(themeIcon);
        themeToggleButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        themeToggleButton.setTooltip(new Tooltip("Переключить тему"));
        Image finalSunImage = sunImage;
        Image finalMoonImage = moonImage;
        themeToggleButton.setOnAction(e -> {
            isDarkMode = !isDarkMode;
            if (isDarkMode && finalSunImage != null) {
                themeIcon.setImage(finalSunImage);
            } else if (!isDarkMode && finalMoonImage != null) {
                themeIcon.setImage(finalMoonImage);
            }
            applyThemeStyles();
        });

        // === USER LABEL — ИСПРАВЛЕННАЯ ВЕРСИЯ ===
        userLabel = new Label(localization.get("main.user.label") + " " + currentUserLogin);
        updateUserLabelStyle(false); // initial style

        userLabel.setOnMouseClicked(e -> toggleProfile());
        userLabel.setOnMouseEntered(e -> updateUserLabelStyle(true));  // hover
        userLabel.setOnMouseExited(e -> updateUserLabelStyle(false));  // exit
        // =================================

        balanceButton = new Button(localization.get("btn.balance"));
        themeAwareButtons.add(balanceButton);
        setupButtonHover(balanceButton, false);
        balanceButton.setOnAction(e -> {
            String originalText = balanceButton.getText();
            balanceButton.setText("...");
            balanceButton.setDisable(true);
            new Thread(() -> {
                try {
                    common.CommandRequest req = new common.CommandRequest("show_balance", java.util.List.of("show_balance"), null, true, currentUserLogin, currentUserPassword);
                    networkService.send(req);
                    common.CommandResponse resp = networkService.receive();
                    Platform.runLater(() -> {
                        if (resp != null && resp.isSuccess()) {
                            balanceButton.setText(resp.getMessage().replace("Ваш баланс: ", "").trim() + " ₽");
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

        hbox.getChildren().addAll(themeToggleButton, userLabel, balanceButton, depositButton, spacer, langLabel, langComboBox);
        return hbox;
    }

    /**
     * Вспомогательный метод для стилизации userLabel.
     * Гарантирует, что font-size и font-weight всегда одинаковы.
     */
    private void updateUserLabelStyle(boolean isHover) {
        // Базовые стили, которые НИКОГДА не меняются
        String baseStyle = "-fx-font-size: 14px; -fx-font-weight: 600; -fx-cursor: hand;";

        String textColor, underline;

        if (isDarkMode) {
            // Тёмная тема
            textColor = isHover ? "-fx-text-fill: #A78BFA;" : "-fx-text-fill: #8B5CF6;";
            underline = isHover ? "-fx-underline: false;" : "-fx-underline: true;";
        } else {
            // Светлая тема
            textColor = isHover ? "-fx-text-fill: #1D4ED8;" : "-fx-text-fill: #2563EB;";
            underline = isHover ? "-fx-underline: false;" : "-fx-underline: true;";
        }

        userLabel.setStyle(baseStyle + textColor + underline);
    }

    private SplitPane createCenterSplit() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        splitPane.setStyle("-fx-background-color: transparent;");
        tableController = new VehicleTableController(localization);
        tableContainer = tableController.createTablePane();
        if (commandHandler != null) commandHandler.setTableController(tableController);
        tableController.getTable().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
                if (selected != null) commandHandler.executeEdit(selected);
            } else if (event.getClickCount() == 1) {
                Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
                if (selected != null && canvasController != null) canvasController.focusOnVehicle(selected);
            }
        });
        if (canvasController == null) canvasController = new VehicleCanvasController(localization);
        javafx.scene.canvas.Canvas canvas = canvasController.createCanvas(600, 600);
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
        canvasContainer = new VBox(10);
        visualTitle = new Label("Визуализация координат");
        visualTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + (isDarkMode ? "#E2E8F0" : "#334155") + ";");
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
        if (isDarkMode) return isPrimary ? D_BTN_P : D_BTN_S;
        return isPrimary ? L_BTN_P : L_BTN_S;
    }
    private String getHoverStyle(boolean isPrimary) {
        if (isDarkMode) return isPrimary ? D_BTN_P_H : D_BTN_S_H;
        return isPrimary ? L_BTN_P_H : L_BTN_S_H;
    }
    private String getBaseStyle(String type) {
        return isDarkMode ? D_BTN_D : L_BTN_D;
    }
    private String getHoverStyle(String type) {
        return isDarkMode ? D_BTN_D_H : L_BTN_D_H;
    }
    private void setupButtonHover(Button btn, boolean isPrimary) {
        btn.setOnMouseEntered(e -> btn.setStyle(getHoverStyle(isPrimary)));
        btn.setOnMouseExited(e -> btn.setStyle(getBaseStyle(isPrimary)));
    }
    private void setupButtonHover(Button btn, String type) {
        btn.setOnMouseEntered(e -> btn.setStyle(getHoverStyle(type)));
        btn.setOnMouseExited(e -> btn.setStyle(getBaseStyle(type)));
    }
    private void applyThemeStyles() {
        if (root == null) return;
        root.setStyle(isDarkMode ? D_BG : L_BG);
        if (topPanel != null) topPanel.setStyle(isDarkMode ? D_CARD : L_CARD);

        if (tableContainer != null) {
            if (isDarkMode) {
                tableContainer.setStyle("-fx-background-color: #050505; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 2); -fx-padding: 15;");
            } else {
                tableContainer.setStyle(L_CARD + " -fx-padding: 15;");
            }
        }

        if (canvasContainer != null) canvasContainer.setStyle(isDarkMode ? D_CARD : L_CARD + " -fx-padding: 15;");
        if (bottomPanel != null) bottomPanel.setStyle("-fx-background-color: transparent;");

        // === ИСПРАВЛЕНИЕ: используем новый метод ===
        if (userLabel != null) {
            updateUserLabelStyle(false); // Сбрасываем hover при смене темы
        }

        if (visualTitle != null) visualTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + (isDarkMode ? "#8B5CF6" : "#2563EB") + ";");

        for (Button btn : themeAwareButtons) {
            if (btn.getStyle().contains(D_BTN_P) || btn.getStyle().contains(L_BTN_P) ||
                    btn.getStyle().contains(D_BTN_S) || btn.getStyle().contains(L_BTN_S) ||
                    btn.getStyle().contains(D_BTN_D) || btn.getStyle().contains(L_BTN_D)) {
                if (btn.getText().equals("Очистить")) {
                    btn.setStyle(getBaseStyle("danger"));
                } else if (btn.getText().equals(localization.get("btn.add"))) {
                    btn.setStyle(getBaseStyle(true));
                } else {
                    btn.setStyle(getBaseStyle(false));
                }
            }
        }

        if (balanceButton != null) balanceButton.setStyle(getBaseStyle(false));
        if (depositButton != null) depositButton.setStyle(getBaseStyle(false));

        if (langComboBox != null) {
            langComboBox.setStyle("-fx-background-color: " + (isDarkMode ? "#334155" : "#F1F5F9") +
                    "; -fx-border-color: " + (isDarkMode ? "#475569" : "#E2E8F0") +
                    "; -fx-border-radius: 6; -fx-background-radius: 6;");
        }

        if (tableController != null) tableController.setDarkMode(isDarkMode);
        if (canvasController != null) canvasController.setDarkMode(isDarkMode);
        commandHandler.setDarkMode(isDarkMode);
    }
    private void handleExit() {
        stopAutoRefresh();
        if (networkService != null && networkService.isConnected()) networkService.disconnect();
        stage.close();
    }
    private void showWarning(String msg) {
        ModernNotifications.showWarning(notificationContainer, msg, isDarkMode);
    }
    public void showSuccessNotification(String msg) {
        ModernNotifications.showSuccess(notificationContainer, msg, isDarkMode);
    }
    public void showErrorNotification(String msg) {
        ModernNotifications.showError(notificationContainer, msg, isDarkMode);
    }
    public void showInfoNotification(String msg) {
        ModernNotifications.showInfo(notificationContainer, msg, isDarkMode);
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
    private void updateLangComboBoxStyle() {
        if (langComboBox == null) return;
// Определяем цвета
        String bgColor = isDarkMode ? "#334155" : "#FFFFFF";
        String textColor = isDarkMode ? "#E2E8F0" : "#1F2937";
        String borderColor = isDarkMode ? "#475569" : "#E2E8F0";
// Стиль для основной кнопки комбобокса
        langComboBox.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-text-fill: " + textColor + ";");
// Стиль для элементов выпадающего списка
        langComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : localization.getLocaleDisplayName(item));
// Применяем темный/светлый фон к каждой ячейке
                String style = "-fx-background-color: " + bgColor + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 13px; -fx-padding: 5 10;";
                setStyle(style);
            }
        });
// Стиль для отображения выбранного элемента (в закрытом виде)
        langComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : localization.getLocaleDisplayName(item));
                setStyle("-fx-text-fill: " + textColor + ";");
            }
        });
    }
}