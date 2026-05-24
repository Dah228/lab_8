package client.gui;

import client.logic.NetworkService;
import common.CommandRequest;
import common.CommandResponse;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;

public class AuthScene {
    private final NetworkService networkService;
    private final LocalizationManager localization;
    private TextField loginField;
    private PasswordField passwordField;
    private Button actionButton;
    private ToggleGroup modeToggle;
    private VBox notificationContainer;
    private Runnable onLoginSuccess;

    // Современные стили
    private static final String BG_GRADIENT = "-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);";
    private static final String CARD_STYLE = "-fx-background-color: white; " +
            "-fx-background-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 30, 0, 0, 10);";
    private static final String INPUT_STYLE = "-fx-background-color: #F3F4F6; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: transparent; " +
            "-fx-border-radius: 10; " +
            "-fx-padding: 12 16; " +
            "-fx-font-size: 14px; " +
            "-fx-transition: all 0.3s;";
    private static final String INPUT_FOCUSED = "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #667eea; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-padding: 12 16; " +
            "-fx-font-size: 14px;";
    private static final String BTN_PRIMARY = "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 14 40; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 10, 0, 0, 3);";
    private static final String BTN_HOVER = "-fx-background-color: linear-gradient(to right, #764ba2 0%, #667eea 100%); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 14 40; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(118,75,162,0.5), 15, 0, 0, 5);";
    private static final String TOGGLE_STYLE = "-fx-background-color: transparent; " +
            "-fx-text-fill: #6B7280; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;";
    private static final String TOGGLE_SELECTED = "-fx-background-color: transparent; " +
            "-fx-text-fill: #667eea; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 700; " +
            "-fx-cursor: hand;";

    public AuthScene(Stage stage, NetworkService networkService, LocalizationManager localization) {
        this.networkService = networkService;
        this.localization = localization;
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    public Scene createScene() {
        // Основной контейнер с градиентом
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle(BG_GRADIENT);

        // Карточка авторизации
        VBox card = new VBox(25);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);
        card.setStyle(CARD_STYLE);
        card.setMaxWidth(420);

        // Начальное состояние для анимации
        card.setOpacity(0);
        card.setTranslateY(30);

        // Заголовок с иконкой
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);

        Circle iconCircle = new Circle(35, Color.web("#667eea"));
        Label iconLabel = new Label("🚀");
        iconLabel.setStyle("-fx-font-size: 32px;");
        StackPane iconContainer = new StackPane(iconCircle, iconLabel);

        Label titleLabel = new Label(localization.get("app.title"));
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; " +
                "-fx-text-fill: #1F2937; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        headerBox.getChildren().addAll(iconContainer, titleLabel);

        // Переключатель режимов
        RadioButton rbLogin = new RadioButton(localization.get("auth.login.button"));
        RadioButton rbRegister = new RadioButton(localization.get("auth.register.button"));
        modeToggle = new ToggleGroup();
        rbLogin.setToggleGroup(modeToggle);
        rbRegister.setToggleGroup(modeToggle);
        rbLogin.setSelected(true);
        rbLogin.setStyle(TOGGLE_SELECTED);
        rbRegister.setStyle(TOGGLE_STYLE);

        HBox modeBox = new HBox(20, rbLogin, rbRegister);
        modeBox.setAlignment(Pos.CENTER);
        modeBox.setStyle("-fx-background-color: #F3F4F6; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 5;");

