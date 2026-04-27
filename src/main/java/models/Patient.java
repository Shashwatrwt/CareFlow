package models;

/**
 * Represents a patient in the hospital system.
 * Implements Comparable for priority-based sorting (by severity).
 */
public class Patient implements Comparable<Patient> {
    private int id;
    private String name;
    private int age;
    private String severity;
    private String status;
    private int bedId;

    public Patient() {}

    public Patient(int id, String name, int age, String severity, String status, int bedId) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.severity = severity;
        this.status = status;
        this.bedId = bedId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getBedId() { return bedId; }
    public void setBedId(int bedId) { this.bedId = bedId; }

    /** Priority 1 = Critical (highest), 5 = default (lowest) */
    private int severityPriority() {
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> 1;
            case "HIGH" -> 2;
            case "MEDIUM" -> 3;
            case "LOW" -> 4;
            default -> 5;
        };
    }

    @Override
    public int compareTo(Patient other) {
        return Integer.compare(this.severityPriority(), other.severityPriority());
    }

    @Override
    public String toString() {
        return "Patient{id=" + id + ", name='" + name + '\'' + ", age=" + age + ", severity='" + severity + '\'' + ", status='" + status + '\'' + ", bedId=" + bedId + '}';
    }
}
