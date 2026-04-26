package database;

import models.Bed;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BedDAO {

    private final List<Bed> beds = new ArrayList<>();
    private boolean initialized = false;

    public List<Bed> getAllBeds() throws SQLException {
        synchronized (beds) {
            return new ArrayList<>(beds);
        }
    }

    public List<Bed> getAvailableBeds() throws SQLException {
        List<Bed> available = new ArrayList<>();
        synchronized (beds) {
            for (Bed b : beds) {
                if (!b.isOccupied()) {
                    available.add(b);
                }
            }
        }
        return available;
    }

    public void assignBed(int bedId, int patientId) throws SQLException {
        synchronized (beds) {
            for (Bed b : beds) {
                if (b.getBedId() == bedId) {
                    b.setOccupied(true);
                    b.setPatientId(patientId);
                    break;
                }
            }
        }
    }

    public void freeBed(int bedId) throws SQLException {
        synchronized (beds) {
            for (Bed b : beds) {
                if (b.getBedId() == bedId) {
                    b.setOccupied(false);
                    b.setPatientId(-1);
                    break;
                }
            }
        }
    }

    public void initializeBeds() throws SQLException {
        synchronized (beds) {
            if (!initialized) {
                beds.clear();
                for (int i = 1; i <= 10; i++) {
                    beds.add(new Bed(i, false, -1));
                }
                initialized = true;
            }
        }
    }

    @SuppressWarnings("unused")
    private Bed mapBed(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Bed(
                rs.getInt("bed_id"),
                rs.getBoolean("is_occupied"),
                rs.getInt("patient_id")
        );
    }
}

