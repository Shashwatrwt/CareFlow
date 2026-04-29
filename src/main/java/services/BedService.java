package services;

import database.BedDAO;
import models.Bed;
import models.Patient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class BedService {
    // The database access object that talks to the database
    private final BedDAO bedDAO = new BedDAO();

    public BedService() {
        try {
            bedDAO.initializeBeds();
        } catch (SQLException e) {
        }
    }

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
        try {
            return bedDAO.getAvailableBeds();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
