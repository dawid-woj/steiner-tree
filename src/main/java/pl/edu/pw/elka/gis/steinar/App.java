package pl.edu.pw.elka.gis.steinar;

import org.graphstream.graph.Graph;
import pl.edu.pw.elka.gis.steinar.algorithms.*;
import pl.edu.pw.elka.gis.steinar.display.DisplaySteinerGraph;
import pl.edu.pw.elka.gis.steinar.io.CSVWriter;
import pl.edu.pw.elka.gis.steinar.io.STPSaver;
import pl.edu.pw.elka.gis.steinar.io.exceptions.NotConsistentFileException;
import pl.edu.pw.elka.gis.steinar.io.STPLoader;
import pl.edu.pw.elka.gis.steinar.model.*;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * Główna klasa programu, stanowiąca moduł kontrolny aplikacji.
 */
public class App {

    private static final Map<SteinerAlgorithmEnum, AbstractMinimumSteinerTreeAlgorithm> steinerAlgorithms
            = new HashMap<>(2);
    static {
        steinerAlgorithms.put(SteinerAlgorithmEnum.HAKIMI, new Hakimi());
        steinerAlgorithms.put(SteinerAlgorithmEnum.KMB, new KMB());
    }

    // Nazwy katalogów z grafami wejściowymi:
    public static final String RES_SIMPLE_GRAPHS_DIRNAME = "proste_grafy/";
    public static final String RES_STEINLIB_GRAPHS_DIRNAME = "steinlib/wybrane/";
    public static final String RES_GENERATED_GRAPHS_DIRNAME = "wygenerowane_grafy/";
    // Nazwa katalogu dla wyników testów:
    public static final String RESULTS_DIRNAME = "rozwiazania/";

    /**
     * ************************* ODCZYT / ZAPIS GRAFÓW I WYNIKÓW ALGORYTMÓW *****************************
     */

