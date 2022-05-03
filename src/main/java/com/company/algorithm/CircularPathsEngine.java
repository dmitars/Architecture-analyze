package com.company.algorithm;

import java.util.*;
import java.util.function.Function;

public class CircularPathsEngine {
    public static <T,R> List<List<T>> processGraph(Map<T, ? extends Set<R>> graph, Function<R,T> accumulator) {
        Set<T> scanned = new HashSet<>();
        var result = new ArrayList<List<T>>();
        for (T p : graph.keySet()) {
            Stack<T> path = new Stack<>();
            path.push(p);
            result.addAll(scanDependencies(path, graph, scanned, accumulator));
        }
        return result;
    }

    private static <T,R> List<List<T>> scanDependencies(Stack<T> path, Map<T, ? extends Set<R>> storage,
                                                        Set<T> scanned, Function<R,T> accumulator) {
        T ref = path.peek();
        var result = new ArrayList<List<T>>();
        // return if already scanned
        if (scanned.contains(ref)) {
            return Collections.emptyList();
        }
        scanned.add(ref);
        Set<R> refs = storage.get(ref);
        if (refs != null) {
            for (R p : refs) {
                var value = accumulator.apply(p);
                if (path.contains(value)) {
                    List<T> circularPath = new ArrayList<>(
                            path.subList(path.indexOf(value), path.size()));
                    circularPath.add(value);

                    result.add(circularPath);
                    break;
                } else {
                    path.push(value);
                    result.addAll(scanDependencies(path, storage, scanned, accumulator));
                    path.pop();
                }
            }
        }
        return result;
    }
}
