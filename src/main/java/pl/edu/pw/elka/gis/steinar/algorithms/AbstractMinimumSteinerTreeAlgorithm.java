package pl.edu.pw.elka.gis.steinar.algorithms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import pl.edu.pw.elka.gis.steinar.model.SteinerGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dawid on 29.12.17.
 */
@NoArgsConstructor
public abstract class AbstractMinimumSteinerTreeAlgorithm {

    protected static final String DIJKSTRA_RESULT_ATTR = "dijkstra_result";
    protected static final String DIJKSTRA_SOLUTION_ATTR = "dijkstra_solution";
    protected static final String PRIM_RESULT_ATTR = "prim_result";
    protected static final String PRIM_SOLUTION_ATTR = "prim_solution";

    @Getter
    protected SteinerGraph steinerGraph;

    public void init(SteinerGraph steinerGraph) {
        this.steinerGraph = steinerGraph;
    }

    public void clear() {
        this.steinerGraph.clearSolution();

        for (Edge edge : this.steinerGraph.getGraph().getEdgeSet()) {
            edge.removeAttribute(PRIM_SOLUTION_ATTR);
            edge.removeAttribute(DIJKSTRA_SOLUTION_ATTR);
        }

        for (Node node : this.steinerGraph.getGraph().getNodeSet()) {
            node.removeAttribute(DIJKSTRA_RESULT_ATTR);
            node.removeAttribute(PRIM_RESULT_ATTR);
        }
    }

    public void compute() {
        if (this.steinerGraph == null) {
            throw new IllegalStateException("SteinerGraph not specified!");
        }

        if (isShortestPathProblem()) {
            findShortestPath();
        } else if (isMinimumSPanningTreeProblem()) {
            findMinimumSpanningTree();
        } else {
            findMinimumSteinerTree();
        }
    }

    private boolean isShortestPathProblem() {
        return this.steinerGraph.getTerminalCount() == 2;
    }

    private boolean isMinimumSPanningTreeProblem() {
        return this.steinerGraph.getTerminalCount() == this.steinerGraph.getNodeCount();
    }

    private void findShortestPath() {
        Graph graph = this.steinerGraph.getGraph();
        List<String> nodeIds = new ArrayList<>(this.steinerGraph.getTerminalNodeIds());

        Dijkstra dijkstra = new Dijkstra();
        dijkstra.init(graph, nodeIds.get(0), SteinerGraph.WEIGHT_ATTR, DIJKSTRA_RESULT_ATTR, DIJKSTRA_SOLUTION_ATTR);
        dijkstra.compute();

        List<Edge> edges = dijkstra.getShortestPathEdges(graph.getNode(nodeIds.get(1)));
        this.steinerGraph.setResultTreeEdges(edges);

        dijkstra.clear();
    }

    private void findMinimumSpanningTree() {
        Graph graph = this.steinerGraph.getGraph();
        Node startNode = getAnyNode();

        Prim prim = new Prim();
        prim.init(graph, startNode.getId(), SteinerGraph.WEIGHT_ATTR, PRIM_RESULT_ATTR, PRIM_SOLUTION_ATTR);
        prim.compute();

        List<Edge> edges = prim.getMinimumSpanningTreeEdges();
        this.steinerGraph.setResultTreeEdges(edges);

        prim.clear();
    }

    private Node getAnyNode() {
        return this.steinerGraph.getNodes().iterator().next();
    }


    protected abstract void findMinimumSteinerTree();

}
