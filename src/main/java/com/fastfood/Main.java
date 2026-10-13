package com.fastfood;

import com.fastfood.dao.DatabaseConnection;
import com.fastfood.dao.UserDao;
import com.fastfood.ui.Menu;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseConnection.initializeDatabase();
            new Menu(new UserDao()).start();
        } catch (Exception e) {
            System.err.println("Application could not start: " + e.getMessage());
        }
    }
}
