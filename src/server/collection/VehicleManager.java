package server.collection;

import common.Vehicle;
import common.VehicleType;
import server.database.UserDao;
import server.database.VehicleDao;

import java.util.*;
import java.util.stream.Collectors;

public class VehicleManager implements IVehicleManager {
    private final VehicleCollection collection;
    private final VehicleDao dao;
    private final UserDao userDao;

    public VehicleManager(VehicleCollection collection, VehicleDao dao, UserDao userDao) {
        this.collection = collection;
        this.dao = dao;
        this.userDao = userDao;
    }

    public ArrayList<Vehicle> showCollection() {
        return collection.getVehicles();
    }

    public HashMap<String, String> getInfo() {
        HashMap<String, String> paramList = new HashMap<>();
        paramList.put("Размер коллекции : ", String.valueOf(collection.size()));
        paramList.put("Тип коллекции : ", collection.getVehicles().getClass().getName());
        paramList.put("Дата инициализации : ", String.valueOf(collection.getInitTime()));
        float summa = 0;
        for (Vehicle v : collection.getVehicles()) {
            summa += v.getEnginePower();
        }
        paramList.put("Общая мощность двигателей : ", String.valueOf(summa));
        if (!collection.isEmpty()) {
            paramList.put("Средняя мощность двигателя : ", String.valueOf(summa / collection.size()));
        } else {
            paramList.put("Средняя мощность двигателя", "0 (коллекция пуста)");
        }
        return paramList;
    }

    public ArrayList<Vehicle> filterByEnginePower(Float power) {
        ArrayList<Vehicle> filtered = new ArrayList<>();
        for (Vehicle v : collection.getVehicles()) {
            if (v.getEnginePower() >= power) {
                filtered.add(v);
            }
        }
        return filtered;
    }

    public ArrayList<Vehicle> filterLessThanType(VehicleType type) {
        ArrayList<Vehicle> filtered = new ArrayList<>();
        for (Vehicle v : collection.getVehicles()) {
            if (v.getType() != null && v.getType().compareTo(type) < 0) {
                filtered.add(v);
            }
        }
        return filtered;
    }

    public ArrayList<Vehicle> sortByID() {
        ArrayList<Vehicle> vehicles = collection.getVehicles();
        vehicles.sort(Comparator.comparingLong(Vehicle::getId));
        return vehicles;
    }

    public ArrayList<Vehicle> sortByIDDescending() {
        ArrayList<Vehicle> vehicles = collection.getVehicles();
        vehicles.sort(Comparator.comparingLong(Vehicle::getId).reversed());
        return vehicles;
    }

    public ArrayList<Vehicle> shuffle() {
        ArrayList<Vehicle> vehicles = collection.getVehicles();
        Collections.shuffle(vehicles);
        return vehicles;
    }

    public Map<Comparable<?>, Long> groupByParam(List<String> args) {
        ValidateParams validator = new ValidateParams(args);
        GroupingField field = validator.getGroupingField();
        return collection.getVehicles().stream()
                .collect(Collectors.groupingBy(field.extractor(), Collectors.counting()));
    }

    //  запись: сначала БД, потом память

    public boolean addElement(Vehicle vehicle) {
        // 1. Сохраняем в БД (там генерируется ID через sequence)
        if (dao.insert(vehicle)) {
            // 2. Только при успехе добавляем в память
            collection.add(vehicle);
            return true;
        }
        return false;
    }

    @Override
    public boolean buyVehicle(long id, String buyerLogin) {
        if (dao.buyVehicle(id, buyerLogin)) {
            Vehicle v = collection.getVehicleByID(id);
            if (v != null) {
                v.setOwnerLogin(buyerLogin); // ← Синхронизация памяти для прокси
            }
            return true;
        }
        return false;
    }

    public boolean addElementManually(Vehicle vehicle) {
        // только для начальной загрузки из БД при старте сервера
        // Не вызывает dao.insert(), сразу добавляет в память
        collection.add(vehicle);
        return true;
    }

    public boolean updateElementByID(long id, Vehicle vehicle, String ownerLogin) {
        // 1. Обновляем в БД с проверкой владельца
        if (dao.update(id, vehicle, ownerLogin)) {
            // 2. Синхронизируем память
            collection.replaceVehicle(id, vehicle);
            return true;
        }
        return false;
    }

    public boolean rmByID(long id, String ownerLogin) {
        // 1. Удаляем из БД с проверкой владельца
        if (dao.delete(id, ownerLogin)) {
            // 2. Удаляем из памяти
            Vehicle v = collection.getVehicleByID(id);
            if (v != null) collection.rmEl(v);
            return true;
        }
        return false;
    }

    public boolean addIfMax(Vehicle veh) {
        Optional<Vehicle> max = collection.getVehicles().stream()
                .max(Comparator.comparingDouble(Vehicle::getDistanceTravelled));
        if (max.isEmpty() || veh.getDistanceTravelled() > max.get().getDistanceTravelled()) {
            return addElement(veh); // используем метод с БД
        }
        return false;
    }
    @Override
    public double getBalance(String login) {
        return userDao.getBalance(login);
    }

    @Override
    public boolean deposit(String login, double amount) {
        if (amount <= 0) return false; // Защита от отрицательных/нулевых пополнений
        return userDao.updateBalance(login, amount);
    }

    @Override
    public boolean setPrice(long id, double price, String ownerLogin) {
        if (price < 0) return false; // Валидация

        // 1. Обновляем в БД (там уже идёт проверка owner_login)
        if (userDao.updatePrice(id, price, ownerLogin)) {
            // 2. Обновляем объект в памяти
            Vehicle v = collection.getVehicleByID(id);
            if (v != null) {
                v.setPrice(price);
            }
            return true;
        }
        return false;
    }

    public void clearCollection(String ownerLogin) {
        // 1. Удаляем из БД только объекты владельца
        dao.clearAll(ownerLogin);


        // 2. Синхронизируем память
        collection.removeIf(v -> ownerLogin.equals(v.getOwnerLogin()));
    }
}