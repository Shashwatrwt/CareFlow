package services;

import database.PatientDAO;
import models.Patient;

import java.sql.SQLException;
import java.util.*;

/**
 * Service for managing patient arrivals and priority-based bed allocation.
 * Critical patients are prioritized first (DSA: Priority Queue implementation).
 */
public class PatientService {
    private final PatientDAO patientDAO = new PatientDAO();
    private final Set<String> patientNames = new HashSet<>();

    public void addPatient(Patient patient) throws SQLException {
        if (patientNames.contains(patient.getName())) {
            throw new IllegalArgumentException("Patient with name '" + patient.getName() + "' already exists.");
        }
        patient.setStatus("WAITING");
        patient.setBedId(-1);
        patientDAO.addPatient(patient);
        patientNames.add(patient.getName());
    }

    public List<Patient> getAllPatients() throws SQLException {
        return patientDAO.getAllPatients();
    }

    public List<Patient> getWaitingPatients() throws SQLException {
        return patientDAO.getAllPatients().stream()
            .filter(p -> "WAITING".equalsIgnoreCase(p.getStatus()))
            .sorted()
            .toList();
    }

    public void updatePatientStatus(int patientId, String status) throws SQLException {
        patientDAO.updateStatus(patientId, status);
    }

    public void assignBedToPatient(int patientId, int bedId) throws SQLException {
        patientDAO.updateBedId(patientId, bedId);
    }
}
