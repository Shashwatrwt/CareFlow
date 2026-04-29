package utils;

import models.Graph;
import models.Graph.Edge;

import java.util.*;


public class Dijkstra {

    public static Result computeShortestPath(Graph graph, String source, String dest) {
        if (!graph.getAdjacencyList().containsKey(source)) {
            return Result.EMPTY;
        }

        Map<String, Double> distance = new HashMap<>();
        Map<String, Double> distanceKm = new HashMap<>();
        Map<String, Double> timeMin = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));

        for (String node : graph.getAdjacencyList().keySet()) {
            distance.put(node, Double.MAX_VALUE);
            distanceKm.put(node, 0.0);
            timeMin.put(node, 0.0);
        }
        distance.put(source, 0.0);
        pq.add(new Node(source, 0.0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (current.distance > distance.get(current.name)) continue;

            for (Edge edge : graph.getEdges(current.name)) {
                double newDist = current.distance + edge.getWeight();
                if (newDist < distance.getOrDefault(edge.getDestination(), Double.MAX_VALUE)) {
                    distance.put(edge.getDestination(), newDist);
                    distanceKm.put(edge.getDestination(), 
                        distanceKm.getOrDefault(current.name, 0.0) + edge.getDistanceKm());
                    timeMin.put(edge.getDestination(), 
                        timeMin.getOrDefault(current.name, 0.0) + edge.getTimeMin());
                    previous.put(edge.getDestination(), current.name);
                    pq.add(new Node(edge.getDestination(), newDist));
                }
            }
        }

        List<String> path = reconstructPath(dest, previous, source);
        if (path.isEmpty()) {
            return Result.EMPTY;
        }

        return new Result(
            path,
            distanceKm.getOrDefault(dest, 0.0),
            timeMin.getOrDefault(dest, 0.0)
        );
    }

    private static List<String> reconstructPath(String dest, Map<String, String> previous, String source) {
        List<String> path = new ArrayList<>();
        String current = dest;
        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }
        return (path.isEmpty() || !path.get(0).equals(source)) ? Collections.emptyList() : path;
    }

    private static class Node {
        final String name;
        final double distance;

        Node(String name, double distance) {
            this.name = name;
            this.distance = distance;
        }
    }

    public static class Result {
        private final List<String> path;
        private final double distanceKm;
        private final double timeMin;

        public static final Result EMPTY = new Result(Collections.emptyList(), -1, -1);

        public Result(List<String> path, double distanceKm, double timeMin) {
            this.path = path;
            this.distanceKm = distanceKm;
            this.timeMin = timeMin;
        }

        public List<String> getPath() { return path; }
        public double getDistanceKm() { return distanceKm; }
        public double getTimeMin() { return timeMin; }
        
        public double getDistance() { return timeMin; }

        public boolean isValid() { return !path.isEmpty() && distanceKm >= 0; }
    }
}
