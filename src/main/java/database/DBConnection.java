package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {
    private static final String URL = "jdbc:postgresql://db.ckkugkqmjyxpsibhaqis.supabase.co:5432/postgres?sslmode=require";
    private static final String USER = "postgres";
    private static final String PASSWORD = "mohit@pbl2026";

    public static Connection getConnection() throws SQLException {
        try {
        Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL driver not found. Ensure postgresql dependency is in pom.xml", e);
        }
        
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✓ Successfully connected to Supabase database!");
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to connect to Supabase database:");
            System.err.println("  Error: " + e.getMessage());
            System.err.println("  URL: " + URL);
            System.err.println("  User: " + USER);
            e.printStackTrace();
        }
    }
}
