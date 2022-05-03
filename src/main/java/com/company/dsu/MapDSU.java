package com.company.dsu;

import java.util.*;


public abstract class MapDSU<T> implements DSU<T> {
    protected Map<Integer, DsuSet<T>> dsuSets = new HashMap<>();
    protected Map<T, DsuElementData<T>> elements = new HashMap<>();
    protected int nextSetName = 0;

    protected int findSetNameFor(T element) {
        return elements.getOrDefault(element, new DsuElementData<>()).getSetIndex();
    }

    protected DsuSet<T> createSetFor(T element) {
        DsuSet<T> elSet = new DsuSet<T>(element, nextSetName);
        dsuSets.put(nextSetName, elSet);
        DsuElementData<T> elData = new DsuElementData<T>(element, nextSetName);
        elements.put(element, elData);
        nextSetName++;
        return elSet;
    }

    protected void addElementToSet(T element, int setName) {
        if (setName < 0)
            throw new IllegalArgumentException("Error: set name must be positive");
        if (elements.containsKey(element))
            throw new IllegalArgumentException("Error: element " + element.toString() + " already exists in DSU");
        DsuElementData<T> elData = new DsuElementData<T>(element, setName);
        var dsuSet = dsuSets.get(setName);
        dsuSet.setSize(dsuSet.getSize() + 1);
        elements.put(element, elData);
    }

    protected void unionSets(DsuSet<T> first, DsuSet<T> second) {
        if (first == null || second == null)
            throw new RuntimeException("What?!");
        if (first.getSize() > second.getSize()) {
            mergeSetsToFirst(first, second);
        } else {
            mergeSetsToFirst(second, first);
        }
    }

    protected void mergeSetsToFirst(DsuSet<T> first, DsuSet<T> second) {
        first.setSize(first.getSize() + second.getSize());
        T secondRoot = second.getRoot();
        elements.get(secondRoot).setNext(first.getRoot());
        elements.values().stream()
                .filter(el -> el.getSetIndex() == second.getName())
                .forEach(value -> value.setSetIndex(first.getName()));
        dsuSets.remove(second.getName());
    }

    public List<Set<T>> getSets() {
        Map<Integer, Set<T>> sets = new HashMap<>();
        dsuSets.keySet().forEach(entry -> sets.put(entry, new HashSet<T>()));
        elements.forEach((key, value) -> sets.get(value.getSetIndex()).add(key));
        return new ArrayList<>(sets.values());
    }

    @Override
    public boolean contains(T element) {
        return findSetNameFor(element) != -1;
    }
}
