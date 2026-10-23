package com.fastfood.dao;

/**
 * Thrown by DAO classes when a database operation fails, so the UI layer
 * never has to import or handle java.sql types.
 */
public class DaoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
