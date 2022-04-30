package org.harrel.java2ts;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;

public class JavaToTsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        var extension = project.getExtensions().create("generateTsDeclarations", GenerateExtension.class);

        project.getTasks().register("generateTsDeclarations", GenerateTsDeclarationsTask.class, task -> {
            task.dependsOn(extension.getSourceSet().get().getCompileJavaTaskName());
            task.getSourceFiles().set(extension.getSourceSet().get().getOutput().getClassesDirs().getFiles());
            task.getTypes().set(extension.getTypes());
            task.getOutput().set(extension.getOutput().orElse(() -> getDefaultOutput(project)));
        });
    }

    private File getDefaultOutput(Project project) {
        return project.getBuildDir().toPath().resolve("generated/java2ts/types.d.ts").toFile();
    }
}
