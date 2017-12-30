package pl.edu.pw.elka.gis.steinar;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import pl.edu.pw.elka.gis.steinar.algorithms.*;
import pl.edu.pw.elka.gis.steinar.display.DisplaySteinerGraph;
import pl.edu.pw.elka.gis.steinar.io.STPSaver;
import pl.edu.pw.elka.gis.steinar.io.exceptions.NotConsistentFileException;
import pl.edu.pw.elka.gis.steinar.io.STPLoader;
import pl.edu.pw.elka.gis.steinar.model.AlgorithmOutput;
import pl.edu.pw.elka.gis.steinar.model.SolutionMeasurement;
import pl.edu.pw.elka.gis.steinar.model.SteinerAlgorithmEnum;
import pl.edu.pw.elka.gis.steinar.model.SteinerGraph;

import java.io.FileNotFoundException;
import java.util.*;


public class App {

    // ********************************** STEINER TREE ALGORITHM USAGE DEMO **************************************

    public static final Map<SteinerAlgorithmEnum, AbstractMinimumSteinerTreeAlgorithm> steinerAlgorithms
            = new HashMap<>(2);
    static {
        steinerAlgorithms.put(SteinerAlgorithmEnum.HAKIMI, new Hakimi());
        steinerAlgorithms.put(SteinerAlgorithmEnum.KMB, new KMB());
    }

