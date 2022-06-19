package com.company.algorithm;

import com.company.storage.Dependency;

import java.util.*;
import java.util.stream.Collectors;


public class LevelsEngine {
    private final Map<String, Set<Dependency>> storageOutgoing;
    private final Map<String, HashSet<String>> abstractions;

    private int level = 1;
    private final Map<Integer, Set<String>> result = new HashMap<>();

    public LevelsEngine(Map<String, Set<Dependency>> storageOutgoing, Map<String, HashSet<String>> abstractions) {
        this.storageOutgoing = storageOutgoing;
        this.abstractions = abstractions;
    }

    public Map<Integer, Set<String>> processValues(Collection<String> inEndpoints){
        addLevelToMap(inEndpoints);
        return result;
    }

    public void addLevelToMap(Collection<String> currentEntities){
        Set<String>nextLevelEntities = new HashSet<>();
        for(var currentEntity: currentEntities){
            result.computeIfAbsent(level, k -> new HashSet<>()).add(currentEntity);
            var dependencies = storageOutgoing.getOrDefault(currentEntity, new HashSet<Dependency>()).stream()
                    .map(Dependency::getDependencyName)
                    .filter(dependencyName -> !abstractions.getOrDefault(dependencyName, new HashSet<>()).contains(currentEntity))
                    .collect(Collectors.toList());
            result.get(level).addAll(dependencies);
            nextLevelEntities.addAll(dependencies.stream()
                    .flatMap(dependency -> abstractions.getOrDefault(dependency,new HashSet<>()).stream())
                            .filter(dependency -> !result.get(level).contains(dependency))
                    .collect(Collectors.toSet()));
        }
        level++;
        if(!nextLevelEntities.isEmpty())
            addLevelToMap(nextLevelEntities);
    }

}
