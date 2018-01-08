package pl.edu.pw.elka.gis.steinar.io;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import pl.edu.pw.elka.gis.steinar.model.SolutionMeasurement;
import pl.edu.pw.elka.gis.steinar.model.SteinerGraph;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Klasa zapisywania problemów (i ew. rozwiązań) minimalnego drzewa Steinera do plików STP.
 */
public class STPSaver {
    static private final String SECTION_FORMATTER = "SECTION %s";
    static private final String COMM_NAME_FORMATTER = "Name %s";
    static private final String GRAPH_NODES_FORMATTER = "Nodes %d";
    static private final String GRAPH_EDGES_FORMATTER = "Edges %d";
    static private final String GRAPH_ONE_EDGE_FORMATTER = "E %s %s %d";
    static private final String TERMINAL_COUNT_FORMATTER = "Terminals %d";
    static private final String TERMINAL_ONE_FORMATTER = "T %s";
    static private final String SOLUTION_ONE_FORMATTER = "S %s %s %d";


    public static void save(String filename, SteinerGraph steinerGraph, SolutionMeasurement solutionMeasurement)
            throws FileNotFoundException {
        Collection<Node> nodeSet = steinerGraph.getNodes();
        HashMap<String, Edge> edgeHashMap = new HashMap<>();

        nodeSet.forEach(node -> {
            Collection<Edge> edges = node.getEdgeSet();
            edges.forEach(edge -> edgeHashMap.put(edge.getId(), edge));
        });

        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.println(STPCommons.STP_HEADER);
            writer.println();

            //Section Result
            if (solutionMeasurement != null) {
                writer.println(String.format(SECTION_FORMATTER, "Result"));
                writer.println(String.format("Length %d", solutionMeasurement.getLength()));
                writer.println(String.format("Time %f", solutionMeasurement.getTime()));
                writer.println(String.format("Algorithm %s", solutionMeasurement.getAlgorithm().name()));
                writer.println(STPCommons.END_SECTION);
                writer.println();
            }

            //Section Comment
            writer.println(String.format(SECTION_FORMATTER, "Comment"));
            writer.println(String.format(COMM_NAME_FORMATTER, "\"" + steinerGraph.getName()) + "\"");
            writer.println(STPCommons.END_SECTION);
            writer.println();

            //Section Graph
            writer.println(String.format(SECTION_FORMATTER, "Graph"));
            writer.println(String.format(GRAPH_NODES_FORMATTER, nodeSet.size()));
            writer.println(String.format(GRAPH_EDGES_FORMATTER, edgeHashMap.size()));
            edgeHashMap.forEach((s, edge) ->
                    writer.println(String.format(GRAPH_ONE_EDGE_FORMATTER, edge.getNode0().getId(), edge.getNode1().getId(), edge.getAttribute(SteinerGraph.WEIGHT_ATTR, Integer.class)))
            );
            writer.println(STPCommons.END_SECTION);
            writer.println();

            //Section Terminals
            List<Node> terminalList = nodeSet.stream()
                    .filter(node -> node.getAttribute(SteinerGraph.TERMINAL_ATTR, Boolean.class))
                    .collect(Collectors.toList());

            writer.println(String.format(SECTION_FORMATTER, "Terminals"));
            writer.println(String.format(TERMINAL_COUNT_FORMATTER, terminalList.size()));
            terminalList.forEach(node ->
                    writer.println(String.format(TERMINAL_ONE_FORMATTER, node.getId()))
            );
            writer.println(STPCommons.END_SECTION);
            writer.println();

            //Section Terminals
            if (solutionMeasurement != null) {
                List<Edge> resultList = steinerGraph.getResultTreeEdges();
                writer.println(String.format(SECTION_FORMATTER, "Solution"));
                resultList.forEach(edge -> writer.println(String.format(SOLUTION_ONE_FORMATTER, edge.getNode0().getId(), edge.getNode1().getId(), edge.getAttribute(SteinerGraph.WEIGHT_ATTR, Integer.class))));
                writer.println(STPCommons.END_SECTION);
                writer.println();
            }

            writer.println("EOF");

            writer.close();

            System.out.println(String.format("Graph %s saved to file %s", steinerGraph.getName(), filename));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(filename);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
