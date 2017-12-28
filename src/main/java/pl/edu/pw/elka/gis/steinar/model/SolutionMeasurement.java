package pl.edu.pw.elka.gis.steinar.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SolutionMeasurement {
    private int length;
    private float time;
    private SteinerAlgorithmEnum algorithm;
}
