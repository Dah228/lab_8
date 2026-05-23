package client.gui;
import common.Vehicle;
import common.VehicleType;
import common.FuelType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VehicleTableController {
    private final LocalizationManager localization;
    private TableView<Vehicle> tableView;
    private final ObservableList<Vehicle> allVehicles = FXCollections.observableArrayList();
    private final ObservableList<Vehicle> filteredVehicles = FXCollections.observableArrayList();
    private TextField filterId;
    private TextField filterName;
    private TextField filterOwner;
    private TextField filterMinPrice;
    private TextField filterMaxPrice;
    private ComboBox<VehicleType> filterType;
    private ComboBox<FuelType> filterFuel;
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.00");

    public VehicleTableController(LocalizationManager localization) {
        this.localization = localization;
    }

    public VBox createTablePane(){
        VBox root = new VBox(15);
        root.setPadding(new Insets(0));
        // Убрали внешние стили, так как теперь таблица лежит внутри карточки в MainScene

        // 1. Панель фильтров
        HBox filterPanel = createFilterPanel();

        // 2. Таблица
        tableView = new TableView<>();
        tableView.setItems(filteredVehicles);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Чистый стиль таблицы
        tableView.setStyle("-fx-background-color: transparent; " +
                "-fx-control-inner-background: white; " +
                "-fx-table-cell-border-color: #F0F0F0;");

        // Стилизация строк
        tableView.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.indexProperty().addListener((obs, oldIndex, newIndex) -> {
                if (!row.isEmpty()) {
                    // Чередование цветов (зебра) очень легкое
                    if ((int)newIndex % 2 == 0){
                        row.setStyle("-fx-background-color: #FAFAFA;");
                    } else {
                        row.setStyle("-fx-background-color: white;");
                    }

                    // Стиль выделенной строки
                    if (row.isSelected()) {
                        row.setStyle("-fx-background-color: #E3F2FD; -fx-font-weight: bold;");
                    }
                }
            });
            return row;
        });

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

        // Стиль полей ввода (современный, с закруглением)
        String fieldStyle = "-fx-background-color: white; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: #E0E0E0; " +
                "-fx-border-radius: 6; " +
                "-fx-border-width: 1; " +
                "-fx-prompt-text-fill: #9E9E9E; " +
                "-fx-padding: 5 8;";

        filterId = new TextField();
        filterId.setPromptText("ID");
        filterId.setPrefWidth(50);
        filterId.setStyle(fieldStyle);

        filterName = new TextField();
        filterName.setPromptText("Имя");
        filterName.setPrefWidth(100);
        filterName.setStyle(fieldStyle);

        filterOwner = new TextField();
        filterOwner.setPromptText("Владелец");
        filterOwner.setPrefWidth(100);
        filterOwner.setStyle(fieldStyle);

        filterMinPrice = new TextField();
        filterMinPrice.setPromptText("Цена от");
        filterMinPrice.setPrefWidth(70);
        filterMinPrice.setStyle(fieldStyle);

        filterMaxPrice = new TextField();
        filterMaxPrice.setPromptText("Цена до");
        filterMaxPrice.setPrefWidth(70);
        filterMaxPrice.setStyle(fieldStyle);

        filterType = new ComboBox<>();
        filterType.getItems().add(null);
        filterType.getItems().addAll(VehicleType.values());
        filterType.setPromptText("Тип");
        filterType.setValue(null);
        filterType.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 6; -fx-background-radius: 6;");
        setupFilterTypeLocalization();

        filterFuel = new ComboBox<>();
        filterFuel.getItems().add(null);
        filterFuel.getItems().addAll(FuelType.values());
        filterFuel.setPromptText("Топливо");
        filterFuel.setValue(null);
        filterFuel.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 6; -fx-background-radius: 6;");
        setupFilterFuelLocalization();

        Callback<Void, Void> updateFilter = v -> { applyFilters(); return null; };
        filterId.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterName.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterOwner.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterMinPrice.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterMaxPrice.textProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterType.valueProperty().addListener((obs, old, newVal) -> updateFilter.call(null));
        filterFuel.valueProperty().addListener((obs, old, newVal) -> updateFilter.call(null));

        hbox.getChildren().addAll(
                filterLabel,
                filterId, filterName, filterOwner,
                filterMinPrice, filterMaxPrice,
                filterType, filterFuel
        );
        return hbox;
    }

    private void setupFilterTypeLocalization() {
        filterType.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(localization.get("table.filter.all_types"));
                else setText(item.toString());
            }
        });
        filterType.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(localization.get("table.filter.all_types"));
                else setText(item.toString());
            }
        });
    }

    private void setupFilterFuelLocalization() {
        filterFuel.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(FuelType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(localization.get("table.filter.all_fuels"));
                else setText(item.toString());
            }
        });
        filterFuel.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(FuelType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(localization.get("table.filter.all_fuels"));
                else setText(item.toString());
            }
        });
    }

    private void setupColumns() {
        // Стиль заголовков
        String headerStyle = "-fx-background-color: #FAFAFA; " +
                "-fx-text-fill: #424242; " +
                "-fx-font-weight: bold; " +
                "-fx-border-width: 0 0 1 0; " +
                "-fx-border-color: #E0E0E0;";

        TableColumn<Vehicle, Number> colId = new TableColumn<>(localization.get("col.id"));
        colId.setStyle(headerStyle);
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()));
        colId.setPrefWidth(50);

        TableColumn<Vehicle, String> colName = new TableColumn<>(localization.get("col.name"));
        colName.setStyle(headerStyle);
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colName.setPrefWidth(120);

        TableColumn<Vehicle, String> colCoords = new TableColumn<>(localization.get("col.coords"));
        colCoords.setStyle(headerStyle);
        colCoords.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCoordinates().getX() + ", " + data.getValue().getCoordinates().getY()
        ));
        colCoords.setPrefWidth(100);

        TableColumn<Vehicle, String> colDate = new TableColumn<>(localization.get("col.creation_date"));
        colDate.setStyle(headerStyle);
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                localization.formatDateTime(data.getValue().getCreationDate())
        ));
        colDate.setPrefWidth(110);

        TableColumn<Vehicle, Float> colPower = new TableColumn<>(localization.get("col.engine_power"));
        colPower.setStyle(headerStyle);
        colPower.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getEnginePower()));
        colPower.setPrefWidth(70);

        TableColumn<Vehicle, Float> colDist = new TableColumn<>(localization.get("col.distance"));
        colDist.setStyle(headerStyle);
        colDist.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDistanceTravelled()));
        colDist.setPrefWidth(70);

        TableColumn<Vehicle, VehicleType> colType = new TableColumn<>(localization.get("col.type"));
        colType.setStyle(headerStyle);
        colType.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getType()));
        colType.setPrefWidth(90);

        TableColumn<Vehicle, FuelType> colFuel = new TableColumn<>(localization.get("col.fuel"));
        colFuel.setStyle(headerStyle);
        colFuel.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getFuelType()));
        colFuel.setPrefWidth(90);

        TableColumn<Vehicle, String> colOwner = new TableColumn<>(localization.get("col.owner"));
        colOwner.setStyle(headerStyle);
        colOwner.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOwnerLogin()));
        colOwner.setPrefWidth(90);

        TableColumn<Vehicle, Double> colPrice = new TableColumn<>(localization.get("col.price"));
        colPrice.setStyle(headerStyle);
        colPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else setText(PRICE_FORMAT.format(price));
            }
        });
        colPrice.setPrefWidth(90);

        tableView.getColumns().addAll(colId, colName, colCoords, colDate, colPower, colDist, colType, colFuel, colOwner, colPrice);
    }

    public void updateLocalization() {
        tableView.getColumns().clear();
        setupColumns();

        // Обновляем фильтры (упрощенно)
        // В реальном коде тут нужно пересоздать HBox фильтров, но для простоты оставим как есть
        // или можно вызвать createFilterPanel заново, если нужно.
    }

    private void applyFilters() {
        String idStr = filterId.getText().trim();
        String nameStr = filterName.getText().trim().toLowerCase();
        String ownerStr = filterOwner.getText().trim().toLowerCase();
        String minPriceStr = filterMinPrice.getText().trim();
        String maxPriceStr = filterMaxPrice.getText().trim();
        VehicleType typeVal = filterType.getValue();
        FuelType fuelVal = filterFuel.getValue();

        List<Vehicle> result = allVehicles.stream()
                .filter(v -> {
                    if (!idStr.isEmpty()) {
                        try { if (v.getId() != Long.parseLong(idStr)) return false; } catch (NumberFormatException e) { return false; }
                    }
                    if (!nameStr.isEmpty() && !v.getName().toLowerCase().contains(nameStr)) return false;
                    if (!ownerStr.isEmpty() && (v.getOwnerLogin() == null || !v.getOwnerLogin().toLowerCase().contains(ownerStr))) return false;
                    if (typeVal != null && v.getType() != typeVal) return false;
                    if (fuelVal != null && v.getFuelType() != fuelVal) return false;
                    if (!minPriceStr.isEmpty()) {
                        try { if (v.getPrice() < Double.parseDouble(minPriceStr)) return false; } catch (NumberFormatException e) { return false; }
                    }
                    if (!maxPriceStr.isEmpty()) {
                        try { if (v.getPrice() > Double.parseDouble(maxPriceStr)) return false; } catch (NumberFormatException e) { return false; }
                    }
                    return true;
                })
                .sorted((v1, v2) -> Long.compare(v1.getId(), v2.getId()))
                .collect(Collectors.toList());

        filteredVehicles.setAll(result);
    }

    public void updateData(List<Vehicle> vehicles) {
        Long selectedId = null;
        Vehicle currentSelection = tableView.getSelectionModel().getSelectedItem();
        if (currentSelection != null) {
            selectedId = currentSelection.getId();
        }

        if (vehicles == null) {
            allVehicles.clear();
            filteredVehicles.clear();
            tableView.getSelectionModel().clearSelection();
            return;
        }

        allVehicles.setAll(vehicles);
        applyFilters();

        if (selectedId != null) {
            final Long finalSelectedId = selectedId;
            Vehicle toSelect = filteredVehicles.stream()
                    .filter(v -> v.getId() == finalSelectedId)
                    .findFirst()
                    .orElse(null);
            if (toSelect != null) {
                tableView.getSelectionModel().select(toSelect);
            }
        }
    }

    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(allVehicles);
    }

    public TableView<Vehicle> getTable() {
        return tableView;
    }
}