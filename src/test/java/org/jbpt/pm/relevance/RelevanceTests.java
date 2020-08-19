package org.jbpt.pm.relevance;

import org.jbpt.pm.models.FDAGraph;
import org.jbpt.pm.models.SAutomaton;
import org.jbpt.pm.relevance.utils.XLogReader;
import org.junit.Test;

public class RelevanceTests {

    @Test
    public void test1() throws Exception {
        var log = XLogReader.openLog("logs/sepsis.xes.gz");
        var dfg = FDAGraph.readJSON("dfgs/sepsis_1.000.json");
        System.out.println(Relevance.compute(log, dfg, true));
    }

    @Test
    public void test2() throws Exception {
        var log = XLogReader.openLog("logs/sepsis.xes.gz");
        var automaton = SAutomaton.readJSON("automata/sepsis_1.000.json");
        System.out.println(Relevance.compute(log, automaton, true));
    }

}
