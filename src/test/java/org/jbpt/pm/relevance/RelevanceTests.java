package org.jbpt.pm.relevance;

import org.deckfour.xes.model.XLog;
import org.jbpt.pm.models.FDAGraph;
import org.jbpt.pm.models.SAutomaton;
import org.jbpt.pm.relevance.utils.XLogReader;
import org.junit.Test;

public class RelevanceTests {

    @Test
    public void test1() throws Exception {
        XLog log = XLogReader.openLog("logs/sepsis.xes.gz");
        FDAGraph dfg = FDAGraph.readJSON("dfgs/sepsis_1.000.json");
        System.out.println(Relevance.compute(log, dfg, true));
    }

    @Test
    public void test2() throws Exception {
        XLog log = XLogReader.openLog("logs/sepsis.xes.gz");
        SAutomaton automaton = SAutomaton.readJSON("automata/sepsis_1.000.json");
        System.out.println(Relevance.compute(log, automaton, true));
    }

    @Test
    public void test3() throws Exception {
        XLog log = XLogReader.openLog("logs/e.xes");
        SAutomaton automaton = SAutomaton.readJSON("automata/n.json");
        System.out.println(Relevance.compute(log, automaton, true));
    }

    @Test
    public void testICPM_E1() throws Exception {
        XLog log = XLogReader.openLog("logs/icpm2020_E1.xes");
        FDAGraph dfg = FDAGraph.readJSON("dfgs/icpm2020_fig5.json");
        System.out.println(Relevance.compute(log, dfg, true));
    }

    @Test
    public void testICPM_E2() throws Exception {
        XLog log = XLogReader.openLog("logs/icpm2020_E2.xes");
        FDAGraph dfg = FDAGraph.readJSON("dfgs/icpm2020_fig5.json");
        System.out.println(Relevance.compute(log, dfg, true));
    }
}
