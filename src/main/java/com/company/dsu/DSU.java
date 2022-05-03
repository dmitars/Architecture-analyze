package com.company.dsu;

import java.util.List;
import java.util.Set;

public interface DSU<T> {
    void processElements(T first, T second);
    void addElement(T element);
    List<Set<T>> getSets();
    boolean contains(T element);
}
