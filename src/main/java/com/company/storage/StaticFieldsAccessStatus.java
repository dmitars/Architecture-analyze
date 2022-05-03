package com.company.storage;

public enum StaticFieldsAccessStatus {
    NONE(0), READ(1), READ_WRITE(2);

    private int value;
    StaticFieldsAccessStatus(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static StaticFieldsAccessStatus min(StaticFieldsAccessStatus first, StaticFieldsAccessStatus second){
        return Math.min(first.getValue(), second.getValue()) == first.getValue() ? first : second;
    }
}
