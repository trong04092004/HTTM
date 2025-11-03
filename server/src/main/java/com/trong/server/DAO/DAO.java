package com.trong.server.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAO {
    protected Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/httm";
    private static final String USER = "trong";
    private static final String PASS = "Trong2004@";


    public DAO() {
        this.connection = getConnection();
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

