package pl.edu.pw.elka.gis.steinar.algorithms;

import lombok.NoArgsConstructor;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dawid on 29.12.17.
 *
 * Implementacja algorytmu Dijkstry (wyszukiwania najkrótszych ścieżek z zadanego wierzchołku
 * do wszystkich pozostałych).
 */
@NoArgsConstructor
public class Dijkstra extends AbstractSpanningTreeAlgorithm {

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
