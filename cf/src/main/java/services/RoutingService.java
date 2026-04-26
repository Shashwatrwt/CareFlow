package services;

import models.Graph;
import utils.Dijkstra;

import java.util.*;

public class RoutingService {

    private final Graph hospitalGraph;
    private final Set<String> nodeNames;

    public RoutingService() {
        this.hospitalGraph = buildCityGraph();
        this.nodeNames = Collections.unmodifiableSet(
            new HashSet<>(hospitalGraph.getAdjacencyList().keySet())
        );
    }

    public Dijkstra.Result findShortestPath(String source, String dest) {
        return Dijkstra.computeShortestPath(hospitalGraph, source, dest);
    }

    public Set<String> getNodeNames() {
        return nodeNames;
    }

    // BUG 2 FIX: Helper to add edges in both directions
    private void addBiEdge(Graph g, String a, String b, int weight) {
        g.addEdge(a, b, weight);
        g.addEdge(b, a, weight);
    }

    private Graph buildCityGraph() {
        Graph graph = new Graph();

        // Dehradun city ambulance network (weights in approximate travel minutes)
        addBiEdge(graph, "Careflow City Hospital", "Ballupur",       10);
        addBiEdge(graph, "Careflow City Hospital", "Nehru Colony",    8);
        addBiEdge(graph, "Careflow City Hospital", "Raipur",         11);
        addBiEdge(graph, "Careflow City Hospital", "Patel Nagar",     9);
        addBiEdge(graph, "Careflow City Hospital", "ISBT",           15);
        addBiEdge(graph, "Careflow City Hospital", "Defence Colony", 10);
        addBiEdge(graph, "ISBT",          "Clement Town",    10);
        addBiEdge(graph, "ISBT",          "Nehru Colony",     9);
        addBiEdge(graph, "ISBT",          "Patel Nagar",     10);
        addBiEdge(graph, "ISBT",          "Mothrowala",      11);
        addBiEdge(graph, "Ballupur",      "Prem Nagar",      12);
        addBiEdge(graph, "Ballupur",      "Rajpur Road",     11);
        addBiEdge(graph, "Prem Nagar",    "Clement Town",    18);
        addBiEdge(graph, "Rajpur Road",   "Canal Road",      10);
        addBiEdge(graph, "Raipur",        "Jogiwala",        12);
        addBiEdge(graph, "Nehru Colony",  "Raipur",          14);
        addBiEdge(graph, "Clement Town",  "Nehru Colony",    14);
        addBiEdge(graph, "Nehru Colony",  "Defence Colony",   8);
        addBiEdge(graph, "Nehru Colony",  "Jogiwala",        13);
        addBiEdge(graph, "Patel Nagar",   "Defence Colony",   7);
        addBiEdge(graph, "Patel Nagar",   "Banjarawala",     10);
        addBiEdge(graph, "Mothrowala",    "Banjarawala",      9);
        addBiEdge(graph, "Mothrowala",    "Jogiwala",         8);

        return graph;
    }

    public Graph getHospitalGraph() {
        return hospitalGraph;
    }
}