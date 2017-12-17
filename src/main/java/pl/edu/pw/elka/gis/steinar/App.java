package pl.edu.pw.elka.gis.steinar;

import pl.edu.pw.elka.gis.steinar.display.DisplaySteinerGraph;
import pl.edu.pw.elka.gis.steinar.io.STPSaver;
import pl.edu.pw.elka.gis.steinar.io.exceptions.NotConsistentFileException;
import pl.edu.pw.elka.gis.steinar.io.STPLoader;
import pl.edu.pw.elka.gis.steinar.model.SolutionMeasurement;
import pl.edu.pw.elka.gis.steinar.model.SteinerAlgorithmEnum;
import pl.edu.pw.elka.gis.steinar.model.SteinerGraph;

import java.io.FileNotFoundException;


public class App {
    public static void main(String[] args) {

        try {
            STPLoader stpLoader = new STPLoader("steinlib/B/b02.stp");//"proste_grafy/g1.stp");
            //System.out.println(stpLoader.getResultGraph().getNodes());
            DisplaySteinerGraph.showGraph(stpLoader.getResultGraph());
            stpLoader.getResultGraph().getEdges("2").forEach( edge -> edge.setAttribute(SteinerGraph.RESULT_TREE_ATTR, true));
            DisplaySteinerGraph.showGraph(stpLoader.getResultGraph());
            STPSaver.save("out1.stp", stpLoader.getResultGraph(), new SolutionMeasurement(1, 2.0f, SteinerAlgorithmEnum.KMB));
        } catch (FileNotFoundException e) {
            System.out.println("Cant find a file" + e.getLocalizedMessage());
        } catch (NotConsistentFileException ex) {
            System.out.println("Problem with reading file: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            System.out.println("Scanner is closed: " + ex.getLocalizedMessage());
        }
    }
}
