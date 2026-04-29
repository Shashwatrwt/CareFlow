package database;

import models.Bed;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BedDAO {

    public List<Bed> getAvailableBeds() throws SQLException {
        List<Bed> availableBeds = new ArrayList<>();
        String sql = "SELECT bed_id, is_occupied, patient_id FROM beds WHERE is_occupied = FALSE ORDER BY bed_id";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Bed b = new Bed(
                    rs.getInt("bed_id"),
                    rs.getBoolean("is_occupied"),
                    rs.getInt("patient_id")
                );
                availableBeds.add(b);
            }
        }
        
        return availableBeds;
    }

    public List<Bed> getAllBeds() throws SQLException {
        List<Bed> allBeds = new ArrayList<>();
        String sql = "SELECT bed_id, is_occupied, patient_id FROM beds ORDER BY bed_id";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Bed b = new Bed(
                    rs.getInt("bed_id"),
                    rs.getBoolean("is_occupied"),
                    rs.getInt("patient_id")
                );
                allBeds.add(b);
            }
        }
        
        return allBeds;
    }

    public void assignBed(int bedId, int patientId) throws SQLException {
        String sql = "UPDATE beds SET is_occupied = TRUE, patient_id = ?, updated_at = CURRENT_TIMESTAMP WHERE bed_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, patientId);
            stmt.setInt(2, bedId);
            stmt.executeUpdate();
        }
    }

    public void freeBed(int bedId) throws SQLException {
        String sql = "UPDATE beds SET is_occupied = FALSE, patient_id = -1, updated_at = CURRENT_TIMESTAMP WHERE bed_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bedId);
            stmt.executeUpdate();
        }
    }

    public void initializeBeds() throws SQLException {
        String checkSql = "SELECT COUNT(*) as count FROM beds";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            
            if (rs.next() && rs.getInt("count") > 0) {
                return;
            }
        }
        
        System.out.println("ℹ Beds table exists. Initialize via SQL script if not already done.");
    }

    public Bed getBedById(int bedId) throws SQLException {
        String sql = "SELECT bed_id, is_occupied, patient_id FROM beds WHERE bed_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bedId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Bed(
                        rs.getInt("bed_id"),
                        rs.getBoolean("is_occupied"),
                        rs.getInt("patient_id")
                    );
                }
            }
        }
        
        return null;
    }
}
