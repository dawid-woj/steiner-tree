package pl.edu.pw.elka.gis.steinar.algorithms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.graphstream.algorithm.util.FibonacciHeap;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dawid on 29.12.17.
 */
@NoArgsConstructor
public abstract class AbstractSpanningTreeAlgorithm {

    @Getter
    @Setter
    private Graph graph;
    @Getter
    @Setter
    private String edgeWeightAttributeName;
    @Getter
    @Setter
    private String resultAttributeName;
    @Getter
    @Setter
    private String inSolutionAttributeName;
    @Getter
    @Setter
    private Node start;

    protected List<Edge> resultantTreeEdges;

    public void init(Graph graph, String startingNodeId, String edgeWeightAttributeName,
                     String resultAttributeName, String inSolutionAttributeName) {
        this.graph = graph;
        this.edgeWeightAttributeName = edgeWeightAttributeName;
        this.resultAttributeName = resultAttributeName;
        this.inSolutionAttributeName = inSolutionAttributeName;
        this.start = this.graph.getNode(startingNodeId);
        this.resultantTreeEdges = new LinkedList<>();
    }

    public void clear() {
        this.graph.getEdgeSet().forEach(edge -> edge.removeAttribute(inSolutionAttributeName));
        this.graph.getNodeSet().forEach(node -> node.removeAttribute(resultAttributeName));
        this.resultantTreeEdges.clear();
    }

    public void compute() {
        if (this.graph == null) {
            throw new IllegalStateException("Graph not specified!");
        }
        if (this.start == null) {
            throw new IllegalStateException("Start/source node not specified!");
        }

        coreAlgorithm();
    }

    protected static class NodeData {
        FibonacciHeap<Integer, Node>.Node nodeInQueue;
        Edge edgeFromParent;
        int distance;
    }

    protected void coreAlgorithm() {
        FibonacciHeap<Integer, Node> queue = new FibonacciHeap<>();

        for (Node node : this.graph) {
            NodeData data = new NodeData();
            int w = node == start ? 0 : Integer.MAX_VALUE;
            data.edgeFromParent = null;
            data.nodeInQueue = queue.add(w, node);
            node.addAttribute(resultAttributeName, data);
        }

        while (!queue.isEmpty()) {
            Node u = queue.extractMin();
            NodeData uData = u.getAttribute(resultAttributeName);
            uData.distance = uData.nodeInQueue.getKey();
            uData.nodeInQueue = null;

            if (uData.edgeFromParent != null) {
                addEdgeToSolution(uData.edgeFromParent);
            }

            for (Edge e : u.getEachLeavingEdge()) {
                Node v = e.getOpposite(u);
                NodeData vData = v.getAttribute(resultAttributeName);
                if (vData.nodeInQueue == null) {
                    continue;
                }

                int alt = relaxFunction(e, uData);

                if (alt < vData.nodeInQueue.getKey()) {
                    vData.edgeFromParent = e;
                    queue.decreaseKey(vData.nodeInQueue, alt);
                }
            }
        }
    }

    protected int getWeight(Edge edge) {
        return edge.getAttribute(edgeWeightAttributeName);
    }

    protected void addEdgeToSolution(Edge e) {
        e.addAttribute(inSolutionAttributeName, true);
        this.resultantTreeEdges.add(e);
    }

    protected abstract int relaxFunction(Edge uv, NodeData uData);

}
