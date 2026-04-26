package ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import models.Patient;
import models.Bed;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import services.PatientService;
import services.BedService;
import services.RoutingService;
import services.UndoService;
import utils.Dijkstra;

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

    private final Map<String, double[]> nodeCoordinates = new LinkedHashMap<>();
    private final Map<String, List<double[]>> roadSegments = new HashMap<>();
    private final String HOSPITAL_LOCATION = "Careflow City Hospital";

    public MainController() {
        nodeCoordinates.put("Careflow City Hospital", new double[]{30.3256, 78.0437});
        nodeCoordinates.put("ISBT",          new double[]{30.2853, 78.0066});
        nodeCoordinates.put("Ballupur",      new double[]{30.3412, 78.0322});
        nodeCoordinates.put("Clement Town",  new double[]{30.2669, 78.0087});
        nodeCoordinates.put("Prem Nagar",    new double[]{30.3389, 77.9981});
        nodeCoordinates.put("Raipur",        new double[]{30.3160, 78.0873});
        nodeCoordinates.put("Rajpur Road",   new double[]{30.3665, 78.0707});
        nodeCoordinates.put("Nehru Colony",  new double[]{30.3036, 78.0503});
        nodeCoordinates.put("Patel Nagar",   new double[]{30.3180, 78.0119});
        nodeCoordinates.put("Mothrowala",    new double[]{30.2792, 78.0212});
        nodeCoordinates.put("Jogiwala",      new double[]{30.2824, 78.0482});
        nodeCoordinates.put("Defence Colony",new double[]{30.3093, 78.0181});
        nodeCoordinates.put("Banjarawala",   new double[]{30.3027, 77.9904});
        nodeCoordinates.put("Canal Road",    new double[]{30.3508, 78.0609});
        buildRoadSegments();

        patientService = new PatientService();
        bedService     = new BedService();
        routingService = new RoutingService();
        undoService    = new UndoService();

        root = new BorderPane();
        root.setPadding(new Insets(10));

        root.setLeft(createLeftPanel());

        patientTable = createPatientTable();
        mapView = createMapView();
        hospitalLayer = new HospitalMapLayer();

        loadMap();

        // MapView is a JavaFX Region, so it resizes naturally with SplitPane.
        mapView.setMinWidth(380);
        mapView.setPrefWidth(700);

        SplitPane splitPane = new SplitPane();
        patientTable.setMinWidth(320);
        splitPane.getItems().addAll(patientTable, mapView);
        splitPane.setDividerPositions(0.4);
        SplitPane.setResizableWithParent(mapView, true);
        Platform.runLater(() -> splitPane.setDividerPositions(0.4));
        root.setCenter(splitPane);
    }

    // -------------------------------------------------------------------------
    // UI builders
    // -------------------------------------------------------------------------

    private VBox createLeftPanel() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setPrefWidth(180);

        Button registerBtn  = new Button("Register Patient");
        Button allocateBtn  = new Button("Allocate Bed");
        Button showBtn      = new Button("Show Patients");
        Button dischargeBtn = new Button("Discharge");
        Button dispatchBtn  = new Button("Dispatch Ambulance");
        Button undoBtn      = new Button("Undo");

        for (Button b : new Button[]{registerBtn, allocateBtn, showBtn,
                                      dischargeBtn, dispatchBtn, undoBtn}) {
            b.setMaxWidth(Double.MAX_VALUE);
        }

        registerBtn .setOnAction(e -> showRegisterDialog());
        allocateBtn .setOnAction(e -> allocateBed());
        showBtn     .setOnAction(e -> refreshPatients());
        dischargeBtn.setOnAction(e -> dischargePatient());
        dispatchBtn .setOnAction(e -> dispatchAmbulance());
        undoBtn     .setOnAction(e -> undo());

        vbox.getChildren().addAll(
            new Label("CareFlow Actions"),
            registerBtn, allocateBtn, showBtn,
            dischargeBtn, dispatchBtn, undoBtn
        );
        return vbox;
    }

    private TableView<Patient> createPatientTable() {
        TableView<Patient> table = new TableView<>();

        TableColumn<Patient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));

        TableColumn<Patient, Number> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleIntegerProperty(d.getValue().getAge()));

        TableColumn<Patient, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getSeverity()));

        TableColumn<Patient, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));

        TableColumn<Patient, Number> bedCol = new TableColumn<>("Bed ID");
        bedCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleIntegerProperty(d.getValue().getBedId()));

        table.getColumns().addAll(List.of(nameCol, ageCol, severityCol, statusCol, bedCol));
        return table;
    }

    // -------------------------------------------------------------------------
    // Map loading
    // -------------------------------------------------------------------------

    private MapView createMapView() {
        MapView map = new MapView();
        map.setCenter(30.3256, 78.0437); // Careflow City Hospital (Dehradun)
        map.setZoom(12.5);
        return map;
    }

    private void loadMap() {
        mapView.addLayer(hospitalLayer);
        initMapMarkers();
    }

    private void buildRoadSegments() {
        addRoadSegment("Careflow City Hospital", "Ballupur", new double[][]{
            {30.3256, 78.0437}, {30.3310, 78.0395}, {30.3360, 78.0358}, {30.3412, 78.0322}
        });
        addRoadSegment("Careflow City Hospital", "Nehru Colony", new double[][]{
            {30.3256, 78.0437}, {30.3198, 78.0465}, {30.3110, 78.0491}, {30.3036, 78.0503}
        });
        addRoadSegment("Careflow City Hospital", "Raipur", new double[][]{
            {30.3256, 78.0437}, {30.3224, 78.0552}, {30.3198, 78.0715}, {30.3160, 78.0873}
        });
        addRoadSegment("Careflow City Hospital", "Patel Nagar", new double[][]{
            {30.3256, 78.0437}, {30.3228, 78.0325}, {30.3202, 78.0220}, {30.3180, 78.0119}
        });
        addRoadSegment("Careflow City Hospital", "ISBT", new double[][]{
            {30.3256, 78.0437}, {30.3145, 78.0302}, {30.3020, 78.0185}, {30.2853, 78.0066}
        });
        addRoadSegment("Careflow City Hospital", "Defence Colony", new double[][]{
            {30.3256, 78.0437}, {30.3200, 78.0330}, {30.3144, 78.0254}, {30.3093, 78.0181}
        });
        addRoadSegment("ISBT", "Clement Town", new double[][]{
            {30.2853, 78.0066}, {30.2800, 78.0078}, {30.2732, 78.0084}, {30.2669, 78.0087}
        });
        addRoadSegment("ISBT", "Nehru Colony", new double[][]{
            {30.2853, 78.0066}, {30.2910, 78.0195}, {30.2968, 78.0348}, {30.3036, 78.0503}
        });
        addRoadSegment("ISBT", "Patel Nagar", new double[][]{
            {30.2853, 78.0066}, {30.2955, 78.0080}, {30.3074, 78.0102}, {30.3180, 78.0119}
        });
        addRoadSegment("ISBT", "Mothrowala", new double[][]{
            {30.2853, 78.0066}, {30.2828, 78.0110}, {30.2810, 78.0165}, {30.2792, 78.0212}
        });
        addRoadSegment("Ballupur", "Prem Nagar", new double[][]{
            {30.3412, 78.0322}, {30.3405, 78.0214}, {30.3400, 78.0108}, {30.3389, 77.9981}
        });
        addRoadSegment("Ballupur", "Rajpur Road", new double[][]{
            {30.3412, 78.0322}, {30.3476, 78.0434}, {30.3558, 78.0553}, {30.3665, 78.0707}
        });
        addRoadSegment("Prem Nagar", "Clement Town", new double[][]{
            {30.3389, 77.9981}, {30.3252, 78.0015}, {30.3058, 78.0042}, {30.2850, 78.0068}, {30.2669, 78.0087}
        });
        addRoadSegment("Rajpur Road", "Canal Road", new double[][]{
            {30.3665, 78.0707}, {30.3621, 78.0675}, {30.3565, 78.0641}, {30.3508, 78.0609}
        });
        addRoadSegment("Raipur", "Jogiwala", new double[][]{
            {30.3160, 78.0873}, {30.3095, 78.0746}, {30.3000, 78.0612}, {30.2920, 78.0534}, {30.2824, 78.0482}
        });
        addRoadSegment("Nehru Colony", "Raipur", new double[][]{
            {30.3036, 78.0503}, {30.3072, 78.0610}, {30.3115, 78.0748}, {30.3160, 78.0873}
        });
        addRoadSegment("Clement Town", "Nehru Colony", new double[][]{
            {30.2669, 78.0087}, {30.2765, 78.0202}, {30.2888, 78.0342}, {30.3036, 78.0503}
        });
        addRoadSegment("Nehru Colony", "Defence Colony", new double[][]{
            {30.3036, 78.0503}, {30.3050, 78.0402}, {30.3073, 78.0286}, {30.3093, 78.0181}
        });
        addRoadSegment("Nehru Colony", "Jogiwala", new double[][]{
            {30.3036, 78.0503}, {30.2972, 78.0500}, {30.2898, 78.0494}, {30.2824, 78.0482}
        });
        addRoadSegment("Patel Nagar", "Defence Colony", new double[][]{
            {30.3180, 78.0119}, {30.3150, 78.0134}, {30.3122, 78.0152}, {30.3093, 78.0181}
        });
        addRoadSegment("Patel Nagar", "Banjarawala", new double[][]{
            {30.3180, 78.0119}, {30.3135, 78.0034}, {30.3078, 77.9964}, {30.3027, 77.9904}
        });
        addRoadSegment("Mothrowala", "Banjarawala", new double[][]{
            {30.2792, 78.0212}, {30.2860, 78.0124}, {30.2945, 78.0018}, {30.3027, 77.9904}
        });
        addRoadSegment("Mothrowala", "Jogiwala", new double[][]{
            {30.2792, 78.0212}, {30.2806, 78.0304}, {30.2816, 78.0388}, {30.2824, 78.0482}
        });
        addRoadSegment("Ballupur", "Rajpur Road", new double[][]{
            {30.3412, 78.0322}, {30.3476, 78.0434}, {30.3558, 78.0553}, {30.3665, 78.0707}
        });
    }

    private void addRoadSegment(String a, String b, double[][] points) {
        roadSegments.put(edgeKey(a, b), asPointList(points));
        roadSegments.put(edgeKey(b, a), asPointList(reverse(points)));
    }

    private String edgeKey(String from, String to) {
        return from + "->" + to;
    }

    private List<double[]> asPointList(double[][] points) {
        List<double[]> list = new ArrayList<>(points.length);
        for (double[] p : points) {
            list.add(new double[]{p[0], p[1]});
        }
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
        try {
            hospitalLayer.setMarkers(nodeCoordinates);
        } catch (Exception e) {
            System.err.println("Failed to initialize markers: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Patient / Bed actions
    // -------------------------------------------------------------------------

    private void showRegisterDialog() {
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle("Register Patient");
        dialog.setHeaderText("Enter patient details");

        ButtonType registerButtonType =
            new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes()
            .addAll(registerButtonType, ButtonType.CANCEL);

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
            new Label("Age:"),  ageField,
            new Label("Severity:"), severityBox
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == registerButtonType) {
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
        });
        task.setOnFailed(e -> showAlert("Error: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void allocateBed() {
        Task<Bed> task = new Task<>() {
            @Override protected Bed call() throws Exception {
                Patient patient = patientService.getNextPatient();
                if (patient == null) throw new IllegalStateException("No patients in queue");

                Bed bed = bedService.assignBed(patient);
                patientService.assignBedToPatient(patient.getId(), bed.getBedId());
                patientService.updatePatientStatus(patient.getId(), "ADMITTED");

                undoService.push(() -> {
                    try {
                        bedService.releaseBed(bed.getBedId());
                        patientService.updatePatientStatus(patient.getId(), "WAITING");
                        patientService.assignBedToPatient(patient.getId(), -1);
                        Platform.runLater(MainController.this::refreshPatients);
                    } catch (Exception ex) { ex.printStackTrace(); }
                });

                return bed;
            }
        };
        task.setOnSucceeded(e -> {
            showAlert("Bed " + task.getValue().getBedId() + " allocated");
            refreshPatients();
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
        task.setOnFailed(e ->
            showAlert("Error loading patients: " + task.getException().getMessage()));
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
                patientService.updatePatientStatus(selected.getId(), "DISCHARGED");
                patientService.assignBedToPatient(selected.getId(), -1);
                return null;
            }
        };
        task.setOnSucceeded(e -> { showAlert("Patient discharged"); refreshPatients(); });
        task.setOnFailed(e -> showAlert("Error: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    // -------------------------------------------------------------------------
    // Ambulance dispatch & routing
    // -------------------------------------------------------------------------

    private void dispatchAmbulance() {
        List<String> nodes = new ArrayList<>(routingService.getNodeNames());
        Collections.sort(nodes);
        nodes.remove(HOSPITAL_LOCATION);

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Dispatch Ambulance");
        dialog.setHeaderText("Select destination from " + HOSPITAL_LOCATION);

        ButtonType dispatchBtn =
            new ButtonType("Dispatch Ambulance", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(dispatchBtn, ButtonType.CANCEL);

        ComboBox<String> destBox = new ComboBox<>();
        destBox.getItems().addAll(nodes);
        if (!nodes.isEmpty()) destBox.setValue(nodes.get(0));

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Hospital Location:"),
            new Label(HOSPITAL_LOCATION + " (Fixed)"),
            new Label(""),
            new Label("Select Destination:"),
            destBox
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == dispatchBtn) {
                String dst = destBox.getValue();
                if (dst == null || dst.trim().isEmpty() || dst.equals(HOSPITAL_LOCATION))
                    return null;
                return dst;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dest ->
            routeAmbulance(HOSPITAL_LOCATION, dest));
    }

    private void routeAmbulance(String source, String destination) {
        if (source == null || destination == null ||
            source.trim().isEmpty() || destination.trim().isEmpty()) {
            showAlert("Please select valid source and destination");
            return;
        }
        if (source.equals(destination)) {
            showAlert("Source and destination must be different");
            return;
        }

        Task<Dijkstra.Result> task = new Task<>() {
            @Override protected Dijkstra.Result call() throws Exception {
                return routingService.findShortestPath(source.trim(), destination.trim());
            }
        };

        task.setOnSucceeded(e -> {
            Dijkstra.Result result = task.getValue();
            if (result.getDistance() == -1) {
                showAlert("No route found from " + source + " to " + destination);
                return;
            }

            List<String> path = result.getPath();

            boolean routedOnRoads = hospitalLayer.setRoute(path, roadSegments);
            if (!routedOnRoads) {
                showAlert("Road geometry missing for part of this route. Please update road segments.");
                return;
            }
            showAlert("Route: " + String.join(" → ", path)
                + "\nEstimated time: " + result.getDistance() + " min");
        });

        task.setOnFailed(e ->
            showAlert("Error: " + task.getException().getMessage()));

        new Thread(task).start();
    }

    // -------------------------------------------------------------------------
    // Undo & helpers
    // -------------------------------------------------------------------------

    private void undo() {
        if (undoService.canUndo()) {
            undoService.undo();
            showAlert("Undo completed");
            refreshPatients();
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

    public BorderPane getRoot() { return root; }

    public void disposeMap() {
        // No explicit shutdown needed for Gluon MapView.
    }

    private static class HospitalMapLayer extends MapLayer {
        private final Map<String, MapPoint> markers = new LinkedHashMap<>();
        private final List<MapPoint> routePoints = new ArrayList<>();

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
                    if (i > 0 && j == 0) continue; // prevent duplicate junction points
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