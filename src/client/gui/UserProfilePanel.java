package client.gui;

import client.logic.NetworkService;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

//панель профиля пользователя
public class UserProfilePanel {
    private final Stage stage;
    private final String currentUserLogin;
    private final LocalizationManager localization;
    private final NetworkService networkService;
    private final ThemeManager themeManager;
    private final Runnable onAutoRefreshStop;

    //стили профиля
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

    private VBox profilePanel;
    private boolean isProfileOpen = false;
    private Button logoutBtn;
    private Button backBtn;
    private Label userLabel;

    public UserProfilePanel(Stage stage, String currentUserLogin, LocalizationManager localization,
                            NetworkService networkService, ThemeManager themeManager,
                            Runnable onAutoRefreshStop) {
        this.stage = stage;
        this.currentUserLogin = currentUserLogin;
        this.localization = localization;
        this.networkService = networkService;
        this.themeManager = themeManager;
        this.onAutoRefreshStop = onAutoRefreshStop;
        this.profilePanel = createProfilePanel();
    }

    public VBox getPanel() {
        return profilePanel;
    }

    public boolean isOpen() {
        return isProfileOpen;
    }

    public void setUserLabel(Label userLabel) {
        this.userLabel = userLabel;
    }

    //создаём панель профиля
    private VBox createProfilePanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(350);
        panel.setPrefHeight(450);
        panel.setStyle(themeManager.isDarkMode() ? D_PROFILE_BG : L_PROFILE_BG);
        panel.setMouseTransparent(false);

        //кнопка закрытия
        Button closeBtn = createCloseButton();

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.getChildren().add(closeBtn);

        //аватар пользователя
        StackPane avatarContainer = createAvatar();

