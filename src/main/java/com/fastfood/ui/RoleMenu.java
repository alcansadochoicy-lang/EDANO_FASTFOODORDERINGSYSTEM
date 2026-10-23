package com.fastfood.ui;

import com.fastfood.dao.DaoException;
import com.fastfood.dao.UserDao;
import com.fastfood.model.User;
import java.util.Scanner;

/**
 * Shared behavior for every role-based menu. Subclasses supply the title and
 * will add their role-specific options in later deliverables.
 */
public abstract class RoleMenu {
    protected final UserDao userDao;
    protected final Scanner scanner;
    protected final User user;

    protected RoleMenu(UserDao userDao, Scanner scanner, User user) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.user = user;
    }

    protected abstract String title();

    public void start() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- " + title() + " ---");
            System.out.println("1. View Account");
            System.out.println("2. Logout");
            System.out.print("Choose an option: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> viewAccount();
                case "2" -> {
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void viewAccount() {
        User current = user;
        try {
            current = userDao.findById(user.id()).orElse(user);
        } catch (DaoException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Username: " + current.username());
        System.out.println("Role: " + current.roles());
        System.out.println("Status: " + current.status());
    }
}
