package ui;

import java.util.*;


public class MapDataProvider {
    private final Map<String, double[]> nodeCoordinates = new LinkedHashMap<>();
    private final Map<String, List<double[]>> roadSegments = new HashMap<>();

    public MapDataProvider() {
        initCoordinates();
        buildRoadSegments();
    }

    public Map<String, double[]> getNodeCoordinates() {
        return nodeCoordinates;
    }

    public Map<String, List<double[]>> getRoadSegments() {
        return roadSegments;
    }

    public double[] getCoordinate(String location) {
        return nodeCoordinates.get(location);
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

        for (String[] pair : pairs) {
            double[] start = nodeCoordinates.get(pair[0]);
            double[] end = nodeCoordinates.get(pair[1]);
            double[][] forward = interpolate(start, end, 4);
            roadSegments.put(pair[0] + "->" + pair[1], asPointList(forward));
            roadSegments.put(pair[1] + "->" + pair[0], asPointList(reverse(forward)));
        }
    }

    private double[][] interpolate(double[] a, double[] b, int pointCount) {
        double[][] result = new double[pointCount][2];
        for (int i = 0; i < pointCount; i++) {
            double t = i / (double) (pointCount - 1);
            result[i][0] = a[0] + t * (b[0] - a[0]);
            result[i][1] = a[1] + t * (b[1] - a[1]);
        }
        return result;
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
}
