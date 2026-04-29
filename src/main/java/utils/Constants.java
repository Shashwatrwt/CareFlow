package utils;


public class Constants {
    public static final String STATUS_WAITING = "WAITING";
    public static final String STATUS_ADMITTED = "ADMITTED";
    public static final String STATUS_DISCHARGED = "DISCHARGED";

    public static final String SEVERITY_CRITICAL = "CRITICAL";
    public static final String SEVERITY_HIGH = "HIGH";
    public static final String SEVERITY_MEDIUM = "MEDIUM";
    public static final String SEVERITY_LOW = "LOW";

    // UI constants
    public static final String HOSPITAL_LOCATION = "Careflow City Hospital";
    public static final double MAP_CENTER_LAT = 30.3256;
    public static final double MAP_CENTER_LON = 78.0437;
    public static final double MAP_ZOOM = 12.5;

    // Severity styling
    public static final String STYLE_CRITICAL = "-fx-background-color: #ffcccc; -fx-text-fill: #cc0000; -fx-font-weight: bold;";
    public static final String STYLE_HIGH = "-fx-background-color: #ffe6cc; -fx-text-fill: #ff6600; -fx-font-weight: bold;";
    public static final String STYLE_MEDIUM = "-fx-background-color: #ffffcc; -fx-text-fill: #ffaa00;";
    public static final String STYLE_LOW = "-fx-background-color: #ccffcc; -fx-text-fill: #00aa00;";

    private Constants() {} // Prevent instantiation
}
