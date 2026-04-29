package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.gluonhq.maps.MapView;
import models.*;
import services.*;
import utils.CSVExporter;
import utils.Dijkstra;
import utils.Constants;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.*;

public class MainController {
    private final PatientService patientService = new PatientService();
    private final BedService bedService = new BedService();
    private final RoutingService routingService = new RoutingService();
    private final UndoService undoService = new UndoService();
    private final PatientHistoryService historyService = new PatientHistoryService();
    private final MapDataProvider mapData = new MapDataProvider();

    private final BorderPane root = new BorderPane();
    private final TableView<Patient> patientTable = new TableView<>();
    private final MapView mapView = new MapView();
    private final HospitalMapLayer hospitalLayer = new HospitalMapLayer();
    private Label statsLabel;

    public MainController() {
        setupTopPanel();
        setupLeftPanel();
        setupCenterPanel();
        setupInitialLoad();
    }

    private void setupTopPanel() {
        HBox topPanel = new HBox(15);
        topPanel.setPadding(new Insets(10));
        topPanel.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");

        statsLabel = new Label("Loading stats...");
        statsLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
        statsLabel.setWrapText(true);

        topPanel.getChildren().add(statsLabel);
        root.setTop(topPanel);
    }

    private void setupLeftPanel() {
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(180);

        Button registerBtn = new Button("Register Patient");
        Button allocateBtn = new Button("Allocate Bed");
        Button refreshBtn = new Button("Refresh Patients");
        Button dischargeBtn = new Button("Discharge");
        Button historyBtn = new Button("View History");
        Button exportBtn = new Button("Export CSV");
        Button dispatchBtn = new Button("Dispatch Ambulance");
        Button undoBtn = new Button("Undo");
        for (Button btn : new Button[]{registerBtn, allocateBtn, refreshBtn, dischargeBtn, 
                                       historyBtn, exportBtn, dispatchBtn, undoBtn}) {
            btn.setMaxWidth(Double.MAX_VALUE);
        }
        registerBtn.setOnAction(e -> actionRegisterPatient());
        allocateBtn.setOnAction(e -> actionAllocateBeds());
        refreshBtn.setOnAction(e -> actionRefreshPatients());
        dischargeBtn.setOnAction(e -> actionDischargePatient());
        historyBtn.setOnAction(e -> actionShowHistory());
        exportBtn.setOnAction(e -> actionExportCSV());
        dispatchBtn.setOnAction(e -> actionDispatchAmbulance());
        undoBtn.setOnAction(e -> actionUndo());

        leftPanel.getChildren().addAll(
            new Label("CareFlow Actions"),
            registerBtn, allocateBtn, refreshBtn,
            dischargeBtn, historyBtn, exportBtn,
            new Separator(),
            dispatchBtn, undoBtn
        );
        root.setLeft(leftPanel);
    }

    private void setupCenterPanel() {
        setupPatientTable();
        setupMapView();

        SplitPane centerPanel = new SplitPane();
        patientTable.setMinWidth(320);
        mapView.setMinWidth(380);
        mapView.setPrefWidth(700);
        centerPanel.getItems().addAll(patientTable, mapView);
        centerPanel.setDividerPositions(0.4);
        Platform.runLater(() -> centerPanel.setDividerPositions(0.4));
        root.setCenter(centerPanel);
    }