        // Обработка переключения
        modeToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == rbLogin) {
                rbLogin.setStyle(TOGGLE_SELECTED);
                rbRegister.setStyle(TOGGLE_STYLE);
                actionButton.setText(localization.get("auth.login.button"));
            } else {
                rbLogin.setStyle(TOGGLE_STYLE);
                rbRegister.setStyle(TOGGLE_SELECTED);
                actionButton.setText(localization.get("auth.register.button"));
            }
        });

        // Поля ввода
        VBox inputBox = new VBox(15);

        Label loginLabel = createInputLabel(localization.get("auth.login"));
        loginField = createStyledInput();
        loginField.setPromptText("username");

        Label passLabel = createInputLabel(localization.get("auth.password"));
        passwordField = createStyledPasswordInput();
        passwordField.setPromptText("••••••••");

        inputBox.getChildren().addAll(loginLabel, loginField, passLabel, passwordField);

        // Кнопка действия
        actionButton = new Button(localization.get("auth.login.button"));
        actionButton.setStyle(BTN_PRIMARY);
        actionButton.setMaxWidth(Double.MAX_VALUE);
        actionButton.setOnAction(e -> handleAction());

        // Hover эффекты
        actionButton.setOnMouseEntered(e -> actionButton.setStyle(BTN_HOVER));
        actionButton.setOnMouseExited(e -> actionButton.setStyle(BTN_PRIMARY));

        // Контейнер для уведомлений
        notificationContainer = new VBox(10);
        notificationContainer.setAlignment(Pos.TOP_CENTER);

        card.getChildren().addAll(headerBox, modeBox, inputBox, actionButton);
        root.getChildren().addAll(notificationContainer, card);

        Scene scene = new Scene(root, 500, 600);

        // Анимация появления карточки
        Platform.runLater(() -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), card);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

            TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), card);
            slideUp.setFromY(30);
            slideUp.setToY(0);
            slideUp.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

            ParallelTransition appear = new ParallelTransition(fadeIn, slideUp);
            appear.play();
        });

        return scene;
    }

    private Label createInputLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #374151; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 13px;");
        return label;
    }

    private TextField createStyledInput() {
        TextField field = new TextField();
        field.setStyle(INPUT_STYLE);
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            field.setStyle(newVal ? INPUT_FOCUSED : INPUT_STYLE);
        });
        return field;
    }

    private PasswordField createStyledPasswordInput() {
        PasswordField field = new PasswordField();
        field.setStyle(INPUT_STYLE);
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            field.setStyle(newVal ? INPUT_FOCUSED : INPUT_STYLE);
        });
        return field;
    }

    private void handleAction() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.isEmpty() || password.isEmpty()) {
            ModernNotifications.showWarning(notificationContainer, localization.get("auth.error.empty"), false);
            return;
        }

        boolean isRegister = modeToggle.getSelectedToggle() instanceof RadioButton &&
                ((RadioButton) modeToggle.getSelectedToggle()).getText().equals(localization.get("auth.register.button"));

        setControlsDisabled(true);

        Thread authThread = new Thread(() -> {
            try {
                CommandResponse response;
                if (isRegister) {
                    response = sendRegisterRequest(login, password);
                } else {
                    response = sendLoginRequest(login, password);
                }

                Platform.runLater(() -> {
                    setControlsDisabled(false);
                    if (response != null && response.isSuccess()) {
                        ModernNotifications.showSuccess(notificationContainer, "✓ Авторизация успешна!", false);
                        if (onLoginSuccess != null) {
                            // Небольшая задержка перед переходом
                            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(500));
                            pause.setOnFinished(e -> onLoginSuccess.run());
                            pause.play();
                        }
                    } else {
                        ModernNotifications.showError(notificationContainer, isRegister ?
                                localization.get("auth.error.register") : localization.get("auth.error.auth"), false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setControlsDisabled(false);
                    ModernNotifications.showError(notificationContainer, localization.get("error.network"), false);
                });
            }
        });
        authThread.setDaemon(true);
        authThread.start();
    }

    private CommandResponse sendRegisterRequest(String login, String password) throws Exception {
        List<String> args = List.of("register", login, password);
        CommandRequest request = new CommandRequest("register", args, null, true, "", "");
        networkService.send(request);
        return networkService.receive();
    }

    private CommandResponse sendLoginRequest(String login, String password) throws Exception {
        List<String> args = List.of("info");
        CommandRequest request = new CommandRequest("info", args, null, true, login, password);
        networkService.send(request);
        return networkService.receive();
    }

    private void setControlsDisabled(boolean disabled) {
        loginField.setDisable(disabled);
        passwordField.setDisable(disabled);
        actionButton.setDisable(disabled);
        for (javafx.scene.control.Toggle toggle : modeToggle.getToggles()) {
            if (toggle instanceof javafx.scene.Node) {
                ((javafx.scene.Node) toggle).setDisable(disabled);
            }
        }
    }

    public String getLoginText() {
        return loginField.getText().trim();
    }

    public String getPasswordText() {
        return passwordField.getText().trim();
    }
}