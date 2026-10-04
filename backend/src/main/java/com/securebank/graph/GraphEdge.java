package com.securebank.graph;

public class GraphEdge {

    private String source;
    private String target;
    private String relationship;
    private String label;

    public GraphEdge() {
    }

    public GraphEdge(String source, String target, String relationship) {
        this.source = source;
        this.target = target;
        this.relationship = relationship;
    }

    public GraphEdge(String source, String target, String relationship, String label) {
        this.source = source;
        this.target = target;
        this.relationship = relationship;
        this.label = label;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
