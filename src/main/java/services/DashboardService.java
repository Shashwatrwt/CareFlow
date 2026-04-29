package services;

import models.DashboardStats;
import models.Patient;
import models.Bed;
import utils.Constants;
import java.util.List;

public class DashboardService {

    public static DashboardStats computeStats(List<Patient> allPatients, List<Bed> allBeds, int dischargedToday) {
        int activePatients = 0;
        int criticalCases = 0;
        int occupiedBeds = 0;
        int availableBeds = 0;

        for (Patient patient : allPatients) {
            if (!Constants.STATUS_DISCHARGED.equals(patient.getStatus())) {
                activePatients++;
            }

            if (Constants.SEVERITY_CRITICAL.equalsIgnoreCase(patient.getSeverity())
                && !Constants.STATUS_DISCHARGED.equals(patient.getStatus())) {
                criticalCases++;
            }
        }

        for (Bed bed : allBeds) {
            if (bed.isOccupied()) {
                occupiedBeds++;
            } else {
                availableBeds++;
            }
        }

        return new DashboardStats(activePatients, occupiedBeds, availableBeds, criticalCases, dischargedToday);
    }
}
