package database;

import models.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PatientDAO {

    public void addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (name, age, severity, status, bed_id) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, patient.getName());
            stmt.setInt(2, patient.getAge());
            stmt.setString(3, patient.getSeverity());
            stmt.setString(4, patient.getStatus());
            stmt.setInt(5, patient.getBedId());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    patient.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT id, name, age, severity, status, bed_id FROM patients ORDER BY id DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Patient p = new Patient(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("severity"),
                    rs.getString("status"),
                    rs.getInt("bed_id")
                );
                patients.add(p);
            }
        }
        
        return patients;
    }

    public void updateStatus(int patientId, String status) throws SQLException {
        String sql = "UPDATE patients SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, patientId);
            stmt.executeUpdate();
        }
    }

    public void updateBedId(int patientId, int bedId) throws SQLException {
        String sql = "UPDATE patients SET bed_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bedId);
            stmt.setInt(2, patientId);
            stmt.executeUpdate();
        }
    }

    public Patient getPatientById(int patientId) throws SQLException {
        String sql = "SELECT id, name, age, severity, status, bed_id FROM patients WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, patientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("severity"),
                        rs.getString("status"),
                        rs.getInt("bed_id")
                    );
                }
            }
        }
        
        return null;
    }
}

