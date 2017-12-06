package pl.edu.pw.elka.gis.steinar.io;

public class NotConsistentFileException extends RuntimeException {
    public NotConsistentFileException(String reason) {
        super(reason);
    }
}
