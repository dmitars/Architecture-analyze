package com.company.algorithm;

public enum Coupling {
    INDEPENDENT(0), DATA(1), PATTERN(3), AREA(5), MANAGEMENT(7), EXTERNAL_LINKS(9), CONTENT(10);

    private final int value;

    Coupling(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
