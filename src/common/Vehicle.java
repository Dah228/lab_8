package common;

import java.io.Serializable;
import java.util.Date;

public class Vehicle implements Serializable {
    private long id;
    private String name;
    private Coordinates coordinates;
    private Date creationDate;
    private float enginePower;
    private float distanceTravelled;
    private VehicleType type;
    private FuelType fuelType;
    private String ownerLogin;
    private double price;


    // Конструктор без параметров (для сериализации и создания новых объектов)
    public Vehicle() {
        this.creationDate = new Date();
    }

    // Геттеры
    public long getId() { return id; }
    public String getName() { return name; }
    public Coordinates getCoordinates() { return coordinates; }
    public Date getCreationDate() { return creationDate; }
    public float getEnginePower() { return enginePower; }
    public float getDistanceTravelled() { return distanceTravelled; }
    public VehicleType getType() { return type; }
    public FuelType getFuelType() { return fuelType; }
    public String getOwnerLogin() { return ownerLogin; } // ← НОВЫЙ ГЕТТЕР

    // Сеттеры
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    public void setCoordinates(int x, float y) {
        if (this.coordinates == null) {
            this.coordinates = new Coordinates();
        }
        this.coordinates.setCoord(x, y);
    }

    // объекта Coordinates (нужен для DAO)
    public void setCoordinatesObject(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void setCreationDate() {
        long fiveYearsInMillis = 5L * 365 * 24 * 60 * 60 * 1000;
        long randomMillis = System.currentTimeMillis() - (long) (Math.random() * fiveYearsInMillis);
        this.creationDate = new Date(randomMillis);
    }

    public void setCreationDateHand(Date date) {
        this.creationDate = date;
    }

    public void setEnginePower(Float power) {
        if (power > 0) {
            this.enginePower = power;
        } else {
            System.out.println("Мощность двигателя не может быть отрицательной");
        }
    }

    public void setType(VehicleType type) { this.type = type; }
    public void setDistanceTravelled(float distanceTravelled) { this.distanceTravelled = distanceTravelled; }
    public void setFuelType(FuelType fuel) { this.fuelType = fuel; }
    public void setOwnerLogin(String ownerLogin) { this.ownerLogin = ownerLogin; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}