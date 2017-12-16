package pl.edu.pw.elka.gis.steinar.io.exceptions;

public class NotConsistentFileException extends RuntimeException {
    public NotConsistentFileException(String reason) {
        super(reason);
    }
}
