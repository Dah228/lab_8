package client.gui;
import common.Vehicle;
import common.VehicleType;
import common.FuelType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер таблицы транспортных средств.
 * Реализует отображение, фильтрацию и сортировку через Streams API.
 */
public class VehicleTableController {
    private final LocalizationManager localization;
    private TableView<Vehicle> tableView;

    // Исходные данные (полученные с сервера)
    private final ObservableList<Vehicle> allVehicles = FXCollections.observableArrayList();
    // Отфильтрованные данные (отображаемые в таблице)
    private final ObservableList<Vehicle> filteredVehicles = FXCollections.observableArrayList();

    // Элементы фильтров
    private TextField filterId;
    private TextField filterName;
    private TextField filterOwner;
    private TextField filterMinPrice;
    private TextField filterMaxPrice;
    private ComboBox<VehicleType> filterType;
    private ComboBox<FuelType> filterFuel;

    // Форматтеры
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.00");

    public VehicleTableController(LocalizationManager localization) {
        this.localization = localization;
    }

    /**
     * Создает VBox с панелью фильтров и таблицей.
     */
    public VBox createTablePane(){
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: white; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(255,182,193,0.4), 10, 0, 0, 2);");

        // 1. Панель фильтров
        HBox filterPanel = createFilterPanel();

        // 2. Таблица
        tableView = new TableView<>();
        tableView.setItems(filteredVehicles);

        // Стилизация таблицы
        tableView.setStyle("-fx-background-color: white; " +
                "-fx-control-inner-background: #fff0f5; " +
                "-fx-table-cell-border-color: #ffb6c1;");

