package utils;

import models.Patient;
import java.io.*;
import java.util.*;

public class FileHandler {

    private static final String FILE = "data/patients.txt";

    public static void save(List<Patient> patients) {
        try {
            new File("data").mkdirs();
            PrintWriter pw = new PrintWriter(FILE);

            for (Patient p : patients) {
                pw.println(p.toString());
            }

            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Patient> load() {
        List<Patient> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    list.add(new Patient(parts[0], Integer.parseInt(parts[1])));
                }
            }
        } catch (Exception e) {
            System.out.println("No previous data.");
        }

        return list;
    }
}