package server.collection;
import common.Vehicle;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class VehicleCollection {
    private final ArrayList<Vehicle> vehicles = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Instant initTime = Instant.now();

    public void add(Vehicle v) {
        lock.lock();
        try { vehicles.add(v); } finally { lock.unlock(); }
    }
    public void clear() {
        lock.lock();
        try { vehicles.clear(); } finally { lock.unlock(); }
    }
    public ArrayList<Vehicle> getVehicles() {
        lock.lock();
        try { return new ArrayList<>(vehicles); } finally { lock.unlock(); }
    }
    public int size() {
        lock.lock();
        try { return vehicles.size(); } finally { lock.unlock(); }
    }
    public Instant getInitTime() { return initTime; }
    public boolean isEmpty() {
        lock.lock();
        try { return vehicles.isEmpty(); } finally { lock.unlock(); }
    }
    public void rmEl(Vehicle v) {
        lock.lock();
        try { vehicles.remove(v); } finally { lock.unlock(); }
    }
    public void removeIf(Predicate<Vehicle> predicate) {
        lock.lock();
        try { vehicles.removeIf(predicate); } finally { lock.unlock(); }
    }
    public List<Long> getAllID() {
        lock.lock();
        try {
            List<Long> id = new ArrayList<>();
            for (Vehicle v : vehicles) id.add(v.getId());
            return id;
        } finally { lock.unlock(); }
    }
    public Vehicle getVehicleByID(long id) {
        lock.lock();
        try {
            for (Vehicle v : vehicles) if (v.getId() == id) return v;
            return null;
        } finally { lock.unlock(); }
    }
    public void replaceVehicle(long id, Vehicle vehicle) {
        lock.lock();
        try {
            int index = (int) id - 1;
            if (index >= 0 && index < vehicles.size()) vehicles.set(index, vehicle);
        } finally { lock.unlock(); }
    }
}