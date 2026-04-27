package services;

import database.BedDAO;
import models.Bed;
import models.Patient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing hospital bed allocation.
 * Provides methods to assign and release beds for patients.
 */
public class BedService {
    private final BedDAO bedDAO = new BedDAO();

    public BedService() {
        try {
            bedDAO.initializeBeds();
        } catch (SQLException e) {
            // Initialization failure handled silently
        }
    }

    /**
     * Represents a single bed allocation result.
     */
    public static class Allocation {
        private final Patient patient;
        private final Bed bed;

        public Allocation(Patient patient, Bed bed) {
            this.patient = patient;
            this.bed = bed;
        }

        public Patient getPatient() { return patient; }
        public Bed getBed() { return bed; }
    }

    /**
     * Assigns beds to multiple patients in batch.
     * Patients should already be sorted by priority (severity).
     * Returns a list of successful allocations.
     */
    public List<Allocation> assignBeds(List<Patient> patients) throws SQLException {
        List<Allocation> allocations = new ArrayList<>();
        List<Bed> available = bedDAO.getAvailableBeds();

        int limit = Math.min(patients.size(), available.size());
        for (int i = 0; i < limit; i++) {
            Patient patient = patients.get(i);
            Bed bed = available.get(i);
            bedDAO.assignBed(bed.getBedId(), patient.getId());
            allocations.add(new Allocation(patient, bed));
        }
        return allocations;
    }

    public void releaseBed(int bedId) throws SQLException {
        bedDAO.freeBed(bedId);
    }

    public List<Bed> getAvailableBeds() throws SQLException {
        return bedDAO.getAvailableBeds();
    }
}
