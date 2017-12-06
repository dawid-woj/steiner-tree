package pl.edu.pw.elka.gis.steinar.io;

import pl.edu.pw.elka.gis.steinar.model.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class STPLoader {
    static private final String STP_HEADER = "33D32945 STP File, STP Format Version 1.0";
    static private final String END_SECTION = "END";

    static private final Pattern SECTION_PATTERN = Pattern.compile("SECTION\\s+(\\w+)");
    static private final Pattern COMM_NAME_PATTERN = Pattern.compile("Name\\s+\"(.*)\"");
    static private final Pattern GRAPH_NODES_PATTERN = Pattern.compile("Nodes\\s+(.*)");//\\s*(\\d+)");
    static private final Pattern GRAPH_EDGES_PATTERN = Pattern.compile("Edges\\s+(.*)");
    static private final Pattern GRAPH_ONE_EDGE_PATTERN = Pattern.compile("E\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    static private final Pattern TERMINAL_COUNT_PATTERN = Pattern.compile("Terminals\\s+(\\d+)");
    static private final Pattern TERMINAL_ONE_PATTERN = Pattern.compile("T\\s+(\\d+)");

    private Graph resultGraph = new Graph();
    private Scanner scanner;
    //TODO rozwiazac kwestie zapisywania do grafu
    //TODO nowa lepsza impplemnatcja grafu

    public void loadFile(String filename) throws FileNotFoundException {
        StringBuilder result = new StringBuilder("");
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            File file = new File(classLoader.getResource(filename).getFile());
            scanner = new Scanner(file);
        } catch (NullPointerException ex) {
            throw new FileNotFoundException(filename);
        }

        try {
            readAndCheckHeader();

            String line;
            while (!((line = readNextLine()).equals("EOF"))) {
                Matcher mather = SECTION_PATTERN.matcher(line);

                if (mather.matches()) {
                    String sectionName = mather.group(1);
                    switch (sectionName) {
                        case "Comment":
                            loadComment();
                            break;
                        case "Graph":
                            loadGraph();
                            break;
                        case "Terminals":
                            loadTerminals();
                            break;
                        default:
                            throw new NotConsistentFileException("Cannot recognize section");
                    }
                }

                if (!scanner.hasNextLine()) {
                    throw new NotConsistentFileException("Missing EOF.");
                }
                result.append(line).append("\n");
            }
            System.out.println(result);
        }
        finally {
            scanner.close();
        }
    }

    private void readAndCheckHeader() {
        if (!readNextLine().equals(STP_HEADER)) {
            throw new NotConsistentFileException("Wrong file header.");
        }
    }

    private void loadComment() {
        String line;
        while (!(line = readNextLine()).equals(END_SECTION)) {
            Matcher mather = COMM_NAME_PATTERN.matcher(line);
            if (mather.matches()) {
                //TODO dodac zapisywanie do grafu
                System.out.println("Graph name:\t" + mather.group(1));
            }
        }

    }

    private void loadGraph() {
        try {
            Integer nodes = null, edges = null;
            String line;

            while (!(line = readNextLine()).equals(END_SECTION)) {
                Matcher matcher = GRAPH_NODES_PATTERN.matcher(line);
                if (matcher.matches()) {
                    if (nodes != null) {
                        throw new NotConsistentFileException("Second nodes count in Graph section");
                    }
                    String sys = matcher.group(1);
                    nodes = Integer.valueOf(sys);
                    System.out.println("Nodes: " + nodes);
                }
                matcher = GRAPH_EDGES_PATTERN.matcher(line);
                if (matcher.matches()) {
                    if (edges != null) {
                        throw new NotConsistentFileException("Second edges count in Graph section");
                    }
                    edges = Integer.valueOf(matcher.group(1));
                    System.out.println("Edges: " + edges);
                }
                matcher = GRAPH_ONE_EDGE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    if (nodes == null || edges == null) {
                        throw new NotConsistentFileException("No nodes or edges count information before edge description.");
                    }
                    resultGraph.addEdge(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)), Integer.valueOf(matcher.group(3)));
                }

            }
            System.out.println(resultGraph);

        } catch (IndexOutOfBoundsException | IllegalStateException ex) {
            throw new NotConsistentFileException("Cannot read Graph section. " + ex.getMessage());
        }
    }

    private void loadTerminals() {
        try {
            Integer terminalsCount = null;
            String line;

            while (!(line = readNextLine()).equals(END_SECTION)) {
                Matcher matcher = TERMINAL_COUNT_PATTERN.matcher(line);
                if (matcher.matches()) {
                    if (terminalsCount != null) {
                        throw new NotConsistentFileException("Second terminal in Graph section");
                    }
                    terminalsCount = Integer.valueOf(matcher.group(1));
                    System.out.println("Terminal count: " + terminalsCount);
                }

                matcher = TERMINAL_ONE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    if (terminalsCount == null) {
                        throw new NotConsistentFileException("No terminal count information before edge description.");
                    }
                    System.out.println("Node " + matcher.group(1) + " is terminal. ");

                }
            }
        } catch (IndexOutOfBoundsException | IllegalStateException ex) {
            throw new NotConsistentFileException("Cannot read Terminal section. " + ex.getMessage());
        }
    }


    private String readNextLine() {
        if (!scanner.hasNextLine()) {
            throw new NotConsistentFileException("Missing line.");
        }
        return scanner.nextLine();
    }
}
