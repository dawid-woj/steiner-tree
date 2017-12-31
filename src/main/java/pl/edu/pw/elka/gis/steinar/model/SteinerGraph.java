package pl.edu.pw.elka.gis.steinar.model;

import lombok.Getter;
import lombok.Setter;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import pl.edu.pw.elka.gis.steinar.algorithms.Utils;

import java.util.*;

//FIXME: powyrzucac niepotrzebne/nieuzywane metody
public class SteinerGraph {
    private static int NEW_GRAPH_ID = 0;

    public static final String WEIGHT_ATTR = "weight";
    public static final String TERMINAL_ATTR = "terminal";
    public static final String RESULT_TREE_ATTR = "result_tree_attr";

    @Getter
    private final Graph graph;
    @Getter
    @Setter
    private String name;
    @Getter
    private Set<String> terminalNodeIds = new HashSet<>();
    @Getter
    private List<Edge> resultTreeEdges = new ArrayList<>();

    public SteinerGraph() {
        graph = new SingleGraph(generateNewGraphID());
    }

    public SteinerGraph(SteinerGraph another) {
        this.name = another.name;
        this.graph = Utils.copyGraph(another.graph, generateNewGraphID());
        this.terminalNodeIds.addAll(another.terminalNodeIds);
        this.resultTreeEdges.addAll(another.resultTreeEdges);
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
        this.graph.removeNode(idNode);
        this.terminalNodeIds.remove(idNode);
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

    public void markAsTerminal(String nodeId) {
        Node node = graph.getNode(nodeId);
        node.changeAttribute(TERMINAL_ATTR, true);
        this.terminalNodeIds.add(nodeId);
    }

    public boolean isTerminal(Node node) {
        Boolean result =  node.getAttribute(TERMINAL_ATTR, Boolean.class);
        return result != null ? result : false;
    }

    public int getTerminalCount() {
        return this.terminalNodeIds.size();
    }

    public int getNodeCount() {
        return this.graph.getNodeCount();
    }

    public int getEdgeCount() {
        return this.graph.getEdgeCount();
    }

    public void markEdgeInResultTree(String idEdge) {
        graph.getEdge(idEdge).setAttribute(RESULT_TREE_ATTR, true);
    }

    public void markEdgeResultTree(Edge edge) {
        edge.setAttribute(RESULT_TREE_ATTR, true);
    }

    public void setResultTreeEdges(Collection<Edge> edges) {
        edges.forEach(this::markEdgeResultTree);
        this.resultTreeEdges.addAll(edges);
    }

    public int getResultTreeWeight() {
        int weight = 0;
        for (Edge edge : this.resultTreeEdges) {
            weight += edge.<Integer>getAttribute(SteinerGraph.WEIGHT_ATTR);
        }
        return weight;
    }

    public int getResultTreeEdgeCount() {
        return this.resultTreeEdges.size();
    }

    public boolean edgeIsResultTree(String idEdge) {
        Edge edge = graph.getNode(idEdge);
        Boolean result = false;
        if(edge.hasAttribute(RESULT_TREE_ATTR)) {
            result = edge.getAttribute(RESULT_TREE_ATTR, Boolean.class);
        }
        return result;
    }

    public void clearSolution() {
        this.graph.getEdgeSet().forEach(edge -> edge.removeAttribute(RESULT_TREE_ATTR));
        this.resultTreeEdges.clear();
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
