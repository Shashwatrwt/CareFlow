package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://db.<HOST>.supabase.co:5432/postgres?sslmode=require";
    private static final String USER = "postgres";
    private static final String PASSWORD = "<PASSWORD>";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
