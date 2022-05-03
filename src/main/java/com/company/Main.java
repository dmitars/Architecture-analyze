package com.company;


import com.company.processors.*;
import com.company.storage.Graph;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;

import java.lang.instrument.ClassDefinition;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        MavenLauncher launcher = new MavenLauncher("/home/dmitars/IdeaProjects/pdftojrxml/testproject4", MavenLauncher.SOURCE_TYPE.APP_SOURCE);
        launcher.addProcessor(new VariableProcessor());
        launcher.addProcessor(new TypeReferenceProcessor());
        launcher.addProcessor(new PackageProcessor());
        launcher.addProcessor(new ClassProcessor());
        launcher.addProcessor(new InvocationProcessor());
        launcher.addProcessor(new AssignmentProcessor());
        launcher.addProcessor(new IfProcessor());
        launcher.run();
        CtModel model = launcher.getModel();
        Graph.postProcess(model);
        Graph.printStatisticForClasses();
        var levels = Graph.findLevels(List.of("com.company.Main"));
        for (var level : levels.entrySet()) {
            System.out.println("Level " + level.getKey() + ":");
            for (var className : level.getValue()) {
                System.out.println(className);
            }
            System.out.println("-----------------\n\n");
        }
    }
}
