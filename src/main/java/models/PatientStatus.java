package models;


public enum PatientStatus {
    WAITING("WAITING"),
    ADMITTED("ADMITTED"),
    DISCHARGED("DISCHARGED");

    private final String displayName;

    PatientStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PatientStatus fromString(String value) {
        if (value == null) return WAITING;
        for (PatientStatus s : PatientStatus.values()) {
            if (s.displayName.equalsIgnoreCase(value)) {
                return s;
            }
        }
        return WAITING;
    }
}
