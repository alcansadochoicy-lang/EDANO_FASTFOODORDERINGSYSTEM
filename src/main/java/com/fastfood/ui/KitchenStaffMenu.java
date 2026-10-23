package com.fastfood.ui;

import com.fastfood.dao.UserDao;
import com.fastfood.model.User;
import java.util.Scanner;

public class KitchenStaffMenu extends RoleMenu {
    public KitchenStaffMenu(UserDao userDao, Scanner scanner, User user) {
        super(userDao, scanner, user);
    }

    @Override
    protected String title() {
        return "Kitchen Staff Menu";
    }
}
