package database;

import models.Patient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Access Object for Patient model.
 * Handles in-memory storage and retrieval of patient records.
 */
public class PatientDAO {

    private final List<Patient> patients = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public void addPatient(Patient patient) throws SQLException {
        patient.setId(nextId.getAndIncrement());
        synchronized (patients) {
            patients.add(patient);
        }
    }

    public List<Patient> getAllPatients() throws SQLException {
        synchronized (patients) {
            return new ArrayList<>(patients);
        }
    }

    public void updateStatus(int patientId, String status) throws SQLException {
        synchronized (patients) {
            for (Patient p : patients) {
                if (p.getId() == patientId) {
                    p.setStatus(status);
                    break;
                }
            }
        }
    }

    public void updateBedId(int patientId, int bedId) throws SQLException {
        synchronized (patients) {
            for (Patient p : patients) {
                if (p.getId() == patientId) {
                    p.setBedId(bedId);
                    break;
                }
            }
        }
    }
}

