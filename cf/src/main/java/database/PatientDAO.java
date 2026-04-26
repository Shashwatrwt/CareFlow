package database;

import models.Patient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PatientDAO {

    private final List<Patient> patients = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public void addPatient(Patient patient) throws SQLException {
        patient.setId(nextId.getAndIncrement());
        synchronized (patients) {
            patients.add(patient);
        }
    }

    public Patient getPatientByName(String name) throws SQLException {
        synchronized (patients) {
            for (Patient p : patients) {
                if (p.getName().equalsIgnoreCase(name)) {
                    return p;
                }
            }
        }
        return null;
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

    @SuppressWarnings("unused")
    private Patient mapPatient(java.sql.ResultSet rs) throws java.sql.SQLException {
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

