package com.securebank.graph;

import java.util.HashMap;
import java.util.Map;

public class GraphNode {

    private String id;
    private String label;
    private GraphNodeType type;
    private Map<String, Object> properties = new HashMap<>();

    public GraphNode() {
    }

    public GraphNode(String id, String label, GraphNodeType type) {
        this.id = id;
        this.label = label;
        this.type = type;
    }

    public GraphNode(String id, String label, GraphNodeType type, Map<String, Object> properties) {
        this.id = id;
        this.label = label;
        this.type = type;
        if (properties != null) {
            this.properties = properties;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public GraphNodeType getType() {
        return type;
    }

    public void setType(GraphNodeType type) {
        this.type = type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public GraphNode addProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
        return this;
    }
}
