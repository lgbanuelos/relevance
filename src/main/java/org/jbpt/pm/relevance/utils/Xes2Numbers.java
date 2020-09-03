package org.jbpt.pm.relevance.utils;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Xes2Numbers {
    public static void main(String[] args) throws Exception {
        XLog log = XLogReader.openLog(args[0]);

        Map<String, Integer> label2index = new HashMap<>();
        for (XTrace trace: log) {
            for (XEvent event : trace) {
                String label = event.getAttributes().get("concept:name").toString();
                if (!label2index.containsKey(label))
                    label2index.put(label, label2index.size());
            }
        }

        for (XTrace trace: log) {
            System.out.println(
                trace.stream().map(e -> label2index.get(e.getAttributes().get("concept:name").toString()).toString()).collect(Collectors.joining(","))
            );
        }
        System.out.println("done");

    }
}
