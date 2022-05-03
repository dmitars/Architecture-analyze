package com.company.dsu;

import java.util.*;
import java.util.stream.Collectors;


public class SplitDSU<T, Q> extends MapDSU<T> {
    private final Map<Integer, Set<Q>> qualifiersOfSetIncluded = new HashMap<>();


    public Map<Integer, Set<Q>> getQualifiersOfSetIncluded() {
        return qualifiersOfSetIncluded;
    }


    @Override
    public void processElements(T first, T second) {

    }

    public void processList(List<T> elementsToProcess, Q qualifier) {
        if (dsuSets.keySet().isEmpty()) {
            addNewSetOf(elementsToProcess, qualifier);
        } else {
            Set<Integer> setIndexes = new HashSet<>();
            Map<Integer, Set<T>> elementsFromSet = new HashMap<>();
            int newSet = -1;
            for (var element : elementsToProcess) {
                var existedSetName = findSetNameFor(element);
                if (existedSetName != -1) {
                    setIndexes.add(existedSetName);
                    elementsFromSet.computeIfAbsent(existedSetName, k -> new HashSet<T>()).add(element);
                } else {
                    if (newSet == -1) {
                        newSet = createSetFor(element).getName();
                        setIndexes.add(newSet);
                    } else {
                        addElementToSet(element, newSet);
                    }
                    elementsFromSet.computeIfAbsent(newSet, k -> new HashSet<T>()).add(element);
                }
            }

            for (var setIndex : setIndexes) {
                if (setIndex != newSet && !containsWhole(setIndex, elementsFromSet.get(setIndex))) {
                    moveNotCrossingElementsToNewSet(setIndex, elementsFromSet.get(setIndex));
                }

                qualifiersOfSetIncluded.computeIfAbsent(setIndex, k -> new HashSet<>()).add(qualifier);
            }

        }
    }

    private void moveNotCrossingElementsToNewSet(int setIndex, Set<T> crossingElements) {
        var allInSet = findElementsForSet(setIndex);
        var set = dsuSets.get(setIndex);
        if (!crossingElements.isEmpty()) {
            T firstElement = allInSet.stream()
                    .filter(el -> !crossingElements.contains(el.getKey()))
                    .findFirst()
                    .orElseThrow()
                    .getKey();
            var newSetIndex = createSetFor(firstElement).getName();
            T firstNotCrossingElement = null;
            for (var elementsEntries : allInSet) {
                if (!crossingElements.contains(elementsEntries.getKey())) {
                    elementsEntries.getValue().setSetIndex(newSetIndex);
                    set.setSize(set.getSize() - 1);
                } else if (firstNotCrossingElement == null) {
                    firstNotCrossingElement = elementsEntries.getKey();
                }
            }

            if(!crossingElements.contains(set.getRoot()))
                set.setRoot(firstNotCrossingElement);

            Set<Q> qualifiers = qualifiersOfSetIncluded.getOrDefault(setIndex, new HashSet<>());

            qualifiersOfSetIncluded.computeIfAbsent(newSetIndex, k -> new HashSet<>()).addAll(qualifiers);
        }
    }

    Set<Map.Entry<T, DsuElementData<T>>> findElementsForSet(int setName) {
        return elements.entrySet().stream()
                .filter(entry -> entry.getValue().getSetIndex() == setName)
                .collect(Collectors.toSet());
    }

    private boolean containsWhole(Integer setIndex, Collection<T> elements) {
        return dsuSets.get(setIndex).getSize() == elements.size();
    }

    private void addNewSetOf(List<T> elements, Q qualifier) {
        var setName = createSetFor(elements.get(0)).getName();
        for (int i = 1; i < elements.size(); i++) {
            addElementToSet(elements.get(i), setName);
        }
        qualifiersOfSetIncluded.computeIfAbsent(setName, k -> new HashSet<>()).add(qualifier);
    }

    @Override
    public void addElement(T element) {

    }
}
