package client.gui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Optional;

public class ModernDialog {

    private static final String DIALOG_BG = "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 30, 0, 0, 10);";

    private static final String BTN_PRIMARY = "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); " +
            "-fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-padding: 10 24; -fx-font-size: 14px;";

    private static final String BTN_SECONDARY = "-fx-background-color: #F3F4F6; " +
            "-fx-text-fill: #4B5563; -fx-font-weight: 600; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-padding: 10 24; -fx-font-size: 14px;";

    private static final String BTN_DANGER = "-fx-background-color: linear-gradient(to right, #ef4444 0%, #dc2626 100%); " +
            "-fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-padding: 10 24; -fx-font-size: 14px;";

    // SUCCESS DIALOG
    public static void showSuccess(String title, String message) {
        Stage dialog = createStyledStage(title);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        content.setStyle(DIALOG_BG);

        // Icon
        Circle iconBg = new Circle(40, Color.rgb(34, 197, 94));
        ImageView icon = createIcon("success");
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        StackPane iconContainer = new StackPane(iconBg, icon);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        // Message
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-wrap-text: true;");
        msgLabel.setMaxWidth(300);
        msgLabel.setAlignment(Pos.CENTER);

        // Button
        Button okButton = new Button("Отлично");
        okButton.setStyle(BTN_PRIMARY);
        okButton.setOnAction(e -> dialog.close());

        content.getChildren().addAll(iconContainer, titleLabel, msgLabel, okButton);

        Scene scene = new Scene(content, 400, 280, Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ERROR DIALOG
    public static void showError(String title, String message) {
        Stage dialog = createStyledStage(title);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        content.setStyle(DIALOG_BG);

        // Icon
        Circle iconBg = new Circle(40, Color.rgb(239, 68, 68));
        ImageView icon = createIcon("error");
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        StackPane iconContainer = new StackPane(iconBg, icon);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        // Message
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-wrap-text: true;");
        msgLabel.setMaxWidth(300);
        msgLabel.setAlignment(Pos.CENTER);

        // Button
        Button okButton = new Button("Понятно");
        okButton.setStyle(BTN_DANGER);
        okButton.setOnAction(e -> dialog.close());

        content.getChildren().addAll(iconContainer, titleLabel, msgLabel, okButton);

        Scene scene = new Scene(content, 400, 280, Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // INFO DIALOG WITH SCROLL
    public static void showInfo(String title, String message) {
        Stage dialog = createStyledStage(title);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle(DIALOG_BG);

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle iconBg = new Circle(20, Color.rgb(59, 130, 246));
        ImageView icon = createIcon("info");
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        StackPane iconContainer = new StackPane(iconBg, icon);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        header.getChildren().addAll(iconContainer, titleLabel);

        // Message Area
        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-background-color: #F9FAFB; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #E5E7EB; " +
                "-fx-border-radius: 8; " +
                "-fx-font-size: 13px; " +
                "-fx-text-fill: #374151; " +
                "-fx-padding: 12;");
        textArea.setPrefHeight(250);

        // Button
        Button okButton = new Button("Закрыть");
        okButton.setStyle(BTN_SECONDARY);
        okButton.setOnAction(e -> dialog.close());

        content.getChildren().addAll(header, textArea, okButton);

        Scene scene = new Scene(content, 550, 400, Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // CONFIRMATION DIALOG
    public static boolean showConfirmation(String title, String message) {
        Stage dialog = createStyledStage(title);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        content.setStyle(DIALOG_BG);

        // Icon
        Circle iconBg = new Circle(40, Color.rgb(251, 191, 36));
        ImageView icon = createIcon("warning");
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        StackPane iconContainer = new StackPane(iconBg, icon);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        // Message
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-wrap-text: true;");
        msgLabel.setMaxWidth(300);
        msgLabel.setAlignment(Pos.CENTER);

        // Buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);

        Button cancelButton = new Button("Отмена");
        cancelButton.setStyle(BTN_SECONDARY);

        Button confirmButton = new Button("Подтвердить");
        confirmButton.setStyle(BTN_PRIMARY);
        confirmButton.setDefaultButton(true);

        final boolean[] result = {false};

        cancelButton.setOnAction(e -> {
            result[0] = false;
            dialog.close();
        });

        confirmButton.setOnAction(e -> {
            result[0] = true;
            dialog.close();
        });

        buttonBox.getChildren().addAll(cancelButton, confirmButton);
        content.getChildren().addAll(iconContainer, titleLabel, msgLabel, buttonBox);

        Scene scene = new Scene(content, 400, 280, Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();

        return result[0];
    }

    // INPUT DIALOG
    public static Optional<String> showInput(String title, String header, String prompt) {
        Stage dialog = createStyledStage(title);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle(DIALOG_BG);

        // Header
        Label headerLabel = new Label(header);
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        // Input Field
        TextField inputField = new TextField();
        inputField.setPromptText(prompt);
        inputField.setStyle("-fx-background-color: #F9FAFB; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #E5E7EB; " +
                "-fx-border-radius: 8; " +
                "-fx-border-width: 1.5; " +
                "-fx-padding: 10 12; " +
                "-fx-font-size: 14px;");
        inputField.setPrefWidth(300);

        // Buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("Отмена");
        cancelButton.setStyle(BTN_SECONDARY);

        Button okButton = new Button("OK");
        okButton.setStyle(BTN_PRIMARY);

        final String[] result = {null};

        cancelButton.setOnAction(e -> dialog.close());

        okButton.setOnAction(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                result[0] = text;
            }
            dialog.close();
        });

        buttonBox.getChildren().addAll(cancelButton, okButton);
        content.getChildren().addAll(headerLabel, inputField, buttonBox);

        Scene scene = new Scene(content, 400, 200, Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();

        return Optional.ofNullable(result[0]);
    }

    // Helper methods
    private static Stage createStyledStage(String title) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle(title);
        stage.setResizable(false);

        // Animation on show
        stage.setOnShown(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), stage.getScene().getRoot());
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });

        return stage;
    }

    private static ImageView createIcon(String type) {
        // Здесь можно добавить реальные иконки (SVG или PNG)
        // Пока используем заглушки
        ImageView icon = new ImageView();
        switch (type) {
            case "success":
                icon.setImage(createCheckmarkImage());
                break;
            case "error":
                icon.setImage(createCrossImage());
                break;
            case "info":
            case "warning":
                icon.setImage(createInfoImage());
                break;
        }
        return icon;
    }

    // Simple SVG-like icons (в реальном проекте лучше использовать файлы)
    private static Image createCheckmarkImage() {
        // Создаем простую иконку галочки
        return null; // Можно заменить на реальную иконку
    }

    private static Image createCrossImage() {
        return null;
    }

    private static Image createInfoImage() {
        return null;
    }
}