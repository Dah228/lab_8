package server.database;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class AuthService {

    public static String hashMD2(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD2");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD2 algorithm unavailable", e);
        }
    }

    public boolean register(String login, String password) {
        if (login == null || password == null || login.isEmpty() || password.isEmpty()) {
            return false;
        }
        String hash = hashMD2(password);
        String color = generateRandomColor(); // Генерируем случайный цвет

        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users(login, password_md2, color) VALUES (?, ?, ?)")) {
            ps.setString(1, login);
            ps.setString(2, hash);
            ps.setString(3, color);
            return ps.executeUpdate() == 1;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            System.err.println("Ошибка регистрации: " + e.getMessage());
            return false;
        }
    }

    private String generateRandomColor() {
        String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
                "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2",
                "#F8B739", "#52B788", "#E63946", "#1D3557", "#A8DADC"};
        return colors[(int)(Math.random() * colors.length)];
    }
    public boolean authenticate(String login, String password) {
        if (login == null || password == null) return false;
        String hash = hashMD2(password);
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT 1 FROM users WHERE login = ? AND password_md2 = ?")) {
            ps.setString(1, login);
            ps.setString(2, hash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка аутентификации: " + e.getMessage());
            return false;
        }
    }
}