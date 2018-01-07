package pl.edu.pw.elka.gis.steinar.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Klasa reprezentująca rezultat działania algorytmu KMB / Hakimi (informację o koszcie drzewa Steinera,
 * czasie wykonania algorytmu i type wykonywanego algorytmu).
 */
@Getter
@Setter
@AllArgsConstructor
public class SolutionMeasurement {
    private int length;
    private float time; // w sekundach
    private SteinerAlgorithmEnum algorithm;
}
