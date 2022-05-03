package com.company.dsu;


public class DsuSet<T> {
    private T root;
    private int size = 1;
    private int name = -1;

    public DsuSet(T root, int size, int name) {
        this.root = root;
        this.size = size;
        this.name = name;
    }

    public DsuSet(T root, int name) {
        this.root = root;
        this.name = name;
    }

    public T getRoot() {
        return root;
    }

    public void setRoot(T root) {
        this.root = root;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }
}
