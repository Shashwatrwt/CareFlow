package services;

import database.PatientDAO;
import models.Patient;

import java.sql.SQLException;
import java.util.*;

public class PatientService {
    private final Queue<Patient> arrivalQueue = new LinkedList<>();
    private final PriorityQueue<Patient> priorityQueue = new PriorityQueue<>();
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
        arrivalQueue.add(patient);
        priorityQueue.add(patient);
    }

    public Patient getNextPatient() {
        return priorityQueue.poll();
    }

    public List<Patient> getAllPatients() throws SQLException {
        return patientDAO.getAllPatients();
    }

    public Queue<Patient> getArrivalQueue() {
        return new LinkedList<>(arrivalQueue);
    }

    public PriorityQueue<Patient> getPriorityQueue() {
        return new PriorityQueue<>(priorityQueue);
    }

    public void updatePatientStatus(int patientId, String status) throws SQLException {
        patientDAO.updateStatus(patientId, status);
    }

    public void assignBedToPatient(int patientId, int bedId) throws SQLException {
        patientDAO.updateBedId(patientId, bedId);
    }
}
