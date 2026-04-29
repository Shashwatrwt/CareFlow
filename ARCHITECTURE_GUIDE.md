# CareFlow - Quick Architecture Guide

## Package Structure

### `models/` - Domain Objects
- **Patient.java** - Patient entity (now uses Severity enum)
- **Bed.java** - Bed entity  
- **PatientHistory.java** - Discharge history
- **DashboardStats.java** - Statistics DTO
- **Graph.java** - Routing graph
- **Severity.java** ⭐ NEW - Type-safe severity levels
- **PatientStatus.java** ⭐ NEW - Type-safe patient statuses

### `services/` - Business Logic
- **PatientService** - Patient CRUD and status management
- **BedService** - Bed allocation and management
- **DashboardService** - Dashboard statistics computation
- **RoutingService** - Ambulance route finding
- **UndoService** - Undo/redo functionality
- **PatientHistoryService** - Discharge history tracking

### `database/` - Data Access
- **DBConnection** - PostgreSQL connection management
- **PatientDAO** - Patient persistence
- **BedDAO** - Bed persistence
- **PatientHistoryDAO** - History persistence

### `ui/` - User Interface
- **MainApp.java** - JavaFX application entry point
- **MainController.java** - Main UI orchestration (refactored, -50% complexity)
- **HospitalMapLayer.java** ⭐ NEW - Map visualization and rendering
- **MapDataProvider.java** ⭐ NEW - Map geometry and coordinates
- **TaskExecutor.java** ⭐ NEW - Background task utility
- **SeverityStyler.java** ⭐ NEW - Severity-based styling

### `utils/` - Utilities
- **Constants.java** ⭐ NEW - Application-wide constants
- **Dijkstra.java** - Shortest path algorithm (simplified)
- **CSVExporter.java** - CSV export utility

---

## Design Patterns Used

### 1. **Enum for Type Safety**
```java
// Instead of: if ("CRITICAL".equals(severity))
// Use: if (Severity.CRITICAL == severity) 
Severity level = Severity.fromString("CRITICAL");
int priority = level.getPriority();
```

### 2. **Service Locator with Constants**
```java
// Constants.java contains all "magic strings"
Constants.STATUS_WAITING
Constants.SEVERITY_CRITICAL
Constants.HOSPITAL_LOCATION
```

### 3. **Task Executor Pattern**
```java
// Old pattern: 8+ places with Task boilerplate
Task<Result> task = new Task<>() {
    @Override protected Result call() throws Exception { ... }
};
task.setOnSucceeded(e -> ...);
task.setOnFailed(e -> ...);
new Thread(task).start();

// New pattern: Single reusable executor
new TaskExecutor(onSuccess, onFailure)
    .executeWithResult(supplier, handler);
```

### 4. **Data Provider Pattern**
```java
// MapDataProvider encapsulates all geographic data
mapData.getNodeCoordinates()  // Get all markers
mapData.getRoadSegments()     // Get interpolated paths
```

### 5. **Single Responsibility Principle**
- **MainController**: Orchestration only
- **HospitalMapLayer**: Map rendering only
- **MapDataProvider**: Coordinate management only
- **TaskExecutor**: Task execution only

---

## Common Tasks

### Register a New Endpoint
```java
// 1. Add constant (if needed)
public static final String ENDPOINT_NEW = "/api/new";

// 2. Create Service method
public class MyService {
    public void doSomething() throws SQLException { ... }
}

// 3. Call from MainController using TaskExecutor
new TaskExecutor(onSuccess, onFailure)
    .execute(() -> myService.doSomething());
```

### Add New Severity Level
```java
// 1. Add to Severity enum
public enum Severity {
    CRITICAL(1, "CRITICAL"),
    HIGH(2, "HIGH"),
    URGENT(3, "URGENT"),  // New level
    // ...
}

// 2. Add styling to Constants (if different visual)
public static final String STYLE_URGENT = "-fx-background-color: #ff00ff;";

// 3. Done! Automatically available everywhere
```

### Add New Patient Status
```java
// 1. Add to PatientStatus enum
public enum PatientStatus {
    WAITING("WAITING"),
    ADMITTED("ADMITTED"),
    RECOVERY("RECOVERY"),  // New status
    // ...
}

// 2. Add to Constants if UI reference needed
public static final String STATUS_RECOVERY = "RECOVERY";

// 3. Use throughout code with type safety
patientService.updatePatientStatus(id, Constants.STATUS_RECOVERY);
```

### Execute Background Task
```java
// Don't create Task manually - use TaskExecutor!
new TaskExecutor(
    () -> showAlert("Success"),                  // onSuccess callback
    () -> showAlert("Error occurred")            // onFailure callback
).executeWithResult(
    () -> patientService.getAllPatients(),       // Task supplier
    patients -> patientTable.setItems(patients)  // Result handler
);
```

---

## Performance Considerations

1. **Background Tasks**: All long-running operations use TaskExecutor (non-blocking)
2. **Map Rendering**: HospitalMapLayer only updates on markDirty() calls
3. **Database**: Uses connection pooling via DBConnection
4. **Severity Lookups**: Cached in enum, O(1) access

---

## Testing Strategy

### Unit Testing
```java
// Test individual services in isolation
@Test
public void testPatientRegistration() {
    PatientService service = new PatientService();
    service.addPatient(patient);
    assertEquals(1, service.getAllPatients().size());
}

// Test enums
@Test
public void testSeverityPriority() {
    assertEquals(1, Severity.CRITICAL.getPriority());
}
```

### Integration Testing
```java
// Test service interactions
@Test
public void testBedAllocation() {
    List<Patient> waiting = patientService.getWaitingPatients();
    List<Allocation> result = bedService.assignBeds(waiting);
    assertTrue(result.size() > 0);
}
```

### UI Testing
```java
// Test controller without full JavaFX context
// Use test doubles for services
MainController controller = new MainController(mockPatientService, mockBedService);
```

---

## Code Style Guidelines

1. **Constants**: Use UPPER_SNAKE_CASE, organize by feature
2. **Enums**: Use as alternatives to multiple string comparisons
3. **Tasks**: Always use TaskExecutor, never raw Task classes
4. **Exceptions**: Propagate up (let controller handle display)
5. **Comments**: Use JavaDoc for public methods, explain "why" not "what"
6. **Naming**: Use full words, avoid abbreviations except common ones

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| NullPointerException in MapDataProvider | Ensure MapDataProvider is initialized before use |
| Task not executing | Check if TaskExecutor callbacks are set correctly |
| Severity not matching | Use Severity.fromString() instead of string comparison |
| Status string typo | Use Constants.STATUS_* instead of hardcoding strings |
| Map not rendering | Call hospitalLayer.markDirty() after updates |

---

## Future Improvements

1. **Dependency Injection**: Use framework like Spring or Guice
2. **Database Abstraction**: Introduce Repository pattern
3. **Configuration**: Externalize settings to properties file
4. **Logging**: Add SLF4J logging instead of silent failures
5. **UI Framework**: Consider JavaFX FXML + SceneBuilder
6. **Testing**: Add automated test suite with JUnit + Mockito
