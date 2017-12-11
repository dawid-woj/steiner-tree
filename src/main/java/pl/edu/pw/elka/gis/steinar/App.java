package pl.edu.pw.elka.gis.steinar;

import pl.edu.pw.elka.gis.steinar.io.NotConsistentFileException;
import pl.edu.pw.elka.gis.steinar.io.STPLoader;

import java.io.FileNotFoundException;

/**
 * Created by Lukier on 2017-12-04.
 */
public class App {
    public static void main(String[] args) {
        STPLoader stpLoader = new STPLoader();
        try {
            stpLoader.loadFile("steinlib/C/c12.stp"); //"proste_grafy/g1.stp");
        } catch (FileNotFoundException e) {
            System.out.println("Cant find a file" + e.getLocalizedMessage());
        } catch (NotConsistentFileException ex) {
            System.out.println("Problem with reading file: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            System.out.println("Scanner is closed: " + ex.getLocalizedMessage());
        }
        SteinerGraph.run();

    }
}