    public static SteinerGraph loadSteinerGraph(String filename) {
        try {
            STPLoader stpLoader = new STPLoader(filename);
            return stpLoader.getResultGraph();
        } catch (FileNotFoundException e) {
            System.out.println("Can't find an input STP file: " + e.getLocalizedMessage());
        } catch (NotConsistentFileException ex) {
            System.out.println("Problem with reading an input STP file: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            System.out.println("Scanner is closed: " + ex.getLocalizedMessage());
        }
        return null;
    }

    public static AlgorithmOutput findMinimalSteinerTree(SteinerGraph steinerGraph,
                                                         SteinerAlgorithmEnum steinerAlgorithm) {
        AbstractMinimumSteinerTreeAlgorithm algorithm = steinerAlgorithms.get(steinerAlgorithm);
        algorithm.init(steinerGraph);
        //TODO: timer start
        algorithm.compute();
        //TODO: timer stop
        float time = 0; //TODO: wziac wartosc z timera
        SolutionMeasurement solution = new SolutionMeasurement(algorithm.getSteinerGraph().getResultTreeWeight(),
                time, steinerAlgorithm);
        return new AlgorithmOutput(solution, algorithm.getSteinerGraph());
    }

    public static void printOutSolutionBasicInfo(AlgorithmOutput output) {
        String graphName = output.getGraph().getName();
        int graphNodeCount = output.getGraph().getNodeCount();
        int graphEdgeCount = output.getGraph().getEdgeCount();
        int terminalCount = output.getGraph().getTerminalCount();

        float algorithmRunningTime = output.getMeasurement().getTime(); //FIXME: jakie jednostki?
        String usedAlgorithmName = output.getMeasurement().getAlgorithm().toString();

        int steinerTreeNodeCount = output.getGraph().getTerminalCount();
        int steinerTreeEdgeCount = output.getGraph().getResultTreeEdgeCount();
        int steinerTreeWeight = output.getMeasurement().getLength();

        System.out.println("Graph name: " + graphName);
        System.out.println("Graph's node count: " + graphNodeCount);
        System.out.println("Graph's edge count: " + graphEdgeCount);
        System.out.println("Terminals count: " + terminalCount);

        System.out.println("\nAlgorithm used: " + usedAlgorithmName);
        System.out.println("Running time [s]: " + algorithmRunningTime);

        System.out.println("\nSteiner tree's node count: " + steinerTreeNodeCount);
        System.out.println("Steiner tree's edge count: " + steinerTreeEdgeCount);
        System.out.println("Steiner tree's weight: " + steinerTreeWeight);
    }

    public static void showSteinerGraph(SteinerGraph steinerGraph) {
        DisplaySteinerGraph.showGraph(steinerGraph);
    }

    public static void saveSolution(AlgorithmOutput output, String filename) {
        try {
            STPSaver.save(filename, output.getGraph(), output.getMeasurement());
        } catch (FileNotFoundException e) {
            System.out.println("Exception occurred during STP file saving: " + e.getLocalizedMessage());
        }
    }

    // ************************************* PRIM/DIJKSTRA TEST *********************************************

    public static Graph exampleGraph() {
        Graph g = new SingleGraph("example");
        g.addNode("A").addAttribute("xy", 0, 1);
        g.addNode("B").addAttribute("xy", 1, 2);
        g.addNode("C").addAttribute("xy", 1, 1);
        g.addNode("D").addAttribute("xy", 1, 0);
        g.addNode("E").addAttribute("xy", 2, 2);
        g.addNode("F").addAttribute("xy", 2, 1);
        g.addEdge("AB", "A", "B").addAttribute("length", 14);
        g.addEdge("AC", "A", "C").addAttribute("length", 9);
        g.addEdge("AD", "A", "D").addAttribute("length", 7);
        g.addEdge("BC", "B", "C").addAttribute("length", 2);
        g.addEdge("CD", "C", "D").addAttribute("length", 10);
        g.addEdge("BE", "B", "E").addAttribute("length", 9);
        g.addEdge("CF", "C", "F").addAttribute("length", 11);
        g.addEdge("DF", "D", "F").addAttribute("length", 15);
        g.addEdge("EF", "E", "F").addAttribute("length", 6);
        for (Node n : g)
            n.addAttribute("label", n.getId());
        for (Edge e : g.getEachEdge())
            e.addAttribute("label", "" + (int) e.getNumber("length"));
        return g;
    }

    public static void dijkstraTest() {
        Graph g = exampleGraph();
        g.display(false);

        Dijkstra algo = new Dijkstra();
        algo.init(g, "A", "length", "result", "solution");
        algo.compute();

        List<String> targetNodes = Arrays.asList("B", "C", "D", "E", "F");
        for (String nodeId : targetNodes) {
            Node targetNode = g.getNode(nodeId);

            System.out.println("path to " + nodeId + ":");
            System.out.println("   weight: " + algo.getShortestPathWeight(targetNode));

            System.out.print("   nodes: ");
            List<Node> nodes = algo.getShortestPathNodes(targetNode);
            ListIterator<Node> li = nodes.listIterator(nodes.size());
            while (li.hasPrevious()) {
                System.out.print(li.previous().getId() + " -> ");
            }
            System.out.println();

            System.out.print("   edges: ");
            List<Edge> edges = algo.getShortestPathEdges(targetNode);
            ListIterator<Edge> lii = edges.listIterator(edges.size());
            while (lii.hasPrevious()) {
                System.out.print(lii.previous().getId() + ", ");
            }
            System.out.println();
        }
        algo.clear();
    }

    public static void primTest() {
        Graph g = exampleGraph();
        String styleSheet =
                "edge { fill-color: black; } " +
                "node { fill-color: black; } " +
                "edge.solution { fill-color: red; size: 3px; }";
        g.addAttribute("ui.stylesheet", styleSheet);
        Viewer view = g.display(false);
        try { Thread.sleep(2000); } catch (Exception e) {}
        view.close();

        Prim algo = new Prim();
        algo.init(g, "A", "length", "result", "solution");

        List<String> targetNodes = Arrays.asList("A", "B", "C", "D", "E", "F");
        for (String nodeId : targetNodes) {
            algo.setStart(g.getNode(nodeId));
            algo.compute();

            System.out.println("MST starting from node " + nodeId + ": ");
            System.out.println("   weight: " + algo.getMinimumSpanningTreeWeight());
            System.out.print("   edges: ");
            for (Edge edge : algo.getMinimumSpanningTreeEdges()) {
                System.out.print(edge + ", ");
            }
            System.out.println();

            g.getEdgeSet().stream()
                    .filter(edge -> edge.hasAttribute("solution") && edge.getAttribute("solution", Boolean.class))
                    .forEach(edge -> edge.setAttribute("ui.class", "solution"));
            view = g.display(false);
            try { Thread.sleep(2000); } catch (Exception e) {}
            view.close();

            algo.clear();
        }
    }

    // ***************************************************************************************************

    public static void main(String[] args) {
//        dijkstraTest();
//        primTest();

        String inputGraphFilename = "proste_grafy/g1.stp";
        String solutionOutputFilename = "g1_solved.stp";
        SteinerAlgorithmEnum algorithm = SteinerAlgorithmEnum.HAKIMI;

        System.out.println("Loading graph and terminals from file: " + inputGraphFilename);
        SteinerGraph steinerGraph = loadSteinerGraph(inputGraphFilename);
        if (steinerGraph != null) {
//            System.out.println("Visualising loaded graph and terminals...");
//            showSteinerGraph(steinerGraph);
            System.out.println("Starting algorithm " + algorithm + "...");
            AlgorithmOutput output = findMinimalSteinerTree(steinerGraph, algorithm);
            System.out.println("...done.");
            printOutSolutionBasicInfo(output);
            System.out.println("Visualising graph with found minimal steiner tree...");
            showSteinerGraph(output.getGraph());
            System.out.println("Saving solution to file: " + solutionOutputFilename);
            saveSolution(output, solutionOutputFilename);
        }
        System.out.println("FINISHED");
    }

}
