package server.database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
    public double getBalance(String login) {
        String sql = "SELECT balance FROM users WHERE login = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("balance") : 0.0;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения баланса: " + e.getMessage());
            return 0.0;
        }
    }

    public boolean updateBalance(String login, double amount) {
        String sql = "UPDATE users SET balance = balance + ? WHERE login = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, login);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Ошибка обновления баланса: " + e.getMessage());
            return false;
        }
    }

    public boolean exists(String login) {
        String sql = "SELECT 1 FROM users WHERE login = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updatePrice(long id, double price, String ownerLogin) {
        String sql = "UPDATE vehicles SET price = ? WHERE id = ? AND owner_login = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, price);
            ps.setLong(2, id);
            ps.setString(3, ownerLogin);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Ошибка обновления цены: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserColor(String login, String color) {
        String sql = "UPDATE users SET color = ? WHERE login = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, color);
            ps.setString(2, login);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Ошибка обновления цвета: " + e.getMessage());
            return false;
        }
    }

    public String getUserColor(String login) {
        String sql = "SELECT color FROM users WHERE login = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("color") : null;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения цвета: " + e.getMessage());
            return null;
        }
    }
}