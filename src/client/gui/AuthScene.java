package client.gui;

import client.logic.NetworkService;
import common.CommandRequest;
import common.CommandResponse;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Сцена авторизации и регистрации.
 */
public class AuthScene {

    private final Stage stage;
    private final NetworkService networkService;
    private final LocalizationManager localization;

    // Поля ввода
    private TextField loginField;
    private PasswordField passwordField;
    private Button actionButton;
    private ToggleGroup modeToggle;
    private Label errorLabel;
    private Label userDisplayLabel; // Для отображения текущего пользователя после входа

    // Callback для перехода на главную сцену
    private Runnable onLoginSuccess;

    public AuthScene(Stage stage, NetworkService networkService, LocalizationManager localization) {
        this.stage = stage;
        this.networkService = networkService;
        this.localization = localization;
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    public Scene createScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f4f4f4;");

        // Заголовок
        Label titleLabel = new Label(localization.get("app.title"));
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Переключатель режима (Вход / Регистрация)
        RadioButton rbLogin = new RadioButton(localization.get("auth.login.button"));
        RadioButton rbRegister = new RadioButton(localization.get("auth.register.button"));
        modeToggle = new ToggleGroup();
        rbLogin.setToggleGroup(modeToggle);
        rbRegister.setToggleGroup(modeToggle);
        rbLogin.setSelected(true); // По умолчанию вход

        HBox modeBox = new HBox(10, rbLogin, rbRegister);
        modeBox.setAlignment(Pos.CENTER);

        // Поля ввода
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        Label loginLabel = new Label(localization.get("auth.login"));
        loginField = new TextField();
        loginField.setPromptText("login");

        Label passLabel = new Label(localization.get("auth.password"));
        passwordField = new PasswordField();
        passLabel.setLabelFor(passwordField);

        grid.add(loginLabel, 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        // Кнопка действия
        actionButton = new Button(localization.get("auth.login.button"));
        actionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20;");

        // Обработчик смены режима
        modeToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == rbLogin) {
                actionButton.setText(localization.get("auth.login.button"));
            } else {
                actionButton.setText(localization.get("auth.register.button"));
            }
        });

        // Обработчик нажатия кнопки
        actionButton.setOnAction(e -> handleAction());

        // Метка ошибки
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        // Сборка сцены
        root.getChildren().addAll(titleLabel, modeBox, grid, actionButton, errorLabel);

        return new Scene(root, 400, 300);
    }

    private void handleAction() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.isEmpty() || password.isEmpty()) {
            showError(localization.get("auth.error.empty"));
            return;
        }

        boolean isRegister = modeToggle.getSelectedToggle() instanceof RadioButton &&
                ((RadioButton) modeToggle.getSelectedToggle()).getText().equals(localization.get("auth.register.button"));

        // Блокируем UI во время запроса
        setControlsDisabled(true);
        errorLabel.setVisible(false);

        // Выполняем сетевой запрос в фоновом потоке
        Thread authThread = new Thread(() -> {
            try {
                CommandResponse response;
                if (isRegister) {
                    response = sendRegisterRequest(login, password);
                } else {
                    response = sendLoginRequest(login, password);
                }

                // Возвращаемся в JavaFX Thread для обновления UI
                Platform.runLater(() -> {
                    setControlsDisabled(false);
                    if (response != null && response.isSuccess()) {
                        // Успех! Сохраняем данные и переходим дальше
                        System.out.println("Авторизация успешна: " + login);
                        if (onLoginSuccess != null) {
                            onLoginSuccess.run();
                        }
                    } else {
                        String msg = response != null ? response.getMessage() : localization.get("error.network");
                        showError(isRegister ? localization.get("auth.error.register") + ": " + msg : localization.get("auth.error.auth"));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setControlsDisabled(false);
                    showError(localization.get("error.network") + ": " + e.getMessage());
                });
            }
        });
        authThread.setDaemon(true);
        authThread.start();
    }

    private CommandResponse sendRegisterRequest(String login, String password) throws Exception {
        // Команда register требует аргументы: ["register", login, password]
        List<String> args = List.of("register", login, password);
        CommandRequest request = new CommandRequest("register", args, null, true, "", "");

        networkService.send(request);
        return networkService.receive();
    }

    private CommandResponse sendLoginRequest(String login, String password) throws Exception {
        // Для проверки логина используем простую команду, например "info" или "show"
        // Сервер проверит auth по login/password из запроса
        List<String> args = List.of("info");
        CommandRequest request = new CommandRequest("info", args, null, true, login, password);

        networkService.send(request);
        return networkService.receive();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
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