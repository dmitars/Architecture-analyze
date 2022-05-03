package com.company.dsu;


public class DefaultDSU<T> extends MapDSU<T>{

    public void processElements(T first, T second) {
        int firstSetName = findSetNameFor(first);
        int secondSetName = findSetNameFor(second);
        if (firstSetName == secondSetName && firstSetName != -1)
            return;
        if (secondSetName == -1) {
            if (firstSetName == -1)
                firstSetName = createSetFor(first).getName();
            addElementToSet(second, firstSetName);
        } else if (firstSetName == -1) {
            addElementToSet(first, secondSetName);
        } else {
            unionSets(dsuSets.get(firstSetName), dsuSets.get(secondSetName));
        }
    }

    public void addElement(T element) {
        if (findSetNameFor(element) == -1)
            createSetFor(element);
    }

}
