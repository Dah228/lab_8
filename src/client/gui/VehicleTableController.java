package client.gui;

import common.Vehicle;
import common.VehicleType;
import common.FuelType;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//таблица с данными
public class VehicleTableController {
    private final LocalizationManager localization;
    private TableView<Vehicle> tableView;
    private final ObservableList<Vehicle> allVehicles = FXCollections.observableArrayList();
    private final ObservableList<Vehicle> filteredVehicles = FXCollections.observableArrayList();

    private TextField filterId, filterName, filterOwner, filterMinPrice, filterMaxPrice;
    private ComboBox<VehicleType> filterType;
    private ComboBox<FuelType> filterFuel;
    private Label filterLabel;

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.00");
    private boolean isDarkMode = false;

    public VehicleTableController(LocalizationManager localization) {
        this.localization = localization;
    }

    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
        updateTableTheme();
        updateFilterStyles();
        //обновляем стили ComboBox
        if (filterType != null) updateTypeComboBoxStyle();
        if (filterFuel != null) updateFuelComboBoxStyle();
    }

    //обновляем тему таблицы
    private void updateTableTheme() {
        if (tableView == null) return;

        //цвета для тем
        String bgColor = isDarkMode ? "#0B132B" : "white";
        String textColor = isDarkMode ? "#E2E8F0" : "#374151";
        String borderColor = isDarkMode ? "#334155" : "#E5E7EB";
        String headerBg = isDarkMode ? "#1E293B" : "white";
        String headerText = isDarkMode ? "#A855F7" : "#1F2937";
        String hoverBg = isDarkMode ? "#1a1a1a" : "#F3F4F6";

        //стиль таблицы
        tableView.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                        "-fx-control-inner-background: " + bgColor + "; " +
                        "-fx-table-cell-border-color: " + borderColor + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-border-color: " + borderColor + ";"
        );

        //стиль колонок
        for (TableColumn<Vehicle, ?> col : tableView.getColumns()) {
            col.setStyle(
                    "-fx-background-color: " + headerBg + "; " +
                            "-fx-text-fill: " + headerText + "; " +
                            "-fx-font-weight: bold; " +
                            "-fx-border-color: " + borderColor + ";"
            );
        }

        //настройка строк
        tableView.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();

            //обработчик выбора строки
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (row.isEmpty()) return;

                if (isNowSelected) {
                    //градиент для выбранной строки
                    String gradient = isDarkMode ?
                            "linear-gradient(to bottom, #8B5CF6, #6D28D9)" :
                            "linear-gradient(to bottom, #60A5FA, #3B82F6)";
                    String shadowColor = isDarkMode ? "rgba(139,92,246,0.8)" : "rgba(59,130,246,0.4)";

                    row.setStyle(
                            "-fx-background-color: " + gradient + "; " +
                                    "-fx-text-fill: #FFFFFF; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-background-insets: 3, 4, 5; " +
                                    "-fx-background-radius: 10; " +
                                    "-fx-effect: dropshadow(gaussian, " + shadowColor + ", 10, 0, 0, 2);"
                    );
                } else {
                    row.setStyle("-fx-background-color: " + bgColor + ";");
                }
            });

            //обработчик наведения мыши
            row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                if (!row.isSelected() && !row.isEmpty()) {
                    row.setStyle(isNowHovered ? "-fx-background-color: " + hoverBg + ";" : "-fx-background-color: " + bgColor + ";");
                }
            });

            return row;
        });

        //стилизация заголовков и скроллбаров
        Platform.runLater(() -> {
            styleHeaders(headerBg, headerText);
            styleScrollbars();
        });

        Platform.runLater(() -> tableView.refresh());
    }

    //стилизация заголовков колонок
    private void styleHeaders(String bgColor, String textColor) {
        tableView.lookupAll(".column-header").forEach(header -> {
            header.setStyle(
                    "-fx-background-color: " + bgColor + "; " +
                            "-fx-text-fill: " + textColor + "; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 13px;"
            );
            header.lookupAll(".label").forEach(label -> {
                label.setStyle(
                        "-fx-text-fill: " + textColor + "; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 13px;"
                );
            });
        });
    }

    //стилизация скроллбаров
    private void styleScrollbars() {
        String thumbColor = isDarkMode ? "#475569" : "#CBD5E1";
        String trackColor = isDarkMode ? "#1C2541" : "transparent";
        String arrowColor = isDarkMode ? "#D8B4FE" : "#6B7280";

        tableView.lookupAll(".scroll-bar").forEach(node -> {
            node.setStyle("-fx-background-color: " + trackColor + ";");
            node.lookupAll(".thumb").forEach(thumb ->
                    thumb.setStyle("-fx-background-color: " + thumbColor + "; -fx-background-insets: 2; -fx-background-radius: 5;"));
            node.lookupAll(".track").forEach(track ->
                    track.setStyle("-fx-background-color: " + trackColor + ";"));
            node.lookupAll(".increment-button").forEach(btn ->
                    btn.setStyle("-fx-background-color: " + trackColor + ";"));
            node.lookupAll(".decrement-button").forEach(btn ->
                    btn.setStyle("-fx-background-color: " + trackColor + ";"));
            node.lookupAll(".increment-arrow").forEach(arr ->
                    arr.setStyle("-fx-background-color: " + arrowColor + ";"));
            node.lookupAll(".decrement-arrow").forEach(arr ->
                    arr.setStyle("-fx-background-color: " + arrowColor + ";"));
        });
    }

    //создаём панель фильтров
    private HBox createFilterPanel() {
        HBox hbox = new HBox(12);
        hbox.setPadding(new Insets(0, 0, 15, 0));
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        //метка "Фильтры"
        filterLabel = new Label(localization.get("table.filter") + ":");
        String labelColor = isDarkMode ? "#E2E8F0" : "#374151";
        filterLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-weight: 500;");

        //цвета для полей
        String fieldBg = isDarkMode ? "#334155" : "white";
        String fieldBorder = isDarkMode ? "#475569" : "#E2E8F0";
        String fieldText = isDarkMode ? "#E2E8F0" : "#1F2937";
        String fieldPrompt = isDarkMode ? "#94A3B8" : "#9CA3AF";

        //общий стиль для полей
        String fieldStyle = buildFieldStyle(fieldBg, fieldBorder, fieldText, fieldPrompt);
        String comboStyle = buildComboStyle(fieldBg, fieldBorder, fieldText);

        //создаём поля фильтрации
        filterId = createFilterField(localization.get("filter.id"), 50, fieldStyle);
        filterName = createFilterField(localization.get("filter.name"), 100, fieldStyle);
        filterOwner = createFilterField(localization.get("filter.owner"), 100, fieldStyle);
        filterMinPrice = createFilterField(localization.get("filter.min_price"), 70, fieldStyle);
        filterMaxPrice = createFilterField(localization.get("filter.max_price"), 70, fieldStyle);

        //ComboBox для типа
        filterType = new ComboBox<>();
        filterType.getItems().add(null);
        filterType.getItems().addAll(VehicleType.values());
        filterType.setPromptText(localization.get("filter.type"));
        filterType.setValue(null);
        filterType.setStyle(comboStyle);
        setupFilterTypeLocalization();

        //ComboBox для топлива
        filterFuel = new ComboBox<>();
        filterFuel.getItems().add(null);
        filterFuel.getItems().addAll(FuelType.values());
        filterFuel.setPromptText(localization.get("filter.fuel"));
        filterFuel.setValue(null);
        filterFuel.setStyle(comboStyle);
        setupFilterFuelLocalization();

        //привязываем обработчики ко всем полям
        Callback<Void, Void> updateFilter = v -> { applyFilters(); return null; };
        filterId.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterName.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterOwner.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterMinPrice.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterMaxPrice.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterType.valueProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterFuel.valueProperty().addListener((obs, old, newVal) -> updateFilter.call(null));

        //добавляем всё в панель
        hbox.getChildren().addAll(
                filterLabel, filterId, filterName, filterOwner,
                filterMinPrice, filterMaxPrice, filterType, filterFuel
        );

        return hbox;
    }

    //создаём стиль для текстового поля
    private String buildFieldStyle(String bg, String border, String text, String prompt) {
        return "-fx-background-color: " + bg + "; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: " + border + "; " +
                "-fx-border-radius: 6; " +
                "-fx-border-width: 1; " +
                "-fx-text-fill: " + text + "; " +
                "-fx-prompt-text-fill: " + prompt + "; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 6 10;";
    }

    //создаём стиль для ComboBox
    private String buildComboStyle(String bg, String border, String text) {
        return "-fx-background-color: " + bg + "; " +
                "-fx-border-color: " + border + "; " +
                "-fx-border-radius: 6; " +
                "-fx-background-radius: 6; " +
                "-fx-text-fill: " + text + "; " +
                "-fx-font-size: 13px;";
    }

    //создаём поле фильтрации
    private TextField createFilterField(String prompt, int width, String style) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(width);
        field.setStyle(style);
        return field;
    }

    public VBox createTablePane() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(0));

        HBox filterPanel = createFilterPanel();
        tableView = new TableView<>();
        tableView.setItems(filteredVehicles);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        setupColumns();
        root.getChildren().addAll(filterPanel, tableView);
        VBox.setVgrow(tableView, javafx.scene.layout.Priority.ALWAYS);

        return root;
    }

    private void setupFilterTypeLocalization() {
        updateTypeComboBoxStyle();
    }

    private void updateTypeComboBoxStyle() {
        String bgColor = isDarkMode ? "#1E293B" : "white";
        String textColor = isDarkMode ? "#E2E8F0" : "#1F2937";
        String hoverBg = isDarkMode ? "#334155" : "#F3F4F6";

        //настраиваем ячейки выпадающего списка - УБРАЛИ cb ->
        filterType.setCellFactory(createStyledListCell(
                item -> item == null ? localization.get("table.filter.all_types") : item.toString(),
                bgColor, textColor, hoverBg
        ));

        //настраиваем кнопку ComboBox
        filterType.setButtonCell(createStyledButtonCell(
                item -> item == null ? localization.get("table.filter.all_types") : item.toString(),
                textColor
        ));
    }

    private void setupFilterFuelLocalization() {
        updateFuelComboBoxStyle();
    }

    private void updateFuelComboBoxStyle() {
        String bgColor = isDarkMode ? "#1E293B" : "white";
        String textColor = isDarkMode ? "#E2E8F0" : "#1F2937";
        String hoverBg = isDarkMode ? "#334155" : "#F3F4F6";

        //убрали cb ->
        filterFuel.setCellFactory(createStyledListCell(
                item -> item == null ? localization.get("table.filter.all_fuels") : item.toString(),
                bgColor, textColor, hoverBg
        ));

        filterFuel.setButtonCell(createStyledButtonCell(
                item -> item == null ? localization.get("table.filter.all_fuels") : item.toString(),
                textColor
        ));
    }

    //создаём стилизованную ячейку для выпадающего списка
    private <T> Callback<ListView<T>, ListCell<T>> createStyledListCell(
            java.util.function.Function<T, String> textProvider,
            String bgColor, String textColor, String hoverBg) {
        return cb -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(textProvider.apply(item));
                    setStyle(buildCellStyle(bgColor, textColor));

                    //hover-эффект
                    setOnMouseEntered(e -> setStyle(buildCellStyle(hoverBg, textColor)));
                    setOnMouseExited(e -> setStyle(buildCellStyle(bgColor, textColor)));
                }
            }
        };
    }

    //создаём стилизованную кнопку для ComboBox
    private <T> ListCell<T> createStyledButtonCell(
            java.util.function.Function<T, String> textProvider,
            String textColor) {
        return new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textProvider.apply(item));
                setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: " + textColor + "; " +
                                "-fx-font-size: 13px; " +
                                "-fx-padding: 6 10;"
                );
            }
        };
    }

    //создаём стиль для ячейки
    private String buildCellStyle(String bgColor, String textColor) {
        return "-fx-background-color: " + bgColor + "; " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 6 10;";
    }

    private void setupColumns() {
        //создаём 10 колонок таблицы
        TableColumn<Vehicle, Number> colId = createColumn(localization.get("col.id"), 30,
                data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()));

        TableColumn<Vehicle, String> colName = createColumn(localization.get("col.name"), 80,
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));

        TableColumn<Vehicle, String> colCoords = createColumn(localization.get("col.coords"), 130,
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getCoordinates().getX() + ", " + data.getValue().getCoordinates().getY()));

        TableColumn<Vehicle, String> colDate = createColumn(localization.get("col.creation_date"), 110,
                data -> new javafx.beans.property.SimpleStringProperty(
                        localization.formatDateTime(data.getValue().getCreationDate())));

        TableColumn<Vehicle, Float> colPower = createColumn(localization.get("col.engine_power"), 90,
                data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getEnginePower()));

        TableColumn<Vehicle, Float> colDist = createColumn(localization.get("col.distance"), 90,
                data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDistanceTravelled()));

        TableColumn<Vehicle, VehicleType> colType = createColumn(localization.get("col.type"), 90,
                data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getType()));

        TableColumn<Vehicle, FuelType> colFuel = createColumn(localization.get("col.fuel"), 90,
                data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getFuelType()));

        TableColumn<Vehicle, String> colOwner = createColumn(localization.get("col.owner"), 80,
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOwnerLogin()));

        //колонка цены с форматированием
        TableColumn<Vehicle, Double> colPrice = new TableColumn<>(localization.get("col.price"));
        colPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(PRICE_FORMAT.format(price));
                }
            }
        });
        colPrice.setMinWidth(80);
        colPrice.setPrefWidth(80);

        tableView.getColumns().addAll(colId, colName, colCoords, colDate, colPower,
                colDist, colType, colFuel, colOwner, colPrice);
        updateTableTheme();
    }

    //создаём колонку таблицы
    private <T> TableColumn<Vehicle, T> createColumn(String title, int width,
                                                     javafx.util.Callback<TableColumn.CellDataFeatures<Vehicle, T>, javafx.beans.value.ObservableValue<T>> cellFactory) {
        TableColumn<Vehicle, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cellFactory);
        column.setMinWidth(width);
        column.setPrefWidth(width);
        return column;
    }

    public void updateLocalization() {
        //обновляем заголовки колонок
        tableView.getColumns().clear();
        setupColumns();

        //обновляем метку фильтров
        if (filterLabel != null) {
            filterLabel.setText(localization.get("table.filter") + ":");
        }

        //обновляем подсказки в полях
        if (filterId != null) filterId.setPromptText(localization.get("filter.id"));
        if (filterName != null) filterName.setPromptText(localization.get("filter.name"));
        if (filterOwner != null) filterOwner.setPromptText(localization.get("filter.owner"));
        if (filterMinPrice != null) filterMinPrice.setPromptText(localization.get("filter.min_price"));
        if (filterMaxPrice != null) filterMaxPrice.setPromptText(localization.get("filter.max_price"));

        //обновляем ComboBox типа
        if (filterType != null) {
            filterType.setPromptText(localization.get("filter.type"));
            if (filterType.getValue() == null) {
                filterType.getButtonCell().setText(localization.get("table.filter.all_types"));
            }
            setupFilterTypeLocalization();
        }

        //обновляем ComboBox топлива
        if (filterFuel != null) {
            filterFuel.setPromptText(localization.get("filter.fuel"));
            if (filterFuel.getValue() == null) {
                filterFuel.getButtonCell().setText(localization.get("table.filter.all_fuels"));
            }
            setupFilterFuelLocalization();
        }

        tableView.refresh();
    }

    private void applyFilters() {
        //получаем значения из полей
        String idStr = filterId.getText().trim();
        String nameStr = filterName.getText().trim().toLowerCase();
        String ownerStr = filterOwner.getText().trim().toLowerCase();
        String minPriceStr = filterMinPrice.getText().trim();
        String maxPriceStr = filterMaxPrice.getText().trim();
        VehicleType typeVal = filterType.getValue();
        FuelType fuelVal = filterFuel.getValue();

        //фильтрация с помощью Stream API
        List<Vehicle> result = allVehicles.stream()
                .filter(v -> matchesFilters(v, idStr, nameStr, ownerStr, minPriceStr, maxPriceStr, typeVal, fuelVal))
                .sorted((v1, v2) -> Long.compare(v1.getId(), v2.getId()))
                .collect(Collectors.toList());

        filteredVehicles.setAll(result);
    }

    //проверяем, соответствует ли объект фильтрам
    private boolean matchesFilters(Vehicle v, String idStr, String nameStr, String ownerStr,
                                   String minPriceStr, String maxPriceStr, VehicleType typeVal, FuelType fuelVal) {
        //фильтр по ID
        if (!idStr.isEmpty()) {
            try {
                if (v.getId() != Long.parseLong(idStr)) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        //фильтр по имени
        if (!nameStr.isEmpty() && !v.getName().toLowerCase().contains(nameStr)) return false;

        //фильтр по владельцу
        if (!ownerStr.isEmpty()) {
            if (v.getOwnerLogin() == null || !v.getOwnerLogin().toLowerCase().contains(ownerStr)) return false;
        }

        //фильтр по типу
        if (typeVal != null && v.getType() != typeVal) return false;

        //фильтр по топливу
        if (fuelVal != null && v.getFuelType() != fuelVal) return false;

        //фильтр по минимальной цене
        if (!minPriceStr.isEmpty()) {
            try {
                if (v.getPrice() < Double.parseDouble(minPriceStr)) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        //фильтр по максимальной цене
        if (!maxPriceStr.isEmpty()) {
            try {
                if (v.getPrice() > Double.parseDouble(maxPriceStr)) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(allVehicles);
    }

    public TableView<Vehicle> getTable() {
        return tableView;
    }

    public void updateDataWithoutSorting(List<Vehicle> vehicles) {
        //сохраняем текущий выбор
        Long selectedId = null;
        Vehicle currentSelection = tableView.getSelectionModel().getSelectedItem();
        if (currentSelection != null) selectedId = currentSelection.getId();

        if (vehicles == null) {
            allVehicles.clear();
            filteredVehicles.clear();
            tableView.getSelectionModel().clearSelection();
            return;
        }

        allVehicles.setAll(vehicles);
        applyFiltersWithoutSorting();

        //восстанавливаем выбор
        if (selectedId != null) {
            final Long finalSelectedId = selectedId;
            Vehicle toSelect = filteredVehicles.stream()
                    .filter(v -> v.getId() == finalSelectedId)
                    .findFirst()
                    .orElse(null);
            if (toSelect != null) tableView.getSelectionModel().select(toSelect);
        }
    }

    private void applyFiltersWithoutSorting() {
        //получаем значения из полей
        String idStr = filterId.getText().trim();
        String nameStr = filterName.getText().trim().toLowerCase();
        String ownerStr = filterOwner.getText().trim().toLowerCase();
        String minPriceStr = filterMinPrice.getText().trim();
        String maxPriceStr = filterMaxPrice.getText().trim();
        VehicleType typeVal = filterType.getValue();
        FuelType fuelVal = filterFuel.getValue();

        //фильтрация без сортировки
        List<Vehicle> result = allVehicles.stream()
                .filter(v -> matchesFilters(v, idStr, nameStr, ownerStr, minPriceStr, maxPriceStr, typeVal, fuelVal))
                .collect(Collectors.toList());

        filteredVehicles.setAll(result);
    }

    //обновляем данные в таблице
    public void updateData(List<Vehicle> vehicles) {
        //сохраняем текущий выбор
        Long selectedId = null;
        Vehicle currentSelection = tableView.getSelectionModel().getSelectedItem();
        if (currentSelection != null) selectedId = currentSelection.getId();

        if (vehicles == null) {
            allVehicles.clear();
            filteredVehicles.clear();
            tableView.getSelectionModel().clearSelection();
            return;
        }

        List<Vehicle> oldList = new ArrayList<>(allVehicles);
        allVehicles.setAll(vehicles);
        applyFilters();

        //восстанавливаем выбор
        if (selectedId != null) {
            final Long finalSelectedId = selectedId;
            Vehicle toSelect = filteredVehicles.stream()
                    .filter(v -> v.getId() == finalSelectedId)
                    .findFirst()
                    .orElse(null);
            if (toSelect != null) {
                Platform.runLater(() -> {
                    tableView.getSelectionModel().select(toSelect);
                    tableView.scrollTo(toSelect);
                });
            }
        }

        animateTableChanges(oldList, vehicles);
    }

    //определяем изменения и запускаем анимацию
    private void animateTableChanges(List<Vehicle> oldList, List<Vehicle> newList) {
        if (oldList.size() < newList.size()) {
            //ищем добавленные объекты
            List<Vehicle> addedVehicles = newList.stream()
                    .filter(v -> oldList.stream().noneMatch(ov -> ov.getId() == v.getId()))
                    .collect(Collectors.toList());
            if (!addedVehicles.isEmpty()) {
                Platform.runLater(() -> animateAddedRows(addedVehicles));
            }
        } else if (oldList.size() > newList.size()) {
            //ищем удалённые объекты
            List<Vehicle> removedVehicles = oldList.stream()
                    .filter(v -> newList.stream().noneMatch(nv -> nv.getId() == v.getId()))
                    .collect(Collectors.toList());
            if (!removedVehicles.isEmpty()) {
                Platform.runLater(() -> animateRemovedRows(removedVehicles));
            }
        }
    }

    //анимация появления новых строк
    private void animateAddedRows(List<Vehicle> addedVehicles) {
        for (Vehicle addedVehicle : addedVehicles) {
            int index = filteredVehicles.indexOf(addedVehicle);
            if (index >= 0) {
                TableRow<Vehicle> row = getRowByIndex(index);
                if (row != null) {
                    //начальное состояние: невидимая и смещённая вверх
                    row.setOpacity(0);
                    row.setTranslateY(-30);

                    //анимация появления
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(600), row);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.setInterpolator(Interpolator.EASE_OUT);

                    //анимация движения вниз
                    TranslateTransition slideDown = new TranslateTransition(Duration.millis(600), row);
                    slideDown.setFromY(-30);
                    slideDown.setToY(0);
                    slideDown.setInterpolator(Interpolator.EASE_OUT);

                    //запускаем одновременно
                    new ParallelTransition(fadeIn, slideDown).play();
                }
            }
        }
    }

    //анимация удаления строк
    private void animateRemovedRows(List<Vehicle> removedVehicles) {
        //плавное исчезновение
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), tableView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.6);
        fadeOut.setInterpolator(Interpolator.EASE_IN);

        //пауза
        PauseTransition pause = new PauseTransition(Duration.millis(150));
        pause.setOnFinished(e -> {
            //плавное появление
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), tableView);
            fadeIn.setFromValue(0.6);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(Interpolator.EASE_OUT);
            fadeIn.play();
        });

        fadeOut.play();
        pause.play();
    }

    //находим строку таблицы по индексу
    private TableRow<Vehicle> getRowByIndex(int index) {
        try {
            Node node = tableView.lookup(".table-row-cell:index(" + index + ")");
            if (node instanceof TableRow) return (TableRow<Vehicle>) node;
        } catch (Exception ignored) {}
        return null;
    }

    private void updateFilterStyles() {
        //цвета для темы
        String bgColor = isDarkMode ? "#334155" : "white";
        String borderColor = isDarkMode ? "#475569" : "#E2E8F0";
        String textColor = isDarkMode ? "#F1F5F9" : "#1F2937";
        String promptColor = isDarkMode ? "#94A3B8" : "#9CA3AF";

        //обновляем метку "Фильтры"
        String filterLabelColor = isDarkMode ? "#E2E8F0" : "#374151";
        if (filterLabel != null) {
            filterLabel.setStyle("-fx-text-fill: " + filterLabelColor + "; -fx-font-weight: 500;");
        }

        //применяем стиль ко всем полям
        String style = buildFieldStyle(bgColor, borderColor, textColor, promptColor);
        if (filterId != null) filterId.setStyle(style);
        if (filterName != null) filterName.setStyle(style);
        if (filterOwner != null) filterOwner.setStyle(style);
        if (filterMinPrice != null) filterMinPrice.setStyle(style);
        if (filterMaxPrice != null) filterMaxPrice.setStyle(style);

        //применяем стиль к ComboBox
        String comboStyle = buildComboStyle(bgColor, borderColor, textColor);
        if (filterType != null) filterType.setStyle(comboStyle);
        if (filterFuel != null) filterFuel.setStyle(comboStyle);
    }
}