package pl.edu.pw.elka.gis.steinar.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Lukier on 2017-12-06.
 */
public class Graph {

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    private class Edge {
        private int end;
        private int wage;
    }

    private HashMap<Integer, List<Edge>> graphMap = new HashMap<>();

    public void addNode(int id) {
        if (!graphMap.containsKey(id)) {
            graphMap.put(id, new ArrayList<>());
        }
    }

    public void addEdge(int node1, int node2, int wage) {
        if (!graphMap.containsKey(node1)) {
            graphMap.put(node1, new ArrayList<>());
        }
        if (!graphMap.containsKey(node2)) {
            graphMap.put(node2, new ArrayList<>());
        }
        //TODO sprawdzic czy juz jest ta krawedz
        graphMap.get(node1).add(new Edge(node2, wage));
        graphMap.get(node2).add(new Edge(node1, wage));
    }


    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();

        graphMap.keySet()
                .forEach((Integer id) -> {
                    stringBuffer.append(id);
                    stringBuffer.append(" -> \n");
                    stringBuffer.append(graphMap.get(id));
                    stringBuffer.append("\n");
                });

        return stringBuffer.toString();
    }
}
