package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private Map<String, List<Edge>> adjacencyList;

    public Graph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addNode(String node) {
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }

    // New: takes distance (km) and time (min) separately
    public void addEdge(String source, String destination, double distanceKm, double timeMin) {
        addNode(source);
        addNode(destination);
        double weight = 0.5 * distanceKm + 0.5 * timeMin;
        adjacencyList.get(source).add(new Edge(destination, distanceKm, timeMin, weight));
        adjacencyList.get(destination).add(new Edge(source, distanceKm, timeMin, weight));
    }

    public List<Edge> getEdges(String node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }

    public Map<String, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    public static class Edge {
        private final String destination;
        private final double distanceKm;
        private final double timeMin;
        private final double weight; // combined weight = 0.5*dist + 0.5*time

        public Edge(String destination, double distanceKm, double timeMin, double weight) {
            this.destination = destination;
            this.distanceKm = distanceKm;
            this.timeMin = timeMin;
            this.weight = weight;
        }

        public String getDestination() { return destination; }
        public double getDistanceKm()  { return distanceKm; }
        public double getTimeMin()     { return timeMin; }
        public double getWeight()      { return weight; }

        @Override
        public String toString() {
            return "Edge{to='" + destination + "', dist=" + distanceKm + "km, time=" + timeMin + "min, w=" + weight + '}';
        }
    }
}
