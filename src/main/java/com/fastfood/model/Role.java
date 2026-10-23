package com.fastfood.model;

/**
 * Every role a user can hold in the Fast Food Ordering System.
 * The enum name is what gets stored in the users.roles column.
 */
public enum Role {
    CUSTOMER("Customer"),
    CASHIER("Cashier"),
    KITCHEN_STAFF("Kitchen Staff"),
    MANAGER("Manager");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** Returns the matching role, or null if the stored text is not a known role. */
    public static Role fromString(String value) {
        for (Role role : values()) {
            if (role.name().equals(value)) {
                return role;
            }
        }
        return null;
    }
}
