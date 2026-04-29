package ui;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import utils.Constants;
import java.util.*;


public class HospitalMapLayer extends MapLayer {
    private final Map<String, MapPoint> markers = new LinkedHashMap<>();
    private final List<MapPoint> routePoints = new ArrayList<>();
    private final List<EdgeLabel> edgeLabels = new ArrayList<>();

    public static class EdgeLabel {
        final String from;
        final String to;
        final double weight;

        public EdgeLabel(String from, String to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    public void setEdgeLabels(List<EdgeLabel> labels) {
        edgeLabels.clear();
        edgeLabels.addAll(labels);
        markDirty();
    }

    public void setMarkers(Map<String, double[]> coordinates) {
        markers.clear();
        for (Map.Entry<String, double[]> entry : coordinates.entrySet()) {
            double[] coord = entry.getValue();
            markers.put(entry.getKey(), new MapPoint(coord[0], coord[1]));
        }
        markDirty();
    }

    public boolean setRoute(List<String> path, Map<String, List<double[]>> roadSegments) {
        routePoints.clear();
        
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i + 1);
            List<double[]> road = roadSegments.get(from + "->" + to);

            if (road == null || road.isEmpty()) {
                markDirty();
                return false;
            }

            for (int j = 0; j < road.size(); j++) {
                if (i > 0 && j == 0) continue; // Skip first point of subsequent segments to avoid duplicates
                double[] coord = road.get(j);
                routePoints.add(new MapPoint(coord[0], coord[1]));
            }
        }
        
        markDirty();
        return routePoints.size() >= 2;
    }

    @Override
    protected void layoutLayer() {
        getChildren().clear();

        // Draw graph edges
        drawEdges();

        // Draw route (if set)
        if (routePoints.size() >= 2) {
            drawRoute();
        }

        // Draw edge labels
        drawEdgeLabels();

        // Draw location markers
        drawMarkers();
    }

    private void drawEdges() {
        for (EdgeLabel edge : edgeLabels) {
            MapPoint fromMarker = markers.get(edge.from);
            MapPoint toMarker = markers.get(edge.to);
            if (fromMarker == null || toMarker == null) continue;

            Point2D p1 = baseMap.getMapPoint(fromMarker.getLatitude(), fromMarker.getLongitude());
            Point2D p2 = baseMap.getMapPoint(toMarker.getLatitude(), toMarker.getLongitude());

            Line edgeLine = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            edgeLine.setStroke(Color.web("#aaaaaa"));
            edgeLine.setStrokeWidth(1.5);
            edgeLine.setMouseTransparent(true);
            getChildren().add(edgeLine);
        }
    }

    private void drawRoute() {
        Polyline routeLine = new Polyline();
        routeLine.setStroke(Color.web("#e74c3c"));
        routeLine.setStrokeWidth(3.5);
        
        for (MapPoint point : routePoints) {
            Point2D p = baseMap.getMapPoint(point.getLatitude(), point.getLongitude());
            routeLine.getPoints().addAll(p.getX(), p.getY());
        }
        
        getChildren().add(routeLine);
    }

    private void drawEdgeLabels() {
        for (EdgeLabel edge : edgeLabels) {
            MapPoint fromMarker = markers.get(edge.from);
            MapPoint toMarker = markers.get(edge.to);
            if (fromMarker == null || toMarker == null) continue;

            Point2D p1 = baseMap.getMapPoint(fromMarker.getLatitude(), fromMarker.getLongitude());
            Point2D p2 = baseMap.getMapPoint(toMarker.getLatitude(), toMarker.getLongitude());

            double midX = (p1.getX() + p2.getX()) / 2.0;
            double midY = (p1.getY() + p2.getY()) / 2.0;

            // Background rectangle for label
            Rectangle background = new Rectangle(34, 20);
            background.setFill(Color.web("#fff9c4"));
            background.setOpacity(0.95);
            background.setArcWidth(6);
            background.setArcHeight(6);
            background.setStroke(Color.web("#f57f17"));
            background.setStrokeWidth(1.2);
            background.setTranslateX(midX - 17);
            background.setTranslateY(midY - 17);
            background.setMouseTransparent(true);

            // Weight text
            Text weightText = new Text(String.format("%.1f", edge.weight));
            weightText.setFont(Font.font("System", FontWeight.BOLD, 13));
            weightText.setFill(Color.web("#b71c1c"));
            weightText.setStroke(Color.web("#fff9c4"));
            weightText.setStrokeWidth(1.0);
            weightText.setTranslateX(midX - 11);
            weightText.setTranslateY(midY - 5);
            weightText.setMouseTransparent(true);

            getChildren().addAll(background, weightText);
        }
    }

    private void drawMarkers() {
        for (Map.Entry<String, MapPoint> entry : markers.entrySet()) {
            String name = entry.getKey();
            MapPoint mp = entry.getValue();
            Point2D p = baseMap.getMapPoint(mp.getLatitude(), mp.getLongitude());

            boolean isHospital = Constants.HOSPITAL_LOCATION.equals(name);

            // Draw marker circle
            Circle dot = new Circle(isHospital ? 8 : 5,
                isHospital ? Color.web("#d63031") : Color.web("#3388ff"));
            dot.setStroke(Color.WHITE);
            dot.setStrokeWidth(isHospital ? 2.0 : 1.0);
            dot.setTranslateX(p.getX());
            dot.setTranslateY(p.getY());

            // Draw location label
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
