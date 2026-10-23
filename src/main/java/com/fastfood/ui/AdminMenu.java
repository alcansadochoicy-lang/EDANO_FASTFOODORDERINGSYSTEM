package com.fastfood.ui;

import com.fastfood.dao.DaoException;
import com.fastfood.dao.UserDao;
import com.fastfood.model.User;
import java.util.List;
import java.util.Scanner;

/** Admin console: view accounts and approve pending registrations. */
public class AdminMenu extends RoleMenu {
    private static final String ROW_FORMAT = "| %-5s | %-15s | %-12s | %-10s |%n";
    private static final String BORDER = "+-------+-----------------+--------------+------------+";

    public AdminMenu(UserDao userDao, Scanner scanner, User user) {
        super(userDao, scanner, user);
    }

    @Override
    protected String title() {
        return "Admin Menu";
    }

    @Override
    public void start() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- " + title() + " ---");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> viewAllUsers();
                    case "2" -> viewPendingUsers();
                    case "3" -> approveUser();
                    case "4" -> {
                        loggedIn = false;
                        System.out.println("Logged out successfully.");
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (DaoException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- All System Users ---");
        printUserTable(userDao.getAllUsers());
    }

    private void viewPendingUsers() {
        System.out.println("\n--- Pending Registrations ---");
        printUserTable(userDao.getPendingUsers());
    }

    private void approveUser() {
        System.out.println("\n--- Approve User Account ---");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No pending accounts to approve.");
            return;
        }
        printUserTable(pending);

        while (true) {
            System.out.print("Enter User ID to approve (0 to cancel): ");
            String input = scanner.nextLine().trim();
            int id;
            try {
                id = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }
            if (id == 0) {
                System.out.println("Approval cancelled.");
                return;
            }
            User target = pending.stream().filter(u -> u.id() == id).findFirst().orElse(null);
            if (target == null) {
                System.out.println("User ID " + id + " is not in the pending list. Try again.");
                continue;
            }
            if (userDao.approveUser(id)) {
                System.out.println("Success! Account '" + target.username() + "' (ID " + id
                        + ", " + target.roles() + ") is now APPROVED.");
            } else {
                System.out.println("Approval failed. The account no longer exists.");
            }
            return;
        }
    }

    /** Renders any list of users as an aligned, bordered table. */
    private static void printUserTable(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No users to display.");
            return;
        }
        System.out.println(BORDER);
        System.out.printf(ROW_FORMAT, "ID", "Username", "Role", "Status");
        System.out.println(BORDER);
        for (User u : users) {
            System.out.printf(ROW_FORMAT, u.id(), truncate(u.username(), 15), u.roles(), u.status());
        }
        System.out.println(BORDER);
        System.out.println(users.size() + " user(s)");
    }

    private static String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}
