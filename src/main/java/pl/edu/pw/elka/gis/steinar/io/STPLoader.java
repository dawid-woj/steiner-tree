package pl.edu.pw.elka.gis.steinar.io;

import pl.edu.pw.elka.gis.steinar.model.SteinerGraph;

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
    static private final Pattern GRAPH_NODES_PATTERN = Pattern.compile("Nodes\\s+(\\d+)");//\\s*()");
    static private final Pattern GRAPH_EDGES_PATTERN = Pattern.compile("Edges\\s+(\\d+)");
    static private final Pattern GRAPH_ONE_EDGE_PATTERN = Pattern.compile("E\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    static private final Pattern TERMINAL_COUNT_PATTERN = Pattern.compile("Terminals\\s+(\\d+)");
    static private final Pattern TERMINAL_ONE_PATTERN = Pattern.compile("T\\s+(\\d+)");

    private final SteinerGraph resultGraph = new SteinerGraph();
    private Scanner scanner;

    public SteinerGraph getResultGraph() {
        return resultGraph;
    }

    public STPLoader(String filename) throws FileNotFoundException {
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
            }
        } finally {
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
                resultGraph.setName(mather.group(1));
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
                    for (Integer i = 0; i < nodes; ++i) {
                        Integer idNew = i + 1;
                        resultGraph.addNode(idNew.toString());
                    }
                }
                matcher = GRAPH_EDGES_PATTERN.matcher(line);
                if (matcher.matches()) {
                    if (edges != null) {
                        throw new NotConsistentFileException("Second edges count in Graph section");
                    }
                    edges = Integer.valueOf(matcher.group(1));
                }
                matcher = GRAPH_ONE_EDGE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    if (nodes == null || edges == null) {
                        throw new NotConsistentFileException("No nodes or edges count information before edge description.");
                    }
                    resultGraph.addEdge(matcher.group(1), matcher.group(2), Integer.valueOf(matcher.group(3)));
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
                }

                matcher = TERMINAL_ONE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    if (terminalsCount == null) {
                        throw new NotConsistentFileException("No terminal count information before edge description.");
                    }
                    resultGraph.setTerminal(matcher.group(1), true);
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
