package org.jbpt.pm.relevance;

import org.deckfour.xes.model.XTrace;

import java.util.*;
import java.util.Map.Entry;

public class EventFrequencyBasedBackgroundModel extends SimpleBackgroundModel {

	Map<String, Integer> n_a_E = new HashMap<>();
	Map<String, Map<String, Integer>> n_a_t = new HashMap<>();
	Map<String, Integer> eventFrequency;
	
	boolean nonFittingSubLog;
	int lengthOfE = 0;

	/**
	 * A constructor of the class.
	 * 
	 * @param nonFittingSubLog If nonFittingSubLog parameter is set to 'true', the
	 *                         event frequency will be computed based on all traces
	 *                         that do not fit the SDFA; Otherwise the whole event
	 *                         log will be used.
	 */

	public EventFrequencyBasedBackgroundModel(boolean nonFittingSubLog) {
		super();
		this.nonFittingSubLog = nonFittingSubLog;
	}

	@Override
	public void openTrace(XTrace trace) {
		super.openTrace(trace);
		eventFrequency = new HashMap<>();
	}

	@Override
	public void processEvent(String eventLabel, double probability) {
		super.processEvent(eventLabel, probability);
		
		if (!this.nonFittingSubLog)
			n_a_E.put(eventLabel, n_a_E.getOrDefault(eventLabel, 0) + 1);
		
		eventFrequency.put(eventLabel, eventFrequency.getOrDefault(eventLabel, 0) + 1);
	}

	@Override
	public void closeTrace(XTrace trace, boolean fitting, Optional<Double> finalStateProb) {
		super.closeTrace(trace, fitting, finalStateProb);
		this.lengthOfE = this.nonFittingSubLog ? totalNumberOfNonFittingTraces : totalNumberOfTraces;

		if (!n_a_t.containsKey(largeString))
			n_a_t.put(largeString, eventFrequency);

		if (this.nonFittingSubLog)
			if (!fitting)
				for (Entry<String, Integer> eventLabel : eventFrequency.entrySet())
					n_a_E.put(eventLabel.getKey(), n_a_E.getOrDefault(eventLabel.getKey(), 0) + eventLabel.getValue());
	}

	protected int logHatLength(Map<String, Integer> logHat) {
		return logHat.values().stream().mapToInt(i -> i).sum() + lengthOfE;
	}

	protected double p(String element, Map<String, Integer> logHat) {
		return logHat.get(element) / (double) logHatLength(logHat);
	}

	@Override
	protected double costBitsUnfittingTraces(String traceId) {
		double bits = 0.0;
		
		for (Entry<String, Integer> eventFrequency : n_a_t.get(traceId).entrySet())
			bits -= log2(p(eventFrequency.getKey(), n_a_E)) * eventFrequency.getValue();
		
		bits -= log2(lengthOfE / (double) logHatLength(n_a_E));
		return bits;
	}

}
