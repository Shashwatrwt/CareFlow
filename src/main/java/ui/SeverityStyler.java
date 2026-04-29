package ui;

import models.Severity;
import utils.Constants;


public class SeverityStyler {
    
    public static String getStyle(String severity) {
        Severity level = Severity.fromString(severity);
        return getStyle(level);
    }

    public static String getStyle(Severity severity) {
        return switch (severity) {
            case CRITICAL -> Constants.STYLE_CRITICAL;
            case HIGH -> Constants.STYLE_HIGH;
            case MEDIUM -> Constants.STYLE_MEDIUM;
            case LOW -> Constants.STYLE_LOW;
        };
    }

    private SeverityStyler() {}
}
