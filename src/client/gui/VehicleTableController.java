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
public class VehicleTableController {
    private final LocalizationManager localization;
    private TableView<Vehicle> tableView;
    private final ObservableList<Vehicle> allVehicles = FXCollections.observableArrayList();
    private final ObservableList<Vehicle> filteredVehicles = FXCollections.observableArrayList();
    private TextField filterId, filterName, filterOwner, filterMinPrice, filterMaxPrice;
    private ComboBox<VehicleType> filterType;
    private ComboBox<FuelType> filterFuel;
    private Label filterLabel; // <--- Добавлено поле для надписи "Фильтры"
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.00");
    private boolean isDarkMode = false;
    public VehicleTableController(LocalizationManager localization) {
        this.localization = localization;
    }
    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
        updateTableTheme();
        updateFilterStyles();
// Обновляем стили ComboBox
        if (filterType != null) updateTypeComboBoxStyle();
        if (filterFuel != null) updateFuelComboBoxStyle();
    }
    private void updateTableTheme() {
        if (tableView == null) return;
        if (!isDarkMode) {
            // === СВЕТЛАЯ ТЕМА ===
            tableView.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-control-inner-background: white; " +
                            "-fx-table-cell-border-color: #E5E7EB; " +
                            "-fx-text-fill: #374151;"
            );
            for (TableColumn<Vehicle, ?> col : tableView.getColumns()) {
                col.setStyle(
                        "-fx-background-color: white; " +
                                "-fx-text-fill: #374151; " +
                                "-fx-font-weight: bold; " +
                                "-fx-border-color: #E5E7EB;"
                );
            }
            tableView.setRowFactory(tv -> {
                TableRow<Vehicle> row = new TableRow<>();
                row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected && !row.isEmpty()) {
                        row.setStyle(
                                "-fx-background-color: linear-gradient(to bottom, #60A5FA, #3B82F6); " +
                                        "-fx-text-fill: #FFFFFF; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-background-insets: 3, 4, 5; " +
                                        "-fx-background-radius: 10; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.4), 10, 0, 0, 2);"
                        );
                    } else if (!row.isEmpty()) {
                        row.setStyle("-fx-background-color: white;");
                    }
                });
                row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                    if (!row.isSelected() && !row.isEmpty()) {
                        row.setStyle(isNowHovered ? "-fx-background-color: #F3F4F6;" : "-fx-background-color: white;");
                    }
                });
                return row;
            });
        } else {
            // === ТЁМНАЯ ТЕМА (ИСПРАВЛЕНО) ===
            tableView.setStyle(
                    "-fx-background-color: #0B132B; " +
                            "-fx-control-inner-background: #0B132B; " +
                            "-fx-table-cell-border-color: #334155; " + // Более видимые линии сетки
                            "-fx-text-fill: #E2E8F0; " + // Светлый текст
                            "-fx-border-color: #334155;"
            );
            for (TableColumn<Vehicle, ?> col : tableView.getColumns()) {
                col.setStyle(
                        "-fx-background-color: #1E293B; " + // Заголовок чуть светлее фона
                                "-fx-text-fill: #A855F7; " + // Фиолетовый текст заголовка
                                "-fx-font-weight: bold; " +
                                "-fx-border-color: #334155;"
                );
            }
            tableView.setRowFactory(tv -> {
                TableRow<Vehicle> row = new TableRow<>();
                row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected && !row.isEmpty()) {
                        row.setStyle(
                                "-fx-background-color: linear-gradient(to bottom, #8B5CF6, #6D28D9); " +
                                        "-fx-text-fill: #FFFFFF; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-background-insets: 3, 4, 5; " +
                                        "-fx-background-radius: 10; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.8), 15, 0, 0, 4);"
                        );
                    } else if (!row.isEmpty()) {
                        row.setStyle("-fx-background-color: #0B132B;");
                    }
                });
                row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                    if (!row.isSelected() && !row.isEmpty()) {
                        row.setStyle(isNowHovered ? "-fx-background-color: #1a1a1a;" : "-fx-background-color: #0B132B;");
                    }
                });
                return row;
            });
        }
        Platform.runLater(() -> {
            String headerTextColor = isDarkMode ? "#A855F7" : "#1F2937";
            String headerBgColor = isDarkMode ? "#1E293B" : "white";

            tableView.lookupAll(".column-header").forEach(header -> {
                header.setStyle(
                        "-fx-background-color: " + headerBgColor + "; " +
                                "-fx-text-fill: " + headerTextColor + "; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 13px;"
                );
                header.lookupAll(".label").forEach(label -> {
                    label.setStyle(
                            "-fx-text-fill: " + headerTextColor + "; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-font-size: 13px;"
                    );
                });
            });

            String thumbColor = isDarkMode ? "#475569" : "#CBD5E1";
            String trackColor = isDarkMode ? "#1C2541" : "transparent";
            String arrowColor = isDarkMode ? "#D8B4FE" : "#6B7280";

            tableView.lookupAll(".scroll-bar").forEach(node -> {
                node.setStyle("-fx-background-color: " + trackColor + ";");
                node.lookupAll(".thumb").forEach(thumb -> thumb.setStyle("-fx-background-color: " + thumbColor + "; -fx-background-insets: 2; -fx-background-radius: 5;"));
                node.lookupAll(".track").forEach(track -> track.setStyle("-fx-background-color: " + trackColor + ";"));
                node.lookupAll(".increment-button").forEach(btn -> btn.setStyle("-fx-background-color: " + trackColor + ";"));
                node.lookupAll(".decrement-button").forEach(btn -> btn.setStyle("-fx-background-color: " + trackColor + ";"));
                node.lookupAll(".increment-arrow").forEach(arr -> arr.setStyle("-fx-background-color: " + arrowColor + ";"));
                node.lookupAll(".decrement-arrow").forEach(arr -> arr.setStyle("-fx-background-color: " + arrowColor + ";"));
            });
        });
        Platform.runLater(() -> tableView.refresh());
    }

    private HBox createFilterPanel() {
        HBox hbox = new HBox(12);
        hbox.setPadding(new Insets(0, 0, 15, 0));
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        filterLabel = new Label("Фильтры:"); // <--- Присваиваем полю
// Цвет устанавливается в зависимости от темы (как у "Язык")
        filterLabel.setStyle("-fx-text-fill: " + (isDarkMode ? "#E2E8F0" : "#374151") + "; -fx-font-weight: 500;");
        filterId = new TextField(); filterId.setPromptText("ID"); filterId.setPrefWidth(50);
        filterName = new TextField(); filterName.setPromptText("Имя"); filterName.setPrefWidth(100);
        filterOwner = new TextField(); filterOwner.setPromptText("Владелец"); filterOwner.setPrefWidth(100);
        filterMinPrice = new TextField(); filterMinPrice.setPromptText("Цена от"); filterMinPrice.setPrefWidth(70);
        filterMaxPrice = new TextField(); filterMaxPrice.setPromptText("Цена до"); filterMaxPrice.setPrefWidth(70);
        filterType = new ComboBox<>(); filterType.getItems().add(null); filterType.getItems().addAll(VehicleType.values());
        filterType.setPromptText("Тип"); filterType.setValue(null); setupFilterTypeLocalization();
        filterFuel = new ComboBox<>(); filterFuel.getItems().add(null); filterFuel.getItems().addAll(FuelType.values());
        filterFuel.setPromptText("Топливо"); filterFuel.setValue(null); setupFilterFuelLocalization();
        Callback<Void, Void> updateFilter = v -> { applyFilters(); return null; };
        filterId.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterName.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterOwner.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterMinPrice.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterMaxPrice.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterType.valueProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterFuel.valueProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        hbox.getChildren().addAll(filterLabel, filterId, filterName, filterOwner, filterMinPrice, filterMaxPrice, filterType, filterFuel);
        updateFilterStyles();
        return hbox;
    }
    private void updateRowStyle(TableRow<Vehicle> row) {
        if (row.isEmpty()) {
            row.setStyle("");
            return;
        }
        if (row.isSelected()) {
            row.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #00E5FF, #0088AA); " +
                            "-fx-text-fill: #FFFFFF; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-insets: 3, 4, 5; " +
                            "-fx-background-radius: 10; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,229,255,0.7), 20, 0.6, 0, 4);"
            );
        } else {
            if (isDarkMode) {
                row.setStyle(((int)row.getIndex() % 2 == 0)
                        ? "-fx-background-color: #0F172A;"
                        : "-fx-background-color: #0B132B;");
            } else {
                row.setStyle(((int)row.getIndex() % 2 == 0)
                        ? "-fx-background-color: #FAFAFA;"
                        : "-fx-background-color: white;");
            }
        }
    }
    public VBox createTablePane(){
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
        String selectedBg = isDarkMode ? "#475569" : "#DBEAFE";
        filterType.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? localization.get("table.filter.all_types") : item.toString());
                setStyle("-fx-background-color: " + bgColor + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 6 10;");
                if (!empty && item != null) {
                    setOnMouseEntered(e -> setStyle("-fx-background-color: " + hoverBg + "; " +
                            "-fx-text-fill: " + textColor + "; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 6 10;"));
                    setOnMouseExited(e -> setStyle("-fx-background-color: " + bgColor + "; " +
                            "-fx-text-fill: " + textColor + "; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 6 10;"));
                }
            }
        });
        filterType.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? localization.get("table.filter.all_types") : item.toString());
                setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 6 10;");
            }
        });
    }
    private void setupFilterFuelLocalization() {
        updateFuelComboBoxStyle();
    }
    private void updateFuelComboBoxStyle() {
        String bgColor = isDarkMode ? "#1E293B" : "white";
        String textColor = isDarkMode ? "#E2E8F0" : "#1F2937";
        String hoverBg = isDarkMode ? "#334155" : "#F3F4F6";
        filterFuel.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(FuelType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? localization.get("table.filter.all_fuels") : item.toString());
                setStyle("-fx-background-color: " + bgColor + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 6 10;");
                if (!empty && item != null) {
                    setOnMouseEntered(e -> setStyle("-fx-background-color: " + hoverBg + "; " +
                            "-fx-text-fill: " + textColor + "; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 6 10;"));
                    setOnMouseExited(e -> setStyle("-fx-background-color: " + bgColor + "; " +
                            "-fx-text-fill: " + textColor + "; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 6 10;"));
                }
            }
        });
        filterFuel.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(FuelType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? localization.get("table.filter.all_fuels") : item.toString());
                setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 6 10;");
            }
        });
    }
    private void setupColumns() {
        TableColumn<Vehicle, Number> colId = new TableColumn<>(localization.get("col.id")); colId.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId())); colId.setPrefWidth(50);
        TableColumn<Vehicle, String> colName = new TableColumn<>(localization.get("col.name")); colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName())); colName.setPrefWidth(120);
        TableColumn<Vehicle, String> colCoords = new TableColumn<>(localization.get("col.coords")); colCoords.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCoordinates().getX() + ", " + data.getValue().getCoordinates().getY())); colCoords.setPrefWidth(100);
        TableColumn<Vehicle, String> colDate = new TableColumn<>(localization.get("col.creation_date")); colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(localization.formatDateTime(data.getValue().getCreationDate()))); colDate.setPrefWidth(110);
        TableColumn<Vehicle, Float> colPower = new TableColumn<>(localization.get("col.engine_power")); colPower.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getEnginePower())); colPower.setPrefWidth(70);
        TableColumn<Vehicle, Float> colDist = new TableColumn<>(localization.get("col.distance")); colDist.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDistanceTravelled())); colDist.setPrefWidth(70);
        TableColumn<Vehicle, VehicleType> colType = new TableColumn<>(localization.get("col.type")); colType.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getType())); colType.setPrefWidth(90);
        TableColumn<Vehicle, FuelType> colFuel = new TableColumn<>(localization.get("col.fuel")); colFuel.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getFuelType())); colFuel.setPrefWidth(90);
        TableColumn<Vehicle, String> colOwner = new TableColumn<>(localization.get("col.owner")); colOwner.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOwnerLogin())); colOwner.setPrefWidth(90);
        TableColumn<Vehicle, Double> colPrice = new TableColumn<>(localization.get("col.price")); colPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));
        colPrice.setCellFactory(tc -> new TableCell<>() { @Override protected void updateItem(Double price, boolean empty) { super.updateItem(price, empty); if (empty || price == null) setText(null); else setText(PRICE_FORMAT.format(price)); }});
        colPrice.setPrefWidth(90);
        tableView.getColumns().addAll(colId, colName, colCoords, colDate, colPower, colDist, colType, colFuel, colOwner, colPrice);
        updateTableTheme();
    }
    public void updateLocalization() { tableView.getColumns().clear(); setupColumns(); }
    private void applyFilters() {
        String idStr = filterId.getText().trim(), nameStr = filterName.getText().trim().toLowerCase(), ownerStr = filterOwner.getText().trim().toLowerCase();
        String minPriceStr = filterMinPrice.getText().trim(), maxPriceStr = filterMaxPrice.getText().trim();
        VehicleType typeVal = filterType.getValue(); FuelType fuelVal = filterFuel.getValue();
        List<Vehicle> result = allVehicles.stream().filter(v -> {
            if (!idStr.isEmpty()) { try { if (v.getId() != Long.parseLong(idStr)) return false; } catch (NumberFormatException e) { return false; } }
            if (!nameStr.isEmpty() && !v.getName().toLowerCase().contains(nameStr)) return false;
            if (!ownerStr.isEmpty() && (v.getOwnerLogin() == null || !v.getOwnerLogin().toLowerCase().contains(ownerStr))) return false;
            if (typeVal != null && v.getType() != typeVal) return false;
            if (fuelVal != null && v.getFuelType() != fuelVal) return false;
            if (!minPriceStr.isEmpty()) { try { if (v.getPrice() < Double.parseDouble(minPriceStr)) return false; } catch (NumberFormatException e) { return false; } }
            if (!maxPriceStr.isEmpty()) { try { if (v.getPrice() > Double.parseDouble(maxPriceStr)) return false; } catch (NumberFormatException e) { return false; } }
            return true;
        }).sorted((v1, v2) -> Long.compare(v1.getId(), v2.getId())).collect(Collectors.toList());
        filteredVehicles.setAll(result);
    }
    public List<Vehicle> getAllVehicles() { return new ArrayList<>(allVehicles); }
    public TableView<Vehicle> getTable() { return tableView; }
    public void updateDataWithoutSorting(List<Vehicle> vehicles) {
        Long selectedId = null; Vehicle currentSelection = tableView.getSelectionModel().getSelectedItem();
        if (currentSelection != null) selectedId = currentSelection.getId();
        if (vehicles == null) { allVehicles.clear(); filteredVehicles.clear(); tableView.getSelectionModel().clearSelection(); return; }
        allVehicles.setAll(vehicles); applyFiltersWithoutSorting();
        if (selectedId != null) {
            final Long finalSelectedId = selectedId;
            Vehicle toSelect = filteredVehicles.stream().filter(v -> v.getId() == finalSelectedId).findFirst().orElse(null);
            if (toSelect != null) tableView.getSelectionModel().select(toSelect);
        }
    }
    private void applyFiltersWithoutSorting() {
        String idStr = filterId.getText().trim(), nameStr = filterName.getText().trim().toLowerCase(), ownerStr = filterOwner.getText().trim().toLowerCase();
        String minPriceStr = filterMinPrice.getText().trim(), maxPriceStr = filterMaxPrice.getText().trim();
        VehicleType typeVal = filterType.getValue(); FuelType fuelVal = filterFuel.getValue();
        List<Vehicle> result = allVehicles.stream().filter(v -> {
            if (!idStr.isEmpty()) { try { if (v.getId() != Long.parseLong(idStr)) return false; } catch (NumberFormatException e) { return false; } }
            if (!nameStr.isEmpty() && !v.getName().toLowerCase().contains(nameStr)) return false;
            if (!ownerStr.isEmpty() && (v.getOwnerLogin() == null || !v.getOwnerLogin().toLowerCase().contains(ownerStr))) return false;
            if (typeVal != null && v.getType() != typeVal) return false;
            if (fuelVal != null && v.getFuelType() != fuelVal) return false;
            if (!minPriceStr.isEmpty()) { try { if (v.getPrice() < Double.parseDouble(minPriceStr)) return false; } catch (NumberFormatException e) { return false; } }
            if (!maxPriceStr.isEmpty()) { try { if (v.getPrice() > Double.parseDouble(maxPriceStr)) return false; } catch (NumberFormatException e) { return false; } }
            return true;
        }).collect(Collectors.toList());
        filteredVehicles.setAll(result);
    }
    public void updateData(List<Vehicle> vehicles) {
        Long selectedId = null; Vehicle currentSelection = tableView.getSelectionModel().getSelectedItem();
        if (currentSelection != null) selectedId = currentSelection.getId();
        if (vehicles == null) { allVehicles.clear(); filteredVehicles.clear(); tableView.getSelectionModel().clearSelection(); return; }
        List<Vehicle> oldList = new ArrayList<>(allVehicles); List<Vehicle> newList = vehicles;
        allVehicles.setAll(vehicles); applyFilters();
        if (selectedId != null) {
            final Long finalSelectedId = selectedId;
            Vehicle toSelect = filteredVehicles.stream().filter(v -> v.getId() == finalSelectedId).findFirst().orElse(null);
            if (toSelect != null) Platform.runLater(() -> { tableView.getSelectionModel().select(toSelect); tableView.scrollTo(toSelect); });
        }
        animateTableChanges(oldList, newList);
    }
    private void animateTableChanges(List<Vehicle> oldList, List<Vehicle> newList) {
        if (oldList.size() < newList.size()) {
            List<Vehicle> addedVehicles = newList.stream().filter(v -> oldList.stream().noneMatch(ov -> ov.getId() == v.getId())).collect(Collectors.toList());
            if (!addedVehicles.isEmpty()) Platform.runLater(() -> animateAddedRows(addedVehicles));
        } else if (oldList.size() > newList.size()) {
            List<Vehicle> removedVehicles = oldList.stream().filter(v -> newList.stream().noneMatch(nv -> nv.getId() == v.getId())).collect(Collectors.toList());
            if (!removedVehicles.isEmpty()) Platform.runLater(() -> animateRemovedRows(removedVehicles));
        }
    }
    private void animateAddedRows(List<Vehicle> addedVehicles) {
        for (Vehicle addedVehicle : addedVehicles) {
            int index = filteredVehicles.indexOf(addedVehicle);
            if (index >= 0) {
                TableRow<Vehicle> row = getRowByIndex(index);
                if (row != null) {
                    row.setOpacity(0); row.setTranslateY(-30);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(600), row); fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.setInterpolator(Interpolator.EASE_OUT);
                    TranslateTransition slideDown = new TranslateTransition(Duration.millis(600), row); slideDown.setFromY(-30); slideDown.setToY(0); slideDown.setInterpolator(Interpolator.EASE_OUT);
                    new ParallelTransition(fadeIn, slideDown).play();
                }
            }
        }
    }
    private void animateRemovedRows(List<Vehicle> removedVehicles) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), tableView); fadeOut.setFromValue(1.0); fadeOut.setToValue(0.6); fadeOut.setInterpolator(Interpolator.EASE_IN);
        PauseTransition pause = new PauseTransition(Duration.millis(150));
        pause.setOnFinished(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), tableView); fadeIn.setFromValue(0.6); fadeIn.setToValue(1.0); fadeIn.setInterpolator(Interpolator.EASE_OUT); fadeIn.play();
        });
        fadeOut.play(); pause.play();
    }
    private TableRow<Vehicle> getRowByIndex(int index) {
        try {
            Node node = tableView.lookup(".table-row-cell:index(" + index + ")");
            if (node instanceof TableRow) return (TableRow<Vehicle>) node;
        } catch (Exception ignored) {}
        return null;
    }
    private void updateFilterStyles() {
// Определяем цвета для темной и светлой темы
        String bgColor = isDarkMode ? "#334155" : "white";
        String borderColor = isDarkMode ? "#475569" : "#E2E8F0";
        String textColor = isDarkMode ? "#F1F5F9" : "#1F2937"; // Более светлый текст для темной темы
        String promptColor = isDarkMode ? "#94A3B8" : "#9CA3AF";

// === ИСПРАВЛЕНИЕ: Обновляем цвет надписи "Фильтры" ===
        String filterLabelColor = isDarkMode ? "#E2E8F0" : "#374151";
        if (filterLabel != null) {
            filterLabel.setStyle("-fx-text-fill: " + filterLabelColor + "; -fx-font-weight: 500;");
        }
// ================================================

// Общий стиль для текстовых полей
        String style = "-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-radius: 6; " +
                "-fx-border-width: 1; " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-prompt-text-fill: " + promptColor + "; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 6 10;";
// Применяем стиль ко всем полям
        if (filterId != null) filterId.setStyle(style);
        if (filterName != null) filterName.setStyle(style);
        if (filterOwner != null) filterOwner.setStyle(style);
        if (filterMinPrice != null) filterMinPrice.setStyle(style);
        if (filterMaxPrice != null) filterMaxPrice.setStyle(style);
// Для ComboBox
        String comboStyle = "-fx-background-color: " + bgColor + "; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-radius: 6; " +
                "-fx-background-radius: 6; " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-font-size: 13px;";
        if (filterType != null) filterType.setStyle(comboStyle);
        if (filterFuel != null) filterFuel.setStyle(comboStyle);
    }
}