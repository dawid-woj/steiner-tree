package pl.edu.pw.elka.gis.steinar.algorithms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;
import pl.edu.pw.elka.gis.steinar.model.SteinerGraph;
import scala.Int;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dawid on 29.12.17.
 */
@NoArgsConstructor
public class KMB extends AbstractMinimumSteinerTreeAlgorithm
{

    @Getter
    @Setter
    @AllArgsConstructor
    class KMBRoad
    {
        private List<Edge> edgeList;
        private String name;
        private int length;
        private Node nodeStart, nodeEnd;

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            KMBRoad kmbRoad = (KMBRoad) o;

            return name != null ? name.equals(kmbRoad.name) : kmbRoad.name == null;
        }

        @Override
        public int hashCode()
        {
            return name != null ? name.hashCode() : 0;
        }
    }

    @Override
    protected void findMinimumSteinerTree()
    {
        //TODO Policz najkrotsze drogi miedzy wierzchołkam
        Set<String> terminalNodeIds = steinerGraph.getTerminalNodeIds();
        Set<Node> terminalNodes = steinerGraph.getNodes().stream().filter(n -> n.getAttribute(SteinerGraph.TERMINAL_ATTR, Boolean.class) == true).collect(Collectors.toSet());
        Map<String, KMBRoad> kmbRoads = new HashMap<>();

        for (Node n1 : terminalNodes)
        {
            Dijkstra dijkstra = new Dijkstra();
            dijkstra.init(steinerGraph.getGraph(), n1.getId(), SteinerGraph.WEIGHT_ATTR, DIJKSTRA_RESULT_ATTR, DIJKSTRA_SOLUTION_ATTR);
            dijkstra.compute();
            for (Node n2 : terminalNodes)
            {
                if (!n1.getId().equals(n2.getId()))
                {
                    String name;
                    if (Integer.valueOf(n1.getId()) > Integer.valueOf(n2.getId()))
                    {
                        name = n2.getId() + ":" + n1.getId();
                    }
                    else
                    {
                        name = n1.getId() + ":" + n2.getId();
                    }

                    //TODO Ograniczyc dijkstre do nieobliczonych drog
                    List<Edge> edges = dijkstra.getShortestPathEdges(n2);
                    int length = edges.stream().mapToInt(e -> e.getAttribute(SteinerGraph.WEIGHT_ATTR, Integer.class)).sum();

                    KMBRoad kmbRoad = new KMBRoad(edges, name, length, n1, n2);
                    kmbRoads.put(name, kmbRoad);
                }
            }
            dijkstra.clear();
        }
        kmbRoads.values().forEach(kmbRoad -> System.out.println(kmbRoad.getName() + " Dlugosc: " + kmbRoad.getLength()));
        Graph graphD = new SingleGraph("KMB_GRAPH");
        graphD.setAutoCreate(true);
        kmbRoads.values().forEach(kmbRoad ->
        {
            String startId = kmbRoad.getNodeStart().getId(), endId = kmbRoad.getNodeEnd().getId();
            if (graphD.getNode(startId) == null)
            {
                graphD.addNode(kmbRoad.getNodeStart().getId());
            }
            if (graphD.getNode(endId) == null)
            {
                graphD.addNode(kmbRoad.getNodeEnd().getId());
            }
            Edge edge = graphD.addEdge(kmbRoad.getName(), kmbRoad.getNodeStart().getId(), kmbRoad.getNodeEnd().getId());
            edge.setAttribute(SteinerGraph.WEIGHT_ATTR, kmbRoad.getLength());
        });

        Prim prim = new Prim();
        Node startNode = kmbRoads.values().iterator().next().getNodeStart();
        prim.init(graphD, startNode.getId(), SteinerGraph.WEIGHT_ATTR, PRIM_RESULT_ATTR, PRIM_SOLUTION_ATTR);
        prim.compute();
        List<Edge> primEdges = prim.getMinimumSpanningTreeEdges();

        Set<Edge> edges = new HashSet<>();
        primEdges.forEach(edge -> edges.addAll(kmbRoads.get(edge.getId()).getEdgeList()));
        this.steinerGraph.setResultTreeEdges(edges);


    }
}
