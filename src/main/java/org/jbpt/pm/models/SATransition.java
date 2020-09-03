package org.jbpt.pm.models;

public class SATransition {
    private Integer from;
    private Integer to;
    private String label;
    private Double prob;

    public Integer getFrom() {
        return from;
    }

    public Integer getTo() {
        return to;
    }

    public String getLabel() {
        return label;
    }

    public Double getProb() {
        return prob;
    }

    public SATransition(Integer from, Integer to, String label, Double probability) {
        this.from = from;
        this.to = to;
        this.label = label;
        this.prob = probability;
    }

    public String toString() {
        return String.format("(%d) - %s [%10.8f] -> (%d)", from, label, prob, to);
    }
}

