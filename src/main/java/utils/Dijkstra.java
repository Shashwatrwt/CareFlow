package utils;

import models.Graph;
import models.Graph.Edge;

import java.util.*;

public class Dijkstra {

    public static Result computeShortestPath(Graph graph, String source, String dest) {
        if (!graph.getAdjacencyList().containsKey(source)) {
            return new Result(Collections.emptyList(), -1, -1, -1);
        }

        Set<String> allNodes = new HashSet<>(graph.getAdjacencyList().keySet());
        for (String node : graph.getAdjacencyList().keySet()) {
            for (Edge edge : graph.getEdges(node)) {
                allNodes.add(edge.getDestination());
            }
        }

        Map<String, Double> dist = new HashMap<>();
        Map<String, Double> totalDistance = new HashMap<>(); // track km separately
        Map<String, Double> totalTime = new HashMap<>();     // track min separately
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.weight));

        for (String node : allNodes) {
            dist.put(node, Double.MAX_VALUE);
            totalDistance.put(node, 0.0);
            totalTime.put(node, 0.0);
        }
        dist.put(source, 0.0);
        pq.add(new Node(source, 0.0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (current.weight > dist.get(current.name)) continue;

            for (Edge edge : graph.getEdges(current.name)) {
                double newDist = current.weight + edge.getWeight();
                if (newDist < dist.getOrDefault(edge.getDestination(), Double.MAX_VALUE)) {
                    dist.put(edge.getDestination(), newDist);
                    totalDistance.put(edge.getDestination(),
                        totalDistance.getOrDefault(current.name, 0.0) + edge.getDistanceKm());
                    totalTime.put(edge.getDestination(),
                        totalTime.getOrDefault(current.name, 0.0) + edge.getTimeMin());
                    prev.put(edge.getDestination(), current.name);
                    pq.add(new Node(edge.getDestination(), newDist));
                }
            }
        }

        List<String> path = new ArrayList<>();
        String at = dest;
        while (at != null) {
            path.add(0, at);
            at = prev.get(at);
        }

        if (path.isEmpty() || !path.get(0).equals(source)) {
            return new Result(Collections.emptyList(), -1, -1, -1);
        }

        return new Result(
            path,
            dist.getOrDefault(dest, -1.0),
            totalDistance.getOrDefault(dest, 0.0),
            totalTime.getOrDefault(dest, 0.0)
        );
    }

    private static class Node {
        String name;
        double weight;
        Node(String name, double weight) {
            this.name = name;
            this.weight = weight;
        }
    }

    public static class Result {
        private final List<String> path;
        private final double weight;       // combined weight
        private final double distanceKm;   // total km
        private final double timeMin;      // total minutes

        public Result(List<String> path, double weight, double distanceKm, double timeMin) {
            this.path = path;
            this.weight = weight;
            this.distanceKm = distanceKm;
            this.timeMin = timeMin;
        }

        public List<String> getPath()      { return path; }
        public double getDistance()         { return weight; }      // kept for compatibility
        public double getDistanceKm()       { return distanceKm; }
        public double getTimeMin()          { return timeMin; }
    }
}

