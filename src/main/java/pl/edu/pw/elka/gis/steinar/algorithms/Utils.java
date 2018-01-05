package pl.edu.pw.elka.gis.steinar.algorithms;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import pl.edu.pw.elka.gis.steinar.model.SteinerGraph;

import java.util.*;

/**
 * Created by dawid on 31.12.17.
 */
public class Utils {

    /**
     * Dodanie (skopiowanie) danego węzła do grafu wraz z jego wszystkimi atrybutami.
     *
     * @param graph graf, do którego dodajemy węzeł
     * @param node  dodawany węzeł pochodzący z innego grafu
     * @return      dodany węzeł
     */
    public static Node addNodeWithAllAttributes(Graph graph, Node node) {
        Node addedNode = graph.addNode(node.getId());
        node.getEachAttributeKey().forEach(key -> {
            Object attr = node.getAttribute(key);
            addedNode.addAttribute(key, attr);
        });
        return addedNode;
    }

    /**
     * Dodanie (skopiowanie) danej krawędzi do grafu wraz z jej wszystkimi atrybutami.
     *
     * @param graph     graf, do którego dodajemy krawędź
     * @param edge      dodawana krawędź pochodząca z innego grafu
     * @param edgeId    id krawędzi (format: "node1Id:node2Id")
     * @param node1Id   id węzła nr 1 krawędzi
     * @param node2Id   id węzła nr 2 krawędzi
     * @return          dodana krawędź
     */
    public static Edge addEdgeWithAllAttributes(Graph graph, Edge edge, String edgeId, String node1Id, String node2Id) {
        Edge addedEdge = graph.addEdge(edgeId, node1Id, node2Id);
        edge.getEachAttributeKey().forEach(key -> {
            Object attr = edge.getAttribute(key);
            addedEdge.addAttribute(key, attr);
        });
        return addedEdge;
    }

    /**
     * Kopiowanie grafu (SingleGraph).
     *
     * @param graph kopiowany graf
     * @param newId id nowego grafu
     * @return      nowy graf
     */
    public static Graph copyGraph(Graph graph, String newId) {
        Graph newGraph = new SingleGraph(newId);

        graph.getNodeSet().forEach(node -> addNodeWithAllAttributes(newGraph, node));
        graph.getEdgeSet().forEach(edge -> addEdgeWithAllAttributes(newGraph, edge, edge.getId(),
                edge.getNode0().getId(), edge.getNode1().getId()));

        return newGraph;
    }

    /**
     * Wygenerowanie wszystkich możliwych id-ków par danych węzłów (id-ków potencjalnych krawędzi między nimi).
     *
     * @param nodes kolekcja węzłów grafu
     * @return      lista id-ków wszystkich par danych węzłów
     */
    public static List<String> getAllNodeIdPairs(Collection<Node> nodes) {
        List<String> pairNodeIds = new LinkedList<>();
        for (Node node1 : nodes) {
            String idNode1 = node1.getId();
            for (Node node2 : nodes) {
                String idNode2 = node2.getId();
                if (!idNode1.equals(idNode2)) {
                    pairNodeIds.add(idNode1 + ":" + idNode2);
                }
            }
        }
        return pairNodeIds;
    }

    /**
     * Zwraca zbiór wszystkich nieterminalnych węzłów danego grafu steinera.
     *
     * @param steinerGraph  graf steinera
     * @return              zbiór wierzchołków nieterminalnych
     */
    public static Set<Node> getNonTerminalNodes(SteinerGraph steinerGraph) {
        Set<Node> nonTerminals = new HashSet<>();
        for (Node node : steinerGraph.getNodes()) {
            if (!steinerGraph.isTerminal(node)) {
                nonTerminals.add(node);
            }
        }
        return nonTerminals;
    }

