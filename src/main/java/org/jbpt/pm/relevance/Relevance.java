package org.jbpt.pm.relevance;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.pm.models.FDAGraph;
import org.jbpt.pm.models.SAutomaton;
import org.jbpt.pm.relevance.utils.FDAG2Aut;

import java.util.HashMap;
import java.util.Map;

public class Relevance {

    public static void scanAndProcess(XLog log, SAutomaton automaton, ReplayInformationGatherer infoGatherer) {
        var transitions = automaton.getTransitions();
        var initialState = automaton.getInitialState();

        for (XTrace trace: log) {
            Integer curr = initialState;
            boolean nonfitting = false;
            infoGatherer.openTrace(trace);
            for (XEvent event: trace) {
                if (event.getAttributes().get("concept:name") == null)
                    continue;
                String label = event.getAttributes().get("concept:name").toString();
                double prob = 0.0;
                if (!nonfitting && transitions.contains(curr, label)) {
                    Pair<Integer, Double> pair = transitions.get(curr, label);
                    curr = pair.getLeft();
                    prob = pair.getRight();
                } else
                    nonfitting = true;

                infoGatherer.processEvent(label, prob);
            }

            if (!nonfitting && !transitions.contains(curr, "#"))
                nonfitting = true;

            infoGatherer.closeTrace(trace, !nonfitting);
        }
    }

    public static Map<String, Object> compute(XLog log, SAutomaton automaton, boolean full) {
        SimpleBackgroundModel analyzer = new SimpleBackgroundModel();
        scanAndProcess(log, automaton, analyzer);
        Map<String, Object> result = new HashMap<>(analyzer.computeRelevance(full));

        if (full)
            result.putAll(Map.of(
                    "numberOfStates", automaton.getStates().size(),
                    "numberOfTransitions", automaton.getTransitions().size()
            ));
        return result;
    }

    public static Map<String, Object> compute(XLog log, FDAGraph fdaGraph, boolean full) {
        return compute(log, FDAG2Aut.convert(fdaGraph), full);
    }
}
