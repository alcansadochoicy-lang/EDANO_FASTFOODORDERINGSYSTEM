package com.fastfood.ui;

import com.fastfood.dao.UserDao;
import com.fastfood.model.User;
import java.util.Scanner;

public class CashierMenu extends RoleMenu {
    public CashierMenu(UserDao userDao, Scanner scanner, User user) {
        super(userDao, scanner, user);
    }

    @Override
    protected String title() {
        return "Cashier Menu";
    }
}
