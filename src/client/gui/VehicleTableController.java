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
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.00");
    private boolean isDarkMode = false;

    public VehicleTableController(LocalizationManager localization) {
        this.localization = localization;
    }

    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
        updateTableTheme();
    }

    private void updateTableTheme() {
        if (tableView == null) return;

        tableView.setStyle(isDarkMode ?
                "-fx-background-color: #1E293B; -fx-control-inner-background: #1E293B; -fx-table-cell-border-color: #334155;" :
                "-fx-background-color: transparent; -fx-control-inner-background: white; -fx-table-cell-border-color: #F0F0F0;");

        tableView.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected && !row.isEmpty()) {
                    if (isDarkMode) {
                        row.setStyle("-fx-background-color: linear-gradient(to bottom, #7C3AED, #6D28D9); -fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(124,58,237,0.5), 15, 0, 0, 2);");
                    } else {
                        row.setStyle("-fx-background-color: linear-gradient(to bottom, #2979FF, #1565C0); -fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(41,121,255,0.6), 20, 0, 0, 2);");
                    }
                    row.getStyleClass().add("selected-row");
                } else if (!row.isEmpty()) {
                    row.getStyleClass().remove("selected-row");
                    if (isDarkMode) {
                        row.setStyle(((int)row.getIndex() % 2 == 0) ? "-fx-background-color: #1E293B;" : "-fx-background-color: #0F172A;");
                    } else {
                        row.setStyle(((int)row.getIndex() % 2 == 0) ? "-fx-background-color: #FAFAFA;" : "-fx-background-color: white;");
                    }
                }
            });

            row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                if (!row.isSelected() && !row.isEmpty()) {
                    if (isNowHovered) {
                        row.setStyle(isDarkMode ?
                                "-fx-background-color: #334155; -fx-effect: dropshadow(gaussian, rgba(124,58,237,0.3), 10, 0, 0, 1);" :
                                "-fx-background-color: #E3F2FD; -fx-effect: dropshadow(gaussian, rgba(41,121,255,0.2), 10, 0, 0, 1);");
                    } else {
                        if (isDarkMode) {
                            row.setStyle(((int)row.getIndex() % 2 == 0) ? "-fx-background-color: #1E293B;" : "-fx-background-color: #0F172A;");
                        } else {
                            row.setStyle(((int)row.getIndex() % 2 == 0) ? "-fx-background-color: #FAFAFA;" : "-fx-background-color: white;");
                        }
                    }
                }
            });
            return row;
        });

        for (TableColumn<Vehicle, ?> col : tableView.getColumns()) {
            col.setStyle(isDarkMode ?
                    "-fx-background-color: #334155; -fx-text-fill: #A855F7; -fx-font-weight: bold; -fx-border-width: 0 0 1 0; -fx-border-color: #475569;" :
                    "-fx-background-color: #FAFAFA; -fx-text-fill: #424242; -fx-font-weight: bold; -fx-border-width: 0 0 1 0; -fx-border-color: #E0E0E0;");
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

    private HBox createFilterPanel() {
        HBox hbox = new HBox(12);
        hbox.setPadding(new Insets(0, 0, 15, 0));
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label filterLabel = new Label("Фильтры:");
        filterLabel.setStyle("-fx-text-fill: #757575; -fx-font-weight: 500;");

        String fieldStyle = "-fx-background-color: " + (isDarkMode ? "#334155" : "white") + "; " +
                "-fx-background-radius: 6; -fx-border-color: " + (isDarkMode ? "#475569" : "#E0E0E0") + "; " +
                "-fx-border-radius: 6; -fx-border-width: 1; -fx-prompt-text-fill: " + (isDarkMode ? "#94A3B8" : "#9E9E9E") + "; -fx-padding: 5 8;";
        String comboStyle = "-fx-background-color: " + (isDarkMode ? "#334155" : "white") + "; " +
                "-fx-border-color: " + (isDarkMode ? "#475569" : "#E0E0E0") + "; -fx-border-radius: 6; -fx-background-radius: 6;";

        filterId = new TextField(); filterId.setPromptText("ID"); filterId.setPrefWidth(50); filterId.setStyle(fieldStyle);
        filterName = new TextField(); filterName.setPromptText("Имя"); filterName.setPrefWidth(100); filterName.setStyle(fieldStyle);
        filterOwner = new TextField(); filterOwner.setPromptText("Владелец"); filterOwner.setPrefWidth(100); filterOwner.setStyle(fieldStyle);
        filterMinPrice = new TextField(); filterMinPrice.setPromptText("Цена от"); filterMinPrice.setPrefWidth(70); filterMinPrice.setStyle(fieldStyle);
        filterMaxPrice = new TextField(); filterMaxPrice.setPromptText("Цена до"); filterMaxPrice.setPrefWidth(70); filterMaxPrice.setStyle(fieldStyle);

        filterType = new ComboBox<>(); filterType.getItems().add(null); filterType.getItems().addAll(VehicleType.values());
        filterType.setPromptText("Тип"); filterType.setValue(null); filterType.setStyle(comboStyle); setupFilterTypeLocalization();
        filterFuel = new ComboBox<>(); filterFuel.getItems().add(null); filterFuel.getItems().addAll(FuelType.values());
        filterFuel.setPromptText("Топливо"); filterFuel.setValue(null); filterFuel.setStyle(comboStyle); setupFilterFuelLocalization();

        Callback<Void, Void> updateFilter = v -> { applyFilters(); return null; };
        filterId.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterName.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterOwner.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterMinPrice.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterMaxPrice.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterType.valueProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterFuel.valueProperty().addListener((obs, old, newVal) -> updateFilter.call(null));

        hbox.getChildren().addAll(filterLabel, filterId, filterName, filterOwner, filterMinPrice, filterMaxPrice, filterType, filterFuel);
        return hbox;
    }

    private void setupFilterTypeLocalization() {
        filterType.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? localization.get("table.filter.all_types") : item.toString());
            }
        });
        filterType.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? localization.get("table.filter.all_types") : item.toString());
            }
        });
    }

    private void setupFilterFuelLocalization() {
        filterFuel.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(FuelType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? localization.get("table.filter.all_fuels") : item.toString());
            }
        });
        filterFuel.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(FuelType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? localization.get("table.filter.all_fuels") : item.toString());
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
}