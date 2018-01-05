package pl.edu.pw.elka.gis.steinar.algorithms.exceptions;

public class HakimiTooBigGraphException extends RuntimeException {
    public HakimiTooBigGraphException() {
        super("Graph has to have less then 64 non terminal nodes.");
    }
}
