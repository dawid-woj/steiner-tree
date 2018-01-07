package pl.edu.pw.elka.gis.steinar.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Klasa reprezentująca wynik działania algorytmu Hakimi / KMB.
 */
@Setter
@Getter
@AllArgsConstructor
public class AlgorithmOutput {
    private SolutionMeasurement measurement;
    private SteinerGraph graph;
}
