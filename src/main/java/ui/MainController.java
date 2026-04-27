package ui;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import models.*;
import services.*;
import utils.CSVExporter;
import utils.Dijkstra;
import java.io.File;
import java.util.*;

public class MainController {
    private final BorderPane root;
    private final TableView<Patient> patientTable;
    private final MapView mapView;
    private final HospitalMapLayer hospitalLayer;
    private final PatientService patientService;
    private final BedService bedService;
    private final RoutingService routingService;
    private final UndoService undoService;
    private final PatientHistoryService historyService;

    private final Map<String, double[]> nodeCoordinates = new LinkedHashMap<>();
    private final Map<String, List<double[]>> roadSegments = new HashMap<>();
    private final String HOSPITAL_LOCATION = "Careflow City Hospital";
    private Label statsLabel;

    public MainController() {
        initCoordinates();
        buildRoadSegments();

        patientService = new PatientService();
        bedService = new BedService();
        routingService = new RoutingService();
        undoService = new UndoService();
        historyService = new PatientHistoryService();

        root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setTop(createDashboard());
        root.setLeft(createLeftPanel());

        patientTable = createPatientTable();
        mapView = createMapView();
        hospitalLayer = new HospitalMapLayer();
        loadMap();

        List<HospitalMapLayer.EdgeLabel> labels = new ArrayList<>();
        Graph graph = routingService.getGraph();
        Set<String> seen = new HashSet<>();
        for (String node : graph.getAdjacencyList().keySet()) {
            for (Graph.Edge edge : graph.getEdges(node)) {
                String from = node;
                String to = edge.getDestination();
                String key = from.compareTo(to) < 0 ? from + "|" + to : to + "|" + from;
                if (seen.add(key)) {
                    labels.add(new HospitalMapLayer.EdgeLabel(from, to, edge.getWeight()));
                }
            }
        }
        hospitalLayer.setEdgeLabels(labels);

        SplitPane splitPane = new SplitPane();
        patientTable.setMinWidth(320);
        mapView.setMinWidth(380);
        mapView.setPrefWidth(700);
        splitPane.getItems().addAll(patientTable, mapView);
        splitPane.setDividerPositions(0.4);
        Platform.runLater(() -> splitPane.setDividerPositions(0.4));
        root.setCenter(splitPane);

        refreshPatients();
        refreshDashboard();
    }

    private void initCoordinates() {
        nodeCoordinates.put("Careflow City Hospital", new double[]{30.3256, 78.0437});
        nodeCoordinates.put("ISBT", new double[]{30.2853, 78.0066});
        nodeCoordinates.put("Ballupur", new double[]{30.3412, 78.0322});
        nodeCoordinates.put("Clement Town", new double[]{30.2669, 78.0087});
        nodeCoordinates.put("Prem Nagar", new double[]{30.3389, 77.9981});
        nodeCoordinates.put("Raipur", new double[]{30.3160, 78.0873});
        nodeCoordinates.put("Rajpur Road", new double[]{30.3665, 78.0707});
        nodeCoordinates.put("Nehru Colony", new double[]{30.3036, 78.0503});
        nodeCoordinates.put("Patel Nagar", new double[]{30.3180, 78.0119});
        nodeCoordinates.put("Mothrowala", new double[]{30.2792, 78.0212});
        nodeCoordinates.put("Jogiwala", new double[]{30.2824, 78.0482});
        nodeCoordinates.put("Defence Colony", new double[]{30.3093, 78.0181});
        nodeCoordinates.put("Banjarawala", new double[]{30.3027, 77.9904});
        nodeCoordinates.put("Canal Road", new double[]{30.3508, 78.0609});
    }

    private HBox createDashboard() {
        HBox dashboard = new HBox(15);
        dashboard.setPadding(new Insets(10));
        dashboard.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");

        statsLabel = new Label("Loading stats...");
        statsLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        statsLabel.setStyle("-fx-text-fill: #333333;");
        statsLabel.setWrapText(true);

        dashboard.getChildren().add(statsLabel);
        return dashboard;
    }

