package database;

import models.PatientHistory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Data access object for patient discharge history.
 * Persists and retrieves patient history records from Supabase database.
 */
public class PatientHistoryDAO {

    public void addHistory(PatientHistory record) throws SQLException {
        String sql = "INSERT INTO patient_history (patient_id, patient_name, discharged_at, stay_duration, final_status) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, record.getId());
            stmt.setString(2, record.getPatientName());
            stmt.setObject(3, record.getDischargedAt());
            stmt.setInt(4, record.getStayDuration());
            stmt.setString(5, record.getFinalStatus());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    record.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<PatientHistory> getAllHistory() throws SQLException {
        List<PatientHistory> historyList = new ArrayList<>();
        String sql = "SELECT id, patient_id, patient_name, discharged_at, stay_duration, final_status FROM patient_history ORDER BY discharged_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                PatientHistory ph = new PatientHistory();
                ph.setId(rs.getInt("id"));
                ph.setPatientId(rs.getInt("patient_id"));
                ph.setPatientName(rs.getString("patient_name"));
                ph.setDischargedAt(rs.getObject("discharged_at", LocalDateTime.class));
                ph.setStayDuration(rs.getInt("stay_duration"));
                ph.setFinalStatus(rs.getString("final_status"));
                historyList.add(ph);
            }
        }
        
        return historyList;
    }

    public List<PatientHistory> getHistoryByDate(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<PatientHistory> historyList = new ArrayList<>();
        String sql = "SELECT id, patient_id, patient_name, discharged_at, stay_duration, final_status FROM patient_history WHERE discharged_at BETWEEN ? AND ? ORDER BY discharged_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, startDate);
            stmt.setObject(2, endDate);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PatientHistory ph = new PatientHistory();
                    ph.setId(rs.getInt("id"));
                    ph.setPatientId(rs.getInt("patient_id"));
                    ph.setPatientName(rs.getString("patient_name"));
                    ph.setDischargedAt(rs.getObject("discharged_at", LocalDateTime.class));
                    ph.setStayDuration(rs.getInt("stay_duration"));
                    ph.setFinalStatus(rs.getString("final_status"));
                    historyList.add(ph);
                }
            }
        }
        
        return historyList;
    }

    public List<PatientHistory> getTodayDischarges() throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        return getHistoryByDate(startOfDay, endOfDay);
    }

}
