package services;

import database.PatientHistoryDAO;
import models.Patient;
import models.PatientHistory;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing patient discharge history.
 */
public class PatientHistoryService {
    private final PatientHistoryDAO historyDAO = new PatientHistoryDAO();

    public void addDischargedPatient(Patient patient) {
        PatientHistory record = new PatientHistory(
            patient.getId(),
            patient.getName(),
            patient.getAge(),
            patient.getSeverity(),
            patient.getBedId(),
            LocalDateTime.now()
        );
        historyDAO.addHistory(record);
    }

    public List<PatientHistory> getAllHistory() {
        return historyDAO.getAllHistory();
    }

    public List<PatientHistory> getTodayDischarges() {
        return historyDAO.getTodayDischarges();
    }

    public int getTodayDischargeCount() {
        return getTodayDischarges().size();
    }

    public List<PatientHistory> getHistoryByDate(LocalDateTime start, LocalDateTime end) {
        return historyDAO.getHistoryByDate(start, end);
    }
}
