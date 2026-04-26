package services;

import database.BedDAO;
import models.Bed;
import models.Patient;

import java.sql.SQLException;
import java.util.List;

public class BedService {
    private final BedDAO bedDAO = new BedDAO();
    private static final int TOTAL_BEDS = 10;

    public BedService() {
        try {
            bedDAO.initializeBeds();
        } catch (SQLException e) {
            System.err.println("Failed to initialize beds: " + e.getMessage());
        }
    }

    public Bed assignBed(Patient patient) throws SQLException {
        List<Bed> available = bedDAO.getAvailableBeds();
        if (available.isEmpty()) {
            throw new IllegalStateException("No beds available.");
        }
        Bed bed = available.get(0);
        bedDAO.assignBed(bed.getBedId(), patient.getId());
        bed.setOccupied(true);
        bed.setPatientId(patient.getId());
        return bed;
    }

    public void releaseBed(int bedId) throws SQLException {
        bedDAO.freeBed(bedId);
    }

    public List<Bed> getAvailableBeds() throws SQLException {
        return bedDAO.getAvailableBeds();
    }

    public int getTotalBeds() {
        return TOTAL_BEDS;
    }
}
