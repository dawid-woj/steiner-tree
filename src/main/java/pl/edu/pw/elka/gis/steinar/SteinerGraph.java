package pl.edu.pw.elka.gis.steinar;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;


/**
 * Created by luko on 08.12.2017.
 */
public class SteinerGraph
{
    public static final String WEIGHT_ATTR = "weight";
    public static final String TERMINAL_ATTR = "terminal";

    private final Graph graph;

    public SteinerGraph(String name) {
        graph = new SingleGraph(name);
    }

    public Edge addEdge(String idNode1, String idNode2, Integer weight) {
        Edge edge = graph.addEdge(idNode1+":"+idNode2, idNode1, idNode2);
        edge.setAttribute(WEIGHT_ATTR, weight == null ? 0 : weight);
        return edge;
    }

    public Node addNode(String idNode) {
        Node node = graph.addNode(idNode);
        node.setAttribute(TERMINAL_ATTR, false);
        return node;
    }

    public Integer getWeight(String nodeId) {
        return getWeight((Node)graph.getNode(nodeId));
    }

    public Integer getWeight(Node n) {
        return n.getAttribute(WEIGHT_ATTR, Integer.class);
    }

    public void setTerminal(String nodeId, boolean isTerminal) {
        Node node = graph.getNode(nodeId);
        node.setAttribute(TERMINAL_ATTR, isTerminal);
    }

    public boolean nodeIsTerminal(String nodeid) {
        return graph.getNode(nodeid).getAttribute(TERMINAL_ATTR, Boolean.class);
    }


    @Override
    public String toString()
    {
        return "SteinerGraph{" +
                "graph=" + graph +
                '}';
    }

    public static void run() {
        SteinerGraph g = new SteinerGraph("DYPA");
        g.addNode("A");
        Node node = g.addNode("B");
        g.addEdge("A", "B", 1);

        System.out.println(node.getEdgeSet());
        System.out.println(g);
    }
}
