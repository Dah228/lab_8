package client.gui;

import common.Coordinates;
import common.FuelType;
import common.Vehicle;
import common.VehicleType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

            // Creation date - ДОБАВЛЕНО
            Pattern datePattern = Pattern.compile("Creation date:\\s*(.+)");
            Matcher dateMatcher = datePattern.matcher(block);
            if (dateMatcher.find()) {
                String dateStr = dateMatcher.group(1).trim();
                if (!dateStr.isEmpty() && !dateStr.equals("null")) {
                    Date creationDate = parseDate(dateStr);
                    if (creationDate != null) {
                        vehicle.setCreationDate(creationDate);
                    }
                }
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

            // Owner
            Pattern ownerPattern = Pattern.compile("Owner:\\s*(.+)");
            Matcher ownerMatcher = ownerPattern.matcher(block);
            if (ownerMatcher.find()) {
                String owner = ownerMatcher.group(1).trim();
                if (!owner.equals("null")) {
                    vehicle.setOwnerLogin(owner);
                }
            }

            if (vehicle.getId() > 0 && vehicle.getName() != null) {
                return vehicle;
            }

        } catch (Exception e) {
            System.err.println("Ошибка парсинга vehicle: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Парсит дату из строки. Поддерживает несколько форматов:
     * - "2026-05-22" (из БД)
     * - "2026-05-22 14:30:00" (с временем)
     * - "Fri May 23 13:05:00 MSK 2026" (стандартный Java формат)
     */
    private static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        // Пробуем разные форматы
        String[] formats = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd",
                "EEE MMM dd HH:mm:ss zzz yyyy",
                "dd.MM.yyyy HH:mm",
                "dd.MM.yyyy"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                return sdf.parse(dateStr);
            } catch (ParseException e) {
                // Пробуем следующий формат
            }
        }

        System.err.println("Не удалось распарсить дату: " + dateStr);
        return null;
    }
}