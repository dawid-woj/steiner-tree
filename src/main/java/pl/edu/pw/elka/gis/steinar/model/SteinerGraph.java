package pl.edu.pw.elka.gis.steinar.model;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.Collection;


public class SteinerGraph {
    private static int NEW_GRAPH_ID = 0;

    public static final String WEIGHT_ATTR = "weight";
    public static final String TERMINAL_ATTR = "terminal";
    public static final String RESULT_TREE_ATTR = "result_tree_attr";

    private final Graph graph;
    private String name;

    public SteinerGraph() {
        graph = new SingleGraph(generateNewGraphID());
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

    public Edge getEdge(String idNode0, String idNode1) {
        return graph.getNode(idNode0).getEdgeToward(idNode1);
    }


    public Collection<Edge> getEdges(Node node) {
        return node.getEdgeSet();
    }

    public Collection<Edge> getEdges(String idNode) {
        Node node = graph.getNode(idNode);
        return getEdges(node);
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
    }

    public boolean nodeIsTerminal(String nodeid) {
        Boolean result =  graph.getNode(nodeid).getAttribute(TERMINAL_ATTR, Boolean.class);
        return result != null ? result : false;
    }

    public void setEdgeResultTree(String idEdge, boolean isResultTree) {
        graph.getEdge(idEdge).setAttribute(RESULT_TREE_ATTR, isResultTree);
    }

    public boolean edgeIsResultTree(String idEdge) {
        Boolean result = graph.getNode(idEdge).getAttribute(RESULT_TREE_ATTR, Boolean.class);
        return result != null ? result : false;
    }

    public void clearSolution() {
        graph.getEdgeSet().forEach(edge -> edge.setAttribute(RESULT_TREE_ATTR, false));
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("SteinerGraph{ graph= '");
        out.append(graph);
        out.append('}');

        getNodes().forEach(node -> {
            out.append(node);
            boolean isTerminal = node.getAttribute(TERMINAL_ATTR, Boolean.class);
            if (isTerminal) {
                out.append(" T");
            }
            out.append(" = { ");
            getEdges(node).forEach(edge -> {
                out.append(edge.getId());
                out.append("(");
                out.append(edge.getAttribute(WEIGHT_ATTR).toString());
                out.append(") ");
            });
            out.append(" }\n");
        });
        return out.toString();
    }


    private static String generateNewGraphID() {
        return "Graph" + String.valueOf(NEW_GRAPH_ID++);
    }
}
