package com.company.storage;

import com.company.algorithm.Coherence;

import java.util.HashSet;

public class ComponentInfo {
    private final String className;
    private final HashSet<Dependency> storageOutgoing = new HashSet<>();
    private final HashSet<Dependency> storageIngoing = new HashSet<>();
    private final HashSet<String> methodsManagedByParams = new HashSet<>();
    private final HashSet<String> methodsWithFieldsAssignment = new HashSet<>();
    private final HashSet<String> methodsWithStaticFieldsAssignment = new HashSet<>();
    private final HashSet<String> classesWithStaticFieldsAssignment = new HashSet<>();
    private Coherence coherence = Coherence.TIME;


    public ComponentInfo(String className) {
        this.className = className;
    }


    public HashSet<String> getMethodsManagedByParams() {
        return methodsManagedByParams;
    }

    public HashSet<String> getMethodsWithStaticFieldsAssignment() {
        return methodsWithStaticFieldsAssignment;
    }

    public String getClassName() {
        return className;
    }

    public HashSet<Dependency> getStorageOutgoing() {
        return storageOutgoing;
    }

    public HashSet<Dependency> getStorageIngoing() {
        return storageIngoing;
    }

    public Coherence getCoherence() {
        return coherence;
    }

    public void setCoherence(Coherence coherence) {
        this.coherence = coherence;
    }

    public void upgradeCoherenceIfRequired(Coherence coherenceToSet) {
        if (coherenceToSet.getValue() > coherence.getValue()){
            setCoherence(coherenceToSet);
        }
    }

    @Override
    public String toString() {
        return "Class: " + className+"\n\n"+
               "Afferent coupling: " + storageIngoing.size() + "\n" +
               "Efferent coupling: " + storageOutgoing.size() + "\n" +
               "Coherence: " + coherence.getValue() + "\n" +
               "Instability: " + ComponentInfoStatistic.instabilityOf(this) + "\n";
    }

    public HashSet<String> getMethodsWithFieldsAssignment() {
        return methodsWithFieldsAssignment;
    }

    public HashSet<String> getClassesWithStaticFieldsAssignment() {
        return classesWithStaticFieldsAssignment;
    }
}
