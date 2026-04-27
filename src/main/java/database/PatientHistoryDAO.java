package database;

import models.PatientHistory;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data access object for patient discharge history.
 */
public class PatientHistoryDAO {
    private final List<PatientHistory> history = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public void addHistory(PatientHistory record) {
        record.setId(nextId.getAndIncrement());
        synchronized (history) {
            history.add(record);
        }
    }

    public List<PatientHistory> getAllHistory() {
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }

    public List<PatientHistory> getHistoryByDate(LocalDateTime startDate, LocalDateTime endDate) {
        synchronized (history) {
            return history.stream()
                .filter(h -> !h.getDischargedAt().isBefore(startDate) && !h.getDischargedAt().isAfter(endDate))
                .toList();
        }
    }

    public List<PatientHistory> getTodayDischarges() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        return getHistoryByDate(startOfDay, endOfDay);
    }

}
