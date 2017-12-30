package pl.edu.pw.elka.gis.steinar.algorithms;

import org.graphstream.graph.Edge;

import java.util.List;

/**
 * Created by dawid on 29.12.17.
 */
public class Prim extends AbstractSpanningTreeAlgorithm {

    public int getMinimumSpanningTreeWeight() {
        int weight = 0;
        for (Edge edge : this.resultantTreeEdges) {
            weight += edge.<Integer>getAttribute(getEdgeWeightAttributeName());
        }
        return weight;
    }

    public List<Edge> getMinimumSpanningTreeEdges() {
        return this.resultantTreeEdges;
    }

    @Override
    protected int relaxFunction(Edge uv, NodeData uData) {
        return getWeight(uv);
    }

}
