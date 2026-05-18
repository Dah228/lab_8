package server.collection;
import common.Vehicle;
import common.VehicleType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleManagerProxy implements IVehicleManager {
    private final IVehicleManager realManager;

    public VehicleManagerProxy(IVehicleManager realManager) {
        this.realManager = realManager;
    }

    private boolean isOwner(long id, String ownerLogin) {
        if (ownerLogin == null) return false;
        for (Vehicle v : realManager.showCollection()) {
            if (v.getId() == id) {
                return ownerLogin.equals(v.getOwnerLogin());
            }
        }
        return false;
    }

    @Override
    public boolean addElement(Vehicle vehicle) {
        if (vehicle.getOwnerLogin() == null || vehicle.getOwnerLogin().isEmpty()) {
            throw new SecurityException("Добавление элемента без указания владельца запрещено");
        }
        return realManager.addElement(vehicle);
    }

    @Override
    public boolean addElementManually(Vehicle vehicle) {
        return realManager.addElementManually(vehicle);
    }

    @Override
    public boolean updateElementByID(long id, Vehicle vehicle, String ownerLogin) {
        if (!isOwner(id, ownerLogin)) {
            System.err.println("PROXY: Попытка изменения чужого элемента. ID=" + id + ", user=" + ownerLogin);
            return false;
        }
        return realManager.updateElementByID(id, vehicle, ownerLogin);
    }

    @Override
    public boolean rmByID(long id, String ownerLogin) {
        if (!isOwner(id, ownerLogin)) {
            System.err.println("PROXY: Попытка удаления чужого элемента. ID=" + id + ", user=" + ownerLogin);
            return false;
        }
        return realManager.rmByID(id, ownerLogin);
    }

    @Override
    public void clearCollection(String ownerLogin) {
        realManager.clearCollection(ownerLogin);
    }

    @Override
    public boolean addIfMax(Vehicle vehicle) {
        if (vehicle.getOwnerLogin() == null || vehicle.getOwnerLogin().isEmpty()) {
            throw new SecurityException("Добавление элемента без указания владельца запрещено");
        }
        return realManager.addIfMax(vehicle);
    }

    @Override
    public boolean buyVehicle(long id, String buyerLogin) {
        return realManager.buyVehicle(id, buyerLogin);
    }

    @Override
    public double getBalance(String login) {
        return realManager.getBalance(login);
    }

    @Override
    public boolean deposit(String login, double amount) {
        return realManager.deposit(login, amount);
    }

    @Override
    public boolean setPrice(long id, double price, String ownerLogin) {
        if (!isOwner(id, ownerLogin)) {
            System.err.println("PROXY: Попытка изменения цены чужого элемента. ID=" + id + ", user=" + ownerLogin);
            return false;
        }
        return realManager.setPrice(id, price, ownerLogin);
    }

    @Override public ArrayList<Vehicle> showCollection() { return realManager.showCollection(); }
    @Override public HashMap<String, String> getInfo() { return realManager.getInfo(); }
    @Override public ArrayList<Vehicle> filterByEnginePower(Float power) { return realManager.filterByEnginePower(power); }
    @Override public ArrayList<Vehicle> filterLessThanType(VehicleType type) { return realManager.filterLessThanType(type); }
    @Override public ArrayList<Vehicle> sortByID() { return realManager.sortByID(); }
    @Override public ArrayList<Vehicle> sortByIDDescending() { return realManager.sortByIDDescending(); }
    @Override public ArrayList<Vehicle> shuffle() { return realManager.shuffle(); }
    @Override public Map<Comparable<?>, Long> groupByParam(List<String> args) { return realManager.groupByParam(args); }
}