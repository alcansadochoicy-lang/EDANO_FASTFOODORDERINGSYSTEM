package com.fastfood.dao;

import com.fastfood.model.Role;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Makes sure every table the application needs exists before the CLI starts.
 */
public final class DatabaseInitializer {
    // Fixed SQL text: no string building. Keep the role list in sync with model.Role.
    private static final String CREATE_USERS_SQL =
            "CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "username TEXT NOT NULL UNIQUE, " +
            "password TEXT NOT NULL, " +
            "roles TEXT NOT NULL CHECK (roles IN ('CUSTOMER', 'CASHIER', 'KITCHEN', 'MANAGER', 'ADMIN')), " +
            "status TEXT NOT NULL DEFAULT 'PENDING')";

    private DatabaseInitializer() {}

    public static void initialize() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(CREATE_USERS_SQL);
            if (!usersTableSupportsAllRoles(statement)) {
                migrateUsersTable(connection);
            }
        }
        new UserDao().seedDefaultAdmin();
    }

    /** CREATE TABLE IF NOT EXISTS leaves an older table untouched, so check its CHECK list. */
    private static boolean usersTableSupportsAllRoles(Statement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery(
                "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'users'")) {
            if (!result.next()) {
                return true;
            }
            String definition = result.getString("sql");
            for (Role role : Role.values()) {
                if (definition == null || !definition.contains("'" + role.name() + "'")) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Rebuilds the users table with the current role list while keeping existing rows.
     * Runs in one transaction, so a failure leaves the original table untouched.
     */
    private static void migrateUsersTable(Connection connection) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE users RENAME TO users_old");
            statement.execute(CREATE_USERS_SQL);
            statement.execute("INSERT INTO users (id, username, password, roles, status) " +
                    "SELECT id, username, password, roles, status FROM users_old");
            statement.execute("DROP TABLE users_old");
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("Could not upgrade the existing 'users' table (" + e.getMessage() +
                    "). If it holds accounts from an older version, delete fastfood.db and run again.", e);
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }
}
