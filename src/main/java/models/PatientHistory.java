package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents discharged patient history.
 */
public class PatientHistory {
    private int id;
    private String name;
    private int age;
    private String severity;
    private int bedId;
    private LocalDateTime dischargedAt;

    public PatientHistory() {}

    public PatientHistory(int id, String name, int age, String severity, int bedId, LocalDateTime dischargedAt) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.severity = severity;
        this.bedId = bedId;
        this.dischargedAt = dischargedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public int getBedId() { return bedId; }
    public void setBedId(int bedId) { this.bedId = bedId; }

    public LocalDateTime getDischargedAt() { return dischargedAt; }
    public void setDischargedAt(LocalDateTime dischargedAt) { this.dischargedAt = dischargedAt; }

    @Override
    public String toString() {
        return String.format("History{id=%d, name='%s', age=%d, severity='%s', bedId=%d, discharged=%s}",
                id, name, age, severity, bedId,
                dischargedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }
}
