package pl.edu.pw.elka.gis.steinar.algorithms;

import lombok.NoArgsConstructor;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dawid on 29.12.17.
 */
@NoArgsConstructor
public class Dijkstra extends AbstractSpanningTreeAlgorithm {

    public List<Path> getAllShortestPaths() {
        List<Path> paths = new LinkedList<>();

        for (Node node : getGraph()) {
            if (node != getStart()) {
                paths.add(getShortestPath(node));
            }
        }

        return paths;
    }

    public Path getShortestPath(Node target) {
        Path path = new Path();

        List<Edge> edges = getShortestPathEdges(target);
        for (Edge e : edges) {
            path.add(e);
        }

        return path;
    }

    public int getShortestPathWeight(Node target) {
       return target.<NodeData>getAttribute(getResultAttributeName()).distance;
    }

    public List<Node> getShortestPathNodes(Node target) {
        List<Node> nodes = new LinkedList<>();
        nodes.add(target);

        Node current = target;
        Edge edge = getEdgeFromParent(current);
        while (edge != null) {
            Node parent = getOtherNode(edge, current);
            nodes.add(parent);
            edge = getEdgeFromParent(parent);
            current = parent;
        }

        return nodes;
    }

    public List<Edge> getShortestPathEdges(Node target) {
        List<Edge> edges = new LinkedList<>();

        Node current = target;
        Edge edge = getEdgeFromParent(current);
        while (edge != null) {
            edges.add(edge);
            Node parent = getOtherNode(edge, current);
            edge = getEdgeFromParent(parent);
            current = parent;
        }

        return edges;
    }

    private Node getOtherNode(Edge edge, Node node) {
        return edge.getOpposite(node);
    }

    private Edge getEdgeFromParent(Node node) {
        NodeData nodeData = node.getAttribute(getResultAttributeName());
        return nodeData.edgeFromParent;
    }

    @Override
    protected int relaxFunction(Edge uv, NodeData uData) {
        return uData.distance + getWeight(uv);
    }

}
