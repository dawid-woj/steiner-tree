package pl.edu.pw.elka.gis.steinar.model;

import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.algorithm.generator.FullGenerator;
import org.graphstream.algorithm.generator.GridGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Metody automatycznej generacji grafów i losowych zbiorów terminali.
 */
public class GraphGeneration {

    /**
     * Generuje losową listę identyfikatorów wierzchóków terminalnych.
     *
     * @param terminalsCount    liczba terminali do wylosowania
     * @param nodesCount        liczba wszystkich wierzchołków grafu (maksymalna dopuszczalna wartość identyfikatora
     *                          losowanego terminala)
     * @return                  lista identyfikatorów terminali
     */
    public static List<String> randomTerminalIds(int terminalsCount, int nodesCount) {
        return ThreadLocalRandom.current().ints(1, nodesCount+1)
                .distinct().limit(terminalsCount).mapToObj(Integer::toString).collect(Collectors.toList());
    }

    /**
     * Generuje graf pełny o zadanej liczbie wierzchołków.
     *
     * @param graphId       identyfikator generowanego grafu
     * @param nodeCount     liczba wierzchołków generowanego grafu
     * @return              wygenerowany graf
     */
    public static Graph generateFullConnectedGraph(String graphId, int nodeCount) {
        Graph graph = new SingleGraph(graphId);

        BaseGenerator gen = new FullGenerator() {
            @Override
            public void begin() {
                nodeNames = 1;
                String id = Integer.toString(nodeNames++);
                addNode(id);
            }

            @Override
            protected void addNode(String id) {
                sendNodeAdded(sourceId, id);

                if (addNodeLabels)
                    sendNodeAttributeAdded(sourceId, id, "label", id);

                sendNodeAttributeAdded(sourceId, id, SteinerGraph.TERMINAL_ATTR, false);

                internalGraph.addNode(id);

                double value;

                for (String attr : nodeAttributes) {
                    value = (random.nextDouble() * (nodeAttributeRange[1] - nodeAttributeRange[0]))
                            + nodeAttributeRange[0];
                    sendNodeAttributeAdded(sourceId, id, attr, value);

                    internalGraph.getNode(id).addAttribute(attr, value);
                }
            }

            @Override
            protected void addEdge(String id, String from, String to) {
                if (directed && randomlyDirected && (random.nextFloat() > 0.5f)) {
                    String tmp = from;
                    from = to;
                    to = tmp;
                }

                if (id == null) {
                    if (from.compareTo(to) < 0) {
                        id = from + ":" + to;
                    } else {
                        id = to + ":" + from;
                    }
                }

                sendEdgeAdded(sourceId, id, from, to, directed);

                internalGraph.addEdge(id, from, to, directed);

                if (addEdgeLabels)
                    sendEdgeAttributeAdded(sourceId, id, "label", id);

                for (String attr : edgeAttributes) {
                    int value = random.nextInt((20 - 1) + 1) + 1;
                    sendEdgeAttributeAdded(sourceId, id, attr, value);

                    internalGraph.getEdge(id).addAttribute(attr, value);
                }
            }
        };
        gen.addEdgeAttribute(SteinerGraph.WEIGHT_ATTR);
        gen.setEdgeAttributesRange(1.0, 20.0);
        gen.addSink(graph);

        gen.begin();
        for (int i = 0; i < nodeCount-1; ++i) {
            gen.nextEvents();
        }
        gen.end();

        return graph;
    }

