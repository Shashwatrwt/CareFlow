package utils;

import models.Patient;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class CSVExporter {
    
    public static void exportPatients(String filePath, List<Patient> patients) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("ID,Name,Age,Severity,Status,Bed ID\n");
            for (Patient p : patients) {
                writer.append(String.valueOf(p.getId()))
                      .append(",").append(escapeCsv(p.getName()))
                      .append(",").append(String.valueOf(p.getAge()))
                      .append(",").append(p.getSeverity())
                      .append(",").append(p.getStatus())
                      .append(",").append(p.getBedId() <= 0 ? "-" : String.valueOf(p.getBedId()))
                      .append("\n");
            }
        }
    }

    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

