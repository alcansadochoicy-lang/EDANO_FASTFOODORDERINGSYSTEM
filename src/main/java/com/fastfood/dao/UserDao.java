package com.fastfood.dao;

import com.fastfood.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    /**
     * Registers a new account with PENDING status.
     * @return true if created, false if the username is already taken
     * @throws DaoException on any other database failure
     */
    public boolean register(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password, roles, status) VALUES (?, ?, ?, 'PENDING')";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, hashPassword(password));
            statement.setString(3, role);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                return false;
            }
            throw new DaoException("Could not register account: " + e.getMessage(), e);
        }
    }

    public Optional<User> login(String username, String password) {
        String sql = "SELECT id, username, password, roles, status FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next() && hashPassword(password).equals(result.getString("password"))) {
                    return Optional.of(mapUser(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Could not log in: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /** Reloads a user so menus can show the latest status (e.g. after approval). */
    public Optional<User> findById(int id) {
        String sql = "SELECT id, username, password, roles, status FROM users WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapUser(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Could not load account: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /** Returns every registered account, regardless of role or status. */
    public List<User> getAllUsers() {
        String sql = "SELECT id, username, password, roles, status FROM users ORDER BY id";
        return queryUsers(sql, null);
    }

    /** Returns every account still awaiting approval, across all roles. */
    public List<User> getPendingUsers() {
        String sql = "SELECT id, username, password, roles, status FROM users WHERE status = ? ORDER BY id";
        return queryUsers(sql, "PENDING");
    }

    /**
     * Changes an account's status to APPROVED.
     * @return true if a row was updated, false if no user has that id
     */
    public boolean approveUser(int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Could not approve account: " + e.getMessage(), e);
        }
    }

    /** Creates the default admin (admin / admin123, already APPROVED) if it does not exist yet. */
    public void seedDefaultAdmin() {
        String sql = "INSERT OR IGNORE INTO users (username, password, roles, status) VALUES (?, ?, ?, 'APPROVED')";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "admin");
            statement.setString(2, hashPassword("admin123"));
            statement.setString(3, "ADMIN");
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Could not create default admin: " + e.getMessage(), e);
        }
    }

    private List<User> queryUsers(String sql, String statusParam) {
        List<User> users = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (statusParam != null) {
                statement.setString(1, statusParam);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    users.add(mapUser(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Could not load users: " + e.getMessage(), e);
        }
        return users;
    }

    private User mapUser(ResultSet result) throws SQLException {
        return new User(
                result.getInt("id"),
                result.getString("username"),
                result.getString("password"),
                result.getString("roles"),
                result.getString("status"));
    }

    private String hashPassword(String password) {
        try {
            byte[] hashed = MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashed) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Password hashing is unavailable.", e);
        }
    }
}
