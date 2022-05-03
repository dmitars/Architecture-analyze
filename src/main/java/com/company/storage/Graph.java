package com.company.storage;

import com.company.algorithm.CircularPathsEngine;
import com.company.algorithm.Coherence;
import com.company.algorithm.Coupling;
import com.company.algorithm.LevelsEngine;
import com.company.dsu.DSU;
import com.company.dsu.DefaultDSU;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.declaration.CtMethodImpl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.company.storage.ComponentInfoStatistic.*;

public class Graph {
    private static final DSU<String> dsu = new DefaultDSU<>();
    private static final Map<String, ComponentInfo> components = new HashMap<>();
    private static final Map<CouplingInfo, Coupling> coupling = new HashMap<>();
    private static final Map<CouplingInfo, Double> varieties = new HashMap<>();
    private static final Map<CtPackageReference, HashSet<CtPackageReference>> packsStorage = new HashMap<>();
    private static final Map<String, HashSet<String>> abstractions = new HashMap<>();
    private static int ifsCount = 0;

    private static List<List<CtPackageReference>> circularPackagePaths = new ArrayList<>();
    private static List<List<String>> circularClassesPaths = new ArrayList<>();


    public static void addDependencyForClass(String className, CtTypeReference<?> dependency, boolean isStatic) {
        if (dependency.getPackage() == null || className.equals(dependency.getQualifiedName())
                || dependency.getQualifiedName().startsWith("java."))
            return;

        String dependencyName = dependency.getQualifiedName();
        var outComponentInfo = components.getOrDefault(className, new ComponentInfo(className));
        var inComponentInfo = components.getOrDefault(dependencyName, new ComponentInfo(dependencyName));

        addNewDependency(outComponentInfo, inComponentInfo, value -> new DefaultDependency(value, isStatic));
    }

    public static void addMethodDependencyForClass(String className, CtTypeReference<?> dependency, CtInvocation<?> method) {
        if (dependency.getPackage() == null || className.equals(dependency.getQualifiedName())
                || dependency.getQualifiedName().startsWith("java."))
            return;

        String dependencyName = dependency.getQualifiedName();
        var outComponentInfo = components.getOrDefault(className, new ComponentInfo(className));
        var inComponentInfo = components.getOrDefault(dependencyName, new ComponentInfo(dependencyName));

        HashSet<Dependency> dependencies = outComponentInfo.getStorageIngoing();
        var addedMethodDependency = dependencies.stream()
                .filter(dependency1 -> dependency1 instanceof MethodsDependency && dependency1.getDependencyName().equals(className))
                .map(dependency1 -> (MethodsDependency) dependency1)
                .findAny();

        addedMethodDependency.ifPresent(dependency1 -> dependency1.addMethodToDependencies(method));

        var methodDependency = addedMethodDependency.orElse(new MethodsDependency(className, new HashSet<>(List.of(method)), method.getExecutable().isStatic()));
        upgradeCoherenceIfRequired(className, recalculateCoherenceInAccordanceWith(methodDependency, method));
        upgradeСouplingIfRequired(className, dependencyName, recalculateCouplingInAccordanceWith(className, methodDependency, method));

        if (addedMethodDependency.isEmpty()) {
            addNewDependency(outComponentInfo, inComponentInfo, name -> new MethodsDependency(name, new HashSet<>(List.of(method))));
        }
    }


    private static void addNewDependency(ComponentInfo outComponent, ComponentInfo inComponent, Function<String, Dependency> applier) {
        HashSet<Dependency> dependencies = outComponent.getStorageOutgoing();
        var dep = applier.apply(inComponent.getClassName());
        dependencies.add(dep);

        HashSet<Dependency> ingoingDependencies = inComponent.getStorageIngoing();
        ingoingDependencies.add(applier.apply(outComponent.getClassName()));

        components.putIfAbsent(outComponent.getClassName(), outComponent);
        components.putIfAbsent(inComponent.getClassName(), inComponent);
    }

    public static void markMethodAsFieldAccessor(String classname, String method, boolean isStatic) {
        var classInfo = components.getOrDefault(classname, new ComponentInfo(classname));
        var methodsWithAssignment = isStatic ? classInfo.getMethodsWithStaticFieldsAssignment() : classInfo.getMethodsWithFieldsAssignment();
        methodsWithAssignment.add(method);
        components.putIfAbsent(classname, classInfo);
    }

    public static void addMethodToManagedByParams(String classname, String methodName) {
        var classInfo = components.getOrDefault(classname, new ComponentInfo(classname));
        var methodsManagedByParams = classInfo.getMethodsManagedByParams();
        methodsManagedByParams.add(methodName);
        components.putIfAbsent(classname, classInfo);
    }

