package org.jbpt.pm.relevance;

import org.deckfour.xes.model.XTrace;

import java.util.Optional;

public interface ReplayInformationGatherer {
    void openTrace(XTrace trace);
    void closeTrace(XTrace trace, boolean fitting, Optional<Double> finalStateProb);
    void processEvent(String eventLabel, double probability);
}