    private void refreshDashboard() {
        Task<DashboardStats> task = new Task<>() {
            @Override protected DashboardStats call() throws Exception {
                List<Patient> all = patientService.getAllPatients();
                List<Bed> beds = bedService.getAvailableBeds();
                int discharged = historyService.getTodayDischargeCount();
                return DashboardService.computeStats(all, beds, discharged);
            }
        };
        task.setOnSucceeded(e -> {
            DashboardStats stats = task.getValue();
            String text = String.format(
                "📊 Total Patients: %d  |  🛏 Occupied: %d  |  Available: %d  |  🚨 Critical: %d  |  ✓ Discharged Today: %d",
                stats.getTotalPatients(), stats.getOccupiedBeds(), stats.getAvailableBeds(),
                stats.getCriticalCases(), stats.getDischargedToday()
            );
            statsLabel.setText(text);
        });
        task.setOnFailed(e -> statsLabel.setText("Error loading stats"));
        new Thread(task).start();
    }

    private VBox createLeftPanel() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setPrefWidth(180);

        Button registerBtn = new Button("Register Patient");
        Button allocateBtn = new Button("Allocate Bed");
        Button showBtn = new Button("Refresh Patients");
        Button dischargeBtn = new Button("Discharge");
        Button historyBtn = new Button("View History");
        Button exportBtn = new Button("Export CSV");
        Button dispatchBtn = new Button("Dispatch Ambulance");
        Button undoBtn = new Button("Undo");

        for (Button b : new Button[]{registerBtn, allocateBtn, showBtn, dischargeBtn, 
                                      historyBtn, exportBtn, dispatchBtn, undoBtn}) {
            b.setMaxWidth(Double.MAX_VALUE);
        }

        registerBtn.setOnAction(e -> showRegisterDialog());
        allocateBtn.setOnAction(e -> allocateBed());
        showBtn.setOnAction(e -> refreshPatients());
        dischargeBtn.setOnAction(e -> dischargePatient());
        historyBtn.setOnAction(e -> showPatientHistory());
        exportBtn.setOnAction(e -> exportToCSV());
        dispatchBtn.setOnAction(e -> dispatchAmbulance());
        undoBtn.setOnAction(e -> undo());

