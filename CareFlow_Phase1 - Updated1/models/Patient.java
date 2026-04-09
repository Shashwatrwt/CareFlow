package models;

public class Patient {
    private String name;
    private int severity; // 1 = Critical

    public Patient(String name, int severity) {
        this.name = name;
        this.severity = severity;
    }

    public String getName() { return name; }
    public int getSeverity() { return severity; }

    public String toString() {
        return name + "," + severity;
    }
}