    public static void markClassAsStaticFieldAccessor(String classType, String qualifiedName) {
        var classInfo = components.getOrDefault(qualifiedName, new ComponentInfo(qualifiedName));
        classInfo.getClassesWithStaticFieldsAssignment().add(classType);
        components.putIfAbsent(qualifiedName, classInfo);
    }

    private static Coherence recalculateCoherenceInAccordanceWith(MethodsDependency methodsDependency, CtInvocation<?> method) {
        var superMethod = method.getParent(CtExecutableReference.class);
        var classOfMethod = method.getExecutable().getDeclaration().getParent(CtClass.class);
        if (classOfMethod != null) {
            List<?> classMembers = classOfMethod.getTypeMembers();
            var publicMethods = classMembers.stream().filter(member -> member instanceof CtMethodImpl<?>)
                    .map(member -> (CtMethodImpl<?>) member)
                    .filter(member -> member.getVisibility() != null && member.getVisibility().equals(ModifierKind.PUBLIC))
                    .collect(Collectors.toSet());
            if (publicMethods.size() <= 1) {
                return Coherence.FUNCTIONAL;
            } else if (methodsDependency.hasMethodWithParams(Collections.singletonList(method.getType()), superMethod)) {
                return Coherence.INFO;
            } else if (methodsDependency.hasMethodWithParams(method.getExecutable().getParameters(), superMethod)) {
                return Coherence.COMMUNICATIVE;
            } else if (publicMethods.stream().anyMatch(pd -> methodsDependency.hasMethodWithName(pd.getSimpleName()))) {
                return Coherence.PROCEDURE;
            } else {
                return Coherence.TIME;
            }
        }

        return Coherence.TIME;
    }

    private static Coupling recalculateCouplingInAccordanceWith(String className, MethodsDependency methodsDependency, CtInvocation<?> method) {
        var computedCoupling = Coupling.DATA;

        if (isNotSimple(method.getType())) {
            computedCoupling = Coupling.EXTERNAL_LINKS;
        }

        for (CtTypeReference<?> parameter : method.getExecutable().getParameters()) {
            if (isNotSimple(parameter) && !parameter.getQualifiedName().equals(className)
                    && !parameter.getQualifiedName().equals(methodsDependency.getDependencyName())) {
                computedCoupling = Coupling.EXTERNAL_LINKS;
                break;
            }
        }
        return computedCoupling;
    }

    private static boolean isNotSimple(CtTypeReference<?> parameter) {
        return parameter.getPackage() != null && !parameter.getQualifiedName().startsWith("java.");
    }

    private static void upgradeCoherenceIfRequired(String className, Coherence coherenceToSet) {
        var componentInfo = components.getOrDefault(className, new ComponentInfo(className));
        componentInfo.upgradeCoherenceIfRequired(coherenceToSet);
        components.putIfAbsent(className, componentInfo);
    }

    private static void upgradeСouplingIfRequired(String className, String dependencyName, Coupling couplingToSet) {
        var couplingInfo = new CouplingInfo(className, dependencyName);
        var couplingToReplace = coupling.getOrDefault(couplingInfo, Coupling.INDEPENDENT);
        if (couplingToSet.getValue() > couplingToReplace.getValue())
            coupling.put(couplingInfo, couplingToSet);
    }


    public static void addDependencyForPackage(CtPackageReference packageReference, CtPackageReference dependency) {
        packsStorage.computeIfAbsent(packageReference, k -> new HashSet<>()).add(dependency);
    }

    public static void addCircularPath(List<CtPackageReference> circularPath) {
        circularPackagePaths.add(circularPath);
    }

    public static void pushElementsToDsu(String first, String second) {
        dsu.processElements(first, second);
    }

    public static void pushElementToDsu(String first) {
        dsu.addElement(first);
    }

    public static Map<Integer, Set<String>> findLevels(Collection<String> inEndpoints) {
        var engine = new LevelsEngine(getStorageOutgoing(), abstractions);
        return engine.processValues(inEndpoints);
    }

    public static void processInheritance(String inheritor, String implementation) {
        pushElementsToDsu(implementation, inheritor);
        abstractions.computeIfAbsent(inheritor, k -> new HashSet<>()).add(implementation);
    }

