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

        // Разбиваем текст на блоки по разделителю
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
            // ID: 123
            Pattern idPattern = Pattern.compile("ID:\\s*(\\d+)");
            Matcher idMatcher = idPattern.matcher(block);
            if (idMatcher.find()) {
                vehicle.setId(Long.parseLong(idMatcher.group(1)));
            }

            // Name: something
            Pattern namePattern = Pattern.compile("Name:\\s*(.+)");
            Matcher nameMatcher = namePattern.matcher(block);
            if (nameMatcher.find()) {
                vehicle.setName(nameMatcher.group(1).trim());
            }

            // Coordinates: (x, y)
            Pattern coordsPattern = Pattern.compile("Coordinates:\\s*\\(([\\d.-]+),\\s*([\\d.-]+)\\)");
            Matcher coordsMatcher = coordsPattern.matcher(block);
            if (coordsMatcher.find()) {
                int x = Integer.parseInt(coordsMatcher.group(1));
                float y = Float.parseFloat(coordsMatcher.group(2));
                vehicle.setCoordinates(x, y);
            }

            // Creation date: ...
            Pattern datePattern = Pattern.compile("Creation date:\\s*(.+)");
            Matcher dateMatcher = datePattern.matcher(block);
            if (dateMatcher.find()) {
                // Можно распарсить дату если нужно
            }

            // Engine power: 123.45
            Pattern powerPattern = Pattern.compile("Engine power:\\s*([\\d.]+)");
            Matcher powerMatcher = powerPattern.matcher(block);
            if (powerMatcher.find()) {
                vehicle.setEnginePower(Float.parseFloat(powerMatcher.group(1)));
            }

            // Distance travelled: 123.45
            Pattern distancePattern = Pattern.compile("Distance travelled:\\s*([\\d.]+)");
            Matcher distanceMatcher = distancePattern.matcher(block);
            if (distanceMatcher.find()) {
                vehicle.setDistanceTravelled(Float.parseFloat(distanceMatcher.group(1)));
            }

            // Type: BOAT
            Pattern typePattern = Pattern.compile("Type:\\s*(\\w+)");
            Matcher typeMatcher = typePattern.matcher(block);
            if (typeMatcher.find()) {
                String typeStr = typeMatcher.group(1).trim();
                if (!typeStr.isEmpty() && !typeStr.equals("null")) {
                    vehicle.setType(VehicleType.valueOf(typeStr.toUpperCase()));
                }
            }

            // Fuel type: GASOLINE
            Pattern fuelPattern = Pattern.compile("Fuel type:\\s*(\\w+)");
            Matcher fuelMatcher = fuelPattern.matcher(block);
            if (fuelMatcher.find()) {
                String fuelStr = fuelMatcher.group(1).trim();
                vehicle.setFuelType(FuelType.valueOf(fuelStr.toUpperCase()));
            }

            // Price: 123.45
            Pattern pricePattern = Pattern.compile("Price:\\s*([\\d.]+)");
            Matcher priceMatcher = pricePattern.matcher(block);
            if (priceMatcher.find()) {
                vehicle.setPrice(Double.parseDouble(priceMatcher.group(1)));
            }

            // Проверяем, что обязательные поля заполнены
            if (vehicle.getId() > 0 && vehicle.getName() != null) {
                return vehicle;
            }

        } catch (Exception e) {
            System.err.println("Ошибка парсинга vehicle: " + e.getMessage());
        }

        return null;
    }
}