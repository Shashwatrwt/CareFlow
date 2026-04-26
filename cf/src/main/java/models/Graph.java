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

    public void addEdge(String source, String destination, int weight) {
        addNode(source);
        addNode(destination);
        adjacencyList.get(source).add(new Edge(destination, weight));
        adjacencyList.get(destination).add(new Edge(source, weight));
    }

    public List<Edge> getEdges(String node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }

    public Map<String, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    public static class Edge {
        private String destination;
        private int weight;

        public Edge(String destination, int weight) {
            this.destination = destination;
            this.weight = weight;
        }

        public String getDestination() { return destination; }
        public int getWeight() { return weight; }

        @Override
        public String toString() {
            return "Edge{destination='" + destination + "', weight=" + weight + '}';
        }
    }
}
