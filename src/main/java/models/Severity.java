package models;


public enum Severity {
    CRITICAL(1, "CRITICAL"),
    HIGH(2, "HIGH"),
    MEDIUM(3, "MEDIUM"),
    LOW(4, "LOW");

    private final int priority;
    private final String displayName;

    Severity(int priority, String displayName) {
        this.priority = priority;
        this.displayName = displayName;
    }

    public int getPriority() {
        return priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Severity fromString(String value) {
        if (value == null) return LOW;
        for (Severity s : Severity.values()) {
            if (s.displayName.equalsIgnoreCase(value)) {
                return s;
            }
        }
        return LOW;
    }
}
