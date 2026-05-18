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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Контроллер таблицы транспортных средств.
 * Реализует отображение, фильтрацию и сортировку через Streams API.
 */
public class VehicleTableController {

    private final LocalizationManager localization;
    private TableView<Vehicle> tableView;

    // Исходные данные (полученные с сервера)
    private ObservableList<Vehicle> allVehicles = FXCollections.observableArrayList();
    // Отфильтрованные данные (отображаемые в таблице)
    private ObservableList<Vehicle> filteredVehicles = FXCollections.observableArrayList();

    // Элементы фильтров
    private TextField filterId;
    private TextField filterName;
    private TextField filterOwner;
    private TextField filterMinPrice;
    private TextField filterMaxPrice;
    private ComboBox<VehicleType> filterType;
    private ComboBox<FuelType> filterFuel;

    // Форматтеры
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
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

        // 1. Панель фильтров
        HBox filterPanel = createFilterPanel();

        // 2. Таблица
        tableView = new TableView<>();
        tableView.setItems(filteredVehicles);
        setupColumns();

        // УБРАЛИ кнопку обновления
        root.getChildren().addAll(filterPanel, tableView);
        VBox.setVgrow(tableView, javafx.scene.layout.Priority.ALWAYS);

        return root;
    }
    private HBox createFilterPanel() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(5));
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Текстовые фильтры
        filterId = new TextField();
        filterId.setPromptText(localization.get("table.column.id"));
        filterId.setPrefWidth(60);
        filterName = new TextField();
        filterName.setPromptText(localization.get("table.column.name"));
        filterName.setPrefWidth(100);
        filterOwner = new TextField();
        filterOwner.setPromptText(localization.get("table.column.owner"));
        filterOwner.setPrefWidth(100);

        // Числовые фильтры
        filterMinPrice = new TextField();
        filterMinPrice.setPromptText(localization.get("filter.min_price"));
        filterMinPrice.setPrefWidth(70);
        filterMaxPrice = new TextField();
        filterMaxPrice.setPromptText(localization.get("filter.max_price"));
        filterMaxPrice.setPrefWidth(70);

        // Выпадающие списки
        filterType = new ComboBox<>();
        filterType.getItems().add(null);
        filterType.getItems().addAll(VehicleType.values());
        filterType.setPromptText(localization.get("table.column.type"));
        filterType.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(localization.get("table.filter.all_types"));
                } else {
                    setText(item.toString());
                }
            }
        });
        filterType.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(localization.get("table.filter.all_types"));
                } else {
                    setText(item.toString());
                }
            }
        });
        filterType.setValue(null);

        filterFuel = new ComboBox<>();
        filterFuel.getItems().add(null);
        filterFuel.getItems().addAll(FuelType.values());
        filterFuel.setPromptText(localization.get("table.column.fuel"));
        filterFuel.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(FuelType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(localization.get("table.filter.all_fuels"));
                } else {
                    setText(item.toString());
                }
            }
        });
        filterFuel.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(FuelType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(localization.get("table.filter.all_fuels"));
                } else {
                    setText(item.toString());
                }
            }
        });
        filterFuel.setValue(null);

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
                new Label(localization.get("table.filter")),
                filterId, filterName, filterOwner,
                filterMinPrice, filterMaxPrice,
                filterType, filterFuel
        );
        return hbox;
    }

    private void setupColumns() {
        TableColumn<Vehicle, Number> colId = new TableColumn<>(localization.get("col.id"));
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()));
        colId.setPrefWidth(50);

        TableColumn<Vehicle, String> colName = new TableColumn<>(localization.get("col.name"));
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colName.setPrefWidth(150);

        TableColumn<Vehicle, String> colCoords = new TableColumn<>(localization.get("col.coords"));
        colCoords.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCoordinates().getX() + ", " + data.getValue().getCoordinates().getY()
        ));
        colCoords.setPrefWidth(100);

        TableColumn<Vehicle, String> colDate = new TableColumn<>(localization.get("col.creation_date"));
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCreationDate().toInstant().atZone(java.time.ZoneId.systemDefault()).format(DATE_FORMATTER)
        ));
        colDate.setPrefWidth(130);

        TableColumn<Vehicle, Float> colPower = new TableColumn<>(localization.get("col.engine_power"));
        colPower.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getEnginePower()));
        colPower.setPrefWidth(80);

        TableColumn<Vehicle, Float> colDist = new TableColumn<>(localization.get("col.distance"));
        colDist.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDistanceTravelled()));
        colDist.setPrefWidth(80);

        TableColumn<Vehicle, VehicleType> colType = new TableColumn<>(localization.get("col.type"));
        colType.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getType()));
        colType.setPrefWidth(100);

        TableColumn<Vehicle, FuelType> colFuel = new TableColumn<>(localization.get("col.fuel"));
        colFuel.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getFuelType()));
        colFuel.setPrefWidth(100);

        TableColumn<Vehicle, String> colOwner = new TableColumn<>(localization.get("col.owner"));
        colOwner.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOwnerLogin()));
        colOwner.setPrefWidth(100);

        TableColumn<Vehicle, Double> colPrice = new TableColumn<>(localization.get("col.price"));
        colPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
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
        filterPanel.getChildren().addAll(
                new Label(localization.get("table.filter") + ":"),
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
                    // Можно доработать под клик по заголовку колонки, но для базового функционала достаточно ID
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

    /**
     * Заглушка для запроса данных с сервера.
     * В Этапе 6 мы подключим это к реальной сети.
     */
    public void requestShowFromServer() {
        System.out.println("Запрос данных с сервера (заглушка)...");
        // Здесь будет вызов networkService.send(new CommandRequest("show", ...))
        // А пока просто добавим тестовые данные для проверки UI
        testFillData();
    }

    private void testFillData() {
        // Тестовые данные для проверки работы таблицы
        Vehicle v1 = new Vehicle();
        v1.setId(1); v1.setName("Test Car"); v1.setOwnerLogin("user1");
        v1.setCoordinates(10, 20); v1.setPrice(1000.0); v1.setType(common.VehicleType.BOAT);
        v1.setFuelType(common.FuelType.GASOLINE); v1.setEnginePower(150f); v1.setDistanceTravelled(5000f);

        Vehicle v2 = new Vehicle();
        v2.setId(2); v2.setName("Fast Plane"); v2.setOwnerLogin("user2");
        v2.setCoordinates(100, 200); v2.setPrice(50000.0); v2.setType(common.VehicleType.PLANE);
        v2.setFuelType(common.FuelType.KEROSENE); v2.setEnginePower(5000f); v2.setDistanceTravelled(100000f);

        updateData(List.of(v1, v2));
    }
}