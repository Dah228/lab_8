package client.gui;

import client.logic.NetworkService;
import common.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CommandDialogHandler {
    private final NetworkService networkService;
    private final LocalizationManager localization;
    private final String login;
    private final String password;
    private VehicleTableController tableController;

    // Современные стили для диалога
    private static final String DIALOG_BG = "-fx-background-color: #FFFFFF; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 5);";

    private static final String INPUT_FIELD_STYLE = "-fx-background-color: #F5F5F5; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: transparent; " +
            "-fx-padding: 10; " +
            "-fx-font-size: 14px;";

    private static final String LABEL_STYLE = "-fx-text-fill: #555555; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px;";

    private static final String BTN_SAVE_STYLE = "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10 20; " +
            "-fx-cursor: hand;";

    private static final String BTN_CANCEL_STYLE = "-fx-background-color: #EEEEEE; " +
            "-fx-text-fill: #333333; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10 20; " +
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
        if (vehicle != null) {
            sendCommand("add", List.of("add"), vehicle);
        }
    }

    public void executeClear() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText(null);
        alert.setContentText("Вы уверены, что хотите удалить ВСЕ свои объекты?");
        alert.getDialogPane().setStyle(DIALOG_BG);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                sendCommand("clear", List.of("clear"), null);
            }
        });
    }

    public void executeUpdate() {
        Vehicle vehicle = showModernVehicleDialog(null);
        if (vehicle != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Введите ID для обновления");
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

    public void executeShuffle() {
        if (tableController == null) return;
        // 1. Получаем текущий список всех объектов из контроллера таблицы
        List<Vehicle> vehicles = tableController.getAllVehicles();
        if (vehicles == null || vehicles.isEmpty()) {
            showError("Коллекция пуста, нечего перемешивать.");
            return;
        }
        // 2. Создаем копию списка и перемешиваем
        List<Vehicle> shuffledVehicles = new java.util.ArrayList<>(vehicles);
        Collections.shuffle(shuffledVehicles);

        // 3. Обновляем таблицу в JavaFX потоке БЕЗ сортировки
        Platform.runLater(() -> {
            tableController.updateDataWithoutSorting(shuffledVehicles);
        });
    }
    public void executeHelp() { sendCommand("help", List.of("help"), null); }

    public void executeFilterByEnginePower() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Фильтр по мощности (минимум)");
        dialog.showAndWait().ifPresent(power -> {
            try {
                Float.parseFloat(power);
                sendCommand("filter_greater_than_engine_power", List.of("filter_greater_than_engine_power", power), null);
            } catch (NumberFormatException e) {
                showError("Некорректное число");
            }
        });
    }

    public void executeFilterLessThanType() {
        ComboBox<VehicleType> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(VehicleType.values());
        comboBox.setValue(VehicleType.BOAT);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Фильтр по типу (меньше чем)");
        alert.getDialogPane().setContent(comboBox);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                VehicleType type = comboBox.getValue();
                sendCommand("filter_less_than_type", List.of("filter_less_than_type", type.name()), null);
            }
        });
    }

    public void executeGroupBy() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("TYPE", "FUELTYPE", "COORDINATES");
        comboBox.setValue("TYPE");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
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
        Vehicle vehicle = showModernVehicleDialog(null);
        if (vehicle != null) {
            sendCommand("add_if_max", List.of("add_if_max"), vehicle);
        }
    }

    public void executeShowBalance() { sendCommand("show_balance", List.of("show_balance"), null); }

    public void executeDeposit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Пополнение баланса");
        dialog.showAndWait().ifPresent(amount -> {
            try {
                double amountDouble = Double.parseDouble(amount);
                if (amountDouble > 0) {
                    sendCommand("deposit", List.of("deposit", amount), null);
                } else {
                    showError("Сумма должна быть > 0");
                }
            } catch (NumberFormatException e) {
                showError("Некорректная сумма");
            }
        });
    }

    public void executeSetPrice() {
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setHeaderText("Установка цены: Введите ID");
        idDialog.showAndWait().ifPresent(id -> {
            try {
                Long.parseLong(id);
                TextInputDialog priceDialog = new TextInputDialog();
                priceDialog.setHeaderText("Введите новую цену");
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

    private void sendCommand(String commandName, List<String> args, Vehicle vehicle) {
        new Thread(() -> {
            try {
                CommandRequest request = new CommandRequest(commandName, args, vehicle, true, login, password);
                networkService.send(request);
                CommandResponse response = networkService.receive();

                Platform.runLater(() -> {
                    if (response != null) {
                        if (response.isSuccess()) {
                            // Стандартная логика для show и других команд, возвращающих список
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

                                // Авто-обновление после изменений
                                if ("update".equals(commandName) || "add".equals(commandName) ||
                                        "remove_by_id".equals(commandName) || "clear".equals(commandName) ||
                                        "buy".equals(commandName) || "set_price".equals(commandName)) {
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
        dialog.getDialogPane().setStyle(DIALOG_BG);
        dialog.setTitle("Результат");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.setResizable(true);
        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(600, 350);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().setStyle(DIALOG_BG);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message != null ? message : "Произошла ошибка");
        alert.showAndWait();
    }

    private Vehicle showModernVehicleDialog(Vehicle existing) {
        Dialog<Vehicle> dialog = new Dialog<>();
        dialog.getDialogPane().setStyle(DIALOG_BG);
        dialog.setTitle(existing == null ? "Добавление ТС" : "Редактирование ТС");
        dialog.setHeaderText(existing == null ? "Заполните данные нового транспортного средства" : "Измените данные ТС");

        dialog.getDialogPane().getButtonTypes().clear();

        Button saveButton = new Button("Сохранить");
        saveButton.setStyle(BTN_SAVE_STYLE);
        Button cancelButton = new Button("Отмена");
        cancelButton.setStyle(BTN_CANCEL_STYLE);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 30, 10, 30));
        grid.setAlignment(Pos.CENTER);

        TextField nameField = createStyledTextField(existing != null ? existing.getName() : "", "Название ТС");
        TextField xField = createStyledTextField(existing != null ? String.valueOf(existing.getCoordinates().getX()) : "0", "Координата X");
        TextField yField = createStyledTextField(existing != null ? String.valueOf(existing.getCoordinates().getY()) : "0", "Координата Y");
        TextField powerField = createStyledTextField(existing != null ? String.valueOf(existing.getEnginePower()) : "0", "Мощность двигателя");
        TextField distanceField = createStyledTextField(existing != null ? String.valueOf(existing.getDistanceTravelled()) : "0", "Пройденная дистанция");
        TextField priceField = createStyledTextField(existing != null ? String.valueOf(existing.getPrice()) : "0", "Цена");

        DatePicker datePicker = new DatePicker();
        if (existing != null && existing.getCreationDate() != null) {
            datePicker.setValue(existing.getCreationDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        } else {
            datePicker.setValue(java.time.LocalDate.now());
        }
        datePicker.setStyle(INPUT_FIELD_STYLE);

        ComboBox<VehicleType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(VehicleType.values());
        typeCombo.setValue(existing != null ? existing.getType() : VehicleType.BOAT);
        typeCombo.setStyle(INPUT_FIELD_STYLE);

        ComboBox<FuelType> fuelCombo = new ComboBox<>();
        fuelCombo.getItems().addAll(FuelType.values());
        fuelCombo.setValue(existing != null ? existing.getFuelType() : FuelType.GASOLINE);
        fuelCombo.setStyle(INPUT_FIELD_STYLE);

        int row = 0;
        grid.add(createLabel("Название:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(createLabel("Координаты (X, Y):"), 0, row);
        HBox coordsBox = new HBox(10, xField, yField);
        grid.add(coordsBox, 1, row++);

        grid.add(createLabel("Дата создания:"), 0, row);
        grid.add(datePicker, 1, row++);

        grid.add(createLabel("Мощность:"), 0, row);
        grid.add(powerField, 1, row++);

        grid.add(createLabel("Дистанция:"), 0, row);
        grid.add(distanceField, 1, row++);

        grid.add(createLabel("Тип ТС:"), 0, row);
        grid.add(typeCombo, 1, row++);

        grid.add(createLabel("Тип топлива:"), 0, row);
        grid.add(fuelCombo, 1, row++);

        grid.add(createLabel("Цена:"), 0, row);
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

        saveButton.setOnAction(e -> {
            try {
                Vehicle v = existing != null ? existing : new Vehicle();
                v.setName(nameField.getText());
                v.setCoordinates(Integer.parseInt(xField.getText()), Float.parseFloat(yField.getText()));
                v.setEnginePower(Float.parseFloat(powerField.getText()));
                v.setDistanceTravelled(Float.parseFloat(distanceField.getText()));
                if (datePicker.getValue() != null) {
                    Date creationDate = Date.from(datePicker.getValue().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                    v.setCreationDate(creationDate);
                }
                v.setType(typeCombo.getValue());
                v.setFuelType(fuelCombo.getValue());
                v.setPrice(Double.parseDouble(priceField.getText()));

                dialog.setResult(v);
                dialog.close();
            } catch (NumberFormatException ex) {
                showError("Проверьте правильность ввода чисел");
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        HBox buttonBar = new HBox(15, cancelButton, saveButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(10, 30, 20, 30));
        grid.add(buttonBar, 1, row);

        dialog.getDialogPane().setContent(grid);

        return dialog.showAndWait().orElse(null);
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

    public void executeShow() { sendCommand("show", List.of("show"), null); }
    public void executeShowSilent() { sendCommand("show", List.of("show"), null); }

    public void executeEdit(Vehicle existingVehicle) {
        if (existingVehicle == null) return;
        Vehicle vehicleToSave = showModernVehicleDialog(existingVehicle);
        if (vehicleToSave != null) {
            vehicleToSave.setId(existingVehicle.getId());
            sendCommand("update", List.of("update", String.valueOf(existingVehicle.getId())), vehicleToSave);
        }
    }
}