package com.company;


import io.bootique.BQCoreModule;
import io.bootique.Bootique;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.meta.application.OptionMetadata;

public class AnalyzatorModule implements BQModule {
    public static void main(String[] args) {
        Bootique
                .app(args)
                .autoLoadModules()
                .module(AnalyzatorModule.class)
                .args("--analyze")
                .exec()
                .exit();

    }

    @Override
    public void configure(Binder binder) {
        OptionMetadata pathOption = OptionMetadata
                .builder("path", "Path to analyzed project root")
                .valueRequired("path")
                .build();

        OptionMetadata varietyOption = OptionMetadata
                .builder("threshold", "Threshold of variety into components splitting algorithm if your want to use it instead of direct dependencies (optional)")
                .valueRequired()
                .build();

        OptionMetadata pointsOption = OptionMetadata
                .builder("points", "Classes with entry points of program, split by comma")
                .valueRequired()
                .build();


        BQCoreModule.extend(binder)
                .addOption(pathOption)
                .addOption(varietyOption)
                .addOption(pointsOption)
                .addCommand(AnalyzeCommand.class);
    }
}
