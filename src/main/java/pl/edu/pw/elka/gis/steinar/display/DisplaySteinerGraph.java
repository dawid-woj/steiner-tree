package pl.edu.pw.elka.gis.steinar.display;

import lombok.Getter;
import lombok.Setter;
import org.graphstream.graph.Graph;
import org.graphstream.ui.view.Viewer;
import pl.edu.pw.elka.gis.steinar.model.SteinerGraph;

import static pl.edu.pw.elka.gis.steinar.model.SteinerGraph.*;

/**
 * Klasa wizualizacji grafów.
 */
public class DisplaySteinerGraph {

    static private DisplaySteinerGraph displaySteinerGraph;
    static private final String styleSheet =
            "edge {" +
                    "	fill-color: black; text-size: 15;" +
                    "}" +
                    "edge.solution {" +
                    "	fill-color: red;" +
                    "}" +
                    "node { " +
                    " fill-color: black; text-style: bold; text-alignment: left; text-size: 18;" +
                    "}" +
                    "node.terminal { " +
                    "   fill-color: green;" +
                    "}";

    private Viewer viewer;

    @Getter
    @Setter
    private SteinerGraph graph;

    private DisplaySteinerGraph() {}

    private void display() {
        if (graph != null) {
            final Graph streamGraph = graph.getGraph();

            streamGraph.addAttribute("ui.stylesheet", styleSheet);

            streamGraph.getEdgeSet().stream()
                    .filter(edge -> edge.hasAttribute(RESULT_TREE_ATTR) && edge.getAttribute(RESULT_TREE_ATTR, Boolean.class))
                    .forEach(edge -> edge.setAttribute("ui.class", "solution"));
            streamGraph.getEdgeSet().forEach(edge -> edge.setAttribute("ui.label", edge.getAttribute(WEIGHT_ATTR, Integer.class)));

            streamGraph.getNodeSet()
                    .stream()
                    .filter(node -> node.hasAttribute(TERMINAL_ATTR) && node.getAttribute(TERMINAL_ATTR, Boolean.class))
                    .forEach(node -> node.setAttribute("ui.class", "terminal"));
            streamGraph.getNodeSet().forEach(node -> node.setAttribute("ui.label", node.getId()));
            viewer = streamGraph.display();
        }
    }

    public static void showGraph(SteinerGraph graphToDisplay) {
        if (displaySteinerGraph == null) {
            displaySteinerGraph = new DisplaySteinerGraph();
        }
        displaySteinerGraph.setGraph(graphToDisplay);
        displaySteinerGraph.display();
    }

}
