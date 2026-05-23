package client.gui;

import client.logic.NetworkService;
import common.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CommandDialogHandler {
    private final NetworkService networkService;
    private final LocalizationManager localization;
    private final String login;
    private final String password;
    private VehicleTableController tableController;

    private static final String DIALOG_STYLE = "-fx-background-color: linear-gradient(to bottom, #F1F8E9, #E8F5E9); " +
            "-fx-border-color: #A5D6A7; -fx-border-width: 2;";
    private static final String HEADER_LABEL_STYLE = "-fx-font-size: 18px; -fx-font-weight: bold; " +
            "-fx-text-fill: #2E7D32;";
    private static final String BTN_GREEN_STYLE = "-fx-background-color: linear-gradient(to bottom, #C8E6C9, #A5D6A7); " +
            "-fx-text-fill: #1B5E20; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-border-color: #81C784; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(129,199,132,0.3), 3, 0, 0, 1); " +
            "-fx-cursor: hand;";

    public CommandDialogHandler(NetworkService networkService, LocalizationManager localization,
                                String login, String password) {
        this.networkService = networkService;
        this.localization = localization;
        this.login = login;
        this.password = password;
    }

    public void setTableController(VehicleTableController tableController) {
        this.tableController = tableController;
    }

    public void executeRemoveById(Long id) {
        if (id == null) return;
        sendCommand("remove_by_id", List.of("remove_by_id", String.valueOf(id)), null);
    }

    public void executeRemoveByIdManual() {
        TextInputDialog dialog = new TextInputDialog();
        styleDialog(dialog, localization.get("dialog.remove_by_id.title"));
        dialog.setHeaderText(localization.get("dialog.remove_by_id.title"));
        dialog.setContentText(localization.get("dialog.remove_by_id.prompt"));
        dialog.showAndWait().ifPresent(id -> {
            try {
                Long.parseLong(id);
                sendCommand("remove_by_id", List.of("remove_by_id", id), null);
            } catch (NumberFormatException e) {
                showError(localization.get("dialog.error.invalid_id"));
            }
        });
    }

    public void executeAdd() {
        Vehicle vehicle = showVehicleDialog(null);
        if (vehicle != null) {
            sendCommand("add", List.of("add"), vehicle);
        }
    }

    public void executeClear() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        styleDialog(alert, localization.get("dialog.clear.title"));
        alert.setTitle(localization.get("app.title"));
        alert.setHeaderText(localization.get("dialog.clear.title"));
        alert.setContentText(localization.get("dialog.clear.confirm"));
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                sendCommand("clear", List.of("clear"), null);
            }
        });
    }

    public void executeUpdate() {
        Vehicle vehicle = showVehicleDialog(null);
        if (vehicle != null) {
            TextInputDialog dialog = new TextInputDialog();
            styleDialog(dialog, localization.get("dialog.update.title"));
            dialog.setTitle(localization.get("app.title"));
            dialog.setHeaderText(localization.get("dialog.update.title"));
            dialog.setContentText(localization.get("dialog.update.prompt"));
            dialog.showAndWait().ifPresent(id -> {
                try {
                    long idLong = Long.parseLong(id);
                    vehicle.setId(idLong);
                    sendCommand("update", List.of("update", id), vehicle);
                } catch (NumberFormatException e) {
                    showError(localization.get("dialog.error.invalid_id"));
                }
            });
        }
    }

    public void executeInfo() { sendCommand("info", List.of("info"), null); }
    public void executeSort() { sendCommand("sort", List.of("sort"), null); }
    public void executePrintDescending() { sendCommand("print_descending", List.of("print_descending"), null); }
    public void executeShuffle() { sendCommand("shuffle", List.of("shuffle"), null); }
    public void executeHelp() { sendCommand("help", List.of("help"), null); }

    public void executeFilterByEnginePower() {
        TextInputDialog dialog = new TextInputDialog();
        styleDialog(dialog, localization.get("dialog.filter.power.title"));
        dialog.setTitle(localization.get("app.title"));
        dialog.setHeaderText(localization.get("dialog.filter.power.title"));
        dialog.setContentText(localization.get("dialog.filter.power.prompt"));
        dialog.showAndWait().ifPresent(power -> {
            try {
                Float.parseFloat(power);
                sendCommand("filter_greater_than_engine_power",
                        List.of("filter_greater_than_engine_power", power), null);
            } catch (NumberFormatException e) {
                showError(localization.get("dialog.error.invalid_value"));
            }
        });
    }

    public void executeFilterLessThanType() {
        ComboBox<VehicleType> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(VehicleType.values());
        comboBox.setValue(VehicleType.BOAT);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        styleDialog(alert, localization.get("dialog.filter.type.title"));
        alert.setTitle(localization.get("app.title"));
        alert.setHeaderText(localization.get("dialog.filter.type.title"));
        alert.getDialogPane().setContent(comboBox);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                VehicleType type = comboBox.getValue();
                sendCommand("filter_less_than_type",
                        List.of("filter_less_than_type", type.name()), null);
            }
        });
    }

    public void executeGroupBy() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("TYPE", "FUELTYPE", "COORDINATES");
        comboBox.setValue("TYPE");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        styleDialog(alert, localization.get("dialog.group_by.title"));
        alert.setTitle(localization.get("app.title"));
        alert.setHeaderText(localization.get("dialog.group_by.title"));
        alert.getDialogPane().setContent(comboBox);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String field = comboBox.getValue();
                sendCommand("group_by", List.of("group_by", field), null);
            }
        });
    }

    public void executeAddIfMax() {
        Vehicle vehicle = showVehicleDialog(null);
        if (vehicle != null) {
            sendCommand("add_if_max", List.of("add_if_max"), vehicle);
        }
    }

    public void executeBuy() {
        TextInputDialog dialog = new TextInputDialog();
        styleDialog(dialog, localization.get("dialog.buy.title"));
        dialog.setTitle(localization.get("app.title"));
        dialog.setHeaderText(localization.get("dialog.buy.title"));
        dialog.setContentText(localization.get("dialog.buy.prompt"));
        dialog.showAndWait().ifPresent(id -> {
            try {
                Long.parseLong(id);
                sendCommand("buy", List.of("buy", id), null);
            } catch (NumberFormatException e) {
                showError(localization.get("dialog.error.invalid_id"));
            }
        });
    }

    public void executeShowBalance() { sendCommand("show_balance", List.of("show_balance"), null); }

    public void executeDeposit() {
        TextInputDialog dialog = new TextInputDialog();
        styleDialog(dialog, localization.get("dialog.deposit.title"));
        dialog.setTitle(localization.get("app.title"));
        dialog.setHeaderText(localization.get("dialog.deposit.title"));
        dialog.setContentText(localization.get("dialog.deposit.prompt"));
        dialog.showAndWait().ifPresent(amount -> {
            try {
                double amountDouble = Double.parseDouble(amount);
                if (amountDouble > 0) {
                    sendCommand("deposit", List.of("deposit", amount), null);
                } else {
                    showError(localization.get("dialog.error.positive_amount"));
                }
            } catch (NumberFormatException e) {
                showError(localization.get("dialog.error.invalid_amount"));
            }
        });
    }

    public void executeSetPrice() {
        TextInputDialog idDialog = new TextInputDialog();
        styleDialog(idDialog, localization.get("dialog.set_price.title_id"));
        idDialog.setTitle(localization.get("app.title"));
        idDialog.setHeaderText(localization.get("dialog.set_price.title_id"));
        idDialog.setContentText(localization.get("dialog.set_price.prompt_id"));
        idDialog.showAndWait().ifPresent(id -> {
            try {
                Long.parseLong(id);
                TextInputDialog priceDialog = new TextInputDialog();
                styleDialog(priceDialog, localization.get("dialog.set_price.title_id"));
                priceDialog.setTitle(localization.get("app.title"));
                priceDialog.setHeaderText(localization.get("dialog.set_price.title_id"));
                priceDialog.setContentText(localization.get("dialog.set_price.prompt_price"));
                priceDialog.showAndWait().ifPresent(price -> {
                    try {
                        double priceDouble = Double.parseDouble(price);
                        if (priceDouble >= 0) {
                            sendCommand("set_price", List.of("set_price", id, price), null);
                        } else {
                            showError(localization.get("dialog.error.negative_price"));
                        }
                    } catch (NumberFormatException e) {
                        showError(localization.get("dialog.error.invalid_price"));
                    }
                });
            } catch (NumberFormatException e) {
                showError(localization.get("dialog.error.invalid_id"));
            }
        });
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
                            if ("show".equals(commandName)) {
                                String message = response.getMessage();
                                if (message != null && !message.trim().isEmpty()) {
                                    List<Vehicle> vehicles = VehicleTextParser.parseVehicleList(message);
                                    if (tableController != null && !vehicles.isEmpty()) {
                                        tableController.updateData(vehicles);
                                    }
                                }
                            } else {
                                Object data = response.getData();
                                if (data instanceof List) {
                                    List<?> dataList = (List<?>) data;
                                    List<Vehicle> vehicles = dataList.stream()
                                            .filter(obj -> obj instanceof Vehicle)
                                            .map(obj -> (Vehicle) obj)
                                            .collect(Collectors.toList());
                                    if (tableController != null && !vehicles.isEmpty()) {
                                        tableController.updateData(vehicles);
                                    }
                                }
                                String message = response.getMessage();
                                if (message != null && !message.trim().isEmpty()) {
                                    showScrollableInfo(message);
                                }
                                if ("update".equals(commandName) ||
                                        "add".equals(commandName) ||
                                        "remove_by_id".equals(commandName) ||
                                        "clear".equals(commandName) ||
                                        "buy".equals(commandName) ||
                                        "set_price".equals(commandName)) {
                                    executeShowSilent();
                                }
                            }
                        } else {
                            showError(response.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Ошибка сети: " + e.getMessage()));
            }
        }).start();
    }

    private void showScrollableInfo(String message) {
        Dialog<Void> dialog = new Dialog<>();
        styleDialog(dialog, localization.get("dialog.result.title"));
        dialog.setTitle(localization.get("app.title"));
        dialog.setHeaderText(localization.get("dialog.result.title"));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.setResizable(true);
        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-size: 13px; -fx-background-color: transparent; -fx-control-inner-background: #F1F8E9;");
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefSize(600, 350);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        styleDialog(alert, localization.get("app.title"));
        alert.setTitle(localization.get("app.title"));
        alert.setHeaderText(null);
        alert.setContentText(message != null ? message : localization.get("dialog.error.unknown"));
        alert.showAndWait();
    }

    private Vehicle showVehicleDialog(Vehicle existing) {
        Dialog<Vehicle> dialog = new Dialog<>();
        styleDialog(dialog, existing == null ? localization.get("dialog.add_vehicle") : localization.get("dialog.edit_vehicle"));
        dialog.setTitle(localization.get("app.title"));
        dialog.setHeaderText(existing == null ? localization.get("dialog.add_vehicle") : localization.get("dialog.edit_vehicle"));
        ButtonType saveButtonType = new ButtonType(localization.get("dialog.save"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(localization.get("dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(saveButtonType).setStyle(BTN_GREEN_STYLE);
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle(BTN_GREEN_STYLE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        String fieldStyle = "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #C8E6C9; -fx-prompt-text-fill: #66BB6A;";
        String labelStyle = "-fx-text-fill: #2E7D32; -fx-font-weight: bold;";

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        nameField.setPromptText(localization.get("dialog.prompt.name"));
        nameField.setStyle(fieldStyle);

        TextField xField = new TextField(existing != null ? String.valueOf(existing.getCoordinates().getX()) : "0");
        xField.setPromptText(localization.get("dialog.prompt.x"));
        xField.setStyle(fieldStyle);

        TextField yField = new TextField(existing != null ? String.valueOf(existing.getCoordinates().getY()) : "0");
        yField.setPromptText(localization.get("dialog.prompt.y"));
        yField.setStyle(fieldStyle);

        TextField powerField = new TextField(existing != null ? String.valueOf(existing.getEnginePower()) : "0");
        powerField.setPromptText(localization.get("dialog.prompt.power"));
        powerField.setStyle(fieldStyle);

        TextField distanceField = new TextField(existing != null ? String.valueOf(existing.getDistanceTravelled()) : "0");
        distanceField.setPromptText(localization.get("dialog.prompt.distance"));
        distanceField.setStyle(fieldStyle);

        TextField priceField = new TextField(existing != null ? String.valueOf(existing.getPrice()) : "0");
        priceField.setPromptText(localization.get("dialog.prompt.price"));
        priceField.setStyle(fieldStyle);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText(localization.get("dialog.prompt.creation_date"));
        datePicker.setStyle(fieldStyle);
        if (existing != null && existing.getCreationDate() != null) {
            datePicker.setValue(existing.getCreationDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate());
        } else {
            datePicker.setValue(java.time.LocalDate.now());
        }

        ComboBox<VehicleType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(VehicleType.values());
        typeCombo.setValue(existing != null && existing.getType() != null ? existing.getType() : VehicleType.BOAT);
        typeCombo.setStyle(fieldStyle);

        ComboBox<FuelType> fuelCombo = new ComboBox<>();
        fuelCombo.getItems().addAll(FuelType.values());
        fuelCombo.setValue(existing != null && existing.getFuelType() != null ? existing.getFuelType() : FuelType.GASOLINE);
        fuelCombo.setStyle(fieldStyle);

        grid.add(new Label(localization.get("dialog.label.name")), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label(localization.get("dialog.label.x")), 0, 1);
        grid.add(xField, 1, 1);
        grid.add(new Label(localization.get("dialog.label.y")), 0, 2);
        grid.add(yField, 1, 2);
        grid.add(new Label(localization.get("dialog.label.power")), 0, 3);
        grid.add(powerField, 1, 3);
        grid.add(new Label(localization.get("dialog.label.distance")), 0, 4);
        grid.add(distanceField, 1, 4);
        grid.add(new Label(localization.get("dialog.label.creation_date")), 0, 5);
        grid.add(datePicker, 1, 5);
        grid.add(new Label(localization.get("dialog.label.type")), 0, 6);
        grid.add(typeCombo, 1, 6);
        grid.add(new Label(localization.get("dialog.label.fuel")), 0, 7);
        grid.add(fuelCombo, 1, 7);
        grid.add(new Label(localization.get("dialog.label.price")), 0, 8);
        grid.add(priceField, 1, 8);

        for (javafx.scene.Node node : grid.getChildren()) {
            if (node instanceof Label) {
                node.setStyle(labelStyle);
            }
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                try {
                    Vehicle v = existing != null ? existing : new Vehicle();
                    v.setName(nameField.getText());
                    v.setCoordinates(Integer.parseInt(xField.getText()), Float.parseFloat(yField.getText()));
                    v.setEnginePower(Float.parseFloat(powerField.getText()));
                    v.setDistanceTravelled(Float.parseFloat(distanceField.getText()));
                    if (datePicker.getValue() != null) {
                        java.time.LocalDate localDate = datePicker.getValue();
                        java.time.LocalDateTime localDateTime = localDate.atStartOfDay();
                        Date creationDate = Date.from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                        v.setCreationDate(creationDate);
                    } else if (existing == null) {
                        v.setCreationDate();
                    }
                    v.setType(typeCombo.getValue());
                    v.setFuelType(fuelCombo.getValue());
                    v.setPrice(Double.parseDouble(priceField.getText()));
                    return v;
                } catch (NumberFormatException e) {
                    showError(localization.get("dialog.error.invalid_data") + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    public void executeShow() {
        sendCommand("show", List.of("show"), null);
    }

    public void executeShowSilent() {
        sendCommand("show", List.of("show"), null);
    }

    public void executeEdit(Vehicle existingVehicle) {
        if (existingVehicle == null) return;
        Vehicle vehicleToSave = showVehicleDialog(existingVehicle);
        if (vehicleToSave != null) {
            vehicleToSave.setId(existingVehicle.getId());
            sendCommand("update", List.of("update", String.valueOf(existingVehicle.getId())), vehicleToSave);
        }
    }

    private void styleDialog(Dialog<?> dialog, String title) {
        dialog.getDialogPane().setStyle(DIALOG_STYLE);
    }
}