        // Чередование цветов строк (Зебра)
        tableView.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.indexProperty().addListener((obs, oldIndex, newIndex) -> {
                if (!row.isEmpty()) {
                    if ((int)newIndex % 2 == 0){
                        row.setStyle("-fx-background-color: #fff0f5;");
                    } else {
                        row.setStyle("-fx-background-color: white;");
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
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(5));
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Стиль для лейбла фильтра
        Label filterLabel = new Label(localization.get("table.filter"));
        filterLabel.setStyle("-fx-text-fill: #ff1493; -fx-font-weight: bold;");

        // Текстовые фильтры
        String fieldStyle = "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #ffb6c1; -fx-prompt-text-fill: #ff69b4;";

        filterId = new TextField();
        filterId.setPromptText(localization.get("table.column.id"));
        filterId.setPrefWidth(60);
        filterId.setStyle(fieldStyle);

        filterName = new TextField();
        filterName.setPromptText(localization.get("table.column.name"));
        filterName.setPrefWidth(100);
        filterName.setStyle(fieldStyle);

        filterOwner = new TextField();
        filterOwner.setPromptText(localization.get("table.column.owner"));
        filterOwner.setPrefWidth(100);
        filterOwner.setStyle(fieldStyle);

        // Числовые фильтры
        filterMinPrice = new TextField();
        filterMinPrice.setPromptText(localization.get("filter.min_price"));
        filterMinPrice.setPrefWidth(70);
        filterMinPrice.setStyle(fieldStyle);

        filterMaxPrice = new TextField();
        filterMaxPrice.setPromptText(localization.get("filter.max_price"));
        filterMaxPrice.setPrefWidth(70);
        filterMaxPrice.setStyle(fieldStyle);

        // Выпадающие списки
        filterType = new ComboBox<>();
        filterType.getItems().add(null);
        filterType.getItems().addAll(VehicleType.values());
        filterType.setPromptText(localization.get("table.column.type"));
        setupFilterTypeLocalization();
        filterType.setValue(null);
        filterType.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #ffb6c1;");

        filterFuel = new ComboBox<>();
        filterFuel.getItems().add(null);
        filterFuel.getItems().addAll(FuelType.values());
        filterFuel.setPromptText(localization.get("table.column.fuel"));
        setupFilterFuelLocalization();
        filterFuel.setValue(null);
        filterFuel.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #ffb6c1;");

        // Привязка событий изменения фильтров
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

    /**
     * Настраивает локализацию для ComboBox типов транспортных средств
     */
    private void setupFilterTypeLocalization() {
        filterType.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(localization.get("table.filter.all_types"));
                } else {
                    setText(item.toString());
                }
            }
        });
        filterType.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(localization.get("table.filter.all_types"));
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    /**
     * Настраивает локализацию для ComboBox типов топлива
     */
    private void setupFilterFuelLocalization() {
        filterFuel.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(FuelType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(localization.get("table.filter.all_fuels"));
                } else {
                    setText(item.toString());
                }
            }
        });
        filterFuel.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(FuelType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(localization.get("table.filter.all_fuels"));
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    private void setupColumns() {
        String headerStyle = "-fx-background-color: linear-gradient(to bottom, #ffb6c1, #ff69b4); " +
                "-fx-text-fill: white; -fx-font-weight: bold;";

        TableColumn<Vehicle, Number> colId = new TableColumn<>(localization.get("col.id"));
        colId.setStyle(headerStyle);
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()));
        colId.setPrefWidth(50);

        TableColumn<Vehicle, String> colName = new TableColumn<>(localization.get("col.name"));
        colName.setStyle(headerStyle);
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colName.setPrefWidth(150);

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
        colDate.setPrefWidth(130);

        TableColumn<Vehicle, Float> colPower = new TableColumn<>(localization.get("col.engine_power"));
        colPower.setStyle(headerStyle);
        colPower.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getEnginePower()));
        colPower.setPrefWidth(80);

        TableColumn<Vehicle, Float> colDist = new TableColumn<>(localization.get("col.distance"));
        colDist.setStyle(headerStyle);
        colDist.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDistanceTravelled()));
        colDist.setPrefWidth(80);

        TableColumn<Vehicle, VehicleType> colType = new TableColumn<>(localization.get("col.type"));
        colType.setStyle(headerStyle);
        colType.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getType()));
        colType.setPrefWidth(100);

        TableColumn<Vehicle, FuelType> colFuel = new TableColumn<>(localization.get("col.fuel"));
        colFuel.setStyle(headerStyle);
        colFuel.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getFuelType()));
        colFuel.setPrefWidth(100);

        TableColumn<Vehicle, String> colOwner = new TableColumn<>(localization.get("col.owner"));
        colOwner.setStyle(headerStyle);
        colOwner.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOwnerLogin()));
        colOwner.setPrefWidth(100);

        TableColumn<Vehicle, Double> colPrice = new TableColumn<>(localization.get("col.price"));
        colPrice.setStyle(headerStyle);
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
        colPrice.setPrefWidth(90);

        tableView.getColumns().addAll(colId, colName, colCoords, colDate, colPower, colDist, colType, colFuel, colOwner, colPrice);
    }

    public void updateLocalization() {
        // Обновляем заголовки столбцов
        tableView.getColumns().clear();
        setupColumns();

        // Обновляем фильтры
        HBox filterPanel = (HBox) ((VBox) tableView.getParent()).getChildren().get(0);
        filterPanel.getChildren().clear();

        Label filterLabel = new Label(localization.get("table.filter") + ":");
        filterLabel.setStyle("-fx-text-fill: #ff1493; -fx-font-weight: bold;");

        filterPanel.getChildren().addAll(
                filterLabel,
                filterId, filterName, filterOwner,
                filterMinPrice, filterMaxPrice,
                filterType, filterFuel
        );

        // Обновляем prompt text
        filterId.setPromptText(localization.get("filter.id"));
        filterName.setPromptText(localization.get("filter.name"));
        filterOwner.setPromptText(localization.get("filter.owner"));
        filterMinPrice.setPromptText(localization.get("filter.min_price"));
        filterMaxPrice.setPromptText(localization.get("filter.max_price"));
        filterType.setPromptText(localization.get("filter.type"));
        filterFuel.setPromptText(localization.get("filter.fuel"));

        // Обновляем локализацию ComboBox'ов
        setupFilterTypeLocalization();
        setupFilterFuelLocalization();
    }

    /**
     * Применяет фильтры и сортировку используя STREAMS API.
     * Это ключевое требование ТЗ.
     */
    private void applyFilters() {
        String idStr = filterId.getText().trim();
        String nameStr = filterName.getText().trim().toLowerCase();
        String ownerStr = filterOwner.getText().trim().toLowerCase();
        String minPriceStr = filterMinPrice.getText().trim();
        String maxPriceStr = filterMaxPrice.getText().trim();
        VehicleType typeVal = filterType.getValue();
        FuelType fuelVal = filterFuel.getValue();

        // STREAMS API CHAIN
        List<Vehicle> result = allVehicles.stream()
                .filter(v -> {
                    // Фильтр по ID
                    if (!idStr.isEmpty()) {
                        try {
                            if (v.getId() != Long.parseLong(idStr)) return false;
                        } catch (NumberFormatException e) { return false; }
                    }
                    // Фильтр по Имени (частичное совпадение)
                    if (!nameStr.isEmpty() && !v.getName().toLowerCase().contains(nameStr)) return false;
                    // Фильтр по Владельцу
                    if (!ownerStr.isEmpty() && (v.getOwnerLogin() == null || !v.getOwnerLogin().toLowerCase().contains(ownerStr))) return false;
                    // Фильтр по Типу
                    if (typeVal != null && v.getType() != typeVal) return false;
                    // Фильтр по Топливу
                    if (fuelVal != null && v.getFuelType() != fuelVal) return false;
                    // Фильтр по Цене (Диапазон)
                    if (!minPriceStr.isEmpty()) {
                        try {
                            if (v.getPrice() < Double.parseDouble(minPriceStr)) return false;
                        } catch (NumberFormatException e) { return false; }
                    }
                    if (!maxPriceStr.isEmpty()) {
                        try {
                            if (v.getPrice() > Double.parseDouble(maxPriceStr)) return false;
                        } catch (NumberFormatException e) { return false; }
                    }
                    return true;
                })
                .sorted((v1, v2) -> {
                    // Сортировка по умолчанию: по ID возрастанию
                    return Long.compare(v1.getId(), v2.getId());
                })
                .collect(Collectors.toList());

        // Обновляем ObservableList таблицы
        filteredVehicles.setAll(result);
    }

    /**
     * Обновляет исходные данные извне (после получения ответа от сервера).
     */
    public void updateData(List<Vehicle> vehicles) {
        if (vehicles == null) {
            allVehicles.clear();
            filteredVehicles.clear();
            return;
        }
        // Заменяем содержимое исходного списка
        allVehicles.setAll(vehicles);
        // Переприменяем фильтры
        applyFilters();
    }

    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(allVehicles);
    }

    public TableView<Vehicle> getTable() {
        return tableView;
    }
}