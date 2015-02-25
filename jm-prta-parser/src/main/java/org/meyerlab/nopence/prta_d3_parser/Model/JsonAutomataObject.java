package org.meyerlab.nopence.prta_d3_parser.Model;

/**
 * @author Dennis Meyer
 */
public class JsonAutomataObject {

    private JsonNode[] nodes;
    private JsonEdge[] edges;
    private JsonAttribute[] attributes;

    public JsonNode[] getNodes() {
        return nodes;
    }

    public void setNodes(JsonNode[] nodes) {
        this.nodes = nodes;
    }

    public JsonEdge[] getEdges() {
        return edges;
    }

    public void setEdges(JsonEdge[] edges) {
        this.edges = edges;
    }

    public JsonAttribute[] getAttributes() {
        return attributes;
    }

    public void setAttributes(JsonAttribute[] attributes) {
        this.attributes = attributes;
    }
}
