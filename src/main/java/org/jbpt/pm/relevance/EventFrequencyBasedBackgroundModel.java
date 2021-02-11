package org.jbpt.pm.relevance;

import org.deckfour.xes.model.XTrace;

import java.util.*;
import java.util.Map.Entry;

public class EventFrequencyBasedBackgroundModel extends SimpleBackgroundModel {

	Map<String, Integer> freqActionInLog = new HashMap<>(); // actions in a log (non-fitting log)
	Map<String, Map<String, Integer>> freqActionInTrace = new HashMap<>(); // actions in a trace
	Map<String, Integer> tempFreqActionInLog;

	boolean nonFittingSubLog;
	int lengthOfLog = 0;

	/**
	 * @param nonFittingSubLog If nonFittingSubLog parameter is set to 'true', the
	 *                         event frequency will be computed based on all traces
	 *                         that do not fit the SDFA; Otherwise the whole event
	 *                         log will be used.
	 **/

	public EventFrequencyBasedBackgroundModel(boolean nonFittingSubLog) {
		super();
		this.nonFittingSubLog = nonFittingSubLog;
	}

	@Override
	public void openTrace(XTrace trace) {
		super.openTrace(trace);
		tempFreqActionInLog = new HashMap<>();
	}

	@Override
	public void processEvent(String eventLabel, double probability) {
		super.processEvent(eventLabel, probability);

		if (!this.nonFittingSubLog)
			freqActionInLog.put(eventLabel, freqActionInLog.getOrDefault(eventLabel, 0) + 1);

		tempFreqActionInLog.put(eventLabel, tempFreqActionInLog.getOrDefault(eventLabel, 0) + 1);
	}

	@Override
	public void closeTrace(XTrace trace, boolean fitting, Optional<Double> finalStateProb) {
		super.closeTrace(trace, fitting, finalStateProb);

		if (!freqActionInTrace.containsKey(largeString))
			freqActionInTrace.put(largeString, tempFreqActionInLog);

		if (this.nonFittingSubLog) {
			if (!fitting)
				for (Entry<String, Integer> eventLabel : tempFreqActionInLog.entrySet())
					freqActionInLog.put(eventLabel.getKey(),
							freqActionInLog.getOrDefault(eventLabel.getKey(), 0) + eventLabel.getValue());
		}
	}

	protected int actionsInLog(Map<String, Integer> freqActionInLog) { // number of actions + #s in the log
		return freqActionInLog.values().stream().mapToInt(i -> i).sum() + this.lengthOfLog;
	}

	protected double p(String element, Map<String, Integer> freqActionInLog) { // probability of an action or # in the
																				// log
		return freqActionInLog.get(element) / (double) actionsInLog(freqActionInLog);
	}

	@Override
	protected double costBitsUnfittingTraces(String traceId) {
		double bits = 0.0;
		this.lengthOfLog = this.nonFittingSubLog ? totalNumberOfNonFittingTraces : totalNumberOfTraces;

		for (Entry<String, Integer> eventFrequency : freqActionInTrace.get(traceId).entrySet())
			bits -= log2(p(eventFrequency.getKey(), freqActionInLog)) * eventFrequency.getValue(); // compute cost of
																									// actions in a
																									// trace
		bits -= log2(this.lengthOfLog / (double) actionsInLog(freqActionInLog)); // compute cost of hashes in the trace

		return bits;
	}

	@Override
	protected double costFrequencyDistribution() {
		double bits = 0.0;
		this.lengthOfLog = this.nonFittingSubLog ? totalNumberOfNonFittingTraces : totalNumberOfTraces;

		for (String label : labels)
			bits += (2 * Math.floor(log2(freqActionInLog.getOrDefault(label, 0) + 1)) + 1); // cost of disfor actions
		bits += (2 * Math.floor(log2(this.lengthOfLog + 1)) + 1); // for hashes

		return bits;
	}
}
