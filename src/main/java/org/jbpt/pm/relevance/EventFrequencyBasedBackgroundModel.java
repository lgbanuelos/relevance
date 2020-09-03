package org.jbpt.pm.relevance;

import org.deckfour.xes.model.XTrace;

import java.util.*;

public class EventFrequencyBasedBackgroundModel extends SimpleBackgroundModel {
    Map<String, Integer> eventFrequency = new HashMap<>();
    Map<String, List<String>> trace2eventLabels = new HashMap<>();
    List<String> events;

    @Override
    public void openTrace(XTrace trace) {
        events = new ArrayList<>();
    }

    @Override
    public void processEvent(String eventLabel, double probability) {
        super.processEvent(eventLabel, probability);
        eventFrequency.put(eventLabel, eventFrequency.getOrDefault(eventLabel, 0) + 1);
        events.add(eventLabel);
    }

    @Override
    public void closeTrace(XTrace trace, boolean fitting, Optional<Double> finalStateProb) {
        super.closeTrace(trace, fitting, finalStateProb);
        if (!trace2eventLabels.containsKey(largeString))
            trace2eventLabels.put(largeString, events);
    }

    protected double costBitsUnfittingTraces(String traceId) {
        return (1 + traceSize.get(traceId)) * log2( 1 + labels.size() );
    }
}
