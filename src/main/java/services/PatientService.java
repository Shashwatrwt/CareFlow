package services;

import database.PatientDAO;
import models.Patient;
import utils.Constants;

import java.sql.SQLException;
import java.util.*;

public class PatientService {
    private final PatientDAO patientDAO = new PatientDAO();
    private final Set<String> patientNames = new HashSet<>();

    public void addPatient(Patient patient) throws SQLException {
        if (patientNames.contains(patient.getName())) {
            throw new IllegalArgumentException("Patient with name '" + patient.getName() + "' already exists.");
        }

        patient.setStatus(Constants.STATUS_WAITING);
        patient.setBedId(-1);

        patientDAO.addPatient(patient);

        patientNames.add(patient.getName());
    }

    public List<Patient> getAllPatients() throws SQLException {
        try {
            return patientDAO.getAllPatients();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Patient> getWaitingPatients() throws SQLException {
        List<Patient> waiting = new ArrayList<>();

        List<Patient> allPatients = patientDAO.getAllPatients();

        for (Patient patient : allPatients) {
            if (Constants.STATUS_WAITING.equalsIgnoreCase(patient.getStatus())) {
                waiting.add(patient);
            }
        }

        Collections.sort(waiting);

        return waiting;
    }

    public void updatePatientStatus(int patientId, String status) throws SQLException {
        patientDAO.updateStatus(patientId, status);
    }
    public void assignBedToPatient(int patientId, int bedId) throws SQLException {
        patientDAO.updateBedId(patientId, bedId);
    }
}