    private void setupPatientTable() {
        TableColumn<Patient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));

        TableColumn<Patient, Number> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getAge()));

        TableColumn<Patient, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getSeverity()));

        severityCol.setCellFactory(col -> new TableCell<Patient, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(SeverityStyler.getStyle(item));
                }
            }
        });

        TableColumn<Patient, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));

        TableColumn<Patient, Number> bedCol = new TableColumn<>("Bed ID");
        bedCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getBedId()));

        patientTable.getColumns().addAll(nameCol, ageCol, severityCol, statusCol, bedCol);
    }

    private void setupMapView() {
        mapView.setCenter(Constants.MAP_CENTER_LAT, Constants.MAP_CENTER_LON);
        mapView.setZoom(Constants.MAP_ZOOM);
        
        mapView.addLayer(hospitalLayer);
        hospitalLayer.setMarkers(mapData.getNodeCoordinates());
        hospitalLayer.setEdgeLabels(getMapEdgeLabels());
    }

    private List<HospitalMapLayer.EdgeLabel> getMapEdgeLabels() {
        List<HospitalMapLayer.EdgeLabel> labels = new ArrayList<>();
        Graph graph = routingService.getGraph();
        Set<String> seenEdges = new HashSet<>();

        for (String node : graph.getAdjacencyList().keySet()) {
            for (Graph.Edge edge : graph.getEdges(node)) {
                String key = makeEdgeKey(node, edge.getDestination());
                if (seenEdges.add(key)) {
                    labels.add(new HospitalMapLayer.EdgeLabel(node, edge.getDestination(), edge.getWeight()));
                }
            }
        }
        return labels;
    }

    private String makeEdgeKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
    }

    private void setupInitialLoad() {
        root.setPadding(new Insets(10));

        Platform.runLater(() -> {
            try {
                database.DBConnection.getConnection().close();
                actionRefreshPatients();
                actionRefreshDashboard();
            } catch (Exception ex) {
                System.err.println("❌ Database Error: " + ex.getMessage());
                ex.printStackTrace();
                statsLabel.setText("❌ Database Error: " + ex.getMessage());
            }
        });
    }

    private void actionRegisterPatient() {
        Patient newPatient = showRegisterDialog();
        if (newPatient == null) return;

        new TaskExecutor(
            () -> showMessage("Patient registered successfully"),
            () -> showMessage("Error registering patient")
        ).execute(() -> {
            patientService.addPatient(newPatient);
            Platform.runLater(() -> {
                actionRefreshPatients();
                actionRefreshDashboard();
            });
        });
    }

    private void actionAllocateBeds() {
        new TaskExecutor(
            () -> showMessage("Beds allocated successfully"),
            () -> showMessage("Error allocating beds")
        ).executeWithResult(
            this::doAllocateBeds,
            this::showAllocationResult
        );
    }

    private List<BedService.Allocation> doAllocateBeds() throws Exception {
        List<Patient> waiting = patientService.getWaitingPatients();
        if (waiting.isEmpty()) {
            throw new Exception("No patients in queue");
        }

        List<BedService.Allocation> allocated = bedService.assignBeds(waiting);
        if (allocated.isEmpty()) {
            throw new Exception("No beds available");
        }

        for (BedService.Allocation alloc : allocated) {
            patientService.assignBedToPatient(alloc.getPatient().getId(), alloc.getBed().getBedId());
            patientService.updatePatientStatus(alloc.getPatient().getId(), Constants.STATUS_ADMITTED);
        }

        undoService.push(() -> undoAllocateBeds(allocated));
        
        return allocated;
    }

    private void undoAllocateBeds(List<BedService.Allocation> allocations) {
        try {
            for (BedService.Allocation alloc : allocations) {
                bedService.releaseBed(alloc.getBed().getBedId());
                patientService.updatePatientStatus(alloc.getPatient().getId(), Constants.STATUS_WAITING);
                patientService.assignBedToPatient(alloc.getPatient().getId(), -1);
            }
            Platform.runLater(this::actionRefreshPatients);
        } catch (Exception e) {
            showMessage("Error during undo: " + e.getMessage());
        }
    }

    private void showAllocationResult(List<BedService.Allocation> allocations) {
        StringBuilder message = new StringBuilder("Beds allocated:\n");
        for (BedService.Allocation alloc : allocations) {
            message.append(String.format("  %s (%s) → Bed %d\n",
                alloc.getPatient().getName(),
                alloc.getPatient().getSeverity(),
                alloc.getBed().getBedId()));
        }
        
        showMessage(message.toString());

        Platform.runLater(() -> {
            actionRefreshPatients();
            actionRefreshDashboard();
        });
    }

    private void actionRefreshPatients() {
        new TaskExecutor(
            () -> {},
            () -> showMessage("Error loading patients")
        ).executeWithResult(
            patientService::getAllPatients,
            this::showPatientList
        );
    }

    private void showPatientList(List<Patient> allPatients) {
        List<Patient> active = new ArrayList<>();
        for (Patient p : allPatients) {
            if (!Constants.STATUS_DISCHARGED.equals(p.getStatus())) {
                active.add(p);
            }
        }

        patientTable.getItems().setAll(active);
    }

    private void actionDischargePatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Please select a patient to discharge");
            return;
        }

        new TaskExecutor(
            () -> showMessage("Patient discharged successfully"),
            () -> showMessage("Error discharging patient")
        ).execute(() -> {
            if (selected.getBedId() > 0) {
                bedService.releaseBed(selected.getBedId());
            }

            historyService.addDischargedPatient(selected);
            patientService.updatePatientStatus(selected.getId(), Constants.STATUS_DISCHARGED);
            patientService.assignBedToPatient(selected.getId(), -1);

            Platform.runLater(() -> {
                actionRefreshPatients();
                actionRefreshDashboard();
            });
        });
    }

    private void actionRefreshDashboard() {
        new TaskExecutor(
            () -> {},
            () -> statsLabel.setText("Error loading stats")
        ).executeWithResult(
            this::calculateStats,
            this::displayStats
        );
    }

    private DashboardStats calculateStats() throws Exception {
        List<Patient> allPatients = patientService.getAllPatients();
        List<Bed> beds = bedService.getAvailableBeds();
        int dischargedToday = historyService.getTodayDischargeCount();

        return DashboardService.computeStats(allPatients, beds, dischargedToday);
    }

    private void displayStats(DashboardStats stats) {
        String text = String.format(
            "📊 Total Patients: %d  |  🛏 Occupied: %d  |  Available: %d  |  🚨 Critical: %d  |  ✓ Discharged Today: %d",
            stats.getTotalPatients(), stats.getOccupiedBeds(), stats.getAvailableBeds(),
            stats.getCriticalCases(), stats.getDischargedToday()
        );
        statsLabel.setText(text);
    }

    private void actionShowHistory() {
        new TaskExecutor(
            () -> {},
            () -> showMessage("Error loading history")
        ).executeWithResult(
            historyService::getAllHistory,
            this::showHistoryTable
        );
    }

    private void showHistoryTable(List<PatientHistory> history) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Patient Discharge History");
        dialog.setHeaderText("Discharged Patients Log");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<PatientHistory> historyTable = new TableView<>();

        TableColumn<PatientHistory, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getPatientName()));

        TableColumn<PatientHistory, String> statusCol = new TableColumn<>("Final Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getFinalStatus()));
        
        TableColumn<PatientHistory, Number> durationCol = new TableColumn<>("Stay Duration");
        durationCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getStayDuration()));
        
        TableColumn<PatientHistory, String> dischargeCol = new TableColumn<>("Discharged");
        dischargeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDischargedAt().toString()));
        
        historyTable.getColumns().addAll(nameCol, statusCol, durationCol, dischargeCol);
        historyTable.getItems().setAll(history);
        historyTable.setPrefHeight(400);
        
        dialog.getDialogPane().setContent(historyTable);
        dialog.showAndWait();
    }

    private void actionExportCSV() {
        new TaskExecutor(
            () -> {},
            () -> showMessage("Error exporting data")
        ).executeWithResult(
            patientService::getAllPatients,
            this::doExportCSV
        );
    }

    private void doExportCSV(List<Patient> patients) throws Exception {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Patient List as CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName("patients_" + System.currentTimeMillis() + ".csv");
        
        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file != null) {
            CSVExporter.exportPatients(file.getAbsolutePath(), patients);
            showMessage("Patient list exported to:\n" + file.getAbsolutePath());
        }
    }

    private void actionDispatchAmbulance() {
        String destination = showDispatchDialog();
        if (destination != null) {
            dispatchAmbulanceTo(Constants.HOSPITAL_LOCATION, destination);
        }
    }

    private String showDispatchDialog() {
        List<String> locations = new ArrayList<>(routingService.getNodeNames());
        locations.remove(Constants.HOSPITAL_LOCATION);
        Collections.sort(locations);

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Dispatch Ambulance");
        dialog.setHeaderText("Select destination from " + Constants.HOSPITAL_LOCATION);

        ButtonType dispatchBtn = new ButtonType("Dispatch", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(dispatchBtn, ButtonType.CANCEL);

        ComboBox<String> destBox = new ComboBox<>();
        destBox.getItems().addAll(locations);
        if (!locations.isEmpty()) {
            destBox.setValue(locations.get(0));
        }

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("From: " + Constants.HOSPITAL_LOCATION),
            new Label("To:"),
            destBox
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> btn == dispatchBtn ? destBox.getValue() : null);
        
        return dialog.showAndWait().orElse(null);
    }

    private void dispatchAmbulanceTo(String source, String destination) {
        if (source == null || destination == null || source.equals(destination)) {
            showMessage("Invalid source or destination");
            return;
        }

        new TaskExecutor(
            () -> {},
            () -> showMessage("Error routing ambulance")
        ).executeWithResult(
            () -> routingService.findShortestPath(source, destination),
            this::displayRoute
        );
    }

    private void displayRoute(Dijkstra.Result result) {
        if (!result.isValid()) {
            showMessage("No route found");
            return;
        }

        List<String> path = result.getPath();
        boolean routeDisplayed = hospitalLayer.setRoute(path, mapData.getRoadSegments());
        
        if (routeDisplayed) {
            String pathText = String.join(" → ", path);
            showMessage("Route: " + pathText + "\nTime: " + String.format("%.1f", result.getTimeMin()) + " min");
        } else {
            showMessage("Road geometry missing for this route");
        }
    }

    private void actionUndo() {
        if (undoService.canUndo()) {
            undoService.undo();
            actionRefreshPatients();
            actionRefreshDashboard();
            showMessage("Undo completed");
        } else {
            showMessage("Nothing to undo");
        }
    }

    private Patient showRegisterDialog() {
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle("Register Patient");
        dialog.setHeaderText("Enter patient details");

        ButtonType registerType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField ageField = new TextField();
        ageField.setPromptText("Age");
        ComboBox<String> severityBox = new ComboBox<>();
        severityBox.getItems().addAll("CRITICAL", "HIGH", "MEDIUM", "LOW");
        severityBox.setValue("MEDIUM");

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Name:"), nameField,
            new Label("Age:"), ageField,
            new Label("Severity:"), severityBox
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == registerType) {
                try {
                    String name = nameField.getText();
                    int age = Integer.parseInt(ageField.getText());
                    String severity = severityBox.getValue();
                    Patient p = new Patient();
                    p.setName(name);
                    p.setAge(age);
                    p.setSeverity(severity);
                    
                    return p;
                } catch (NumberFormatException ex) {
                    showMessage("Invalid age format");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("CareFlow");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public BorderPane getRoot() {
        return root;
    }
}
