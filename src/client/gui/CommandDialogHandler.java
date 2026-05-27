package client.gui;

import client.logic.NetworkService;
import common.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandDialogHandler {
    private final NetworkService networkService;
    private final LocalizationManager localization;
    private final String login, password;
    private VehicleTableController tableController;
    private String previousDataHash = "";
    private boolean isFirstLoad = true;
    private boolean isDarkMode = false;

    private static final String DIALOG_BG = "-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 5);";
    private static final String INPUT_FIELD_STYLE = "-fx-background-color: #F5F5F5; -fx-background-radius: 8; -fx-border-color: transparent; -fx-padding: 10; -fx-font-size: 14px;";
    private static final String LABEL_STYLE = "-fx-text-fill: #555555; -fx-font-weight: bold; -fx-font-size: 14px;";
    private static final String BTN_SAVE_STYLE = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;";
    private static final String BTN_CANCEL_STYLE = "-fx-background-color: #EEEEEE; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;";

    public CommandDialogHandler(NetworkService networkService, LocalizationManager localization, String login, String password) {
        this.networkService = networkService;
        this.localization = localization;
        this.login = login;
        this.password = password;
    }

    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
    }

    public void setTableController(VehicleTableController tableController) {
        this.tableController = tableController;
    }

    public void executeBuy(Long id) {
        if (id == null) return;
        sendCommand("buy", List.of("buy", String.valueOf(id)), null);
    }

    public void executeRemoveById(Long id) {
        if (id == null) return;
        sendCommand("remove_by_id", List.of("remove_by_id", String.valueOf(id)), null);
    }

    public void executeAdd() {
        Vehicle vehicle = showModernVehicleDialog(null);
        if (vehicle != null) sendCommand("add", List.of("add"), vehicle);
    }

    public void executeShuffle() {
        if (tableController == null) return;
        List<Vehicle> originalVehicles = tableController.getAllVehicles();
        if (originalVehicles == null || originalVehicles.isEmpty()) {
            showError(localization.get("dialog.error.empty_collection"));
            return;
        }
        List<Vehicle> shuffledVehicles = new java.util.ArrayList<>(originalVehicles);
        Collections.shuffle(shuffledVehicles);
        Platform.runLater(() -> tableController.updateDataWithoutSorting(shuffledVehicles));
        PauseTransition returnTimer = new PauseTransition(Duration.seconds(4));
        returnTimer.setOnFinished(event -> Platform.runLater(() -> tableController.updateData(originalVehicles)));
        returnTimer.play();
    }

    private void sendCommand(String commandName, List<String> args, Vehicle vehicle) {
        new Thread(() -> {
            try {
                CommandRequest request = new CommandRequest(commandName, args, vehicle, true, login, password);
                networkService.send(request);
                CommandResponse response = networkService.receive();
                Platform.runLater(() -> {
                    if (response != null) {
                        if (response.isSuccess()) {
                            if ("add".equals(commandName) || "update".equals(commandName) ||
                                    "remove_by_id".equals(commandName) || "clear".equals(commandName)) {
                                String message = response.getMessage();
                                if (message != null && !message.trim().isEmpty()) {
                                    VBox notificationContainer = findNotificationContainer();
                                    if (notificationContainer != null)
                                        ModernNotifications.showSuccess(notificationContainer, message, isDarkMode);
                                }
                            }
                            if ("show".equals(commandName)) {
                                String message = response.getMessage();
                                if (message != null && !message.trim().isEmpty()) {
                                    List<Vehicle> vehicles = VehicleTextParser.parseVehicleList(message);
                                    if (tableController != null && !vehicles.isEmpty()) {
                                        tableController.updateData(vehicles);
                                        previousDataHash = calculateDataHash(vehicles);
                                        isFirstLoad = false;
                                    }
                                }
                            } else {
                                Object data = response.getData();
                                if (data instanceof List) {
                                    List<Vehicle> vehicles = ((List<?>) data).stream()
                                            .filter(obj -> obj instanceof Vehicle)
                                            .map(obj -> (Vehicle) obj)
                                            .collect(Collectors.toList());
                                    if (tableController != null && !vehicles.isEmpty())
                                        tableController.updateData(vehicles);
                                }
                                String message = response.getMessage();
                                if (message != null && !message.trim().isEmpty() &&
                                        !"show".equals(commandName) && !"add".equals(commandName) &&
                                        !"update".equals(commandName) && !"remove_by_id".equals(commandName) &&
                                        !"clear".equals(commandName)) {
                                    VBox notificationContainer = findNotificationContainer();
                                    if (notificationContainer != null)
                                        ModernNotifications.showInfo(notificationContainer, message, isDarkMode);
                                }
                            }
                            if ("update".equals(commandName) || "add".equals(commandName) ||
                                    "remove_by_id".equals(commandName) || "clear".equals(commandName) ||
                                    "buy".equals(commandName) || "set_price".equals(commandName)) {
                                executeShowSilent();
                            }
                        } else {
                            if ("buy".equals(commandName)) {
                                String errorMsg = response.getMessage();
                                if (errorMsg != null && !errorMsg.trim().isEmpty()) {
                                    VBox notificationContainer = findNotificationContainer();
                                    if (notificationContainer != null)
                                        ModernNotifications.showError(notificationContainer, errorMsg, isDarkMode);
                                }
                            } else {
                                VBox notificationContainer = findNotificationContainer();
                                if (notificationContainer != null)
                                    ModernNotifications.showError(notificationContainer,
                                            response.getMessage() != null ? response.getMessage() : localization.get("error.server"), isDarkMode);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    VBox notificationContainer = findNotificationContainer();
                    if (notificationContainer != null)
                        ModernNotifications.showError(notificationContainer, localization.get("error.network"), isDarkMode);
                });
            }
        }).start();
    }

    private VBox findNotificationContainer() {
        try {
            if (tableController != null && tableController.getTable() != null) {
                javafx.scene.Node node = tableController.getTable();
                while (node != null) {
                    if (node.getParent() != null && node.getParent() instanceof javafx.scene.layout.StackPane) {
                        for (javafx.scene.Node child : ((javafx.scene.layout.StackPane) node.getParent()).getChildren()) {
                            if (child instanceof VBox && child.getStyle().contains("-fx-background-color: transparent"))
                                return (VBox) child;
                        }
                    }
                    node = node.getParent();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    @SuppressWarnings("unchecked")
    private Vehicle showModernVehicleDialog(Vehicle existing) {
        Dialog<Vehicle> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? localization.get("dialog.add_vehicle") : localization.get("dialog.edit_vehicle"));
        dialog.setHeaderText(existing == null ? localization.get("dialog.header.add") : localization.get("dialog.header.edit"));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        // === ИСПРАВЛЕНО: Стили в зависимости от темы ===
        String dialogBg = isDarkMode
                ? "-fx-background-color: #1E293B; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0, 0, 5);"
                : "-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 5);";
        dialog.getDialogPane().setStyle(dialogBg);
        dialog.getDialogPane().setPrefWidth(500);

        // === Стили для шапки диалога ===
        String headerBgStyle = isDarkMode
                ? "-fx-background-color: #0F172A; -fx-text-fill: #E2E8F0; -fx-font-weight: bold; -fx-border-color: #1E293B; -fx-border-width: 0 0 1 0;"
                : "-fx-background-color: #F3F4F6; -fx-text-fill: #1F2937; -fx-font-weight: bold; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;";

        Node headerPanel = dialog.getDialogPane().lookup(".header-panel");
        if (headerPanel != null) {
            headerPanel.setStyle(headerBgStyle);
        }
        for (Node node : dialog.getDialogPane().lookupAll(".header-panel .header-text")) {
            node.setStyle("-fx-text-fill: " + (isDarkMode ? "#E2E8F0" : "#1F2937") + ";");
        }

        // === Стили для кнопок ===
        String btnSaveStyle = isDarkMode
                ? "-fx-background-color: linear-gradient(to right, #10B981, #059669); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"
                : "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;";
        String btnCancelStyle = isDarkMode
                ? "-fx-background-color: #475569; -fx-text-fill: #E2E8F0; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"
                : "-fx-background-color: #EEEEEE; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;";

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        saveButton.setStyle(btnSaveStyle);
        saveButton.setText(localization.get("dialog.save"));

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle(btnCancelStyle);
        cancelButton.setText(localization.get("dialog.cancel"));

        // === Стили для полей ввода ===
        String inputStyle = isDarkMode
                ? "-fx-background-color: #334155; -fx-background-radius: 8; -fx-border-color: #475569; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-padding: 10; -fx-font-size: 14px; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #94A3B8;"
                : "-fx-background-color: #F5F5F5; -fx-background-radius: 8; -fx-border-color: transparent; -fx-padding: 10; -fx-font-size: 14px;";

        // === Стили для меток ===
        String labelStyle = isDarkMode
                ? "-fx-text-fill: #E2E8F0; -fx-font-weight: bold; -fx-font-size: 14px;"
                : "-fx-text-fill: #555555; -fx-font-weight: bold; -fx-font-size: 14px;";

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 30, 10, 30));
        grid.setAlignment(Pos.CENTER);

        TextField nameField = createStyledTextField(existing != null ? existing.getName() : "", localization.get("dialog.prompt.name"), inputStyle);
        TextField xField = createStyledTextField(existing != null ? String.valueOf(existing.getCoordinates().getX()) : "0", localization.get("dialog.prompt.x"), inputStyle);
        TextField yField = createStyledTextField(existing != null ? String.valueOf(existing.getCoordinates().getY()) : "0", localization.get("dialog.prompt.y"), inputStyle);
        TextField powerField = createStyledTextField(existing != null ? String.valueOf(existing.getEnginePower()) : "0", localization.get("dialog.prompt.power"), inputStyle);
        TextField distanceField = createStyledTextField(existing != null ? String.valueOf(existing.getDistanceTravelled()) : "0", localization.get("dialog.prompt.distance"), inputStyle);
        TextField priceField = createStyledTextField(existing != null ? String.valueOf(existing.getPrice()) : "0", localization.get("dialog.prompt.price"), inputStyle);

        DatePicker datePicker = new DatePicker();
        if (existing != null && existing.getCreationDate() != null) datePicker.setValue(existing.getCreationDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        else datePicker.setValue(java.time.LocalDate.now());
        datePicker.setStyle(inputStyle);

        ComboBox<VehicleType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(VehicleType.values());
        typeCombo.setValue(existing != null ? existing.getType() : VehicleType.BOAT);
        typeCombo.setStyle(inputStyle);

        ComboBox<FuelType> fuelCombo = new ComboBox<>();
        fuelCombo.getItems().addAll(FuelType.values());
        fuelCombo.setValue(existing != null ? existing.getFuelType() : FuelType.GASOLINE);
        fuelCombo.setStyle(inputStyle);

        int row = 0;
        grid.add(createLabel(localization.get("dialog.label.name"), labelStyle), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(createLabel(localization.get("dialog.label.coords"), labelStyle), 0, row);
        grid.add(new HBox(10, xField, yField), 1, row++);

        grid.add(createLabel(localization.get("dialog.label.creation_date"), labelStyle), 0, row);
        grid.add(datePicker, 1, row++);

        grid.add(createLabel(localization.get("dialog.label.power"), labelStyle), 0, row);
        grid.add(powerField, 1, row++);

        grid.add(createLabel(localization.get("dialog.label.distance"), labelStyle), 0, row);
        grid.add(distanceField, 1, row++);

        grid.add(createLabel(localization.get("dialog.label.type"), labelStyle), 0, row);
        grid.add(typeCombo, 1, row++);

        grid.add(createLabel(localization.get("dialog.label.fuel"), labelStyle), 0, row);
        grid.add(fuelCombo, 1, row++);

        grid.add(createLabel(localization.get("dialog.label.price"), labelStyle), 0, row);
        grid.add(priceField, 1, row++);

        Runnable validate = () -> {
            boolean valid = !nameField.getText().trim().isEmpty();
            try {
                Integer.parseInt(xField.getText());
                Float.parseFloat(yField.getText());
                Float.parseFloat(powerField.getText());
                Float.parseFloat(distanceField.getText());
                Double.parseDouble(priceField.getText());
            } catch (NumberFormatException e) {
                valid = false;
            }
            saveButton.setDisable(!valid);
        };

        nameField.textProperty().addListener((o, n, w) -> validate.run());
        xField.textProperty().addListener((o, n, w) -> validate.run());
        yField.textProperty().addListener((o, n, w) -> validate.run());
        powerField.textProperty().addListener((o, n, w) -> validate.run());
        distanceField.textProperty().addListener((o, n, w) -> validate.run());
        priceField.textProperty().addListener((o, n, w) -> validate.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    Vehicle v = existing != null ? existing : new Vehicle();
                    v.setName(nameField.getText());
                    v.setCoordinates(Integer.parseInt(xField.getText()), Float.parseFloat(yField.getText()));
                    v.setEnginePower(Float.parseFloat(powerField.getText()));
                    v.setDistanceTravelled(Float.parseFloat(distanceField.getText()));
                    if (datePicker.getValue() != null) v.setCreationDate(Date.from(datePicker.getValue().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
                    v.setType(typeCombo.getValue());
                    v.setFuelType(fuelCombo.getValue());
                    v.setPrice(Double.parseDouble(priceField.getText()));
                    return v;
                } catch (NumberFormatException ex) {
                    showError(localization.get("dialog.error.invalid_numbers"));
                }
            }
            return null;
        });

        dialog.getDialogPane().setContent(grid);
        validate.run();

        // Применяем стили к ComboBox (ячейки списка)
        Platform.runLater(() -> applyComboBoxStyles(dialog, isDarkMode));

        return dialog.showAndWait().orElse(null);
    }

    private TextField createStyledTextField(String text, String prompt, String style) {
        TextField tf = new TextField(text);
        tf.setPromptText(prompt);
        tf.setStyle(style);
        return tf;
    }

    private Label createLabel(String text, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        return l;
    }

    // Применяет стили к выпадающим спискам внутри диалога
    private void applyComboBoxStyles(Dialog<Vehicle> dialog, boolean dark) {
        String cellBg = dark ? "#334155" : "#FFFFFF";
        String cellText = dark ? "#E2E8F0" : "#1F2937";
        String cellStyle = "-fx-background-color: " + cellBg + "; -fx-text-fill: " + cellText + "; -fx-font-size: 13px;";
        for (Node node : dialog.getDialogPane().getChildren()) {
            if (node instanceof ComboBox) {
                ComboBox combo = (ComboBox) node;
                combo.setCellFactory(lv -> new ListCell() {
                    @Override protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        setStyle(cellStyle);
                        setText(empty || item == null ? null : item.toString());
                    }
                });
                combo.setButtonCell(new ListCell() {
                    @Override protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        setStyle(cellStyle);
                        setText(empty || item == null ? null : item.toString());
                    }
                });
            }
        }
    }

    private TextField createStyledTextField(String text, String prompt) {
        TextField tf = new TextField(text);
        tf.setPromptText(prompt);
        tf.setStyle(INPUT_FIELD_STYLE);
        return tf;
    }

    private Label createLabel(String text) {
        Label l = new Label(text);
        l.setStyle(LABEL_STYLE);
        return l;
    }

    public void executeShow() {
        sendCommand("show", List.of("show"), null);
    }

    public void executeShowSilent() {
        new Thread(() -> {
            try {
                CommandRequest request = new CommandRequest("show", List.of("show"), null, true, login, password);
                networkService.send(request);
                CommandResponse response = networkService.receive();
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        String message = response.getMessage();
                        if (message != null && !message.trim().isEmpty()) {
                            List<Vehicle> vehicles = VehicleTextParser.parseVehicleList(message);
                            if (tableController != null && !vehicles.isEmpty()) {
                                String currentHash = calculateDataHash(vehicles);
                                if (!currentHash.equals(previousDataHash) || isFirstLoad) {
                                    tableController.updateData(vehicles);
                                    previousDataHash = currentHash;
                                    isFirstLoad = false;
                                }
                            }
                        }
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }

    public void executeEdit(Vehicle existingVehicle) {
        if (existingVehicle == null) return;
        Vehicle vehicleToSave = showModernVehicleDialog(existingVehicle);
        if (vehicleToSave != null) {
            vehicleToSave.setId(existingVehicle.getId());
            sendCommand("update", List.of("update", String.valueOf(existingVehicle.getId())), vehicleToSave);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().setStyle(DIALOG_BG);
        // === ИСПРАВЛЕНО: Локализация заголовка ошибки ===
        alert.setTitle(localization.get("app.status.error"));
        // ================================================
        alert.setHeaderText(null);
        alert.setContentText(message != null ? message : localization.get("dialog.error.unknown"));
        alert.showAndWait();
    }

    public void executeClear() {
        // === ИСПРАВЛЕНО: Локализация диалога подтверждения ===
        boolean confirmed = ModernDialog.showConfirmation(
                localization.get("dialog.clear.title"),
                localization.get("dialog.clear.confirm"),
                isDarkMode,
                localization
        );
        if (confirmed)
            sendCommand("clear", List.of("clear"), null);
    }

    public void executeDeposit() {
        // === ИСПРАВЛЕНО: Локализация диалога ввода ===
        Optional<String> result = ModernDialog.showInput(
                localization.get("dialog.deposit.title"),
                localization.get("dialog.deposit.prompt"),
                localization.get("dialog.deposit.example"),
                isDarkMode,
                localization
        );
        result.ifPresent(amount -> {
            try {
                double amountDouble = Double.parseDouble(amount);
                if (amountDouble > 0)
                    sendCommand("deposit", List.of("deposit", amount), null);
                else
                    showError(localization.get("dialog.error.positive_amount"));
            } catch (NumberFormatException e) {
                showError(localization.get("dialog.error.invalid_amount"));
            }
        });
    }

    private String calculateDataHash(List<Vehicle> vehicles) {
        if (vehicles == null || vehicles.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        List<Vehicle> sortedVehicles = new java.util.ArrayList<>(vehicles);
        sortedVehicles.sort((v1, v2) -> Long.compare(v1.getId(), v2.getId()));
        for (Vehicle v : sortedVehicles) {
            sb.append(v.getId()).append("|")
                    .append(v.getName() != null ? v.getName() : "").append("|")
                    .append(v.getCoordinates() != null ? v.getCoordinates().getX() : 0).append("|")
                    .append(v.getCoordinates() != null ? v.getCoordinates().getY() : 0).append("|")
                    .append(v.getEnginePower() != 0 ? v.getEnginePower() : 0f).append("|")
                    .append(v.getDistanceTravelled() != 0 ? v.getDistanceTravelled() : 0f).append("|")
                    .append(v.getType() != null ? v.getType() : "").append("|")
                    .append(v.getFuelType() != null ? v.getFuelType() : "").append("|")
                    .append(v.getPrice() != 0 ? v.getPrice() : 0.0).append("|")
                    .append(v.getOwnerLogin() != null ? v.getOwnerLogin() : "").append("|")
                    .append(v.getCreationDate() != null ? v.getCreationDate().getTime() : 0).append(";");
        }
        return String.valueOf(sb.toString().hashCode());
    }
}