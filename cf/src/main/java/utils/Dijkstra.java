package utils;

import models.Graph;
import models.Graph.Edge;

import java.util.*;

public class Dijkstra {

    public static Result computeShortestPath(Graph graph, String source, String dest) {
        if (!graph.getAdjacencyList().containsKey(source)) {
            return new Result(Collections.emptyList(), -1);
        }

        // BUG 1 FIX: Collect ALL nodes (sources + destinations) for dist map
        Set<String> allNodes = new HashSet<>(graph.getAdjacencyList().keySet());
        for (String node : graph.getAdjacencyList().keySet()) {
            for (Edge edge : graph.getEdges(node)) {
                allNodes.add(edge.getDestination());
            }
        }

        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));

        for (String node : allNodes) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(source, 0);
        pq.add(new Node(source, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (current.distance > dist.get(current.name)) continue;

            for (Edge edge : graph.getEdges(current.name)) {
                int newDist = current.distance + edge.getWeight();
                if (newDist < dist.getOrDefault(edge.getDestination(), Integer.MAX_VALUE)) {
                    dist.put(edge.getDestination(), newDist);
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
            return new Result(Collections.emptyList(), -1);
        }

        return new Result(path, dist.getOrDefault(dest, -1));
    }

    private static class Node {
        String name;
        int distance;

        Node(String name, int distance) {
            this.name = name;
            this.distance = distance;
        }
    }

    public static class Result {
        private final List<String> path;
        private final int distance;

        public Result(List<String> path, int distance) {
            this.path = path;
            this.distance = distance;
        }

        public List<String> getPath() { return path; }
        public int getDistance() { return distance; }
    }
}