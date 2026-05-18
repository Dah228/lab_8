package client.gui;

import common.Coordinates;
import common.FuelType;
import common.Vehicle;
import common.VehicleType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VehicleTextParser {

    public static List<Vehicle> parseVehicleList(String text) {
        List<Vehicle> vehicles = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return vehicles;
        }
        String[] blocks = text.split("-{24,}");
        for (String block : blocks) {
            Vehicle vehicle = parseVehicle(block);
            if (vehicle != null) {
                vehicles.add(vehicle);
            }
        }
        return vehicles;
    }

    private static Vehicle parseVehicle(String block) {
        if (block == null || block.trim().isEmpty()) {
            return null;
        }
        Vehicle vehicle = new Vehicle();
        try {
            // ID
            Pattern idPattern = Pattern.compile("ID:\\s*(\\d+)");
            Matcher idMatcher = idPattern.matcher(block);
            if (idMatcher.find()) {
                vehicle.setId(Long.parseLong(idMatcher.group(1)));
            }

            // Name
            Pattern namePattern = Pattern.compile("Name:\\s*(.+)");
            Matcher nameMatcher = namePattern.matcher(block);
            if (nameMatcher.find()) {
                vehicle.setName(nameMatcher.group(1).trim());
            }

            // Coordinates
            Pattern coordsPattern = Pattern.compile("Coordinates:\\s*\\(([\\d.-]+),\\s*([\\d.-]+)\\)");
            Matcher coordsMatcher = coordsPattern.matcher(block);
            if (coordsMatcher.find()) {
                int x = Integer.parseInt(coordsMatcher.group(1));
                float y = Float.parseFloat(coordsMatcher.group(2));
                vehicle.setCoordinates(x, y);
            }

            // Engine power
            Pattern powerPattern = Pattern.compile("Engine power:\\s*([\\d.]+)");
            Matcher powerMatcher = powerPattern.matcher(block);
            if (powerMatcher.find()) {
                vehicle.setEnginePower(Float.parseFloat(powerMatcher.group(1)));
            }

            // Distance travelled
            Pattern distancePattern = Pattern.compile("Distance travelled:\\s*([\\d.]+)");
            Matcher distanceMatcher = distancePattern.matcher(block);
            if (distanceMatcher.find()) {
                vehicle.setDistanceTravelled(Float.parseFloat(distanceMatcher.group(1)));
            }

            // Type
            Pattern typePattern = Pattern.compile("Type:\\s*(\\w+)");
            Matcher typeMatcher = typePattern.matcher(block);
            if (typeMatcher.find()) {
                String typeStr = typeMatcher.group(1).trim();
                if (!typeStr.isEmpty() && !typeStr.equals("null")) {
                    vehicle.setType(VehicleType.valueOf(typeStr.toUpperCase()));
                }
            }

            // Fuel type
            Pattern fuelPattern = Pattern.compile("Fuel type:\\s*(\\w+)");
            Matcher fuelMatcher = fuelPattern.matcher(block);
            if (fuelMatcher.find()) {
                String fuelStr = fuelMatcher.group(1).trim();
                vehicle.setFuelType(FuelType.valueOf(fuelStr.toUpperCase()));
            }

            // Price
            Pattern pricePattern = Pattern.compile("Price:\\s*([\\d.]+)");
            Matcher priceMatcher = pricePattern.matcher(block);
            if (priceMatcher.find()) {
                vehicle.setPrice(Double.parseDouble(priceMatcher.group(1)));
            }

            // --- ДОБАВИЛИ ПАРСИНГ ВЛАДЕЛЬЦА ---
            Pattern ownerPattern = Pattern.compile("Owner:\\s*(.+)");
            Matcher ownerMatcher = ownerPattern.matcher(block);
            if (ownerMatcher.find()) {
                String owner = ownerMatcher.group(1).trim();
                if (!owner.equals("null")) {
                    vehicle.setOwnerLogin(owner);
                }
            }
            // ----------------------------------

            if (vehicle.getId() > 0 && vehicle.getName() != null) {
                return vehicle;
            }

        } catch (Exception e) {
            System.err.println("Ошибка парсинга vehicle: " + e.getMessage());
        }
        return null;
    }
}