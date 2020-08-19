package org.jbpt.pm.relevance;

import org.deckfour.xes.model.XTrace;

public interface ReplayInformationGatherer {
    void openTrace(XTrace trace);
    void closeTrace(XTrace trace, boolean fitting);
    void processEvent(String eventLabel, double probability);
}
