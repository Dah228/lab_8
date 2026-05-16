package server.database;

import common.Coordinates;
import common.FuelType;
import common.Vehicle;
import common.VehicleType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDao {

    public List<Vehicle> loadAll() {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicles ORDER BY id";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки коллекции: " + e.getMessage());
        }
        return list;
    }

    public boolean insert(Vehicle v) {
        // Добавили price в список колонок и значений
        String sql = "INSERT INTO vehicles(owner_login, name, x, y, creation_date, " +
                "engine_power, distance_travelled, type, fuel_type, price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, v.getOwnerLogin());
            ps.setString(2, v.getName());
            ps.setInt(3, v.getCoordinates().getX());
            ps.setFloat(4, v.getCoordinates().getY());
            ps.setDate(5, new java.sql.Date(v.getCreationDate().getTime()));
            ps.setFloat(6, v.getEnginePower());
            ps.setFloat(7, v.getDistanceTravelled());
            ps.setString(8, v.getType() != null ? v.getType().name() : null);
            ps.setString(9, v.getFuelType().name());
            ps.setDouble(10, v.getPrice()); // ← НОВОЕ

            if (ps.executeUpdate() == 1) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        v.setId(keys.getLong(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Ошибка вставки: " + e.getMessage());
            return false;
        }
    }

    public String getOwner(long id) {
        String sql = "SELECT owner_login FROM vehicles WHERE id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("owner_login") : null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean update(long id, Vehicle v, String ownerLogin) {
        // Проверяем право на изменение
        String currentOwner = getOwner(id);
        if (currentOwner == null || !currentOwner.equals(ownerLogin)) {
            return false;
        }
        String sql = "UPDATE vehicles SET name = ?, x = ?, y = ?, creation_date = ?, " +
                "engine_power = ?, distance_travelled = ?, type = ?, fuel_type = ? " +
                "WHERE id = ? AND owner_login = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getName());
            ps.setInt(2, v.getCoordinates().getX());
            ps.setFloat(3, v.getCoordinates().getY());
            ps.setDate(4, new java.sql.Date(v.getCreationDate().getTime()));
            ps.setFloat(5, v.getEnginePower());
            ps.setFloat(6, v.getDistanceTravelled());
            ps.setString(7, v.getType() != null ? v.getType().name() : null);
            ps.setString(8, v.getFuelType().name());
            ps.setLong(9, id);
            ps.setString(10, ownerLogin);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Ошибка обновления: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(long id, String ownerLogin) {
        String sql = "DELETE FROM vehicles WHERE id = ? AND owner_login = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setString(2, ownerLogin);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Ошибка удаления: " + e.getMessage());
            return false;
        }
    }

    public void clearAll(String ownerLogin) {
        String sql = "DELETE FROM vehicles WHERE owner_login = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ownerLogin);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка очистки: " + e.getMessage());
        }
    }

    /**
     * Атомарная покупка ТС. Цена берётся из БД, баланс проверяется автоматически.
     */
    public boolean buyVehicle(long id, String buyerLogin) {
        try (Connection conn = DBManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Блокируем строку машины, читаем владельца и цену
                String owner;
                double price;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT owner_login, price FROM vehicles WHERE id = ? FOR UPDATE")) {
                    ps.setLong(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        owner = rs.getString("owner_login");
                        price = rs.getDouble("price");
                    }
                }

                if (owner.equals(buyerLogin)) {
                    conn.rollback();
                    return false; // Своя машина
                }

                // 2. Блокируем строку покупателя, проверяем баланс
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT balance FROM users WHERE login = ? FOR UPDATE")) {
                    ps.setString(1, buyerLogin);
                    try (ResultSet rs = ps.executeQuery()) {
                        double balance = rs.next() ? rs.getDouble("balance") : 0.0;
                        if (balance < price) {
                            conn.rollback();
                            return false; // Недостаточно средств
                        }
                    }
                }

                // 3. Списываем у покупателя
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE users SET balance = balance - ? WHERE login = ?")) {
                    ps.setDouble(1, price);
                    ps.setString(2, buyerLogin);
                    ps.executeUpdate();
                }

                // 4. Зачисляем продавцу
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE users SET balance = balance + ? WHERE login = ?")) {
                    ps.setDouble(1, price);
                    ps.setString(2, owner);
                    ps.executeUpdate();
                }

                // 5. Передаём право владения
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE vehicles SET owner_login = ? WHERE id = ?")) {
                    ps.setString(1, buyerLogin);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                }

                conn.commit(); // Всё успешно
                return true;
            } catch (SQLException e) {
                conn.rollback(); // Откат при любой ошибке
                throw e;
            } finally {
                conn.setAutoCommit(true); // Возвращаем стандартный режим
            }
        } catch (SQLException e) {
            System.err.println("Ошибка транзакции покупки: " + e.getMessage());
            return false;
        }
    }

    private Vehicle mapRow(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setId(rs.getLong("id"));
        v.setOwnerLogin(rs.getString("owner_login"));
        v.setName(rs.getString("name"));
        Coordinates coords = new Coordinates();
        coords.setCoord(rs.getInt("x"), rs.getFloat("y"));
        v.setCoordinatesObject(coords);
        v.setCreationDateHand(rs.getDate("creation_date"));
        v.setEnginePower(rs.getFloat("engine_power"));
        v.setDistanceTravelled(rs.getFloat("distance_travelled"));
        String typeStr = rs.getString("type");
        v.setType(typeStr != null ? VehicleType.valueOf(typeStr) : null);
        v.setFuelType(FuelType.valueOf(rs.getString("fuel_type")));
        v.setPrice(rs.getDouble("price"));
        return v;
    }
}