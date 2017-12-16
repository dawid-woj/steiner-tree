package pl.edu.pw.elka.gis.steinar.io;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import pl.edu.pw.elka.gis.steinar.model.SteinerGraph;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class STPSaver {
    static private final String SECTION_FORMATTER = "SECTION %s";
    static private final String COMM_NAME_FORMATTER = "Name %s";
    static private final String GRAPH_NODES_FORMATTER = "Nodes %d";
    static private final String GRAPH_EDGES_FORMATTER = "Edges %d";
    static private final String GRAPH_ONE_EDGE_FORMATTER = "E %s %s %d";
    static private final String TERMINAL_COUNT_FORMATTER = "Terminals %d";
    static private final String TERMINAL_ONE_FORMATTER = "T %s";

    public static void save(String filename, SteinerGraph steinerGraph) throws FileNotFoundException {
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

            //Secion Comment
            writer.println(String.format(SECTION_FORMATTER, "Comment"));
            writer.println(String.format(COMM_NAME_FORMATTER, steinerGraph.getName()));
            writer.println(STPCommons.END_SECTION);
            writer.println();
            //Section Graph
            writer.println(String.format(SECTION_FORMATTER, "Graph"));
            writer.println(String.format(GRAPH_NODES_FORMATTER, nodeSet.size()));
            writer.println(String.format(GRAPH_EDGES_FORMATTER, edgeHashMap.size()));
            edgeHashMap.forEach((s,edge) -> {
                writer.println(String.format(GRAPH_ONE_EDGE_FORMATTER, edge.getNode0().getId(), edge.getNode1().getId(), edge.getAttribute(SteinerGraph.WEIGHT_ATTR)));
            });
            writer.println(STPCommons.END_SECTION);
            writer.println();

            //Section Terminals
            List<Node> terminaList = nodeSet.stream()
                    .filter(node -> node.getAttribute(SteinerGraph.TERMINAL_ATTR, Boolean.class))
                    .collect(Collectors.toList());;

            writer.println(String.format(SECTION_FORMATTER, "Terminals"));
            writer.println(String.format(TERMINAL_COUNT_FORMATTER, terminaList.size()));
            terminaList.forEach(node -> {
                writer.println(String.format(TERMINAL_ONE_FORMATTER, node.getId()));
            });
            writer.println(STPCommons.END_SECTION);
            writer.println();
            //TODO Dodac zapisywanie rezultatow

            writer.println("EOF");

            writer.close();
        }
        catch (FileNotFoundException e) {
            throw new FileNotFoundException(filename);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace(); // Nie powinno sie zdarzyc jak mam zakodowane na stałe kodowanie
        }
    }
}
