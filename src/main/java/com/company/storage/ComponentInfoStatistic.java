package com.company.storage;

import com.company.dsu.SplitDSU;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentInfoStatistic {
    public static double instabilityOf(ComponentInfo componentInfo) {
        var ingoingNumber = componentInfo.getStorageIngoing().size();
        return ingoingNumber == 0 ? 1 : (double) componentInfo.getStorageOutgoing().size() / ingoingNumber;
    }

    public static StaticFieldsAccessStatus getStaticAccessStatusToFieldsOf(String accessor, ComponentInfo componentInfo) {
        if (componentInfo.getClassesWithStaticFieldsAssignment().contains(accessor))
            return StaticFieldsAccessStatus.READ_WRITE;

        var staticDependenciesOf = componentInfo.getStorageIngoing().stream()
                .filter(dependency -> dependency.isStatic() && dependency.getDependencyName().equals(accessor))
                .collect(Collectors.toSet());
        if (staticDependenciesOf.isEmpty())
            return StaticFieldsAccessStatus.NONE;
        Set<String> methodsWithStaticAccess = componentInfo.getMethodsWithStaticFieldsAssignment().stream()
                .filter(method -> staticDependenciesOf.stream().filter(dependency -> dependency instanceof MethodsDependency)
                        .map(value -> (MethodsDependency) value)
                        .flatMap(methodsDependency -> methodsDependency.getMethods().stream())
                        .anyMatch(value -> value.equals(method)))
                .collect(Collectors.toSet());
        if (!methodsWithStaticAccess.isEmpty()) {
            return StaticFieldsAccessStatus.READ_WRITE;
        }

        return StaticFieldsAccessStatus.READ;
    }

    public static boolean oneManagesAnother(ComponentInfo first, ComponentInfo second){
        return manages(first.getClassName(), second) || manages(second.getClassName(), first);
    }

    private static boolean manages(String accessor, ComponentInfo componentInfo){
        return methodsStreamOf(accessor, componentInfo)
                .anyMatch(method -> componentInfo.getMethodsManagedByParams().contains(method));
    }

    public static boolean oneHasAccessToAnotherFields(ComponentInfo first, ComponentInfo second){
        return hasAccessToFields(first.getClassName(), second) || hasAccessToFields(second.getClassName(), first);
    }

    private static boolean hasAccessToFields(String accessor, ComponentInfo componentInfo){
        return methodsStreamOf(accessor, componentInfo)
                .anyMatch(method -> componentInfo.getMethodsWithFieldsAssignment().contains(method));
    }

    private static Stream<String> methodsStreamOf(String accessor, ComponentInfo componentInfo){
        return componentInfo.getStorageIngoing().stream()
                .filter(dependency -> dependency instanceof MethodsDependency && dependency.getDependencyName().equals(accessor))
                .flatMap(dependency -> ((MethodsDependency)dependency).getMethods().stream());
    }

    public static void printStatisticFor(ComponentInfo componentInfo) {
        System.out.println(componentInfo.toString());
        printSplitting(componentInfo);
        System.out.println("------------------------------------------------------------------------\n\n");
    }


    private static void printSplitting(ComponentInfo componentInfo) {
        System.out.println("Can be split on: ");
        SplitDSU<String, String> splitDSU = new SplitDSU<>();
        componentInfo.getStorageIngoing().stream()
                .filter(dependency -> dependency instanceof MethodsDependency)
                .map(it -> (MethodsDependency) it)
                .forEach(it -> splitDSU.processList(it.getMethods(), it.getDependencyName()));

        var sets = splitDSU.getSets();
        var qualifiersForSet = splitDSU.getQualifiersOfSetIncluded();
        for (var i = 0; i < sets.size(); i++) {
            System.out.println("\nPossible interface " + i + ":");
            sets.get(i).forEach(System.out::println);
            System.out.println("\n\nDependency needed in:");
            qualifiersForSet.get(i).forEach(System.out::println);
            System.out.println("------------------------------\n");
        }
    }
}
