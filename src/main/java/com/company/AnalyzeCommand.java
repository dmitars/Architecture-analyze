package com.company;

import com.company.processors.*;
import com.company.storage.Graph;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import spoon.Launcher;
import spoon.reflect.CtModel;

import java.util.List;

public class AnalyzeCommand implements Command {

    @Override
    public CommandOutcome run(Cli cli) {
        if(!cli.hasOption("path")){
            return CommandOutcome.failed(1, "Path to project into --path option required");
        }
        var entryPoints = cli.hasOption("points") ? List.of(cli.optionString("points").split(",")) : List.of("com.company.Main");

        Launcher launcher = new Launcher();
        launcher.addInputResource(cli.optionString("path"));//"/home/dmitars/IdeaProjects/pdftojrxml/testproject4");
        //MavenLauncher launcher = new MavenLauncher("/home/dmitars/IdeaProjects/pdftojrxml/testproject4", MavenLauncher.SOURCE_TYPE.APP_SOURCE);
        launcher.addProcessor(new VariableProcessor());
        launcher.addProcessor(new TypeReferenceProcessor());
        launcher.addProcessor(new PackageProcessor());
        launcher.addProcessor(new ClassProcessor());
        launcher.addProcessor(new InvocationProcessor());
        launcher.addProcessor(new AssignmentProcessor());
        launcher.addProcessor(new IfProcessor());
        launcher.run();
        CtModel model = launcher.getModel();
        Graph.postProcess(model, cli.hasOption("threshold") ? Double.parseDouble(cli.optionString("threshold")) : null);

        Graph.printStatisticForClasses();
        var levels = Graph.findLevels(entryPoints);
        for (var level : levels.entrySet()) {
            System.out.println("Level " + level.getKey() + ":");
            for (var className : level.getValue()) {
                System.out.println(className);
            }
            System.out.println("-----------------\n\n");
        }
        return CommandOutcome.succeeded();
    }
}
