package com.securebank.graph;

import java.util.ArrayList;
import java.util.List;

public class TransactionGraph {

    private List<GraphNode> nodes = new ArrayList<>();
    private List<GraphEdge> edges = new ArrayList<>();
    private GraphAnalysisResult analysis;
    private GraphNetworkStats stats;

    public TransactionGraph() {
    }

    public TransactionGraph(List<GraphNode> nodes, List<GraphEdge> edges) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.edges = edges != null ? edges : new ArrayList<>();
    }

    public TransactionGraph(List<GraphNode> nodes, List<GraphEdge> edges,
                            GraphAnalysisResult analysis, GraphNetworkStats stats) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.edges = edges != null ? edges : new ArrayList<>();
        this.analysis = analysis;
        this.stats = stats;
    }

    public List<GraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<GraphNode> nodes) {
        this.nodes = nodes;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<GraphEdge> edges) {
        this.edges = edges;
    }

    public GraphAnalysisResult getAnalysis() {
        return analysis;
    }

    public void setAnalysis(GraphAnalysisResult analysis) {
        this.analysis = analysis;
    }

    public GraphNetworkStats getStats() {
        return stats;
    }

    public void setStats(GraphNetworkStats stats) {
        this.stats = stats;
    }
}
