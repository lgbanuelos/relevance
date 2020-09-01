package org.jbpt.pm.models;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;

public class SAutomaton {
    private final Integer initialState;
    private final List<SATransition> transitions;

    private transient Set<Integer> states;
    private transient Table<Integer, String, Pair<Integer, Double>> transTable;
    private transient Map<Integer, Double> finalStates;

    SAutomaton(List<SATransition> saTransitions, Integer initialState) {
        this.transitions = saTransitions;
        this.initialState = initialState;
        complete();
    }

    private SAutomaton complete() {
        return complete(1e-6);
    }

    private SAutomaton complete(double epsilon) {
        Table<Integer, String, Pair<Integer, Double>> table = HashBasedTable.create();
        Set<Integer> stateSet = new HashSet<>();
        Map<Integer, Double> outgoingProb = new HashMap<>();
        Map<Integer, Double> sinkAbsorvingProb = new HashMap<>();

        for (SATransition stTransition: transitions) {
            table.put(stTransition.getFrom(), stTransition.getLabel(), Pair.of(stTransition.getTo(), Math.log(stTransition.getProb())));
            stateSet.add(stTransition.getFrom());
            stateSet.add(stTransition.getTo());
            outgoingProb.put(stTransition.getFrom(), outgoingProb.getOrDefault(stTransition.getFrom(), 0.0) + stTransition.getProb());
        }

        for (Integer state: stateSet) {
            if (!outgoingProb.containsKey(state) || 1.0 - outgoingProb.get(state) > epsilon)
                sinkAbsorvingProb.put(state, Math.log(1.0 - outgoingProb.getOrDefault(state, 0.0)));
        }

        finalStates = sinkAbsorvingProb;

        transTable = table;
        states = stateSet;

        return this;
    }

    public static SAutomaton of(List<SATransition> saTransitions, Integer initialState) {
        return new SAutomaton(saTransitions, initialState);
    }

    public Integer getInitialState() {
        return initialState;
    }

    public Table<Integer, String, Pair<Integer, Double>> getTransitions() {
        return transTable;
    }

    public Set<Integer> getStates() {
        return states;
    }

    public static SAutomaton readJSON(String fileName) throws Exception {
        JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        Gson gson = new Gson();
        SAutomaton automaton = gson.fromJson(reader, SAutomaton.class);
        automaton.complete();

        return automaton;
    }
    public void toJSON(String filename) throws Exception {
        FileWriter writer = new FileWriter(filename);
        Gson gson = new Gson();
        IOUtils.write(gson.toJson(this), writer);
        writer.flush();
        writer.close();
    }

    public boolean isFinalState(Integer state) {
        return finalStates.containsKey(state);
    }

    public double getFinalStateProb(Integer state) {
        return finalStates.get(state);
    }
}
