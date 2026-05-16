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
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users(login, password_md2) VALUES (?, ?)")) {
            ps.setString(1, login);
            ps.setString(2, hash);
            return ps.executeUpdate() == 1;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // логин уже занят
        } catch (SQLException e) {
            System.err.println("Ошибка регистрации: " + e.getMessage());
            return false;
        }
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