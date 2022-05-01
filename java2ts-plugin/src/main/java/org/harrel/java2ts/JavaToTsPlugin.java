package org.harrel.java2ts;

import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.util.Set;

public class JavaToTsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        var extension = project.getExtensions().create("generateTsDeclarations", GenerateExtension.class);

        project.getTasks().register("generateTsDeclarations", GenerateTsDeclarationsTask.class, task -> {
            task.dependsOn(extension.getSourceSet().get().getCompileJavaTaskName());
            task.getSourceFiles().set(extension.getSourceSet().get().getOutput().getClassesDirs().getFiles());
            if (extension.getTypes().getOrElse(Set.of()).isEmpty()) {
                task.getTypes().set((Iterable<? extends String>) null);
            } else {
                task.getTypes().set(extension.getTypes());
            }
            task.getOutput().set(extension.getOutput().orElse(() -> getDefaultOutput(project)));
            task.getSorting().set(extension.getSorting());
            task.getRecursive().set(extension.getRecursive());
            task.getUnsupportedTypes().set(extension.getUnsupportedTypes());

            if (extension.getNameResolver().isPresent()) {
                SerializableFunction<Class<?>, String> serial = closureToFunction(extension.getNameResolver().get());
                task.getNameResolver().set(serial);
            }
        });
    }

    private File getDefaultOutput(Project project) {
        return project.getBuildDir().toPath().resolve("generated/java2ts/types.d.ts").toFile();
    }

    private SerializableFunction<Class<?>, String> closureToFunction(Closure<String> closure) {
        Closure<String> dehydrated = closure.dehydrate();
        return dehydrated::call;
    }
}
