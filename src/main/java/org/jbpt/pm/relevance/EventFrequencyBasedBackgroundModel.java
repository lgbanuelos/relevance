package org.jbpt.pm.relevance;

import org.deckfour.xes.model.XTrace;

import java.util.*;
import java.util.Map.Entry;

public class EventFrequencyBasedBackgroundModel extends SimpleBackgroundModel {

	Map<String, Integer> freqActionInLog = new HashMap<>(); // action in a log
	Map<String, Map<String, Integer>> freqActionInTrace = new HashMap<>(); // action in trace 
	Map<String, Integer> actionFrequency;

	boolean nonFittingSubLog;
	int lengthOfLog = 0;

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
		actionFrequency = new HashMap<>();
	}

	@Override
	public void processEvent(String eventLabel, double probability) {
		super.processEvent(eventLabel, probability);
		
		if (!this.nonFittingSubLog)
			freqActionInLog.put(eventLabel, freqActionInLog.getOrDefault(eventLabel, 0) + 1);
		
		actionFrequency.put(eventLabel, actionFrequency.getOrDefault(eventLabel, 0) + 1);
	}

	@Override
	public void closeTrace(XTrace trace, boolean fitting, Optional<Double> finalStateProb) {
		super.closeTrace(trace, fitting, finalStateProb);
		if (!freqActionInTrace.containsKey(largeString))
			freqActionInTrace.put(largeString, actionFrequency);

		if (this.nonFittingSubLog)
			if (!fitting)
				for (Entry<String, Integer> eventLabel : actionFrequency.entrySet())
					freqActionInLog.put(eventLabel.getKey(), freqActionInLog.getOrDefault(eventLabel.getKey(), 0) + eventLabel.getValue());
	}

	protected int logHatLength(Map<String, Integer> logHat) {
		return logHat.values().stream().mapToInt(i -> i).sum() + lengthOfLog;
	}

	protected double p(String element, Map<String, Integer> logHat) {
		return logHat.get(element) / (double) logHatLength(logHat);
	}

	@Override
	protected double costBitsUnfittingTraces(String traceId) {
		double bits = 0.0;
		this.lengthOfLog = this.nonFittingSubLog ? totalNumberOfNonFittingTraces : totalNumberOfTraces;
	
		for (Entry<String, Integer> eventFrequency : freqActionInTrace.get(traceId).entrySet())
			bits -= log2(p(eventFrequency.getKey(), freqActionInLog)) * eventFrequency.getValue();
		bits -= log2(lengthOfLog / (double) logHatLength(freqActionInLog));
		return bits;
	}

	@Override
	protected double costDistribution() {
		//\sum_x ( 2 \floor(\log_2 (x+1)) + 1)
		final long[] result = {0};
		freqActionInLog.values().forEach(v -> result[0] += (2*Math.floor(log2(v+1))+1)); //cost of frequency distribution of actions
		result[0] +=  (2*Math.floor(log2(lengthOfLog+1))+1);//cost of frequency distribution of end-of-trace symbols.

		System.out.println(freqActionInLog);
		System.out.println(lengthOfLog);
		System.out.println(result[0]);
	        return result[0];
	    }
}