    /**
     * Zwraca zbiór wszystkich terminalnów węzłów danego grafu steinera.
     *
     * @param steinerGraph  graf steinera
     * @return              zbiór wierzchołków terminalnych
     */
    public static Set<Node> getTerminalNodes(SteinerGraph steinerGraph) {
        Set<Node> nonTerminals = new HashSet<>();
        for (Node node : steinerGraph.getNodes()) {
            if (steinerGraph.isTerminal(node)) {
                nonTerminals.add(node);
            }
        }
        return nonTerminals;
    }

    /**
     * Generowanie wszystkich podzbiorów danego zbioru węzłów grafu.
     *
     * @param nodes zbiór węzłów grafu
     * @return      lista wszystkich podzbiorów danego zbioru węzłów
     */
    public static List<List<Node>> getNodeSubsets(List<Node> nodes) {
        List<List<Node>> subsets = new ArrayList<>();
        int subsetsCount = 1 << nodes.size();

        for (int i = 0; i < subsetsCount; ++i) {
            ArrayList<Node> subset = new ArrayList<>();
            int mask = 1;

            for (int k = 0; k < nodes.size(); ++k) {
                if ((mask & i) != 0) {
                    subset.add(nodes.get(k));
                }
                mask <<= 1;
            }

            subsets.add(subset);
        }

        return subsets;
    }

    /**
     * Generowanie podgrafu indukowanego na zadanym zbiorze węzłów grafu.
     *
     * @param graph graf
     * @param nodes podzbiór węzłów grafu
     * @return      podgraf indukowany na podzbiorze węzłów grafu
     */
    public static Graph getInducedSubgraph(Graph graph, Collection<Node> nodes) {
        int maxNodeCount = nodes.size();
        int maxEdgeCount = maxNodeCount * (maxNodeCount-1) / 2;

        Graph newGraph = new SingleGraph(graph.getId() + "_induced", true, false,
                maxNodeCount, maxEdgeCount);

        for (Node node : nodes) {
            addNodeWithAllAttributes(newGraph, node);
        }

        List<String> nodePairIds = getAllNodeIdPairs(nodes);
        for (String id : nodePairIds) {
            Edge edge = graph.getEdge(id);
            if (edge != null) {
                String[] pair = id.split(":");
                addEdgeWithAllAttributes(newGraph, edge, id, pair[0], pair[1]);
            }
        }

        return newGraph;
    }

    /**
     * Zwraca graf (SingleGraph) jako rezultat usunięcia wszystkich węzłów terminalnych danego grafu steinera.
     *
     * @param steinerGraph  graf steinera
     * @return              graf bez węzłów terminalnych (SingleGraph)
     */
    public static Graph getGraphWithRemovedTerminalNodes(SteinerGraph steinerGraph) {
        Graph newGraph = new SingleGraph(steinerGraph.getGraph().getId() + "_induced", true, false,
                steinerGraph.getNodeCount(), steinerGraph.getEdgeCount());

        Collection<Node> nodes = steinerGraph.getNodes();

        for (Node node : nodes) {
            if (!steinerGraph.isTerminal(node)) {
                addNodeWithAllAttributes(newGraph, node);
            }
        }

        for (Node node : nodes) {
            node.getEachEdge().forEach(edge -> {
                Node n0 = edge.getNode0();
                Node n1 = edge.getNode1();
                if (newGraph.getNode(n0.getId()) != null && newGraph.getNode(n1.getId()) != null
                        && newGraph.getEdge(edge.getId()) == null) {
                    addEdgeWithAllAttributes(newGraph, edge, edge.getId(), n0.getId(), n1.getId());
                }
            });
        }

        return newGraph;
    }

    /**
     * Zwraca graf steinera (SteinerGraph) jako rezultat usunięcia wszystkich węzłów terminalnych danego grafu steinera.
     *
     * @param steinerGraph  graf steinera
     * @return              graf bez węzłów terminalnych (SteinerGraph)
     */
    public static SteinerGraph getSteinerGraphWithRemovedTerminalNodes(SteinerGraph steinerGraph) {
        SteinerGraph newGraph = new SteinerGraph(steinerGraph);
        newGraph.getTerminalNodeIds().forEach(newGraph::deleteNode);
        return newGraph;
    }

}
