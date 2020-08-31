package org.jbpt.pm.models;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.List;

public class FDAGraph {
    private List<FDAGNode> nodes;
    private List<FDAGArc> arcs;

    public List<FDAGNode> getNodes() {
        return nodes;
    }

    public List<FDAGArc> getArcs() {
        return arcs;
    }

    public void toDot(PrintStream out) {
        out.println("digraph G {");
        nodes.forEach(n -> {
            if (n != null) out.println(n.toDot());
        });
        arcs.forEach(a -> {
            if (a != null) out.println(a.toDot());
        });
        out.println("}");
    }

    public static FDAGraph readJSON(String fileName) throws Exception {
        Gson gson = new Gson();
        return gson.fromJson(new FileReader(fileName), FDAGraph.class);
    }
}
