package client.gui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ModernNotifications {
    private static final Duration ANIMATION_DURATION = Duration.millis(400);
    private static final Duration DISPLAY_DURATION = Duration.seconds(3);

    public static void showSuccess(VBox container, String message, boolean isDarkMode) {
        showToast(container, message, isDarkMode ? "#064E3B" : "#10B981", "✓", isDarkMode);
    }
    public static void showError(VBox container, String message, boolean isDarkMode) {
        showToast(container, message, isDarkMode ? "#7F1D1D" : "#EF4444", "✕", isDarkMode);
    }
    public static void showInfo(VBox container, String message, boolean isDarkMode) {
        showToast(container, message, isDarkMode ? "#1E3A8A" : "#3B82F6", "ℹ", isDarkMode);
    }
    public static void showWarning(VBox container, String message, boolean isDarkMode) {
        showToast(container, message, isDarkMode ? "#78350F" : "#F59E0B", "⚠", isDarkMode);
    }

    private static void showToast(VBox container, String message, String bgColor, String icon, boolean isDarkMode) {
        if (container == null) return;
        HBox toast = new HBox(12);
        toast.setPadding(new Insets(14, 20, 14, 20));
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");
        toast.setMaxWidth(400);

        Circle iconCircle = new Circle(14, Color.WHITE);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        StackPane iconContainer = new StackPane(iconCircle, iconLabel);

        Label textLabel = new Label(message);
        textLabel.setStyle("-fx-text-fill: " + (isDarkMode ? "#E2E8F0" : "white") + "; -fx-font-size: 14px; -fx-font-weight: 500;");
        textLabel.setWrapText(true); textLabel.setMaxWidth(320);
        toast.getChildren().addAll(iconContainer, textLabel);
        toast.setOpacity(0); toast.setTranslateY(-20);
        container.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(ANIMATION_DURATION, toast); fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.setInterpolator(Interpolator.EASE_OUT);
        TranslateTransition slideIn = new TranslateTransition(ANIMATION_DURATION, toast); slideIn.setFromY(-20); slideIn.setToY(0); slideIn.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition showAnimation = new ParallelTransition(fadeIn, slideIn);

        FadeTransition fadeOut = new FadeTransition(ANIMATION_DURATION, toast); fadeOut.setFromValue(1); fadeOut.setToValue(0); fadeOut.setInterpolator(Interpolator.EASE_IN);
        TranslateTransition slideOut = new TranslateTransition(ANIMATION_DURATION, toast); slideOut.setFromY(0); slideOut.setToY(-20); slideOut.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition hideAnimation = new ParallelTransition(fadeOut, slideOut);
        hideAnimation.setOnFinished(e -> container.getChildren().remove(toast));

        showAnimation.play();
        PauseTransition displayPause = new PauseTransition(DISPLAY_DURATION);
        displayPause.setOnFinished(e -> hideAnimation.play());
        displayPause.play();
    }
}