package pl.edu.pw.elka.gis.steinar.io;

import lombok.RequiredArgsConstructor;
import pl.edu.pw.elka.gis.steinar.model.AlgorithmOutput;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * Created by dawid on 07.01.18.
 */
@RequiredArgsConstructor
public class CSVWriter {

    private final String HEADER = "Nazwa grafu,Liczba wierzchołków,Liczba krawędzi,Liczba terminali," +
            "Czas wykonania [s],Koszt drzewa Steinera,Liczba krawędzi drzewa Steinera";

    private final String filename;

    public void deleteFileIfExists() {
        try {
            File file = new File(this.filename);
            Files.deleteIfExists(file.toPath());
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    private void appendContent(String content) {
        File file = new File(this.filename);
        try {
            Files.write(file.toPath(), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    public void writeHeader() {
        appendContent(HEADER + System.lineSeparator());
    }

    public void append(AlgorithmOutput output) {
        String graphName = output.getGraph().getName();
        int graphNodeCount = output.getGraph().getNodeCount();
        int graphEdgeCount = output.getGraph().getEdgeCount();
        int terminalCount = output.getGraph().getTerminalCount();
        float algorithmRunningTime = output.getMeasurement().getTime();
        int steinerTreeEdgeCount = output.getGraph().getResultTreeEdgeCount();
        int steinerTreeWeight = output.getMeasurement().getLength();

        String line = graphName + "," + graphNodeCount + "," + graphEdgeCount + "," + terminalCount + "," +
                algorithmRunningTime + "," + steinerTreeWeight + "," + steinerTreeEdgeCount + System.lineSeparator();
        appendContent(line);
    }


}
