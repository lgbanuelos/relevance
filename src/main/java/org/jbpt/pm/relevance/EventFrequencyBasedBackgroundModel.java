package org.jbpt.pm.relevance;

import org.deckfour.xes.model.XTrace;

import java.util.*;

public class EventFrequencyBasedBackgroundModel extends SimpleBackgroundModel {
    Map<String, Integer> n_a_E = new HashMap<>();
    Map<String, Map<String, Integer>> n_a_t = new HashMap<>();
    Map<String, Integer> eventFrequency;

    @Override
    public void openTrace(XTrace trace) {
        eventFrequency = new HashMap<>();
    }

    @Override
    public void processEvent(String eventLabel, double probability) {
        super.processEvent(eventLabel, probability);
        n_a_E.put(eventLabel, n_a_E.getOrDefault(eventLabel, 0) + 1);
        eventFrequency.put(eventLabel, eventFrequency.getOrDefault(eventLabel, 0) + 1);
    }

    @Override
    public void closeTrace(XTrace trace, boolean fitting, Optional<Double> finalStateProb) {
        super.closeTrace(trace, fitting, finalStateProb);
        if (!n_a_t.containsKey(largeString))
            n_a_t.put(largeString, eventFrequency);
    }

    protected double costBitsUnfittingTraces(String traceId) {
        return (1 + traceSize.get(traceId)) * log2( 1 + labels.size() );
    }
}
