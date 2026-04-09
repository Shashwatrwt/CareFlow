import models.*;
import utils.FileHandler;
import java.util.*;

public class CareFlowSystem {

    private Hospital hospital;
    private List<Patient> allPatients;

    public CareFlowSystem() {
        hospital = new Hospital(5);
        allPatients = FileHandler.load();

        for (Patient p : allPatients) {
            hospital.registerPatient(p);
        }
    }

    public void start() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n==== CAREFLOW (FINAL) ====");
            System.out.println("1. Register Patient");
            System.out.println("2. Process & Allocate Beds");
            System.out.println("3. View Beds");
            System.out.println("4. Search Patient");
            System.out.println("5. Show All (Sorted)");
            System.out.println("6. Statistics");
            System.out.println("7. Discharge Patient");
            System.out.println("8. Exit");
            System.out.print("Enter choice: ");

            int choice = safeInt(sc);
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();

                    if (isDuplicate(name)) {
                        System.out.println("Patient with this name already exists!");
                        break;
                    }

                    System.out.println("Severity: 1=Critical, 2=Serious, 3=Normal");
                    int severity = safeInt(sc);
                    sc.nextLine();

                    Patient p = new Patient(name, severity);

                    if (severity == 1) {
                        hospital.addToPriority(p);
                    } else {
                        hospital.registerPatient(p);
                    }

                    allPatients.add(p);
                    System.out.println("Patient registered.");
                    break;

                case 2:
                    hospital.processPatients();
                    hospital.allocateBeds();
                    break;

                case 3:
                    hospital.showBeds();
                    break;

                case 4:
                    System.out.print("Enter name: ");
                    hospital.searchPatient(sc.nextLine());
                    break;

                case 5:
                    hospital.showSortedPatients(allPatients);
                    break;

                case 6:
                    hospital.showStats(allPatients.size());
                    break;

                case 7:
                    System.out.print("Enter name to discharge: ");
                    hospital.dischargePatient(sc.nextLine(), allPatients);
                    break;

                case 8:
                    FileHandler.save(allPatients);
                    System.out.println("Saved. Exiting...");
                    return;

                default:
                    System.out.println("Invalid choice");
            }
        }
    }

    private boolean isDuplicate(String name) {
        for (Patient p : allPatients) {
            if (p.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private int safeInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            sc.next();
            System.out.print("Enter valid number: ");
        }
        return sc.nextInt();
    }
}