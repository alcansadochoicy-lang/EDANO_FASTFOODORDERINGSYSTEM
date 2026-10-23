package com.fastfood;

import com.fastfood.dao.DatabaseInitializer;
import com.fastfood.dao.UserDao;
import com.fastfood.ui.Authentication;
import java.util.Scanner;

/** Single entry point: initialize the database, create shared resources, hand off to the UI. */
public class Main {
    public static void main(String[] args) {
        try {
            DatabaseInitializer.initialize();
            System.out.println("Database initialized.");
        } catch (Exception e) {
            System.err.println("Application could not start: " + e.getMessage());
            return;
        }

        UserDao userDao = new UserDao();
        try (Scanner scanner = new Scanner(System.in)) {
            new Authentication(userDao, scanner).start();
        }
    }
}