    private static SteinerGraph loadSteinerGraph(String filename) {
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

    private static void saveSolution(AlgorithmOutput output, String filename) {
        try {
            STPSaver.save(filename, output.getGraph(), output.getMeasurement());
        } catch (FileNotFoundException e) {
            System.out.println("Exception occurred during STP file saving: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private static void saveSteinerGraph(SteinerGraph graph, String filename) {
        try {
            STPSaver.save(filename, graph, null);
        } catch (FileNotFoundException e) {
            System.out.println("Exception occurred during STP file saving: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * ********************** WYKONYWANIE ALGORYTMÓW KMB/HAKIMI I WIZUALIZACJA GRAFÓW **************************
     */

    private static AlgorithmOutput findMinimalSteinerTree(SteinerGraph steinerGraph,
                                                         SteinerAlgorithmEnum steinerAlgorithm) {
        AbstractMinimumSteinerTreeAlgorithm algorithm = steinerAlgorithms.get(steinerAlgorithm);
        algorithm.init(steinerGraph);

        long t = System.currentTimeMillis();
        algorithm.compute();
        long td = System.currentTimeMillis() - t;

        SolutionMeasurement solution = new SolutionMeasurement(algorithm.getSteinerGraph().getResultTreeWeight(),
                (float)(td)/1000, steinerAlgorithm);
        return new AlgorithmOutput(solution, algorithm.getSteinerGraph());
    }

    private static void printOutSolutionBasicInfo(AlgorithmOutput output) {
        String graphName = output.getGraph().getName();
        int graphNodeCount = output.getGraph().getNodeCount();
        int graphEdgeCount = output.getGraph().getEdgeCount();
        int terminalCount = output.getGraph().getTerminalCount();

        float algorithmRunningTime = output.getMeasurement().getTime();
        String usedAlgorithmName = output.getMeasurement().getAlgorithm().toString();

        int steinerTreeEdgeCount = output.getGraph().getResultTreeEdgeCount();
        int steinerTreeWeight = output.getMeasurement().getLength();

        System.out.println("Graph name: " + graphName);
        System.out.println("Graph's node count: " + graphNodeCount);
        System.out.println("Graph's edge count: " + graphEdgeCount);
        System.out.println("Terminals count: " + terminalCount);

        System.out.println("\nAlgorithm used: " + usedAlgorithmName);
        System.out.println("Running time [s]: " + algorithmRunningTime);

        System.out.println("Steiner tree's edge count: " + steinerTreeEdgeCount);
        System.out.println("Steiner tree's weight: " + steinerTreeWeight);
    }

    private static void showSteinerGraph(SteinerGraph steinerGraph) {
        DisplaySteinerGraph.showGraph(steinerGraph);
    }

    /**
     * ************************* GENERACJA GRAFÓW PEŁNYCH I KRAT DO TESTOWANIA *****************************
     */

    private static void generateAndSaveFullConnectedSteinerGraphs() {
        //int[] nodeCounts = {12, 14, 16, 18, 20, 22, 24, 26};
        int[] nodeCounts = {28, 30, 32, 34, 36};
        int terminals = 10;
        for (int nodes : nodeCounts) {
            String name = "full_" + nodes;
            Graph g = GraphGeneration.generateFullConnectedGraph(name, nodes);
            SteinerGraph sg = new SteinerGraph(name, g, GraphGeneration.randomTerminalIds(terminals, nodes));
            saveSteinerGraph(sg, name + ".stp");
        }
    }

    private static void generateAndSaveGridSteinerGraphs() {
        int[] nodeCounts = {9, 16, 25, 36};
        int terminals = 6;
        for (int nodes : nodeCounts) {
            String name = "grid_" + nodes;
            Graph g = GraphGeneration.generateGridGraph(name, nodes);
            SteinerGraph sg = new SteinerGraph(name, g, GraphGeneration.randomTerminalIds(terminals, nodes));
            saveSteinerGraph(sg, name + ".stp");
        }
    }

    /**
     * ***************************** TESTOWANIE ALGORYTMÓW KMB/HAKIMI **********************************
     */

    private static void runTests(CSVWriter resultsWriter, String inputGraphFilenamePrefix, String[] graphNames,
                                 int[] optimumWeights, SteinerAlgorithmEnum algoType) {
        int i = 0;
        for (String name : graphNames) {
            String inputFilename = inputGraphFilenamePrefix + name + ".stp";
            String outputFilename = RESULTS_DIRNAME + name + "_" + algoType + ".stp";

            System.out.println("Loading graph and terminals from file: " + inputFilename);
            SteinerGraph steinerGraph = loadSteinerGraph(inputFilename);

            if (steinerGraph != null) {
                AlgorithmOutput output = new AlgorithmOutput();

                int max = 3;
                float avgTime = 0f;
                for (int k = 1; k <= max; ++k) {
                    System.out.println("   Starting algorithm " + algoType + "(" + k + "/" + max + ")...");
                    output = findMinimalSteinerTree(steinerGraph, algoType);
                    avgTime += output.getMeasurement().getTime();
                    System.out.println("   ...done. Time [s]: " + output.getMeasurement().getTime());
                    if (k < max) {
                        steinerGraph.clearSolution();
                    }
                }
                avgTime /= max;
                output.getMeasurement().setTime(avgTime);

                System.out.println("   Saving solution to file: " + outputFilename);
                saveSolution(output, outputFilename);

                System.out.println("   Appending solution info to results file...");
                if (optimumWeights != null) {
                    resultsWriter.append(output, optimumWeights[i++]);
                } else {
                    resultsWriter.append(output, null);
                }
            } else {
                System.out.println("SteinerGraph is null!");
                return;
            }
        }
    }

    private static void runSimpleGraphTests(SteinerAlgorithmEnum algoType) {
        String[] graphNames = {"g1b", "g1d", "g2a", "g2b", "g2c", "g3a", "g3b", "g4a", "g4b", "g5"};
        int[] optimumWeights = {5, 7, 14, 13, 16, 14, 6, 8, 7, 28};

        CSVWriter resultsWriter = new CSVWriter(RESULTS_DIRNAME + "proste_grafy_wyniki_" + algoType + ".csv");
        resultsWriter.deleteFileIfExists();
        resultsWriter.writeHeader();

        runTests(resultsWriter, RES_SIMPLE_GRAPHS_DIRNAME, graphNames, optimumWeights, algoType);
    }

    private static void runSteinlibGraphTests(SteinerAlgorithmEnum algoType) {
        String[] graphNames = {"b05", "b06", "b10", "b11", "b18", "c11", "c12", "c13", "c14", "c15", "c19", "c20",
                "d19", "d20", "e06", "p455", "p457", "p459", "p461", "p463", "p464", "p465", "p466"};
        int[] optimumWeights = {61, 122, 86, 88, 218, 32, 46, 258, 323, 556, 146, 267,
                310, 537, 73, 1138, 1609, 2345, 4474, 1510, 2545, 3853, 6234};

        CSVWriter resultsWriter = new CSVWriter(RESULTS_DIRNAME + "steinlib_wyniki_" + algoType + ".csv");
        resultsWriter.deleteFileIfExists();
        resultsWriter.writeHeader();

        runTests(resultsWriter, RES_STEINLIB_GRAPHS_DIRNAME, graphNames, optimumWeights, algoType);
    }

    private static void runGeneratedFullConnectedGraphTests(SteinerAlgorithmEnum algoType) {
        String[] graphNames = {"full_12", "full_14", "full_16", "full_18", "full_20", "full_22", "full_24", "full_26",
        "full_28", "full_30", "full_32", "full_34", "full_36"};

        CSVWriter resultsWriter = new CSVWriter(RESULTS_DIRNAME + "grafy_pelne_wyniki_" + algoType + ".csv");
        resultsWriter.deleteFileIfExists();
        resultsWriter.writeHeader();

        runTests(resultsWriter, RES_GENERATED_GRAPHS_DIRNAME, graphNames, null, algoType);
    }

    private static void runGeneratedGridGraphTests(SteinerAlgorithmEnum algoType) {
        String[] graphNames = {"grid_9", "grid_16", "grid_25", "grid_36"};

        CSVWriter resultsWriter = new CSVWriter(RESULTS_DIRNAME + "kraty_wyniki_" + algoType + ".csv");
        resultsWriter.deleteFileIfExists();
        resultsWriter.writeHeader();

        runTests(resultsWriter, RES_GENERATED_GRAPHS_DIRNAME, graphNames, null, algoType);
    }

    /**
     * ************************* METODA MAIN *****************************
     */

    public static void main(String[] args) {
//        generateAndSaveFullConnectedSteinerGraphs();
//        generateAndSaveGridSteinerGraphs();

        runSimpleGraphTests(SteinerAlgorithmEnum.HAKIMI);
        runSimpleGraphTests(SteinerAlgorithmEnum.KMB);

        runGeneratedFullConnectedGraphTests(SteinerAlgorithmEnum.KMB);
        runGeneratedGridGraphTests(SteinerAlgorithmEnum.KMB);

        runSteinlibGraphTests(SteinerAlgorithmEnum.KMB);

        runGeneratedFullConnectedGraphTests(SteinerAlgorithmEnum.HAKIMI);
        runGeneratedGridGraphTests(SteinerAlgorithmEnum.HAKIMI);

//        String inputGraphFilename = "test.stp";
//        String solutionOutputFilename = "aaa.stp";
//        SteinerAlgorithmEnum algorithm = SteinerAlgorithmEnum.KMB;
//
//        System.out.println("Loading graph and terminals from file: " + inputGraphFilename);
//        SteinerGraph steinerGraph = loadSteinerGraph(inputGraphFilename);
//        if (steinerGraph != null) {
//            System.out.println("Starting algorithm " + algorithm + "...");
//            AlgorithmOutput output = findMinimalSteinerTree(steinerGraph, algorithm);
//            System.out.println("...done.");
//            printOutSolutionBasicInfo(output);
//            System.out.println("Visualising graph with found minimal steiner tree...");
//            showSteinerGraph(output.getGraph());
//            System.out.println("Saving solution to file: " + solutionOutputFilename);
//            saveSolution(output, solutionOutputFilename);
//        }
//        System.out.println("FINISHED");
    }

}
