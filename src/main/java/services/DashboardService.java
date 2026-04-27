package services;

import models.DashboardStats;
import models.Patient;
import models.Bed;
import java.util.List;

/**
 * Service for computing dashboard statistics.
 */
public class DashboardService {
    
    public static DashboardStats computeStats(List<Patient> allPatients, List<Bed> allBeds, int dischargedToday) {
        int total = (int) allPatients.stream().filter(p -> !p.getStatus().equals("DISCHARGED")).count();
        int critical = (int) allPatients.stream()
            .filter(p -> "CRITICAL".equalsIgnoreCase(p.getSeverity()) && !p.getStatus().equals("DISCHARGED"))
            .count();
        long occupied = allBeds.stream().filter(Bed::isOccupied).count();
        long available = allBeds.stream().filter(b -> !b.isOccupied()).count();

        return new DashboardStats(total, (int) occupied, (int) available, critical, dischargedToday);
    }
}
