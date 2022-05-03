package com.company.algorithm;

public enum Coherence {
    TIME(3), PROCEDURE(5), COMMUNICATIVE(7), INFO(9),FUNCTIONAL(10);

    private final int value;

    Coherence(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
