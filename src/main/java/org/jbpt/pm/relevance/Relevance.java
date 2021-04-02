package org.jbpt.pm.relevance;

import com.google.common.collect.Table;
import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.pm.models.FDAGraph;
import org.jbpt.pm.models.SAutomaton;
import org.jbpt.pm.relevance.utils.FDAG2Aut;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class Relevance {
	public static void scanAndProcess(XLog log, SAutomaton automaton, ReplayInformationGatherer infoGatherer) {
		Table<Integer, String, Pair<Integer, Double>> transitions = automaton.getTransitions();
		Integer initialState = automaton.getInitialState();

		for (XTrace trace: log) {
			Integer curr = initialState;
			boolean nonfitting = false;
			infoGatherer.openTrace(trace);
			for (XEvent event: trace) {
				if (event.getAttributes().get("concept:name") == null ||(
					  event.getAttributes().containsKey("lifecycle:transition") &&
					 !event.getAttributes().get("lifecycle:transition").toString().toUpperCase().equals("COMPLETE")
					 ))
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


			if (!nonfitting && automaton.isFinalState(curr)) {
				infoGatherer.closeTrace(trace, true, Optional.of(automaton.getFinalStateProb(curr)));
			} else
				infoGatherer.closeTrace(trace, false, Optional.empty());
		}
	}

	private static Map<String, Object> run(XLog log, SAutomaton automaton, boolean full, SimpleBackgroundModel analyzer) {
		scanAndProcess(log, automaton, analyzer);
		Map<String, Object> result = new HashMap<>(analyzer.computeRelevance(full));

		if (full) {
			//result.put("numberOfStates", automaton.getStates().size());
			//result.put("numberOfTransitions", automaton.getTransitions().size());
			//result.put("sizeOfDFGsm", fdaGraph.getArcs().size()+fdaGraph.getNodes().size());
		}

		return result;
	}

	public static Map<String, Object> compute(XLog log, SAutomaton automaton, boolean full) {
		return run(log, automaton, full, new SimpleBackgroundModel());
    }

	public static Map<String, Object> computeNew(XLog log, SAutomaton automaton, boolean full, boolean nonFittingSubLog) {
		return run(log, automaton, full, new EventFrequencyBasedBackgroundModel(nonFittingSubLog));
	}

    public static Map<String, Object> compute(XLog log, FDAGraph fdaGraph, boolean full) {
    	Map<String, Object> result = new HashMap<>(compute(log, FDAG2Aut.convert(fdaGraph), full));
       
    	if (full) {
            //result.put("numberOfNodes", fdaGraph.getNodes().size());
            //result.put("numberOfArcs", fdaGraph.getArcs().size());
    		result.put("sizeOfDFGs", fdaGraph.getArcs().size()+fdaGraph.getNodes().size());
        }
       
    	return result;
    }

	public static Map<String, Object> computeNew(XLog log, FDAGraph fdaGraph, boolean full, boolean nonFittingSubLog) {
		Map<String, Object> result = new HashMap<>(compute(log, FDAG2Aut.convert(fdaGraph), full));

		if (full) {
			//result.put("numberOfNodes", fdaGraph.getNodes().size());
			//result.put("numberOfArcs", fdaGraph.getArcs().size());
			result.put("sizeOfDFGs", fdaGraph.getArcs().size()+fdaGraph.getNodes().size());
		}

		return result;
	}
}
