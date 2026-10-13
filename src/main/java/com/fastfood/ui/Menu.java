package com.fastfood.ui;

import com.fastfood.dao.UserDao;
import com.fastfood.model.User;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

public class Menu {
    private final UserDao userDao;
    private final Scanner scanner = new Scanner(System.in);

    public Menu(UserDao userDao) {
        this.userDao = userDao;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\\n===== FAST FOOD ORDERING SYSTEM =====");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    running = false;
                    System.out.println("Thank you for using the system. Goodbye!");
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void register() {
        System.out.println("\\n--- Register Account ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        System.out.println("Select role: 1. TEACHER  2. STUDENT");
        System.out.print("Enter choice: ");
        String role = switch (scanner.nextLine().trim()) {
            case "1" -> "TEACHER";
            case "2" -> "STUDENT";
            default -> null;
        };
        if (role == null) {
            System.out.println("Invalid role. Registration cancelled.");
            return;
        }

        try {
            if (userDao.register(username, password, role)) {
                System.out.println("Account registered successfully. Status: PENDING");
            } else {
                System.out.println("Username already exists. Please choose another.");
            }
        } catch (SQLException e) {
            System.out.println("Could not register account: " + e.getMessage());
        }
    }

    private void login() {
        System.out.println("\\n--- Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        try {
            Optional<User> result = userDao.login(username, password);
            if (result.isEmpty()) {
                System.out.println("Invalid username or password.");
                return;
            }
            User user = result.get();
            System.out.println("\\nLogin successful! Welcome, " + user.username() + "!");
            System.out.println("Role: " + user.roles());
            System.out.println("Account status: " + user.status());
            if ("PENDING".equals(user.status())) {
                System.out.println("Your account is pending approval.");
            }
            loggedInMenu(user);
        } catch (SQLException e) {
            System.out.println("Could not log in: " + e.getMessage());
        }
    }

    private void loggedInMenu(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\\n--- User Menu ---");
            System.out.println("1. View Account");
            System.out.println("2. Logout");
            System.out.print("Choose an option: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> {
                    System.out.println("Username: " + user.username());
                    System.out.println("Role: " + user.roles());
                    System.out.println("Status: " + user.status());
                }
                case "2" -> {
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
