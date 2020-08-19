package org.jbpt.pm.relevance;

import com.google.common.collect.Table;
import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.pm.models.FDAGraph;
import org.jbpt.pm.models.SAutomaton;
import org.jbpt.pm.relevance.utils.FDAG2Aut;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Relevance {
    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
    public static double h0(int accumulated_rho, double totalNumberOfTraces) {
        if (accumulated_rho == 0 || accumulated_rho == totalNumberOfTraces)
            return 0;
        else {
            double p = ((double) accumulated_rho) / totalNumberOfTraces;
            return -p * log2(p) - (1 - p) * log2(1 - p);
        }
    }

    interface ReplayInformationGatherer {
        void openTrace(XTrace trace);
        void closeTrace(XTrace trace, boolean fitting);
        void processEvent(String eventLabel, double probability);
    }

    static class H0Analyzer implements ReplayInformationGatherer {
        int numberOfEvents = 0;
        int totalNumberOfTraces = 0;
        int totalNumberOfNonFittingTraces = 0;

        Set<String> labels = new HashSet<>();
        Map<String, Integer> traceFrequency = new HashMap<>();
        Map<String, Integer> traceSize = new HashMap<>();
        Map<String, Double> log2OfModelProbability = new HashMap<>();

        // Local values
        double lprob = 0.0;         // Trace replay probability
        String largeString = "";    // Identifier associated with the current trace (needed for identifying trace duplicates)

        // Methods to recollect statistics during log replay
        @Override
        public void openTrace(XTrace trace) {
            lprob = 0.0;
            largeString = "";
        }

        @Override
        public void closeTrace(XTrace trace, boolean fitting) {
            traceSize.put(largeString, trace.size());
            totalNumberOfTraces++;
            if (fitting)
                log2OfModelProbability.put(largeString, lprob / Math.log(2));
            else
                totalNumberOfNonFittingTraces++;
            traceFrequency.put(largeString, traceFrequency.getOrDefault(largeString, 0) + 1);
        }

        @Override
        public void processEvent(String eventLabel, double probability) {
            largeString += eventLabel;
            numberOfEvents++;
            labels.add(eventLabel);
            lprob += probability;
        }

        protected double costBitsUnfittingTraces(String traceId) {
            return (1 + traceSize.get(traceId)) * log2( 1 + labels.size() );
        }

        public Map<String, Object> computeRelevance(boolean full) {
            int accumulated_rho = 0;
            double accumulated_cost_bits = 0;
            double accumulated_temp_cost_bits = 0;
            double accumulated_prob_fitting_traces = 0;

            for (String traceString: traceFrequency.keySet()) {
                double traceFreq = traceFrequency.get(traceString);

                double cost_bits = 0.0;
                double nftrace_cost_bits = 0.0;

                if (log2OfModelProbability.containsKey(traceString)) { // fitting trace!
                    cost_bits = -log2OfModelProbability.get(traceString);
                    accumulated_rho += traceFreq;
                } else
                    nftrace_cost_bits = cost_bits = costBitsUnfittingTraces(traceString);

                accumulated_temp_cost_bits += nftrace_cost_bits * traceFreq;

                accumulated_cost_bits += (cost_bits * traceFreq) / totalNumberOfTraces;

                if (log2OfModelProbability.containsKey(traceString))
                    accumulated_prob_fitting_traces += traceFreq / totalNumberOfTraces;
            }

            Map<String, Object> result = new HashMap<>();
            if (full) {
                result.put("numberOfTraces", totalNumberOfTraces);
                result.put("numberOfNonFittingTraces", totalNumberOfNonFittingTraces);
                result.put("coverage", accumulated_prob_fitting_traces);
                result.put("costOfBackgroundModel", accumulated_temp_cost_bits);
            }

            result.put("relevance", h0(accumulated_rho, totalNumberOfTraces) + accumulated_cost_bits);

            return result;
        }
    }

    public static void scanAndProcess(XLog log, SAutomaton automaton, ReplayInformationGatherer infoGatherer) {
        Table<Integer, String, Pair<Integer, Double>> transitions = automaton.getTransitions();
        Integer initialState = automaton.getInitialState();

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
        H0Analyzer analyzer = new H0Analyzer();
        scanAndProcess(log, automaton, analyzer);
        Map<String, Object> result = new HashMap<>(analyzer.computeRelevance(full));

        if (full)
            result.put("numberOfStates", automaton.getStates().size());
            result.put("numberOfTransitions", automaton.getTransitions().size());
        return result;
    }

    public static Map<String, Object> compute(XLog log, FDAGraph fdaGraph, boolean full) {
        return compute(log, FDAG2Aut.convert(fdaGraph), full);
    }
}
