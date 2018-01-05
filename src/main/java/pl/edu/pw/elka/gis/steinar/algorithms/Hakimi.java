package pl.edu.pw.elka.gis.steinar.algorithms;

import lombok.NoArgsConstructor;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import pl.edu.pw.elka.gis.steinar.algorithms.exceptions.HakimiTooBigGraphException;
import pl.edu.pw.elka.gis.steinar.model.SteinerGraph;
import scala.Int;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
public class Hakimi extends AbstractMinimumSteinerTreeAlgorithm {

    private static final String PRIM_RESULT_ATTR = "prim_result";
    private static final String PRIM_SOLUTION_ATTR = "prim_solution";

    @Override
    protected void findMinimumSteinerTree() {
        int minWeight = Integer.MAX_VALUE;
        List<Edge> minTree = new ArrayList<>();
        Set<Node> terminalNodes = Utils.getTerminalNodes(steinerGraph);

        List<Node> nonTerminals = Utils.getNonTerminalNodes(steinerGraph).stream().collect(Collectors.toList());

        if(nonTerminals.size() >= 64)
        {
            throw new HakimiTooBigGraphException();
        }

        long subsetsCount = 1l << nonTerminals.size();

        for (long i = 0; i < subsetsCount; ++i) {
            //Wylicz wierzchołki znajdujace sie w podzbioerze
            ArrayList<Node> nodeSubset = new ArrayList<>();
            long mask = 1;

            for (int k = 0; k < nonTerminals.size(); ++k) {
                if ((mask & i) != 0) {
                    nodeSubset.add(nonTerminals.get(k));
                }
                mask <<= 1;
            }

            //Oblicz minimalne drzewo oparte na nich
            nodeSubset.addAll(terminalNodes);
            Graph graph = Utils.getInducedSubgraph(steinerGraph.getGraph(), nodeSubset);
            Prim MST = new Prim();
            MST.init(graph, terminalNodes.iterator().next().getId(), SteinerGraph.WEIGHT_ATTR, PRIM_RESULT_ATTR, PRIM_SOLUTION_ATTR);
            MST.compute();

            int MSTweight = MST.getMinimumSpanningTreeWeight();

            //Czy drzewo zostało znalezione
            boolean hasFoundTree =  nodeSubset.size()-1 == MST.getMinimumSpanningTreeEdges().size();

            //Jesli tak to sprawdz czy jest ono najmniejsze
            if (hasFoundTree && MSTweight != 0 && minWeight > MSTweight) {
                minWeight = MSTweight;
                minTree = MST.getMinimumSpanningTreeEdges();
            }

        }
        steinerGraph.setResultTreeEdges(new HashSet<>(minTree));
    }
}