    /**
     * Generuje regularną kratę o zadanej liczbie wierzchołków.
     *
     * @param graphId       identyfikator generowanego grafu
     * @param nodeCount     liczba wierzchołków generowanego grafu (powinna być kwadratem liczby naturalnej)
     * @return              wygenerowany graf
     */
    public static Graph generateGridGraph(String graphId, int nodeCount) {
        Graph graph = new SingleGraph(graphId);

        BaseGenerator gen = new GridGenerator(false, false, false) {
            private int nodeNr = 1;
            private Map<String, String> nodeIdsMap = new HashMap<>();

            @Override
            public String nodeName(int x, int y) {
                String key = Integer.toString(x) + "_" + Integer.toString(y);
                if (!nodeIdsMap.containsKey(key)) {
                    nodeIdsMap.put(key, Integer.toString(nodeNr));
                }
                return key;
            }

            @Override
            protected void addNode(String _id) {
                String id = nodeIdsMap.get(_id);
                nodeNr++;

                sendNodeAdded(sourceId, id);

                if (addNodeLabels)
                    sendNodeAttributeAdded(sourceId, id, "label", id);

                sendNodeAttributeAdded(sourceId, id, SteinerGraph.TERMINAL_ATTR, false);

                double value;

                for (String attr : nodeAttributes) {
                    value = (random.nextDouble() * (nodeAttributeRange[1] - nodeAttributeRange[0]))
                            + nodeAttributeRange[0];
                    sendNodeAttributeAdded(sourceId, id, attr, value);
                }
            }

            @Override
            protected void addEdge(String id, String from, String to) {
                if (directed && randomlyDirected && (random.nextFloat() > 0.5f)) {
                    String tmp = from;
                    from = to;
                    to = tmp;
                }

                if (id == null) {
                    if (from.compareTo(to) < 0) {
                        id = from + ":" + to;
                    } else {
                        id = to + ":" + from;
                        String tmp = from;
                        from = to;
                        to = tmp;
                    }
                }

                sendEdgeAdded(sourceId, id, from, to, directed);

                if (addEdgeLabels)
                    sendEdgeAttributeAdded(sourceId, id, "label", id);

                for (String attr : edgeAttributes) {
                    int value = random.nextInt((20 - 1) + 1) + 1;
                    sendEdgeAttributeAdded(sourceId, id, attr, value);
                }
            }

            @Override
            public boolean nextEvents() {
                currentSize++;

                for (int y = 0; y < currentSize; ++y) {
                    String id = nodeName(currentSize, y);

                    addNode(id, currentSize, y);
                    addEdge(null, nodeIdsMap.get(nodeName(currentSize - 1, y)), nodeIdsMap.get(id));

                    if (y > 0) {
                        addEdge(null, nodeIdsMap.get(nodeName(currentSize, y - 1)), nodeIdsMap.get(id));

                        if (cross) {
                            addEdge(null, nodeName(currentSize - 1, y - 1), nodeIdsMap.get(id));
                            addEdge(null, nodeIdsMap.get(nodeName(currentSize, y - 1)),
                                    nodeIdsMap.get(nodeName(currentSize - 1, y)));
                        }
                    }
                }

                for (int x = 0; x <= currentSize; ++x) {
                    String id = nodeName(x, currentSize);

                    addNode(id, x, currentSize);
                    addEdge(null, nodeIdsMap.get(nodeName(x, currentSize - 1)), nodeIdsMap.get(id));

                    if (x > 0) {
                        addEdge(null, nodeIdsMap.get(nodeName(x - 1, currentSize)), nodeIdsMap.get(id));

                        if (cross) {
                            addEdge(null, nodeIdsMap.get(nodeName(x - 1, currentSize - 1)), nodeIdsMap.get(id));
                            addEdge(null, nodeIdsMap.get(nodeName(x - 1, currentSize)),
                                    nodeIdsMap.get(nodeName(x, currentSize - 1)));
                        }
                    }
                }

                return true;
            }

            @Override
            public void end() {
                if (tore) {
                    if (currentSize > 0) {
                        for (int y = 0; y <= currentSize; ++y) {
                            addEdge(null, nodeIdsMap.get(nodeName(currentSize, y)),
                                    nodeIdsMap.get(nodeName(0, y)));

                            if (cross) {
                                if (y > 0) {
                                    addEdge(null, nodeIdsMap.get(nodeName(currentSize, y)),
                                            nodeIdsMap.get(nodeName(0, y - 1)));
                                    addEdge(null, nodeIdsMap.get(nodeName(currentSize, y - 1)),
                                            nodeIdsMap.get(nodeName(0, y)));
                                }
                            }
                        }

                        for (int x = 0; x <= currentSize; ++x) {
                            addEdge(null, nodeIdsMap.get(nodeName(x, currentSize)),
                                    nodeIdsMap.get(nodeName(x, 0)));

                            if (cross) {
                                if (x > 0) {
                                    addEdge(null, nodeIdsMap.get(nodeName(x, currentSize)),
                                            nodeIdsMap.get(nodeName(x - 1, 0)));
                                    addEdge(null, nodeIdsMap.get(nodeName(x - 1, currentSize)),
                                            nodeIdsMap.get(nodeName(x, 0)));
                                }
                            }
                        }

                        if (cross) {
                            addEdge(null, nodeIdsMap.get(nodeName(currentSize, 0)),
                                    nodeIdsMap.get(nodeName(0, currentSize)));
                            addEdge(null, nodeIdsMap.get(nodeName(0, 0)),
                                    nodeIdsMap.get(nodeName(currentSize, currentSize)));
                        }
                    }
                }

                super.end();
            }
        };
        gen.addEdgeAttribute(SteinerGraph.WEIGHT_ATTR);
        gen.setEdgeAttributesRange(1.0, 20.0);
        gen.addSink(graph);

        int iterations = (int)Math.sqrt(nodeCount)-1;

        gen.begin();
        for (int i = 0; i < iterations; ++i) {
            gen.nextEvents();
        }
        gen.end();

        return graph;
    }

}
