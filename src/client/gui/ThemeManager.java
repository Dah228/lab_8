package client.gui;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;

//управление темами оформления
public class ThemeManager {
    private boolean isDarkMode = false;

    //светлая тема
    private static final String L_BG = "-fx-background-color: #F8FAFC;";
    private static final String L_CARD = "-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);";
    private static final String L_BTN_P = "-fx-background-color: linear-gradient(to right, #3B82F6, #2563EB); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String L_BTN_P_H = "-fx-background-color: linear-gradient(to right, #2563EB, #1D4ED8);";
    private static final String L_BTN_S = "-fx-background-color: #F1F5F9; -fx-text-fill: #334155; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String L_BTN_S_H = "-fx-background-color: #E2E8F0; -fx-border-color: #CBD5E1;";
    private static final String L_BTN_D = "-fx-background-color: #F1F5F9; -fx-text-fill: #EF4444; -fx-border-color: #FECACA; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String L_BTN_D_H = "-fx-background-color: #FEE2E2; -fx-border-color: #FCA5A5;";

    //тёмная тема
    private static final String D_BG = "-fx-background-color: #0B1120;";
    private static final String D_CARD = "-fx-background-color: #1E293B; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 2);";
    private static final String D_BTN_P = "-fx-background-color: linear-gradient(to right, #8B5CF6, #7C3AED); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String D_BTN_P_H = "-fx-background-color: linear-gradient(to right, #A78BFA, #8B5CF6);";
    private static final String D_BTN_S = "-fx-background-color: #334155; -fx-text-fill: #E2E8F0; -fx-border-color: #475569; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String D_BTN_S_H = "-fx-background-color: #475569; -fx-border-color: #64748B;";
    private static final String D_BTN_D = "-fx-background-color: linear-gradient(to right, #DC2626, #B91C1C); -fx-text-fill: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String D_BTN_D_H = "-fx-background-color: linear-gradient(to right, #EF4444, #DC2626);";

    public boolean isDarkMode() {
        return isDarkMode;
    }

    public void toggleTheme() {
        isDarkMode = !isDarkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
    }

    //получаем базовый стиль кнопки
    public String getBaseStyle(boolean isPrimary) {
        return isDarkMode ? (isPrimary ? D_BTN_P : D_BTN_S) : (isPrimary ? L_BTN_P : L_BTN_S);
    }

    //получаем стиль кнопки при наведении
    public String getHoverStyle(boolean isPrimary) {
        return isDarkMode ? (isPrimary ? D_BTN_P_H : D_BTN_S_H) : (isPrimary ? L_BTN_P_H : L_BTN_S_H);
    }

    //получаем базовый стиль опасной кнопки
    public String getBaseStyle(String type) {
        return isDarkMode ? D_BTN_D : L_BTN_D;
    }

    //получаем стиль опасной кнопки при наведении
    public String getHoverStyle(String type) {
        return isDarkMode ? D_BTN_D_H : L_BTN_D_H;
    }

    //настраиваем hover-эффект для кнопки
    public void setupButtonHover(Button btn, boolean isPrimary) {
        btn.setOnMouseEntered(e -> btn.setStyle(getHoverStyle(isPrimary)));
        btn.setOnMouseExited(e -> btn.setStyle(getBaseStyle(isPrimary)));
    }

    //настраиваем hover-эффект для опасной кнопки
    public void setupButtonHover(Button btn, String type) {
        btn.setOnMouseEntered(e -> btn.setStyle(getHoverStyle(type)));
        btn.setOnMouseExited(e -> btn.setStyle(getBaseStyle(type)));
    }

    //применяем стили темы ко всем элементам
    public void applyThemeStyles(BorderPane root, HBox topPanel, VBox tableContainer,
                                 VBox canvasContainer, HBox bottomPanel, Label userLabel,
                                 Label visualTitle, List<Button> themeAwareButtons,
                                 Button balanceButton, Button depositButton,
                                 ComboBox<Locale> langComboBox, VBox profilePanel,
                                 LocalizationManager localization,
                                 VehicleTableController tableController,
                                 VehicleCanvasController canvasController,
                                 CommandDialogHandler commandHandler) {
        if (root == null) return;

        //фон и карточки
        root.setStyle(isDarkMode ? D_BG : L_BG);
        if (topPanel != null) topPanel.setStyle(isDarkMode ? D_CARD : L_CARD);

        if (tableContainer != null) {
            tableContainer.setStyle(isDarkMode ?
                    "-fx-background-color: #050505; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 2); -fx-padding: 15;" :
                    L_CARD + " -fx-padding: 15;");
        }

        if (canvasContainer != null) canvasContainer.setStyle(isDarkMode ? D_CARD : L_CARD + " -fx-padding: 15;");
        if (bottomPanel != null) bottomPanel.setStyle("-fx-background-color: transparent;");

        //метка пользователя
        if (userLabel != null) {
            String baseColor = isDarkMode ? "#8B5CF6" : "#2563EB";
            userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + baseColor + "; -fx-cursor: hand; -fx-underline: true;");
        }

        //заголовок визуализации
        if (visualTitle != null) {
            visualTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + (isDarkMode ? "#8B5CF6" : "#2563EB") + ";");
        }

        //обновляем стили кнопок
        for (Button btn : themeAwareButtons) {
            String key = getButtonKey(btn.getText(), localization);
            if (key != null) {
                if (key.equals("btn.clear")) {
                    btn.setStyle(getBaseStyle("danger"));
                } else if (key.equals("btn.add")) {
                    btn.setStyle(getBaseStyle(true));
                } else {
                    btn.setStyle(getBaseStyle(false));
                }
            }
        }

        if (balanceButton != null) balanceButton.setStyle(getBaseStyle(false));
        if (depositButton != null) depositButton.setStyle(getBaseStyle(false));

        //обновляем ComboBox языка
        updateLangComboBoxStyle(langComboBox, localization);

        //обновляем контроллеры
        if (tableController != null) tableController.setDarkMode(isDarkMode);
        if (canvasController != null) canvasController.setDarkMode(isDarkMode);
        if (commandHandler != null) commandHandler.setDarkMode(isDarkMode);
    }

    //определяем ключ кнопки по тексту
    public String getButtonKey(String buttonText, LocalizationManager localization) {
        if (buttonText == null) return null;

        //проверяем все локализованные варианты
        if (buttonText.equals(localization.get("btn.clear")) || buttonText.equals("Очистить") ||
                buttonText.equals("Clear") || buttonText.equals("Tøm") || buttonText.equals("Išvalyti")) {
            return "btn.clear";
        }
        if (buttonText.equals(localization.get("btn.add")) || buttonText.equals("Добавить") ||
                buttonText.equals("Add") || buttonText.equals("Legg til") || buttonText.equals("Pridėti")) {
            return "btn.add";
        }
        if (buttonText.equals(localization.get("btn.remove")) || buttonText.equals("Удалить по ID") ||
                buttonText.equals("Remove by ID") || buttonText.equals("Fjern etter ID") || buttonText.equals("Šalinti pagal ID")) {
            return "btn.remove";
        }
        if (buttonText.equals(localization.get("btn.shuffle")) || buttonText.equals("Перемешать") ||
                buttonText.equals("Shuffle") || buttonText.equals("Bland") || buttonText.equals("Sumaišyti")) {
            return "btn.shuffle";
        }
        if (buttonText.equals(localization.get("btn.buy")) || buttonText.equals("Купить") ||
                buttonText.equals("Buy") || buttonText.equals("Kjøp") || buttonText.equals("Pirkti")) {
            return "btn.buy";
        }

        return null;
    }

    //обновляем стили ComboBox языка
    private void updateLangComboBoxStyle(ComboBox<Locale> langComboBox, LocalizationManager localization) {
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
}