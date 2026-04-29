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

    public Graph getGraph() {
        return hospitalGraph;
    }

    public Dijkstra.Result findShortestPath(String source, String dest) {
        return Dijkstra.computeShortestPath(hospitalGraph, source, dest);
    }

    public Set<String> getNodeNames() {
        return nodeNames;
    }

    private void addEdge(Graph g, String a, String b, double distKm, double timeMin) {
        g.addEdge(a, b, distKm, timeMin);
    }

    private Graph buildCityGraph() {
        Graph graph = new Graph();

        addEdge(graph, "Careflow City Hospital", "Ballupur",        2.1,  5.0);
        addEdge(graph, "Careflow City Hospital", "Nehru Colony",    3.2,  7.0);
        addEdge(graph, "Careflow City Hospital", "Raipur",          4.5, 10.0);
        addEdge(graph, "Careflow City Hospital", "Patel Nagar",     3.8,  8.0);
        addEdge(graph, "Careflow City Hospital", "ISBT",            5.5, 12.0);
        addEdge(graph, "Careflow City Hospital", "Defence Colony",  2.8,  6.0);
        addEdge(graph, "Careflow City Hospital", "Canal Road",      3.5,  8.0);

        addEdge(graph, "ISBT", "Clement Town",                      2.2,  5.0);
        addEdge(graph, "ISBT", "Nehru Colony",                      3.1,  7.0);
        addEdge(graph, "ISBT", "Patel Nagar",                       2.7,  6.0);
        addEdge(graph, "ISBT", "Mothrowala",                        1.8,  4.0);

        addEdge(graph, "Ballupur", "Prem Nagar",                    4.0,  9.0);
        addEdge(graph, "Ballupur", "Rajpur Road",                   4.4, 10.0);

        addEdge(graph, "Prem Nagar", "Clement Town",                6.8, 15.0);
        addEdge(graph, "Rajpur Road", "Canal Road",                 2.2,  5.0);
        addEdge(graph, "Raipur", "Canal Road",                      4.5, 10.0);

        addEdge(graph, "Raipur", "Jogiwala",                        4.5, 10.0);
        addEdge(graph, "Nehru Colony", "Raipur",                    4.1,  9.0);
        addEdge(graph, "Clement Town", "Nehru Colony",              4.5, 10.0);
        addEdge(graph, "Nehru Colony", "Defence Colony",            3.2,  7.0);
        addEdge(graph, "Nehru Colony", "Jogiwala",                  2.7,  6.0);

        addEdge(graph, "Patel Nagar", "Defence Colony",             1.4,  3.0);
        addEdge(graph, "Patel Nagar", "Banjarawala",                2.7,  6.0);

        addEdge(graph, "Mothrowala", "Banjarawala",                 4.1,  9.0);
        addEdge(graph, "Mothrowala", "Jogiwala",                    3.2,  7.0);

        return graph;
    }
}

