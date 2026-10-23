package com.fastfood.dao;

import com.fastfood.model.Role;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Makes sure every table the application needs exists before the CLI starts.
 */
public final class DatabaseInitializer {
    private DatabaseInitializer() {}

    public static void initialize() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createUsersTableSql());
            verifyUsersTableIsCurrent(statement);
        }
    }

    private static String createUsersTableSql() {
        String allowedRoles = Arrays.stream(Role.values())
                .map(role -> "'" + role.name() + "'")
                .collect(Collectors.joining(", "));
        return "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL, " +
                "roles TEXT NOT NULL CHECK (roles IN (" + allowedRoles + ")), " +
                "status TEXT NOT NULL DEFAULT 'PENDING')";
    }

    /**
     * CREATE TABLE IF NOT EXISTS leaves an old table untouched. If a fastfood.db from
     * the earlier TEACHER/STUDENT version is still around, registration for the new
     * roles would fail with a confusing CHECK error, so fail early with a clear message.
     */
    private static void verifyUsersTableIsCurrent(Statement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery(
                "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'users'")) {
            if (result.next()) {
                String definition = result.getString("sql");
                for (Role role : Role.values()) {
                    if (definition == null || !definition.contains("'" + role.name() + "'")) {
                        throw new SQLException("The existing 'users' table was created for an older " +
                                "version and does not support the role " + role.name() +
                                ". Delete fastfood.db and run the application again.");
                    }
                }
            }
        }
    }
}
