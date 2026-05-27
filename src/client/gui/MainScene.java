package client.gui;

import client.logic.NetworkService;
import common.Vehicle;
import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
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
    private BorderPane root;
    private HBox topPanel;
    private VBox tableContainer;
    private VBox canvasContainer;
    private HBox bottomPanel;
    private Label visualTitle;
    private List<Button> themeAwareButtons = new ArrayList<>();
    private Label userLabel;
    private Button balanceButton;
    private Button depositButton;
    private Button themeToggleButton;
    private ImageView themeIconView;
    private ComboBox<Locale> langComboBox;
    private VBox notificationContainer;
    private List<Vehicle> lastCanvasVehicles = List.of();
    private ScheduledExecutorService refreshScheduler;
    private boolean isDarkMode = false;

    // === Профиль ===
    private VBox profilePanel;
    private boolean isProfileOpen = false;
    private Button logoutBtn;
    private Button backBtn;

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

        profilePanel = createProfilePanel();
        profilePanel.setVisible(false);
        profilePanel.setOpacity(0);
        profilePanel.setTranslateY(-20);

        StackPane mainContainer = new StackPane(root, notificationContainer, profilePanel);
        Scene scene = new Scene(mainContainer, 1300, 850);

        KeyCombination exitKey = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(exitKey, () -> handleExit());

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

    private VBox createProfilePanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(350);
        panel.setPrefHeight(450);
        panel.setStyle(isDarkMode ? D_PROFILE_BG : L_PROFILE_BG);
        panel.setMouseTransparent(false);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: " + (isDarkMode ? "#94A3B8" : "#9CA3AF") + "; " +
                        "-fx-font-size: 18px; " + "-fx-font-weight: bold; " + "-fx-cursor: hand; " + "-fx-padding: 5 10;"
        );
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
                "-fx-background-color: " + (isDarkMode ? "#334155" : "#F3F4F6") + "; " +
                        "-fx-background-radius: 20; " + "-fx-text-fill: #EF4444; " +
                        "-fx-font-size: 18px; " + "-fx-font-weight: bold; " + "-fx-cursor: hand; " + "-fx-padding: 5 10;"
        ));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: " + (isDarkMode ? "#94A3B8" : "#9CA3AF") + "; " +
                        "-fx-font-size: 18px; " + "-fx-font-weight: bold; " + "-fx-cursor: hand; " + "-fx-padding: 5 10;"
        ));
        closeBtn.setOnAction(e -> closeProfile());

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.getChildren().add(closeBtn);

        Circle avatar = new Circle(60);
        avatar.setFill(new javafx.scene.paint.LinearGradient(
                0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.rgb(102, 126, 234)),
                new javafx.scene.paint.Stop(1, Color.rgb(118, 75, 162))
        ));
        avatar.setStroke(Color.WHITE);
        avatar.setStrokeWidth(4);
        Label avatarIcon = new Label("👤");
        avatarIcon.setStyle("-fx-font-size: 40px; -fx-text-fill: white;");
        StackPane avatarContainer = new StackPane(avatar, avatarIcon);
        avatarContainer.setAlignment(Pos.CENTER);

        Label userNameLabel = new Label(currentUserLogin);
        userNameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + (isDarkMode ? "#E2E8F0" : "#1F2937") + ";");

        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER);
        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px;");
        Label statusText = new Label(localization.get("profile.status.online"));
        statusText.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: 500;");
        statusBox.getChildren().addAll(statusDot, statusText);

        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle(isDarkMode ? D_SEPARATOR : L_SEPARATOR);
        separator.setPadding(new Insets(10, 0, 10, 0));

        logoutBtn = new Button(localization.get("btn.exit"));
        logoutBtn.setStyle(PROFILE_BTN_LOGOUT);
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> handleLogout());
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(PROFILE_BTN_LOGOUT_H));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(PROFILE_BTN_LOGOUT));

        backBtn = new Button(localization.get("dialog.cancel"));
        backBtn.setStyle(isDarkMode ? PROFILE_BTN_BACK_D : PROFILE_BTN_BACK_L);
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> closeProfile());
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(isDarkMode ? PROFILE_BTN_BACK_H_D : PROFILE_BTN_BACK_H_L));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(isDarkMode ? PROFILE_BTN_BACK_D : PROFILE_BTN_BACK_L));

        panel.getChildren().addAll(topBar, avatarContainer, userNameLabel, statusBox, separator, new Region(), logoutBtn, backBtn);
        VBox.setVgrow(panel.getChildren().get(panel.getChildren().size() - 3), Priority.ALWAYS);
        return panel;
    }

    private void toggleProfile() {
        if (isProfileOpen) closeProfile(); else openProfile();
    }

    private void openProfile() {
        isProfileOpen = true;
        profilePanel.setVisible(true);
        javafx.geometry.Bounds labelBounds = userLabel.localToScene(userLabel.getBoundsInLocal());
        double labelX = labelBounds.getMinX();
        double labelY = labelBounds.getMaxY() + 10;
        double panelWidth = profilePanel.getPrefWidth();
        double sceneWidth = stage.getScene().getWidth();
        double panelX = Math.max(15, Math.min(labelX - 140, sceneWidth - panelWidth - 15));
        profilePanel.setLayoutX(panelX);
        profilePanel.setLayoutY(Math.max(15, labelY));

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), profilePanel);
        fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.setInterpolator(Interpolator.EASE_OUT);
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(200), profilePanel);
        slideDown.setFromY(-20); slideDown.setToY(0); slideDown.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fadeIn, slideDown).play();
    }

    private void closeProfile() {
        if (!isProfileOpen) return;
        isProfileOpen = false;
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), profilePanel);
        fadeOut.setFromValue(1); fadeOut.setToValue(0); fadeOut.setInterpolator(Interpolator.EASE_IN);
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(150), profilePanel);
        slideUp.setFromY(0); slideUp.setToY(-20); slideUp.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition exitAnim = new ParallelTransition(fadeOut, slideUp);
        exitAnim.setOnFinished(e -> profilePanel.setVisible(false));
        exitAnim.play();
    }

    private void handleLogout() {
        closeProfile();
        PauseTransition pause = new PauseTransition(Duration.millis(200));
        pause.setOnFinished(e -> {
            stopAutoRefresh();
            if (networkService != null && networkService.isConnected()) networkService.disconnect();
            returnToAuth();
        });
        pause.play();
    }

    private void returnToAuth() {
        NetworkService newNetworkService = new NetworkService("localhost", 7301);
        javafx.concurrent.Task<Boolean> connectTask = new javafx.concurrent.Task<>() {
            @Override protected Boolean call() { return newNetworkService.connect(); }
        };
        connectTask.setOnSucceeded(event -> {
            if (connectTask.getValue()) {
                try {
                    client.logic.ConnectionInitializer initializer = new client.logic.ConnectionInitializer(newNetworkService, "connected");
                    common.CommandResponse initResponse = initializer.initialize();
                    if (initResponse != null) {
                        client.logic.CommandRegistryLoader loader = new client.logic.CommandRegistryLoader(newNetworkService);
                        loader.loadCommands(initResponse);
                        AuthScene authScene = new AuthScene(stage, newNetworkService, localization);
                        authScene.setOnLoginSuccess(() -> {
                            MainScene newMainScene = new MainScene(stage, localization, newNetworkService,
                                    authScene.getLoginText(), authScene.getPasswordText());
                            stage.setScene(newMainScene.createScene());
                            stage.setTitle(localization.get("app.title") + " - " + authScene.getLoginText());
                            stage.setMaximized(true);
                            stage.setResizable(true);
                        });
                        stage.setScene(authScene.createScene());
                        stage.setWidth(520);
                        stage.setHeight(600);
                        stage.setResizable(false);
                        stage.centerOnScreen();
                        stage.setTitle(localization.get("app.title"));
                    } else showError(localization.get("error.init_connection"));
                } catch (Exception ex) { showError(localization.get("error.init_detail") + ex.getMessage()); }
            } else showError(localization.get("error.connect"));
        });
        connectTask.setOnFailed(event -> showError(localization.get("error.connect_detail") + connectTask.getException().getMessage()));
        new Thread(connectTask).start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(localization.get("app.status.error"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void startAutoRefresh() {
        refreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AutoRefresh-" + currentUserLogin); t.setDaemon(true); return t;
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

        Image sunImage = null, moonImage = null;
        try {
            sunImage = new Image(getClass().getResourceAsStream("/sun.png"));
            moonImage = new Image(getClass().getResourceAsStream("/moon.png"));
            if (sunImage.isError() || moonImage.isError()) throw new Exception("Load error");
        } catch (Exception e) {
            System.out.println("Иконки темы не найдены, используется текстовый режим.");
        }

        themeToggleButton = new Button();
        themeIconView = new ImageView();
        if (sunImage != null && moonImage != null) {
            themeIconView.setFitWidth(24); themeIconView.setFitHeight(24); themeIconView.setPreserveRatio(true);
            themeIconView.setImage(moonImage);
            themeToggleButton.setGraphic(themeIconView);
        } else {
            // === ИСПРАВЛЕНО: Показываем эмодзи если картинки не загрузились ===
            themeToggleButton.setText("🌙");
            themeToggleButton.setStyle("-fx-font-size: 20px; -fx-background-color: transparent; -fx-cursor: hand;");
        }
        themeToggleButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        themeToggleButton.setOnAction(e -> {
            isDarkMode = !isDarkMode;
            updateThemeIcon();
            applyThemeStyles();
        });

        userLabel = new Label(localization.get("main.user.label") + " " + currentUserLogin);
        String baseColor = isDarkMode ? "#8B5CF6" : "#2563EB";
        userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + baseColor + "; -fx-cursor: hand; -fx-underline: true;");

        userLabel.setOnMouseClicked(e -> toggleProfile());
        userLabel.setOnMouseEntered(e -> {
            String hoverColor = isDarkMode ? "#A78BFA" : "#1D4ED8";
            userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + hoverColor + "; -fx-cursor: hand; -fx-underline: false;");
        });
        userLabel.setOnMouseExited(e -> {
            String baseCol = isDarkMode ? "#8B5CF6" : "#2563EB";
            userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + baseCol + "; -fx-cursor: hand; -fx-underline: true;");
        });

        balanceButton = new Button(localization.get("btn.balance"));
        themeAwareButtons.add(balanceButton);
        setupButtonHover(balanceButton, false);
        balanceButton.setOnAction(e -> {
            String originalText = balanceButton.getText();
            balanceButton.setText("..."); balanceButton.setDisable(true);
            new Thread(() -> {
                try {
                    common.CommandRequest req = new common.CommandRequest("show_balance", java.util.List.of("show_balance"), null, true, currentUserLogin, currentUserPassword);
                    networkService.send(req);
                    common.CommandResponse resp = networkService.receive();
                    Platform.runLater(() -> {
                        if (resp != null && resp.isSuccess()) {
                            balanceButton.setText(resp.getMessage().replace(localization.get("error.balance_prefix", localization.getCurrentLocale()), "").trim() + localization.get("error.currency_suffix", localization.getCurrentLocale()));
                        } else { balanceButton.setText(localization.get("error.network")); }
                        PauseTransition pause = new PauseTransition(Duration.seconds(3));
                        pause.setOnFinished(ev -> { balanceButton.setText(originalText); balanceButton.setDisable(false); });
                        pause.play();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        balanceButton.setText(localization.get("error.network"));
                        PauseTransition pause = new PauseTransition(Duration.seconds(3));
                        pause.setOnFinished(ev -> { balanceButton.setText(originalText); balanceButton.setDisable(false); });
                        pause.play();
                    });
                }
            }).start();
        });

        depositButton = new Button(localization.get("btn.deposit"));
        themeAwareButtons.add(depositButton);
        setupButtonHover(depositButton, false);
        depositButton.setOnAction(e -> { if (commandHandler != null) commandHandler.executeDeposit(); });

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
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
            if (selected != null) { localization.setLocale(selected); updateUITexts(); }
        });

        hbox.getChildren().addAll(themeToggleButton, userLabel, balanceButton, depositButton, spacer, langLabel, langComboBox);
        return hbox;
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
            } else Platform.runLater(() -> canvasController.resetView());
        });

        canvasContainer = new VBox(10);
        visualTitle = new Label(localization.get("main.visual.title"));
        visualTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + (isDarkMode ? "#E2E8F0" : "#334155") + ";");

        Pane canvasPane = new Pane(canvas);
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvasContainer.getChildren().addAll(visualTitle, canvasPane);
        VBox.setVgrow(canvasPane, Priority.ALWAYS);

        splitPane.getItems().addAll(tableContainer, canvasContainer);

        // === ИСПРАВЛЕНО: 40% таблица, 60% визуализация ===
        splitPane.setDividerPositions(0.4);

        return splitPane;
    }

    private HBox createBottomPanel() {
        HBox hbox = new HBox(15);
        hbox.setPadding(new Insets(5)); hbox.setAlignment(Pos.CENTER); hbox.setStyle("-fx-background-color: transparent;");

        Button btnAdd = createStyledButton(localization.get("btn.add"), true);
        Button btnRemove = createStyledButton(localization.get("btn.remove"), false);
        Button btnShuffle = createStyledButton(localization.get("btn.shuffle"), false);
        Button btnBuy = createStyledButton(localization.get("btn.buy"), false);
        Button btnClear = createStyledButton(localization.get("btn.clear"), "danger");

        List<Button> buttons = List.of(btnAdd, btnRemove, btnShuffle, btnBuy, btnClear);
        for (Button btn : buttons) {
            HBox.setHgrow(btn, Priority.ALWAYS); btn.setMaxWidth(Double.MAX_VALUE); btn.setPrefHeight(45);
            btn.setFont(javafx.scene.text.Font.font(14)); themeAwareButtons.add(btn);
        }

        btnAdd.setOnAction(e -> commandHandler.executeAdd());
        btnRemove.setOnAction(e -> {
            Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
            if (selected != null) commandHandler.executeRemoveById(selected.getId());
            else showWarning(localization.get("table.warning.select_for_delete"));
        });
        btnShuffle.setOnAction(e -> commandHandler.executeShuffle());
        btnBuy.setOnAction(e -> {
            Vehicle selected = tableController.getTable().getSelectionModel().getSelectedItem();
            if (selected != null) commandHandler.executeBuy(selected.getId());
            else showWarning(localization.get("table.warning.select_for_buy"));
        });
        btnClear.setOnAction(e -> commandHandler.executeClear());

        hbox.getChildren().addAll(buttons);
        return hbox;
    }

    private Button createStyledButton(String text, boolean isPrimary) {
        Button btn = new Button(text); btn.setStyle(getBaseStyle(isPrimary)); setupButtonHover(btn, isPrimary); return btn;
    }
    private Button createStyledButton(String text, String type) {
        Button btn = new Button(text); btn.setStyle(getBaseStyle("danger")); setupButtonHover(btn, "danger"); return btn;
    }
    private String getBaseStyle(boolean isPrimary) {
        return isDarkMode ? (isPrimary ? D_BTN_P : D_BTN_S) : (isPrimary ? L_BTN_P : L_BTN_S);
    }
    private String getHoverStyle(boolean isPrimary) {
        return isDarkMode ? (isPrimary ? D_BTN_P_H : D_BTN_S_H) : (isPrimary ? L_BTN_P_H : L_BTN_S_H);
    }
    private String getBaseStyle(String type) { return isDarkMode ? D_BTN_D : L_BTN_D; }
    private String getHoverStyle(String type) { return isDarkMode ? D_BTN_D_H : L_BTN_D_H; }
    private void setupButtonHover(Button btn, boolean isPrimary) {
        btn.setOnMouseEntered(e -> btn.setStyle(getHoverStyle(isPrimary)));
        btn.setOnMouseExited(e -> btn.setStyle(getBaseStyle(isPrimary)));
    }
    private void setupButtonHover(Button btn, String type) {
        btn.setOnMouseEntered(e -> btn.setStyle(getHoverStyle(type)));
        btn.setOnMouseExited(e -> btn.setStyle(getBaseStyle(type)));
    }

    private void updateThemeIcon() {
        if (themeIconView != null && themeIconView.getImage() != null) {
            Image sunImage = new Image(getClass().getResourceAsStream("/sun.png"));
            Image moonImage = new Image(getClass().getResourceAsStream("/moon.png"));
            if (!sunImage.isError() && !moonImage.isError()) {
                themeIconView.setImage(isDarkMode ? sunImage : moonImage);
            }
        } else if (themeToggleButton != null) {
            // === ИСПРАВЛЕНО: Правильный эмодзи для солнца ===
            themeToggleButton.setText(isDarkMode ? "☀️" : "🌙");
        }
    }

    private void applyThemeStyles() {
        if (root == null) return;
        root.setStyle(isDarkMode ? D_BG : L_BG);
        if (topPanel != null) topPanel.setStyle(isDarkMode ? D_CARD : L_CARD);
        if (tableContainer != null) {
            tableContainer.setStyle(isDarkMode ?
                    "-fx-background-color: #050505; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 2); -fx-padding: 15;" :
                    L_CARD + " -fx-padding: 15;");
        }
        if (canvasContainer != null) canvasContainer.setStyle(isDarkMode ? D_CARD : L_CARD + " -fx-padding: 15;");
        if (bottomPanel != null) bottomPanel.setStyle("-fx-background-color: transparent;");

        if (userLabel != null) {
            String baseColor = isDarkMode ? "#8B5CF6" : "#2563EB";
            userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + baseColor + "; -fx-cursor: hand; -fx-underline: true;");
        }
        if (visualTitle != null) visualTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + (isDarkMode ? "#8B5CF6" : "#2563EB") + ";");

        // === ИСПРАВЛЕНО: Надёжное обновление стилей кнопок по ключам ===
        for (Button btn : themeAwareButtons) {
            String key = getButtonKey(btn.getText());
            if (key != null) {
                if (key.equals("btn.clear")) btn.setStyle(getBaseStyle("danger"));
                else if (key.equals("btn.add")) btn.setStyle(getBaseStyle(true));
                else btn.setStyle(getBaseStyle(false));
            }
        }
        if (balanceButton != null) balanceButton.setStyle(getBaseStyle(false));
        if (depositButton != null) depositButton.setStyle(getBaseStyle(false));

        updateLangComboBoxStyle();

        if (profilePanel != null) {
            profilePanel.setStyle(isDarkMode ? D_PROFILE_BG : L_PROFILE_BG);
            for (Node node : profilePanel.getChildren()) updateProfileNodeStyle(node);
        }

        updateThemeIcon();
        if (tableController != null) tableController.setDarkMode(isDarkMode);
        if (canvasController != null) canvasController.setDarkMode(isDarkMode);
        commandHandler.setDarkMode(isDarkMode);
    }

    // === Вспомогательный метод для определения ключа кнопки по тексту ===
    private String getButtonKey(String buttonText) {
        if (buttonText == null) return null;
        if (buttonText.equals(localization.get("btn.clear")) || buttonText.equals("Очистить") || buttonText.equals("Clear") || buttonText.equals("Tøm") || buttonText.equals("Išvalyti")) return "btn.clear";
        if (buttonText.equals(localization.get("btn.add")) || buttonText.equals("Добавить") || buttonText.equals("Add") || buttonText.equals("Legg til") || buttonText.equals("Pridėti")) return "btn.add";
        if (buttonText.equals(localization.get("btn.remove")) || buttonText.equals("Удалить по ID") || buttonText.equals("Remove by ID") || buttonText.equals("Fjern etter ID") || buttonText.equals("Šalinti pagal ID")) return "btn.remove";
        if (buttonText.equals(localization.get("btn.shuffle")) || buttonText.equals("Перемешать") || buttonText.equals("Shuffle") || buttonText.equals("Bland") || buttonText.equals("Sumaišyti")) return "btn.shuffle";
        if (buttonText.equals(localization.get("btn.buy")) || buttonText.equals("Купить") || buttonText.equals("Buy") || buttonText.equals("Kjøp") || buttonText.equals("Pirkti")) return "btn.buy";
        return null;
    }

    private void updateLangComboBoxStyle() {
        if (langComboBox == null) return;

        String bgColor = isDarkMode ? "#1E293B" : "#FFFFFF";
        String textColor = isDarkMode ? "#E2E8F0" : "#1F2937";
        String hoverBg = isDarkMode ? "#334155" : "#F3F4F6";
        String borderColor = isDarkMode ? "#475569" : "#E2E8F0";

        langComboBox.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-text-fill: " + textColor + ";");

        langComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : localization.getLocaleDisplayName(item));

                String style = "-fx-background-color: " + bgColor + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 13px; -fx-padding: 5 10;";
                setStyle(style);

                if (!empty && item != null) {
                    setOnMouseEntered(e -> setStyle(
                            "-fx-background-color: " + hoverBg + "; " +
                                    "-fx-text-fill: " + textColor + "; " +
                                    "-fx-font-size: 13px; -fx-padding: 5 10;"));
                    setOnMouseExited(e -> setStyle(style));
                }
            }
        });

        langComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : localization.getLocaleDisplayName(item));
                setStyle("-fx-text-fill: " + textColor + ";");
            }
        });
    }

    private void updateProfileNodeStyle(Node node) {
        if (node instanceof VBox || node instanceof HBox) {
            for (Node child : ((javafx.scene.layout.Region) node).getChildrenUnmodifiable()) updateProfileNodeStyle(child);
        } else if (node instanceof Label) {
            Label label = (Label) node;
            String text = label.getText();
            if (text != null && text.equals(currentUserLogin)) {
                label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + (isDarkMode ? "#E2E8F0" : "#1F2937") + ";");
            } else if (text != null && text.equals(localization.get("profile.status.online"))) {
                label.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: 500;");
            } else if ("✕".equals(text)) {
                label.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (isDarkMode ? "#94A3B8" : "#9CA3AF") + "; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10;");
            }
        } else if (node instanceof Button) {
            Button btn = (Button) node;
            if (btn == logoutBtn) btn.setStyle(isDarkMode ? PROFILE_BTN_LOGOUT : PROFILE_BTN_LOGOUT);
            else if (btn == backBtn) btn.setStyle(isDarkMode ? PROFILE_BTN_BACK_D : PROFILE_BTN_BACK_L);
        } else if (node instanceof Region) {
            Region region = (Region) node;
            if (region.getPrefHeight() == 1) region.setStyle(isDarkMode ? D_SEPARATOR : L_SEPARATOR);
        }
    }

    private void handleExit() {
        stopAutoRefresh();
        if (networkService != null && networkService.isConnected()) networkService.disconnect();
        stage.close();
    }

    private void showWarning(String msg) {
        if (notificationContainer != null) ModernNotifications.showWarning(notificationContainer, msg, isDarkMode);
    }
    public void showSuccessNotification(String msg) {
        if (notificationContainer != null) ModernNotifications.showSuccess(notificationContainer, msg, isDarkMode);
    }
    public void showErrorNotification(String msg) {
        if (notificationContainer != null) ModernNotifications.showError(notificationContainer, msg, isDarkMode);
    }
    public void showInfoNotification(String msg) {
        if (notificationContainer != null) ModernNotifications.showInfo(notificationContainer, msg, isDarkMode);
    }

    private void updateUITexts() {
        stage.setTitle(localization.get("app.title") + " - " + currentUserLogin);
        userLabel.setText(localization.get("main.user.label") + " " + currentUserLogin);
        visualTitle.setText(localization.get("main.visual.title"));

        balanceButton.setText(localization.get("btn.balance"));
        depositButton.setText(localization.get("btn.deposit"));

        if (logoutBtn != null) logoutBtn.setText(localization.get("btn.exit"));
        if (backBtn != null) backBtn.setText(localization.get("dialog.cancel"));

        // === ИСПРАВЛЕНО: Надёжное обновление кнопок по ключам ===
        for (Button btn : themeAwareButtons) {
            String key = getButtonKey(btn.getText());
            if (key != null) {
                btn.setText(localization.get(key));
            }
        }

        if (tableController != null) tableController.updateLocalization();
        applyThemeStyles();
    }

    public void updateVisualization() {
        if (tableController != null && canvasController != null) {
            List<Vehicle> currentVehicles = tableController.getAllVehicles();
            if (hasDataChanged(lastCanvasVehicles, currentVehicles)) {
                canvasController.updateData(currentVehicles);
                canvasController.resetView();
                lastCanvasVehicles = new ArrayList<>(currentVehicles);
            } else canvasController.updateData(currentVehicles);
        }
    }

    private boolean hasDataChanged(List<Vehicle> oldList, List<Vehicle> newList) {
        if (oldList == null && newList == null) return false;
        if (oldList == null || newList == null) return true;
        if (oldList.size() != newList.size()) return true;
        for (int i = 0; i < oldList.size(); i++) {
            Vehicle ov = oldList.get(i), nv = newList.get(i);
            if (ov.getId() != nv.getId() ||
                    ov.getCoordinates().getX() != nv.getCoordinates().getX() ||
                    ov.getCoordinates().getY() != nv.getCoordinates().getY()) return true;
        }
        return false;
    }
}