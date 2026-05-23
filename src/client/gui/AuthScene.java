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

public class AuthScene {
    private final NetworkService networkService;
    private final LocalizationManager localization;
    private TextField loginField;
    private PasswordField passwordField;
    private Button actionButton;
    private ToggleGroup modeToggle;
    private Label errorLabel;
    private Runnable onLoginSuccess;

    private static final String BTN_GREEN_STYLE = "-fx-background-color: linear-gradient(to bottom, #A5D6A7, #81C784); " +
            "-fx-text-fill: white; -fx-padding: 12 25; " +
            "-fx-font-weight: bold; -fx-background-radius: 10; " +
            "-fx-border-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(102,187,106,0.4), 8, 0, 0, 2); " +
            "-fx-cursor: hand;";

    public AuthScene(Stage stage, NetworkService networkService, LocalizationManager localization) {
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
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #F1F8E9, #E8F5E9, #C8E6C9);");

        Label titleLabel = new Label(localization.get("app.title"));
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; " +
                "-fx-text-fill: #2E7D32; " +
                "-fx-effect: dropshadow(gaussian, rgba(46,125,50,0.2), 5, 0, 0, 1);");

        RadioButton rbLogin = new RadioButton(localization.get("auth.login.button"));
        RadioButton rbRegister = new RadioButton(localization.get("auth.register.button"));
        modeToggle = new ToggleGroup();
        rbLogin.setToggleGroup(modeToggle);
        rbRegister.setToggleGroup(modeToggle);
        rbLogin.setSelected(true);
        rbLogin.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        rbRegister.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");

        HBox modeBox = new HBox(10, rbLogin, rbRegister);
        modeBox.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        String fieldStyle = "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #C8E6C9; -fx-padding: 5;";
        Label loginLabel = new Label(localization.get("auth.login"));
        loginLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        loginField = new TextField();
        loginField.setPromptText("login");
        loginField.setStyle(fieldStyle);

        Label passLabel = new Label(localization.get("auth.password"));
        passLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        passwordField = new PasswordField();
        passwordField.setStyle(fieldStyle);
        passLabel.setLabelFor(passwordField);

        grid.add(loginLabel, 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        actionButton = new Button(localization.get("auth.login.button"));
        actionButton.setStyle(BTN_GREEN_STYLE);
        modeToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == rbLogin) {
                actionButton.setText(localization.get("auth.login.button"));
            } else {
                actionButton.setText(localization.get("auth.register.button"));
            }
        });
        actionButton.setOnAction(e -> handleAction());

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #C62828; -fx-font-size: 12px; -fx-font-weight: bold;");
        errorLabel.setVisible(false);

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

        setControlsDisabled(true);
        errorLabel.setVisible(false);

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