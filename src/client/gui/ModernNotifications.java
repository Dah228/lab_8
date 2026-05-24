package client.gui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Современные анимированные уведомления (Toast notifications)
 */
public class ModernNotifications {

    private static final Duration ANIMATION_DURATION = Duration.millis(400);
    private static final Duration DISPLAY_DURATION = Duration.seconds(3);

    // Стили для разных типов уведомлений
    private static final String SUCCESS_STYLE = "-fx-background-color: #10B981; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3), 20, 0, 0, 5);";

    private static final String ERROR_STYLE = "-fx-background-color: #EF4444; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.3), 20, 0, 0, 5);";

    private static final String INFO_STYLE = "-fx-background-color: #3B82F6; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 20, 0, 0, 5);";

    private static final String WARNING_STYLE = "-fx-background-color: #F59E0B; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.3), 20, 0, 0, 5);";

    /**
     * Показать уведомление об успехе
     */
    public static void showSuccess(VBox container, String message) {
        showToast(container, message, SUCCESS_STYLE, "✓");
    }

    /**
     * Показать уведомление об ошибке
     */
    public static void showError(VBox container, String message) {
        showToast(container, message, ERROR_STYLE, "✕");
    }

    /**
     * Показать информационное уведомление
     */
    public static void showInfo(VBox container, String message) {
        showToast(container, message, INFO_STYLE, "ℹ");
    }

    /**
     * Показать предупреждение
     */
    public static void showWarning(VBox container, String message) {
        showToast(container, message, WARNING_STYLE, "⚠");
    }

    private static void showToast(VBox container, String message, String style, String icon) {
        if (container == null) return;

        // Создаем уведомление
        HBox toast = new HBox(12);
        toast.setPadding(new Insets(14, 20, 14, 20));
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setStyle(style);
        toast.setMaxWidth(400);

        // Иконка
        Circle iconCircle = new Circle(14, Color.WHITE);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        StackPane iconContainer = new StackPane(iconCircle, iconLabel);

        // Текст
        Label textLabel = new Label(message);
        textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 500;");
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(320);

        toast.getChildren().addAll(iconContainer, textLabel);

        // Начальное состояние (прозрачное и сдвинутое)
        toast.setOpacity(0);
        toast.setTranslateY(-20);

        // Добавляем в контейнер
        container.getChildren().add(toast);

        // Анимация появления (Fade In + Slide Down)
        FadeTransition fadeIn = new FadeTransition(ANIMATION_DURATION, toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition slideIn = new TranslateTransition(ANIMATION_DURATION, toast);
        slideIn.setFromY(-20);
        slideIn.setToY(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition showAnimation = new ParallelTransition(fadeIn, slideIn);

        // Анимация исчезновения (Fade Out + Slide Up)
        FadeTransition fadeOut = new FadeTransition(ANIMATION_DURATION, toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(Interpolator.EASE_IN);

        TranslateTransition slideOut = new TranslateTransition(ANIMATION_DURATION, toast);
        slideOut.setFromY(0);
        slideOut.setToY(-20);
        slideOut.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition hideAnimation = new ParallelTransition(fadeOut, slideOut);
        hideAnimation.setOnFinished(e -> container.getChildren().remove(toast));

        // Запускаем анимацию появления
        showAnimation.play();

        // Через DISPLAY_DURATION запускаем анимацию исчезновения
        PauseTransition displayPause = new PauseTransition(DISPLAY_DURATION);
        displayPause.setOnFinished(e -> hideAnimation.play());
        displayPause.play();
    }


    public static void showSuccess(VBox container, String message, boolean isDarkMode) {
        showToast(container, message, isDarkMode ? "#4C1D95" : "#10B981", "✓", isDarkMode);
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
        toast.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");
        toast.setMaxWidth(400);

        Circle iconCircle = new Circle(14, Color.WHITE);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        StackPane iconContainer = new StackPane(iconCircle, iconLabel);

        Label textLabel = new Label(message);
        textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 500;");
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(320);

        toast.getChildren().addAll(iconContainer, textLabel);
        toast.setOpacity(0);
        toast.setTranslateY(-20);

        container.getChildren().add(toast);

        // Анимации (без изменений)
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), toast);
        slideIn.setFromY(-20);
        slideIn.setToY(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition showAnimation = new ParallelTransition(fadeIn, slideIn);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(Interpolator.EASE_IN);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), toast);
        slideOut.setFromY(0);
        slideOut.setToY(-20);
        slideOut.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition hideAnimation = new ParallelTransition(fadeOut, slideOut);
        hideAnimation.setOnFinished(e -> container.getChildren().remove(toast));

        showAnimation.play();

        PauseTransition displayPause = new PauseTransition(Duration.seconds(3));
        displayPause.setOnFinished(e -> hideAnimation.play());
        displayPause.play();
    }

}