package client.gui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.util.Optional;

public class ModernDialog {
    private static final String L_DIALOG_BG = "-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 30, 0, 0, 10);";
    private static final String D_DIALOG_BG = "-fx-background-color: #1E293B; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 30, 0, 0, 10);";
    private static final String L_BTN_PRIMARY = "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 24; -fx-font-size: 14px;";
    private static final String D_BTN_PRIMARY = "-fx-background-color: linear-gradient(to right, #8B5CF6, #7C3AED); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 24; -fx-font-size: 14px;";
    private static final String L_BTN_SECONDARY = "-fx-background-color: #F3F4F6; -fx-text-fill: #4B5563; -fx-font-weight: 600; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 24; -fx-font-size: 14px;";
    private static final String D_BTN_SECONDARY = "-fx-background-color: #334155; -fx-text-fill: #E2E8F0; -fx-font-weight: 600; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 24; -fx-font-size: 14px;";
    private static final String L_INPUT = "-fx-background-color: #F9FAFB; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-padding: 10 12; -fx-font-size: 14px;";
    private static final String D_INPUT = "-fx-background-color: #334155; -fx-background-radius: 8; -fx-border-color: #475569; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-padding: 10 12; -fx-font-size: 14px;";

    public static boolean showConfirmation(String title, String message, boolean isDarkMode) {
        Stage dialog = createStyledStage(title, isDarkMode);
        VBox content = new VBox(20); content.setAlignment(Pos.CENTER); content.setPadding(new Insets(30));
        content.setStyle(isDarkMode ? D_DIALOG_BG : L_DIALOG_BG);

        Circle iconBg = new Circle(40, Color.rgb(251, 191, 36));
        Label iconLabel = new Label("⚠"); iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        StackPane iconContainer = new StackPane(iconBg, iconLabel);

        Label titleLabel = new Label(title); titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + (isDarkMode ? "#E2E8F0" : "#1F2937") + ";");
        Label msgLabel = new Label(message); msgLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (isDarkMode ? "#94A3B8" : "#6B7280") + "; -fx-wrap-text: true;");
        msgLabel.setMaxWidth(300); msgLabel.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(12); buttonBox.setAlignment(Pos.CENTER);
        Button cancelButton = new Button("Отмена"); cancelButton.setStyle(isDarkMode ? D_BTN_SECONDARY : L_BTN_SECONDARY);
        Button confirmButton = new Button("Подтвердить"); confirmButton.setStyle(isDarkMode ? D_BTN_PRIMARY : L_BTN_PRIMARY);
        confirmButton.setDefaultButton(true);

        final boolean[] result = {false};
        cancelButton.setOnAction(e -> { result[0] = false; dialog.close(); });
        confirmButton.setOnAction(e -> { result[0] = true; dialog.close(); });
        buttonBox.getChildren().addAll(cancelButton, confirmButton);
        content.getChildren().addAll(iconContainer, titleLabel, msgLabel, buttonBox);

        Scene scene = new Scene(content, 400, 280, Color.TRANSPARENT);
        dialog.setScene(scene); dialog.showAndWait();
        return result[0];
    }

    public static Optional<String> showInput(String title, String header, String prompt, boolean isDarkMode) {
        Stage dialog = createStyledStage(title, isDarkMode);
        VBox content = new VBox(20); content.setPadding(new Insets(30));
        content.setStyle(isDarkMode ? D_DIALOG_BG : L_DIALOG_BG);

        Label headerLabel = new Label(header); headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: " + (isDarkMode ? "#E2E8F0" : "#374151") + ";");
        TextField inputField = new TextField(); inputField.setPromptText(prompt);
        inputField.setStyle(isDarkMode ? D_INPUT : L_INPUT); inputField.setPrefWidth(300);

        HBox buttonBox = new HBox(12); buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Button cancelButton = new Button("Отмена"); cancelButton.setStyle(isDarkMode ? D_BTN_SECONDARY : L_BTN_SECONDARY);
        Button okButton = new Button("OK"); okButton.setStyle(isDarkMode ? D_BTN_PRIMARY : L_BTN_PRIMARY);
        final String[] result = {null};

        cancelButton.setOnAction(e -> dialog.close());
        okButton.setOnAction(e -> { String text = inputField.getText().trim(); if (!text.isEmpty()) result[0] = text; dialog.close(); });
        buttonBox.getChildren().addAll(cancelButton, okButton);
        content.getChildren().addAll(headerLabel, inputField, buttonBox);

        Scene scene = new Scene(content, 400, 200, Color.TRANSPARENT);
        dialog.setScene(scene); dialog.showAndWait();
        return Optional.ofNullable(result[0]);
    }

    private static Stage createStyledStage(String title, boolean isDarkMode) {
        Stage stage = new Stage(); stage.initModality(Modality.APPLICATION_MODAL); stage.initStyle(StageStyle.TRANSPARENT); stage.setTitle(title); stage.setResizable(false);
        stage.setOnShown(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), stage.getScene().getRoot());
            fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();
        });
        return stage;
    }
}