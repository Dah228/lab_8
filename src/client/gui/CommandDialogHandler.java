package client.gui;
import client.logic.NetworkService;
import common.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.List;
import java.util.stream.Collectors;

public class CommandDialogHandler {
    private final NetworkService networkService;
    private final LocalizationManager localization;
    private final String login;
    private final String password;
    private VehicleTableController tableController;

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

    // ==================== ПУБЛИЧНЫЕ МЕТОДЫ КОМАНД ====================
    public void executeAdd() {
        Vehicle vehicle = showVehicleDialog(null);
        if (vehicle != null) {
            sendCommand("add", List.of("add"), vehicle);
        }
    }

    public void executeRemoveById() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(localization.get("app.title"));
        dialog.setHeaderText("Удаление по ID");
        dialog.setContentText("Введите ID:");
        dialog.showAndWait().ifPresent(id -> {
            try {
                Long.parseLong(id); // валидация
                sendCommand("remove_by_id", List.of("remove_by_id", id), null);
            } catch (NumberFormatException e) {
                showError("Некорректный ID");
            }
        });
    }

    public void executeClear() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(localization.get("app.title"));
        alert.setHeaderText("Очистка коллекции");
        alert.setContentText("Вы уверены, что хотите удалить все свои объекты?");
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
            dialog.setTitle(localization.get("app.title"));
            dialog.setHeaderText("Обновление элемента");
            dialog.setContentText("Введите ID:");
            dialog.showAndWait().ifPresent(id -> {
                try {
                    long idLong = Long.parseLong(id);
                    vehicle.setId(idLong);
                    sendCommand("update", List.of("update", id), vehicle);
                } catch (NumberFormatException e) {
                    showError("Некорректный ID");
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
        dialog.setTitle(localization.get("app.title"));
        dialog.setHeaderText("Фильтр по мощности двигателя");
        dialog.setContentText("Введите минимальную мощность:");
        dialog.showAndWait().ifPresent(power -> {
            try {
                Float.parseFloat(power);
                sendCommand("filter_greater_than_engine_power",
                        List.of("filter_greater_than_engine_power", power), null);
            } catch (NumberFormatException e) {
                showError("Некорректное значение");
            }
        });
    }

    public void executeFilterLessThanType() {
        ComboBox<VehicleType> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(VehicleType.values());
        comboBox.setValue(VehicleType.BOAT);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(localization.get("app.title"));
        alert.setHeaderText("Фильтр по типу (меньше)");
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
        alert.setTitle(localization.get("app.title"));
        alert.setHeaderText("Группировка по полю");
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
        dialog.setTitle(localization.get("app.title"));
        dialog.setHeaderText("Покупка ТС");
        dialog.setContentText("Введите ID транспортного средства:");
        dialog.showAndWait().ifPresent(id -> {
            try {
                Long.parseLong(id);
                sendCommand("buy", List.of("buy", id), null);
            } catch (NumberFormatException e) {
                showError("Некорректный ID");
            }
        });
    }

    public void executeShowBalance() { sendCommand("show_balance", List.of("show_balance"), null); }

    public void executeDeposit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(localization.get("app.title"));
        dialog.setHeaderText("Пополнение баланса");
        dialog.setContentText("Введите сумму:");
        dialog.showAndWait().ifPresent(amount -> {
            try {
                double amountDouble = Double.parseDouble(amount);
                if (amountDouble > 0) {
                    sendCommand("deposit", List.of("deposit", amount), null);
                } else {
                    showError("Сумма должна быть положительной");
                }
            } catch (NumberFormatException e) {
                showError("Некорректная сумма");
            }
        });
    }

    public void executeSetPrice() {
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle(localization.get("app.title"));
        idDialog.setHeaderText("Установка цены");
        idDialog.setContentText("Введите ID:");
        idDialog.showAndWait().ifPresent(id -> {
            try {
                Long.parseLong(id);
                TextInputDialog priceDialog = new TextInputDialog();
                priceDialog.setTitle(localization.get("app.title"));
                priceDialog.setHeaderText("Установка цены");
                priceDialog.setContentText("Введите цену:");
                priceDialog.showAndWait().ifPresent(price -> {
                    try {
                        double priceDouble = Double.parseDouble(price);
                        if (priceDouble >= 0) {
                            sendCommand("set_price", List.of("set_price", id, price), null);
                        } else {
                            showError("Цена не может быть отрицательной");
                        }
                    } catch (NumberFormatException e) {
                        showError("Некорректная цена");
                    }
                });
            } catch (NumberFormatException e) {
                showError("Некорректный ID");
            }
        });
    }

    // ==================== ВНУТРЕННЯЯ ЛОГИКА ====================
    /**
     * Отправляет команду на сервер и обрабатывает ответ:
     * - если в data есть List<Vehicle> → обновляет таблицу
     * - если есть текстовое сообщение → показывает в прокручиваемом окне
     */
    private void sendCommand(String commandName, List<String> args, Vehicle vehicle) {
        new Thread(() -> {
            try {
                CommandRequest request = new CommandRequest(commandName, args, vehicle, true, login, password);
                networkService.send(request);
                CommandResponse response = networkService.receive();
                Platform.runLater(() -> {
                    if (response != null) {
                        if (response.isSuccess()) {
                            // Для команды show ТОЛЬКО обновляем таблицу (без диалога)
                            if ("show".equals(commandName)) {
                                String message = response.getMessage();
                                if (message != null && !message.trim().isEmpty()) {
                                    List<Vehicle> vehicles = VehicleTextParser.parseVehicleList(message);
                                    if (tableController != null && !vehicles.isEmpty()) {
                                        tableController.updateData(vehicles);
                                    }
                                    // УБРАЛИ showScrollableInfo(message);
                                }
                            } else {
                                // Для остальных команд - как было
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

    /** Прокручиваемое окно для текстовых результатов */
    private void showScrollableInfo(String message) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(localization.get("app.title"));
        dialog.setHeaderText("Результат выполнения");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.setResizable(true);

        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-size: 13px; -fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefSize(600, 350);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    /** Окно ошибки (компактное, без скролла) */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(localization.get("app.title"));
        alert.setHeaderText(null);
        alert.setContentText(message != null ? message : "Произошла неизвестная ошибка");
        alert.showAndWait();
    }

    /** Диалог добавления/редактирования Vehicle */
    private Vehicle showVehicleDialog(Vehicle existing) {
        Dialog<Vehicle> dialog = new Dialog<>();
        dialog.setTitle(localization.get("app.title"));
        dialog.setHeaderText(existing == null ? "Добавление ТС" : "Редактирование ТС");

        ButtonType saveButtonType = new ButtonType(localization.get("dialog.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        nameField.setPromptText("Название");
        TextField xField = new TextField(existing != null ? String.valueOf(existing.getCoordinates().getX()) : "0");
        xField.setPromptText("X");
        TextField yField = new TextField(existing != null ? String.valueOf(existing.getCoordinates().getY()) : "0");
        yField.setPromptText("Y");
        TextField powerField = new TextField(existing != null ? String.valueOf(existing.getEnginePower()) : "0");
        powerField.setPromptText("Мощность");
        TextField distanceField = new TextField(existing != null ? String.valueOf(existing.getDistanceTravelled()) : "0");
        distanceField.setPromptText("Дистанция");
        TextField priceField = new TextField(existing != null ? String.valueOf(existing.getPrice()) : "0");
        priceField.setPromptText("Цена");

        ComboBox<VehicleType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(VehicleType.values());
        typeCombo.setValue(existing != null && existing.getType() != null ? existing.getType() : VehicleType.BOAT);

        ComboBox<FuelType> fuelCombo = new ComboBox<>();
        fuelCombo.getItems().addAll(FuelType.values());
        fuelCombo.setValue(existing != null && existing.getFuelType() != null ? existing.getFuelType() : FuelType.GASOLINE);

        grid.add(new Label("Название:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("X:"), 0, 1); grid.add(xField, 1, 1);
        grid.add(new Label("Y:"), 0, 2); grid.add(yField, 1, 2);
        grid.add(new Label("Мощность:"), 0, 3); grid.add(powerField, 1, 3);
        grid.add(new Label("Дистанция:"), 0, 4); grid.add(distanceField, 1, 4);
        grid.add(new Label("Тип:"), 0, 5); grid.add(typeCombo, 1, 5);
        grid.add(new Label("Топливо:"), 0, 6); grid.add(fuelCombo, 1, 6);
        grid.add(new Label("Цена:"), 0, 7); grid.add(priceField, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                try {
                    Vehicle v = existing != null ? existing : new Vehicle();
                    v.setName(nameField.getText());
                    v.setCoordinates(Integer.parseInt(xField.getText()), Float.parseFloat(yField.getText()));
                    v.setEnginePower(Float.parseFloat(powerField.getText()));
                    v.setDistanceTravelled(Float.parseFloat(distanceField.getText()));
                    v.setType(typeCombo.getValue());
                    v.setFuelType(fuelCombo.getValue());
                    v.setPrice(Double.parseDouble(priceField.getText()));
                    if (existing == null) v.setCreationDate();
                    return v;
                } catch (NumberFormatException e) {
                    showError("Некорректные данные: " + e.getMessage());
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

    /** Тихий вызов show (без диалоговых окон) для автообновления */
    public void executeShowSilent() {
        sendCommand("show", List.of("show"), null);
    }

    /** Редактирование существующего объекта (вызывается из таблицы или канваса) */
    public void executeEdit(Vehicle existingVehicle) {
        if (existingVehicle == null) return;
        Vehicle vehicleToSave = showVehicleDialog(existingVehicle);
        if (vehicleToSave != null) {
            vehicleToSave.setId(existingVehicle.getId()); // сохраняем оригинальный ID
            sendCommand("update", List.of("update", String.valueOf(existingVehicle.getId())), vehicleToSave);
        }
    }
}