        //имя пользователя
        Label userNameLabel = new Label(currentUserLogin);
        userNameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " +
                (themeManager.isDarkMode() ? "#E2E8F0" : "#1F2937") + ";");

        //статус онлайн
        HBox statusBox = createStatusBox();

        //разделитель
        Region separator = createSeparator();

        //кнопки профиля
        logoutBtn = createLogoutButton();
        backBtn = createBackButton();

        panel.getChildren().addAll(topBar, avatarContainer, userNameLabel, statusBox, separator,
                new Region(), logoutBtn, backBtn);
        VBox.setVgrow(panel.getChildren().get(panel.getChildren().size() - 3), Priority.ALWAYS);

        return panel;
    }

    //создаём кнопку закрытия
    private Button createCloseButton() {
        Button closeBtn = new Button("✕");
        String normalStyle = "-fx-background-color: transparent; -fx-text-fill: " +
                (themeManager.isDarkMode() ? "#94A3B8" : "#9CA3AF") +
                "; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10;";
        String hoverStyle = "-fx-background-color: " +
                (themeManager.isDarkMode() ? "#334155" : "#F3F4F6") +
                "; -fx-background-radius: 20; -fx-text-fill: #EF4444; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10;";

        closeBtn.setStyle(normalStyle);
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(hoverStyle));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(normalStyle));
        closeBtn.setOnAction(e -> closeProfile());

        return closeBtn;
    }

    //создаём аватар
    private StackPane createAvatar() {
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

        return avatarContainer;
    }

    //создаём статус онлайн
    private HBox createStatusBox() {
        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER);

        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px;");

        Label statusText = new Label(localization.get("profile.status.online"));
        statusText.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: 500;");

        statusBox.getChildren().addAll(statusDot, statusText);

        return statusBox;
    }

    //создаём разделитель
    private Region createSeparator() {
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle(themeManager.isDarkMode() ? D_SEPARATOR : L_SEPARATOR);
        separator.setPadding(new Insets(10, 0, 10, 0));

        return separator;
    }

    //создаём кнопку выхода
    private Button createLogoutButton() {
        Button logoutBtn = new Button(localization.get("btn.exit"));
        logoutBtn.setStyle(PROFILE_BTN_LOGOUT);
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> handleLogout());
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(PROFILE_BTN_LOGOUT_H));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(PROFILE_BTN_LOGOUT));

        return logoutBtn;
    }

    //создаём кнопку назад
    private Button createBackButton() {
        Button backBtn = new Button(localization.get("dialog.cancel"));
        backBtn.setStyle(themeManager.isDarkMode() ? PROFILE_BTN_BACK_D : PROFILE_BTN_BACK_L);
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> closeProfile());
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(themeManager.isDarkMode() ? PROFILE_BTN_BACK_H_D : PROFILE_BTN_BACK_H_L));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(themeManager.isDarkMode() ? PROFILE_BTN_BACK_D : PROFILE_BTN_BACK_L));

        return backBtn;
    }

    //переключаем профиль
    public void toggleProfile() {
        if (isProfileOpen) closeProfile(); else openProfile();
    }

    //открываем профиль с анимацией
    public void openProfile() {
        if (userLabel == null) return;

        isProfileOpen = true;
        profilePanel.setVisible(true);

        //позиционируем под меткой пользователя
        javafx.geometry.Bounds labelBounds = userLabel.localToScene(userLabel.getBoundsInLocal());
        double labelX = labelBounds.getMinX();
        double labelY = labelBounds.getMaxY() + 10;
        double panelWidth = profilePanel.getPrefWidth();
        double sceneWidth = stage.getScene().getWidth();
        double panelX = Math.max(15, Math.min(labelX - 140, sceneWidth - panelWidth - 15));
        profilePanel.setLayoutX(panelX);
        profilePanel.setLayoutY(Math.max(15, labelY));

        //анимация появления
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

    //закрываем профиль с анимацией
    public void closeProfile() {
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

    //обрабатываем выход из аккаунта
    private void handleLogout() {
        closeProfile();
        PauseTransition pause = new PauseTransition(Duration.millis(200));
        pause.setOnFinished(e -> {
            if (onAutoRefreshStop != null) onAutoRefreshStop.run();
            if (networkService != null && networkService.isConnected()) networkService.disconnect();
            returnToAuth();
        });
        pause.play();
    }

    //возвращаемся на экран авторизации
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
                        });

                        stage.setScene(authScene.createScene());
                        stage.setMinWidth(800);
                        stage.setMinHeight(600);
                        stage.setMaximized(true);
                        stage.centerOnScreen();
                        stage.setTitle(localization.get("app.title"));
                    } else {
                        showError(localization.get("error.init_connection"));
                    }
                } catch (Exception ex) {
                    showError(localization.get("error.init_detail") + ex.getMessage());
                }
            } else {
                showError(localization.get("error.connect"));
            }
        });

        connectTask.setOnFailed(event ->
                showError(localization.get("error.connect_detail") + connectTask.getException().getMessage())
        );

        new Thread(connectTask).start();
    }

    //показываем ошибку
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(localization.get("app.status.error"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //обновляем стили элементов профиля
    public void updateStyles() {
        if (profilePanel != null) {
            profilePanel.setStyle(themeManager.isDarkMode() ? D_PROFILE_BG : L_PROFILE_BG);
            for (Node node : profilePanel.getChildren()) {
                updateProfileNodeStyle(node);
            }
        }
    }

    //обновляем стили элементов профиля
    private void updateProfileNodeStyle(Node node) {
        if (node instanceof VBox || node instanceof HBox) {
            for (Node child : ((javafx.scene.layout.Region) node).getChildrenUnmodifiable()) {
                updateProfileNodeStyle(child);
            }
        } else if (node instanceof Label) {
            Label label = (Label) node;
            String text = label.getText();
            if (text != null && text.equals(currentUserLogin)) {
                label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " +
                        (themeManager.isDarkMode() ? "#E2E8F0" : "#1F2937") + ";");
            } else if (text != null && text.equals(localization.get("profile.status.online"))) {
                label.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: 500;");
            } else if ("✕".equals(text)) {
                label.setStyle("-fx-background-color: transparent; -fx-text-fill: " +
                        (themeManager.isDarkMode() ? "#94A3B8" : "#9CA3AF") +
                        "; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10;");
            }
        } else if (node instanceof Button) {
            Button btn = (Button) node;
            if (btn == logoutBtn) {
                btn.setStyle(PROFILE_BTN_LOGOUT);
            } else if (btn == backBtn) {
                btn.setStyle(themeManager.isDarkMode() ? PROFILE_BTN_BACK_D : PROFILE_BTN_BACK_L);
            }
        } else if (node instanceof Region) {
            Region region = (Region) node;
            if (region.getPrefHeight() == 1) {
                region.setStyle(themeManager.isDarkMode() ? D_SEPARATOR : L_SEPARATOR);
            }
        }
    }
}