        vbox.getChildren().addAll(
            new Label("CareFlow Actions"),
            registerBtn, allocateBtn, showBtn,
            dischargeBtn, historyBtn, exportBtn,
            new Separator(),
            dispatchBtn, undoBtn
        );
        return vbox;
    }

    private TableView<Patient> createPatientTable() {
        TableView<Patient> table = new TableView<>();

        TableColumn<Patient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));

        TableColumn<Patient, Number> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getAge()));

        TableColumn<Patient, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getSeverity()));
        severityCol.setCellFactory(col -> new TableCell<Patient, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(getSeverityStyle(item));
                }
            }
        });

        TableColumn<Patient, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));

        TableColumn<Patient, Number> bedCol = new TableColumn<>("Bed ID");
        bedCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getBedId()));

        table.getColumns().addAll(List.of(nameCol, ageCol, severityCol, statusCol, bedCol));
        return table;
    }

    private String getSeverityStyle(String severity) {
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> "-fx-background-color: #ffcccc; -fx-text-fill: #cc0000; -fx-font-weight: bold;";
            case "HIGH" -> "-fx-background-color: #ffe6cc; -fx-text-fill: #ff6600; -fx-font-weight: bold;";
            case "MEDIUM" -> "-fx-background-color: #ffffcc; -fx-text-fill: #ffaa00;";
            case "LOW" -> "-fx-background-color: #ccffcc; -fx-text-fill: #00aa00;";
            default -> "";
        };
    }

    private MapView createMapView() {
        MapView map = new MapView();
        map.setCenter(30.3256, 78.0437);
        map.setZoom(12.5);
        return map;
    }

    private void loadMap() {
        mapView.addLayer(hospitalLayer);
        initMapMarkers();
    }

    private void buildRoadSegments() {
        String[][] pairs = {
            {"Careflow City Hospital", "Ballupur"}, {"Careflow City Hospital", "Nehru Colony"},
            {"Careflow City Hospital", "Raipur"}, {"Careflow City Hospital", "Patel Nagar"},
            {"Careflow City Hospital", "ISBT"}, {"Careflow City Hospital", "Defence Colony"},
            {"Careflow City Hospital", "Canal Road"},
            {"ISBT", "Clement Town"}, {"ISBT", "Nehru Colony"}, {"ISBT", "Patel Nagar"}, {"ISBT", "Mothrowala"},
            {"Ballupur", "Prem Nagar"}, {"Ballupur", "Rajpur Road"},
            {"Prem Nagar", "Clement Town"}, {"Rajpur Road", "Canal Road"},
            {"Raipur", "Canal Road"},
            {"Raipur", "Jogiwala"}, {"Nehru Colony", "Raipur"}, {"Clement Town", "Nehru Colony"},
            {"Nehru Colony", "Defence Colony"}, {"Nehru Colony", "Jogiwala"},
            {"Patel Nagar", "Defence Colony"}, {"Patel Nagar", "Banjarawala"},
            {"Mothrowala", "Banjarawala"}, {"Mothrowala", "Jogiwala"}
        };
        for (String[] p : pairs) {
            double[][] fwd = interpolate(nodeCoordinates.get(p[0]), nodeCoordinates.get(p[1]), 4);
            roadSegments.put(p[0] + "->" + p[1], asPointList(fwd));
            roadSegments.put(p[1] + "->" + p[0], asPointList(reverse(fwd)));
        }
    }

    private double[][] interpolate(double[] a, double[] b, int points) {
        double[][] result = new double[points][2];
        for (int i = 0; i < points; i++) {
            double t = i / (double) (points - 1);
            result[i][0] = a[0] + t * (b[0] - a[0]);
            result[i][1] = a[1] + t * (b[1] - a[1]);
        }
        return result;
    }

    private List<double[]> asPointList(double[][] points) {
        List<double[]> list = new ArrayList<>(points.length);
        for (double[] p : points) list.add(new double[]{p[0], p[1]});
        return list;
    }

    private double[][] reverse(double[][] points) {
        double[][] reversed = new double[points.length][2];
        for (int i = 0; i < points.length; i++) {
            reversed[i][0] = points[points.length - 1 - i][0];
            reversed[i][1] = points[points.length - 1 - i][1];
        }
        return reversed;
    }

    private void initMapMarkers() {
        hospitalLayer.setMarkers(nodeCoordinates);
    }

    private void showRegisterDialog() {
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
                    Patient p = new Patient();
                    p.setName(nameField.getText());
                    p.setAge(Integer.parseInt(ageField.getText()));
                    p.setSeverity(severityBox.getValue());
                    return p;
                } catch (NumberFormatException ex) {
                    showAlert("Invalid age format");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::registerPatient);
    }

    private void registerPatient(Patient patient) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                patientService.addPatient(patient);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            showAlert("Patient registered: " + patient.getName());
            refreshPatients();
            refreshDashboard();
        });
        task.setOnFailed(e -> showAlert("Error: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void allocateBed() {
        Task<List<BedService.Allocation>> task = new Task<>() {
            @Override protected List<BedService.Allocation> call() throws Exception {
                List<Patient> waiting = patientService.getWaitingPatients();
                if (waiting.isEmpty()) throw new IllegalStateException("No patients in queue");

                List<BedService.Allocation> allocations = bedService.assignBeds(waiting);
                if (allocations.isEmpty()) throw new IllegalStateException("No beds available");

                for (BedService.Allocation alloc : allocations) {
                    Patient patient = alloc.getPatient();
                    Bed bed = alloc.getBed();
                    patientService.assignBedToPatient(patient.getId(), bed.getBedId());
                    patientService.updatePatientStatus(patient.getId(), "ADMITTED");
                }

                undoService.push(() -> {
                    try {
                        for (BedService.Allocation alloc : allocations) {
                            bedService.releaseBed(alloc.getBed().getBedId());
                            patientService.updatePatientStatus(alloc.getPatient().getId(), "WAITING");
                            patientService.assignBedToPatient(alloc.getPatient().getId(), -1);
                        }
                        Platform.runLater(MainController.this::refreshPatients);
                    } catch (Exception ex) { /* Silently ignore undo failures */ }
                });

                return allocations;
            }
        };
        task.setOnSucceeded(e -> {
            List<BedService.Allocation> allocations = task.getValue();
            StringBuilder sb = new StringBuilder("Beds allocated:\n");
            for (BedService.Allocation alloc : allocations) {
                sb.append(String.format("  %s (%s) → Bed %d%n",
                    alloc.getPatient().getName(),
                    alloc.getPatient().getSeverity(),
                    alloc.getBed().getBedId()));
            }
            showAlert(sb.toString());
            refreshPatients();
            refreshDashboard();
        });
        task.setOnFailed(e -> showAlert("Error: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void refreshPatients() {
        Task<List<Patient>> task = new Task<>() {
            @Override protected List<Patient> call() throws Exception {
                return patientService.getAllPatients();
            }
        };
        task.setOnSucceeded(e -> patientTable.getItems().setAll(
            task.getValue().stream()
                .filter(p -> !"DISCHARGED".equalsIgnoreCase(p.getStatus()))
                .toList()
        ));
        task.setOnFailed(e -> showAlert("Error loading patients: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void dischargePatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a patient to discharge");
            return;
        }
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                if (selected.getBedId() > 0) bedService.releaseBed(selected.getBedId());
                historyService.addDischargedPatient(selected);
                patientService.updatePatientStatus(selected.getId(), "DISCHARGED");
                patientService.assignBedToPatient(selected.getId(), -1);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            showAlert("Patient discharged successfully");
            refreshPatients();
            refreshDashboard();
        });
        task.setOnFailed(e -> showAlert("Error: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void showPatientHistory() {
        Task<List<PatientHistory>> task = new Task<>() {
            @Override protected List<PatientHistory> call() throws Exception {
                return historyService.getAllHistory();
            }
        };
        task.setOnSucceeded(e -> showHistoryDialog(task.getValue()));
        task.setOnFailed(e -> showAlert("Error loading history: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void showHistoryDialog(List<PatientHistory> history) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Patient Discharge History");
        dialog.setHeaderText("Discharged Patients Log");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<PatientHistory> historyTable = new TableView<>();
        
        TableColumn<PatientHistory, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));
        
        TableColumn<PatientHistory, Number> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getAge()));
        
        TableColumn<PatientHistory, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getSeverity()));
        
        TableColumn<PatientHistory, String> dischargeCol = new TableColumn<>("Discharged");
        dischargeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDischargedAt().toString()));
        
        historyTable.getColumns().addAll(List.of(nameCol, ageCol, severityCol, dischargeCol));
        historyTable.getItems().setAll(history);
        historyTable.setPrefHeight(400);
        
        dialog.getDialogPane().setContent(historyTable);
        dialog.showAndWait();
    }

    private void exportToCSV() {
        Task<List<Patient>> task = new Task<>() {
            @Override protected List<Patient> call() throws Exception {
                return patientService.getAllPatients();
            }
        };
        task.setOnSucceeded(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Patient List as CSV");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            chooser.setInitialFileName("patients_" + System.currentTimeMillis() + ".csv");
            
            File file = chooser.showSaveDialog(root.getScene().getWindow());
            if (file != null) {
                try {
                    CSVExporter.exportPatients(file.getAbsolutePath(), task.getValue());
                    showAlert("Patient list exported to:\n" + file.getAbsolutePath());
                } catch (Exception ex) {
                    showAlert("Export failed: " + ex.getMessage());
                }
            }
        });
        task.setOnFailed(e -> showAlert("Error: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void dispatchAmbulance() {
        List<String> nodes = new ArrayList<>(routingService.getNodeNames());
        Collections.sort(nodes);
        nodes.remove(HOSPITAL_LOCATION);

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Dispatch Ambulance");
        dialog.setHeaderText("Select destination from " + HOSPITAL_LOCATION);

        ButtonType dispatchBtn = new ButtonType("Dispatch", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(dispatchBtn, ButtonType.CANCEL);

        ComboBox<String> destBox = new ComboBox<>();
        destBox.getItems().addAll(nodes);
        if (!nodes.isEmpty()) destBox.setValue(nodes.get(0));

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("From: " + HOSPITAL_LOCATION),
            new Label("To:"),
            destBox
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == dispatchBtn) return destBox.getValue();
            return null;
        });

        dialog.showAndWait().ifPresent(dest -> routeAmbulance(HOSPITAL_LOCATION, dest));
    }

    private void routeAmbulance(String source, String destination) {
        if (source == null || destination == null || source.equals(destination)) {
            showAlert("Invalid source or destination");
            return;
        }

        Task<Dijkstra.Result> task = new Task<>() {
            @Override protected Dijkstra.Result call() throws Exception {
                return routingService.findShortestPath(source, destination);
            }
        };

        task.setOnSucceeded(e -> {
            Dijkstra.Result result = task.getValue();
            if (result.getDistance() == -1) {
                showAlert("No route found");
                return;
            }

            List<String> path = result.getPath();
            if (hospitalLayer.setRoute(path, roadSegments)) {
                showAlert("Route: " + String.join(" → ", path) + "\nTime: " + result.getDistance() + " min");
            } else {
                showAlert("Road geometry missing for this route");
            }
        });

        task.setOnFailed(e -> showAlert("Error: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void undo() {
        if (undoService.canUndo()) {
            undoService.undo();
            refreshPatients();
            refreshDashboard();
            showAlert("Undo completed");
        } else {
            showAlert("Nothing to undo");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("CareFlow");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public BorderPane getRoot() {
        return root;
    }

    private static class HospitalMapLayer extends MapLayer {
        private final Map<String, MapPoint> markers = new LinkedHashMap<>();
        private final List<MapPoint> routePoints = new ArrayList<>();
        private final List<EdgeLabel> edgeLabels = new ArrayList<>();

        static class EdgeLabel {
            final String from;
            final String to;
            final double weight;
            EdgeLabel(String from, String to, double weight) {
                this.from = from;
                this.to = to;
                this.weight = weight;
            }
        }

        void setEdgeLabels(List<EdgeLabel> labels) {
            edgeLabels.clear();
            edgeLabels.addAll(labels);
            markDirty();
        }

        void setMarkers(Map<String, double[]> coords) {
            markers.clear();
            for (Map.Entry<String, double[]> e : coords.entrySet()) {
                double[] c = e.getValue();
                markers.put(e.getKey(), new MapPoint(c[0], c[1]));
            }
            markDirty();
        }

        boolean setRoute(List<String> path, Map<String, List<double[]>> roads) {
            routePoints.clear();
            for (int i = 0; i < path.size() - 1; i++) {
                String from = path.get(i);
                String to = path.get(i + 1);
                List<double[]> road = roads.get(from + "->" + to);

                if (road == null || road.isEmpty()) {
                    routePoints.clear();
                    markDirty();
                    return false;
                }

                for (int j = 0; j < road.size(); j++) {
                    if (i > 0 && j == 0) continue;
                    double[] c = road.get(j);
                    routePoints.add(new MapPoint(c[0], c[1]));
                }
            }
            markDirty();
            return routePoints.size() >= 2;
        }

        @Override
        protected void layoutLayer() {
            getChildren().clear();

            for (EdgeLabel edge : edgeLabels) {
                MapPoint fromMp = markers.get(edge.from);
                MapPoint toMp = markers.get(edge.to);
                if (fromMp == null || toMp == null) continue;

                Point2D p1 = baseMap.getMapPoint(fromMp.getLatitude(), fromMp.getLongitude());
                Point2D p2 = baseMap.getMapPoint(toMp.getLatitude(), toMp.getLongitude());

                Line edgeLine = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                edgeLine.setStroke(Color.web("#aaaaaa"));
                edgeLine.setStrokeWidth(1.5);
                edgeLine.setMouseTransparent(true);
                getChildren().add(edgeLine);
            }

            if (routePoints.size() >= 2) {
                Polyline line = new Polyline();
                line.setStroke(Color.web("#e74c3c"));
                line.setStrokeWidth(3.5);
                for (MapPoint rp : routePoints) {
                    Point2D p = baseMap.getMapPoint(rp.getLatitude(), rp.getLongitude());
                    line.getPoints().addAll(p.getX(), p.getY());
                }
                getChildren().add(line);
            }

            for (EdgeLabel edge : edgeLabels) {
                MapPoint fromMp = markers.get(edge.from);
                MapPoint toMp = markers.get(edge.to);
                if (fromMp == null || toMp == null) continue;

                Point2D p1 = baseMap.getMapPoint(fromMp.getLatitude(), fromMp.getLongitude());
                Point2D p2 = baseMap.getMapPoint(toMp.getLatitude(), toMp.getLongitude());

                double midX = (p1.getX() + p2.getX()) / 2.0;
                double midY = (p1.getY() + p2.getY()) / 2.0;

                Rectangle bg = new Rectangle(34, 20);
                bg.setFill(Color.web("#fff9c4"));
                bg.setOpacity(0.95);
                bg.setArcWidth(6);
                bg.setArcHeight(6);
                bg.setStroke(Color.web("#f57f17"));
                bg.setStrokeWidth(1.2);
                bg.setTranslateX(midX - 17);
                bg.setTranslateY(midY - 17);
                bg.setMouseTransparent(true);

                Text weightText = new Text(String.format("%.1f", edge.weight));
                weightText.setFont(Font.font("System", FontWeight.BOLD, 13));
                weightText.setFill(Color.web("#b71c1c"));
                weightText.setStroke(Color.web("#fff9c4"));
                weightText.setStrokeWidth(1.0);
                weightText.setTranslateX(midX - 11);
                weightText.setTranslateY(midY - 5);
                weightText.setMouseTransparent(true);

                getChildren().addAll(bg, weightText);
            }

            for (Map.Entry<String, MapPoint> entry : markers.entrySet()) {
                String name = entry.getKey();
                MapPoint mp = entry.getValue();
                Point2D p = baseMap.getMapPoint(mp.getLatitude(), mp.getLongitude());

                boolean isHospital = "Careflow City Hospital".equals(name);

                Circle dot = new Circle(isHospital ? 8 : 5,
                    isHospital ? Color.web("#d63031") : Color.web("#3388ff"));
                dot.setStroke(Color.WHITE);
                dot.setStrokeWidth(isHospital ? 2.0 : 1.0);
                dot.setTranslateX(p.getX());
                dot.setTranslateY(p.getY());

                Text label = new Text(name);
                label.setFont(Font.font("System", FontWeight.BOLD, isHospital ? 15 : 13));
                label.setFill(isHospital ? Color.web("#7f0000") : Color.web("#111111"));
                label.setStroke(Color.WHITE);
                label.setStrokeWidth(isHospital ? 1.0 : 0.7);
                label.setTranslateX(p.getX() + 8);
                label.setTranslateY(p.getY() - 8);

                getChildren().addAll(dot, label);
            }
        }
    }
}
