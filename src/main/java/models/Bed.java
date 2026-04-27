package models;

public class Bed {
    private int bedId;
    private boolean isOccupied;
    private int patientId;

    public Bed() {}

    public Bed(int bedId, boolean isOccupied, int patientId) {
        this.bedId = bedId;
        this.isOccupied = isOccupied;
        this.patientId = patientId;
    }

    public int getBedId() { return bedId; }
    public void setBedId(int bedId) { this.bedId = bedId; }

    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    @Override
    public String toString() {
        return "Bed{bedId=" + bedId + ", isOccupied=" + isOccupied + ", patientId=" + patientId + '}';
    }
}
