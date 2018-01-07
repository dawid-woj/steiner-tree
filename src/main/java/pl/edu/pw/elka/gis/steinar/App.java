package pl.edu.pw.elka.gis.steinar;

import org.jfree.data.io.CSV;
import pl.edu.pw.elka.gis.steinar.algorithms.*;
import pl.edu.pw.elka.gis.steinar.display.DisplaySteinerGraph;
import pl.edu.pw.elka.gis.steinar.io.CSVWriter;
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

    private static final Map<SteinerAlgorithmEnum, AbstractMinimumSteinerTreeAlgorithm> steinerAlgorithms
            = new HashMap<>(2);
    static {
        steinerAlgorithms.put(SteinerAlgorithmEnum.HAKIMI, new Hakimi());
        steinerAlgorithms.put(SteinerAlgorithmEnum.KMB, new KMB());
    }

    public static final String RES_SIMPLE_GRAPHS_DIRNAME = "proste_grafy/";
    public static final String RES_STEINLIB_GRAPHS_DIRNAME = "steinlib/wybrane/";
    public static final String RES_GENERATED_GRAPHS_DIRNAME = "wygenerowane_grafy/";
    public static final String RESULTS_DIRNAME = "rozwiazania/";

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

    private static void saveSolution(AlgorithmOutput output, String filename) {
        try {
            STPSaver.save(filename, output.getGraph(), output.getMeasurement());
        } catch (FileNotFoundException e) {
            System.out.println("Exception occurred during STP file saving: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private static void runTests(CSVWriter resultsWriter, String inputGraphFilenamePrefix, String[] graphNames,
                                 SteinerAlgorithmEnum algoType) {
        for (String name : graphNames) {
            String inputFilename = inputGraphFilenamePrefix + name + ".stp";
            String outputFilename = RESULTS_DIRNAME + name + "_" + algoType + ".stp";

            System.out.println("Loading graph and terminals from file: " + inputFilename);
            SteinerGraph steinerGraph = loadSteinerGraph(inputFilename);

            if (steinerGraph != null) {
                System.out.println("   Starting algorithm " + algoType + "...");
                AlgorithmOutput output = findMinimalSteinerTree(steinerGraph, algoType);
                System.out.println("   ...done. Time [s]: " + output.getMeasurement().getTime());

                System.out.println("   Saving solution to file: " + outputFilename);
                saveSolution(output, outputFilename);

                System.out.println("   Appending solution info to results file...");
                resultsWriter.append(output);
            } else {
                System.out.println("SteinerGraph is null!");
                return;
            }
        }
    }

    private static void runSimpleGraphTests(SteinerAlgorithmEnum algoType) {
        String[] graphNames = {"g1b", "g1d", "g2a", "g2b", "g2c", "g3a", "g3b", "g4a", "g4b", "g5"};

        CSVWriter resultsWriter = new CSVWriter(RESULTS_DIRNAME + "proste_grafy_wyniki_" + algoType + ".csv");
        resultsWriter.deleteFileIfExists();
        resultsWriter.writeHeader();

        runTests(resultsWriter, RES_SIMPLE_GRAPHS_DIRNAME, graphNames, algoType);
    }

    private static void runSteinlibGraphTests(SteinerAlgorithmEnum algoType) {
        String[] graphNames = {/*TODO*/};

        CSVWriter resultsWriter = new CSVWriter(RESULTS_DIRNAME + "steinlib_wyniki_" + algoType + ".csv");
        resultsWriter.deleteFileIfExists();
        resultsWriter.writeHeader();

        runTests(resultsWriter, RES_STEINLIB_GRAPHS_DIRNAME, graphNames, algoType);
    }

    private static void runGeneratedGraphTests(SteinerAlgorithmEnum algoType) {
        String[] graphNames = {/*TODO*/};

        CSVWriter resultsWriter = new CSVWriter(RESULTS_DIRNAME + "wygenerowane_grafy_wyniki_" + algoType + ".csv");
        resultsWriter.deleteFileIfExists();
        resultsWriter.writeHeader();

        runTests(resultsWriter, RES_GENERATED_GRAPHS_DIRNAME, graphNames, algoType);
    }

    public static void main(String[] args) {
        runSimpleGraphTests(SteinerAlgorithmEnum.HAKIMI);
//        runSimpleGraphTests(SteinerAlgorithmEnum.KMB);

//        String inputGraphFilename = "steinlib/E/e20.stp";
//        String solutionOutputFilename = "e20_solved.stp";
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
