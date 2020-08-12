package org.jbpt.pm.models;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SAutomaton {
    private final Integer initialState;
    private final Table<Integer, String, Pair<Integer, Double>> transitions;
    private final Set<Integer> states;

    SAutomaton(Table<Integer, String, Pair<Integer, Double>> transitions, Set<Integer> states, Integer initialState) {
        this.transitions = transitions;
        this.initialState = initialState;
        this.states = states;
    }

    public static SAutomaton of(List<SATransition> saTransitions, Set<Integer> saStates, Integer initialState) {
        Table<Integer, String, Pair<Integer, Double>> transitions = HashBasedTable.create();
        for (SATransition stTransition: saTransitions)
            transitions.put(stTransition.getFrom(), stTransition.getLabel(), Pair.of(stTransition.getTo(), Math.log(stTransition.getProb())));
        return new SAutomaton(transitions, saStates, initialState);
    }

    public Integer getInitialState() {
        return initialState;
    }

    public Table<Integer, String, Pair<Integer, Double>> getTransitions() {
        return transitions;
    }

    public Set<Integer> getStates() {
        return states;
    }

    public static SAutomaton readJSON(String fileName) throws Exception {
        JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        Gson gson = new GsonBuilder().create();

        Table<Integer, String, Pair<Integer, Double>> transitions = HashBasedTable.create();
        Set<Integer> states = new HashSet<>();
        Set<Integer> targets = new HashSet<>();

        reader.beginArray();
        while (reader.hasNext()) {
            SATransition stTransition = gson.fromJson(reader, SATransition.class);
            transitions.put(stTransition.getFrom(), stTransition.getLabel(), Pair.of(stTransition.getTo(), Math.log(stTransition.getProb())));
            states.add(stTransition.getFrom());
            states.add(stTransition.getTo());
            targets.add(stTransition.getTo());
        }
        reader.close();

        Set<Integer> sources = new HashSet<>(states);
        sources.removeAll(targets);

        return new SAutomaton(transitions, states, sources.iterator().next()); // There should be only one initial state
    }
}
