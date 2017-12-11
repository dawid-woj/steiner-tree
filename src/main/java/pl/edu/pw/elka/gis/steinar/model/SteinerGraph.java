package pl.edu.pw.elka.gis.steinar.model;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class SteinerGraph {
    private static int NEW_GRPAH_ID = 0;

    public static final String WEIGHT_ATTR = "weight";
    public static final String TERMINAL_ATTR = "terminal";

    private final Graph graph;
    private String name;

    public SteinerGraph() {
        graph = new SingleGraph(generataNewGraphID());
    }

    public SteinerGraph(SteinerGraph another) {
        this.name = another.name;
        this.graph = another.graph;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Edge addEdge(String idNode1, String idNode2, Integer weight) {
        Edge edge = graph.addEdge(idNode1 + ":" + idNode2, idNode1, idNode2);
        edge.setAttribute(WEIGHT_ATTR, weight == null ? 0 : weight);
        return edge;
    }

    public Node addNode(String idNode) {
        Node node = graph.addNode(idNode);
        node.setAttribute(TERMINAL_ATTR, false);
        return node;
    }

    public void deleteNode(String idNode) {
        graph.removeNode(idNode);
    }

    public Collection<Node> getNodes() {
        return graph.getNodeSet();
    }

    public Collection<Edge> getEdges(Node node) {
        return node.getEdgeSet();
    }

    public Collection<Edge> getEdges(String idNode) {
        return getEdges(idNode);
    }

    public Integer getLength(Edge edge) {
        return edge.getAttribute("weight", Integer.class);
    }

    public Integer getLength(String idEdge) {
        Edge edge = graph.getEdge(idEdge);
        return getLength(edge);
    }

    public Integer getWeight(String nodeId) {
        return getWeight((Node) graph.getNode(nodeId));
    }

    public Integer getWeight(Node n) {
        return n.getAttribute(WEIGHT_ATTR, Integer.class);
    }

    public void setTerminal(String nodeId, boolean isTerminal) {
        Node node = graph.getNode(nodeId);
        node.setAttribute(TERMINAL_ATTR, isTerminal);
        //TODO problem z wczytaniem terminala
    }

    public boolean nodeIsTerminal(String nodeid) {
        return graph.getNode(nodeid).getAttribute(TERMINAL_ATTR, Boolean.class);
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("SteinerGraph{" +
                "graph=" + graph +
                '}');

        getNodes().forEach(node -> {
            out.append(node);
            boolean isTerminal = node.getAttribute(TERMINAL_ATTR, Boolean.class);
            if (isTerminal == true) {
                out.append(" T");
            }
            out.append(" = { ");
            getEdges(node).forEach(edge -> out.append(edge.getId() + "(" + edge.getAttribute(WEIGHT_ATTR) + ") "));
            out.append(" }\n");
        });
        return out.toString();
    }


    private static String generataNewGraphID() {
        return "Graph" + String.valueOf(NEW_GRPAH_ID++);
    }
}
