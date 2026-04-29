package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class PatientHistory {
    private int id;
    private int patientId;
    private String patientName;
    private LocalDateTime dischargedAt;
    private int stayDuration;
    private String finalStatus;

    public PatientHistory() {}

    public PatientHistory(int id, int patientId, String patientName, LocalDateTime dischargedAt, int stayDuration, String finalStatus) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.dischargedAt = dischargedAt;
        this.stayDuration = stayDuration;
        this.finalStatus = finalStatus;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public LocalDateTime getDischargedAt() { return dischargedAt; }
    public void setDischargedAt(LocalDateTime dischargedAt) { this.dischargedAt = dischargedAt; }

    public int getStayDuration() { return stayDuration; }
    public void setStayDuration(int stayDuration) { this.stayDuration = stayDuration; }

    public String getFinalStatus() { return finalStatus; }
    public void setFinalStatus(String finalStatus) { this.finalStatus = finalStatus; }

    @Override
    public String toString() {
        return String.format("History{id=%d, patientId=%d, name='%s', discharged=%s, duration=%d hrs, status='%s'}",
                id, patientId, patientName, 
                dischargedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                stayDuration, finalStatus);
    }
}
