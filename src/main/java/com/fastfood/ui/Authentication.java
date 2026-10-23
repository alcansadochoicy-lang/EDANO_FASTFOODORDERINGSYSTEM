package com.fastfood.ui;

import com.fastfood.dao.DaoException;
import com.fastfood.dao.UserDao;
import com.fastfood.model.Role;
import com.fastfood.model.User;
import java.util.Optional;
import java.util.Scanner;

/** Handles the startup menu: registration, login, and routing to the role-based menu. */
public class Authentication {
    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== FAST FOOD ORDERING SYSTEM =====");
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
        System.out.println("\n--- Register Account ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        Role role = selectRole();
        if (role == null) {
            System.out.println("Invalid role. Registration cancelled.");
            return;
        }

        try {
            if (userDao.register(username, password, role.name())) {
                System.out.println("Account registered successfully. Status: PENDING");
            } else {
                System.out.println("Username already exists. Please choose another.");
            }
        } catch (DaoException e) {
            System.out.println(e.getMessage());
        }
    }

    private Role selectRole() {
        Role[] roles = Role.values();
        System.out.println("Select role:");
        for (int i = 0; i < roles.length; i++) {
            System.out.println((i + 1) + ". " + roles[i].name());
        }
        System.out.print("Enter choice: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            return (choice >= 1 && choice <= roles.length) ? roles[choice - 1] : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void login() {
        System.out.println("\n--- Login ---");
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
            System.out.println("\nLogin successful! Welcome, " + user.username() + "!");
            System.out.println("Role: " + user.roles());
            System.out.println("Account status: " + user.status());
            if ("PENDING".equals(user.status())) {
                System.out.println("Your account is pending approval.");
            }
            routeToMenu(user);
        } catch (DaoException e) {
            System.out.println(e.getMessage());
        }
    }

    private void routeToMenu(User user) {
        Role role = Role.fromString(user.roles());
        if (role == null) {
            System.out.println("Unknown role '" + user.roles() + "'. Please contact an administrator.");
            return;
        }
        RoleMenu menu = switch (role) {
            case CUSTOMER -> new CustomerMenu(userDao, scanner, user);
            case CASHIER -> new CashierMenu(userDao, scanner, user);
            case KITCHEN_STAFF -> new KitchenStaffMenu(userDao, scanner, user);
            case MANAGER -> new ManagerMenu(userDao, scanner, user);
        };
        menu.start();
    }
}
