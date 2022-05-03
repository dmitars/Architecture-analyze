package com.company.dsu;


public class DsuElementData<T> {
    private T next = null;
    private int setIndex = -1;

    public DsuElementData(){}

    public DsuElementData(T next, int setIndex) {
        this.next = next;
        this.setIndex = setIndex;
    }


    public T getNext() {
        return next;
    }

    public void setNext(T next) {
        this.next = next;
    }

    public int getSetIndex() {
        return setIndex;
    }

    public void setSetIndex(int setIndex) {
        this.setIndex = setIndex;
    }
}
