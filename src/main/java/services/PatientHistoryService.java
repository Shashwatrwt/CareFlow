package services;

import database.PatientHistoryDAO;
import models.Patient;
import models.PatientHistory;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class PatientHistoryService {
    private final PatientHistoryDAO historyDAO = new PatientHistoryDAO();

    public void addDischargedPatient(Patient patient) throws SQLException {
        PatientHistory record = new PatientHistory(
            patient.getId(),
            patient.getId(),
            patient.getName(),
            LocalDateTime.now(),
            24,
            "DISCHARGED"
        );
        historyDAO.addHistory(record);
    }

    public List<PatientHistory> getAllHistory() throws SQLException {
        try {
            return historyDAO.getAllHistory();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public List<PatientHistory> getTodayDischarges() throws SQLException {
        try {
            return historyDAO.getTodayDischarges();
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }
    public int getTodayDischargeCount() throws SQLException {
        try {
            return getTodayDischarges().size();
        } catch (SQLException e) {
            return 0;
        }
    }

    public List<PatientHistory> getHistoryByDate(LocalDateTime start, LocalDateTime end) throws SQLException {
        return historyDAO.getHistoryByDate(start, end);
    }
}