    public static Map<String, Set<Dependency>> getStorageOutgoing() {
        return components.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getStorageOutgoing()));
    }

    public static void postProcess(CtModel model) {
        components.values().stream()
                .filter(entry -> ComponentInfoStatistic.instabilityOf(entry) < 0.8)
                .forEach(entry -> entry.getStorageOutgoing().forEach(value -> pushElementsToDsu(entry.getClassName(), value.getDependencyName())));
        components.values().forEach(componentInfo -> componentInfo.getStorageOutgoing().removeIf(value -> !dsu.contains(value.getDependencyName())));
        components.keySet().removeIf(key -> !dsu.contains(key));
        ifsCount = Query.getElements(model.getRootPackage(), new TypeFilter<>(CtIf.class)).size();

        upgradeVarietyOfChanges();
        processCircularDependencies();
    }

    private static void upgradeVarietyOfChanges() {
        var componentInfos = components.values().stream().toList();
        for (int i = 0; i < componentInfos.size(); i++) {
            for (int j = i + 1; j < componentInfos.size(); j++) {
                var component = componentInfos.get(i);
                var secondComponent = componentInfos.get(j);
                var couplingValue = coupling.getOrDefault(new CouplingInfo(component.getClassName(), secondComponent.getClassName()), Coupling.INDEPENDENT);
                if (couplingValue.getValue() < Coupling.MANAGEMENT.getValue()) {
                    if (oneManagesAnother(component, secondComponent))
                        couplingValue = Coupling.MANAGEMENT;
                }
                if (couplingValue.getValue() < Coupling.AREA.getValue()) {
                    var status = getCommonStaticAccessOf(component, secondComponent);

                    if (status == StaticFieldsAccessStatus.READ_WRITE)
                        couplingValue = Coupling.AREA;
                    else if (status == StaticFieldsAccessStatus.READ)
                        couplingValue = Coupling.PATTERN;
                }

                if (couplingValue.getValue() == Coupling.EXTERNAL_LINKS.getValue()) {
                    if (oneHasAccessToAnotherFields(component, secondComponent))
                        couplingValue = Coupling.CONTENT;
                }

                var variety = calculateVariety(component.getCoherence(), secondComponent.getCoherence(), couplingValue);
                varieties.putIfAbsent(new CouplingInfo(component.getClassName(), secondComponent.getClassName()), variety);
            }
        }
    }

    private static double calculateVariety(Coherence firstCoherence, Coherence secondCoherence, Coupling coupling) {
        if (coupling == Coupling.INDEPENDENT)
            return 0;
        return (0.15 * ((double) firstCoherence.getValue() + secondCoherence.getValue()) + 0.7 * coupling.getValue()) / 10;
    }

    private static StaticFieldsAccessStatus getCommonStaticAccessOf(ComponentInfo firstComponent, ComponentInfo secondComponent) {
        var firstOutgoing = new HashSet<>(firstComponent.getStorageOutgoing());
        var secondOutgoing = new HashSet<>(secondComponent.getStorageOutgoing());
        firstOutgoing.retainAll(secondOutgoing);
        var commonStatus = StaticFieldsAccessStatus.NONE;
        for (var dependency : firstOutgoing) {
            var firstStatus = getStaticAccessStatusToFieldsOf(firstComponent.getClassName(), components.get(dependency.getDependencyName()));
            var secondStatus = getStaticAccessStatusToFieldsOf(secondComponent.getClassName(), components.get(dependency.getDependencyName()));
            var minStatus = StaticFieldsAccessStatus.min(firstStatus, secondStatus);

            if (minStatus == StaticFieldsAccessStatus.READ_WRITE)
                return minStatus;

            if (minStatus.getValue() > commonStatus.getValue()) {
                commonStatus = minStatus;
            }
        }
        return commonStatus;
    }

    private static void processCircularDependencies() {
        circularClassesPaths = CircularPathsEngine.processGraph(getStorageOutgoing(), Dependency::getDependencyName);
        circularPackagePaths = CircularPathsEngine.processGraph(packsStorage, Function.identity());
    }

    public static void printStatisticForClasses() {
        components.values().forEach(ComponentInfoStatistic::printStatisticFor);

        var sets = dsu.getSets();
        for (var set : sets) {
            System.out.println("\nfound Component:");
            set.forEach(System.out::println);
            System.out.println("------------------------------\n");
        }

        for (var circularPath : circularPackagePaths) {
            System.out.println("\n\nCircular path between packages found: ");
            for (var value : circularPath) {
                System.out.print(value + " -> ");
            }
            System.out.println("\n------------------------------\n");
        }

        for (var circularPath : circularClassesPaths) {
            System.out.println("\n\nCircular path between classes found: ");
            for (var value : circularPath) {
                System.out.print(value + " -> ");
            }
            System.out.println("\n------------------------------\n");
        }

        for (var entry : varieties.entrySet()) {
            System.out.println(entry.getKey().getFirstClassName() + " " + entry.getKey().getSecondClassName() + " " + entry.getValue());
        }

        System.out.println("\n");
        System.out.println("Abstractness: " + (double) abstractions.size() / components.size());
        System.out.println("Cyclomatic complexity: " + (ifsCount + 1));
    }
}
