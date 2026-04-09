package models;

import java.util.*;

public class Hospital {

    private Queue<Patient> queue;
    private PriorityQueue<Patient> pq;
    private Patient[] beds;

    public Hospital(int bedCount) {
        queue = new LinkedList<>();
        pq = new PriorityQueue<>(Comparator.comparingInt(Patient::getSeverity));
        beds = new Patient[bedCount];
    }

    public void registerPatient(Patient p) {
        queue.add(p);
        System.out.println("Added to queue: " + p.getName());
    }

    public void addToPriority(Patient p) {
        pq.add(p);
        System.out.println("Critical patient directly added to priority queue.");
    }

    public void processPatients() {
        while (!queue.isEmpty()) {
            pq.add(queue.poll());
        }
        System.out.println("Moved to priority queue.");
    }

    public void allocateBeds() {
        for (int i = 0; i < beds.length; i++) {
            if (beds[i] == null && !pq.isEmpty()) {
                Patient p = pq.poll();
                beds[i] = p;
                System.out.println("Bed " + i + " -> " + p.getName());
            }
        }
    }

    public void showBeds() {
        for (int i = 0; i < beds.length; i++) {
            if (beds[i] == null) {
                System.out.println("Bed " + i + ": Empty");
            } else {
                System.out.println("Bed " + i + ": " + beds[i].getName());
            }
        }
    }

    public void searchPatient(String name) {

        for (Patient p : queue) {
            if (p.getName().equalsIgnoreCase(name)) {
                System.out.println("In Queue");
                return;
            }
        }

        for (Patient p : pq) {
            if (p.getName().equalsIgnoreCase(name)) {
                System.out.println("In Priority Queue");
                return;
            }
        }

        for (int i = 0; i < beds.length; i++) {
            if (beds[i] != null && beds[i].getName().equalsIgnoreCase(name)) {
                System.out.println("In Bed " + i);
                return;
            }
        }

        System.out.println("Not found");
    }

    public void showSortedPatients(List<Patient> allPatients) {
        List<Patient> list = new ArrayList<>(allPatients);
        list.sort(Comparator.comparingInt(Patient::getSeverity));

        System.out.println("--- Sorted Patients ---");
        for (Patient p : list) {
            System.out.println(p.getName() + " (" + p.getSeverity() + ")");
        }
    }

    public void showStats(int total) {
        int free = 0;

        for (Patient b : beds) {
            if (b == null) free++;
        }

        System.out.println("Total Patients: " + total);
        System.out.println("Free Beds: " + free);
    }

    public void dischargePatient(String name, List<Patient> allPatients) {
        for (int i = 0; i < beds.length; i++) {
            if (beds[i] != null && beds[i].getName().equalsIgnoreCase(name)) {

                System.out.println(name + " discharged from Bed " + i);
                beds[i] = null;

                allocateBeds(); // refill bed
                return;
            }
        }

        System.out.println("Patient not in bed.");
    }
}