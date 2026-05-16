package server.collection;

import common.Vehicle;
import common.VehicleType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IVehicleManager {
    boolean addElement(Vehicle vehicle);
    boolean addElementManually(Vehicle vehicle);
    boolean updateElementByID(long id, Vehicle vehicle, String ownerLogin);
    boolean rmByID(long id, String ownerLogin);
    void clearCollection(String ownerLogin);
    boolean addIfMax(Vehicle vehicle);
    boolean buyVehicle(long id, String buyerLogin);
    double getBalance(String login);
    boolean deposit(String login, double amount);
    boolean setPrice(long id, double price, String ownerLogin);

    ArrayList<Vehicle> showCollection();
    HashMap<String, String> getInfo();
    ArrayList<Vehicle> filterByEnginePower(Float power);
    ArrayList<Vehicle> filterLessThanType(VehicleType type);
    ArrayList<Vehicle> sortByID();
    ArrayList<Vehicle> sortByIDDescending();
    ArrayList<Vehicle> shuffle();
    Map<Comparable<?>, Long> groupByParam(List<String> args);

}