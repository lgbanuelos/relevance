package org.jbpt.pm.relevance.utils;

import org.jbpt.pm.models.*;

import java.util.*;

public class FDAG2Aut {
    public static SAutomaton convert(FDAGraph dfg) {
        Map<Integer, Integer> aggregateOutgoingFrequency = new HashMap<>();
        Map<Integer, String> nodeInfo = new HashMap<>();

        for (FDAGNode node: dfg.getNodes())
            if (node != null) {
                nodeInfo.put(node.getId(), node.getLabel());
            }

        Set<Integer> sinks = new HashSet<>(nodeInfo.keySet());
        Set<Integer> sources = new HashSet<>(nodeInfo.keySet());

        for (FDAGArc arc: dfg.getArcs()) {
            if (arc != null) {
                aggregateOutgoingFrequency.put(
                        arc.getFrom(),
                        aggregateOutgoingFrequency.getOrDefault(arc.getFrom(), 0) +
                                arc.getFreq()
                );
                sinks.remove(arc.getFrom());
                sources.remove(arc.getTo());
            }
        }

        List<SATransition> transitions = new ArrayList<>();

        for (FDAGArc arc: dfg.getArcs()) {
            if (!sinks.contains((arc.getTo()))) {
                String label = nodeInfo.get(arc.getTo());
                transitions.add(new SATransition(
                        arc.getFrom(), arc.getTo(), label,
                        (double) arc.getFreq() / aggregateOutgoingFrequency.get(arc.getFrom())
                ));
            }
        }

        for (Integer sink: sinks)
            nodeInfo.remove(sink);

        return SAutomaton.of(transitions, sources.iterator().next());
    }
}
