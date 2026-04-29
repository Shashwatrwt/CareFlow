package models;


public class DashboardStats {
    private int totalPatients;
    private int occupiedBeds;
    private int availableBeds;
    private int criticalCases;
    private int dischargedToday;

    public DashboardStats(int totalPatients, int occupiedBeds, int availableBeds, int criticalCases, int dischargedToday) {
        this.totalPatients = totalPatients;
        this.occupiedBeds = occupiedBeds;
        this.availableBeds = availableBeds;
        this.criticalCases = criticalCases;
        this.dischargedToday = dischargedToday;
    }

    public int getTotalPatients() { return totalPatients; }
    public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }

    public int getOccupiedBeds() { return occupiedBeds; }
    public void setOccupiedBeds(int occupiedBeds) { this.occupiedBeds = occupiedBeds; }

    public int getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(int availableBeds) { this.availableBeds = availableBeds; }

    public int getCriticalCases() { return criticalCases; }
    public void setCriticalCases(int criticalCases) { this.criticalCases = criticalCases; }

    public int getDischargedToday() { return dischargedToday; }
    public void setDischargedToday(int dischargedToday) { this.dischargedToday = dischargedToday; }
}
