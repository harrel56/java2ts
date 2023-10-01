package dev.harrel.java2ts;

import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.util.Optional;

import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;

public class JavaToTsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        var extension = project.getExtensions().create("generateTsDeclarations", GenerateExtension.class);

        project.getTasks().register("generateTsDeclarations", GenerateTsDeclarationsTask.class, task -> {
            SourceSet sourceSet = extension.getSourceSet().convention(getDefaultSourceSet(project)).get();

            task.dependsOn(sourceSet.getCompileJavaTaskName());
            task.getRuntimeClasspath().set(sourceSet.getRuntimeClasspath());
            task.getCompiledSources().set(sourceSet.getOutput().getClassesDirs());
            task.getIncludeTypes().set(extension.getIncludeTypes());
            task.getExcludeTypes().set(extension.getExcludeTypes());
            task.getOutput().set(extension.getOutput().orElse(getDefaultOutput(project)));
            task.getSorting().set(extension.getSorting());
            task.getTransitive().set(extension.getTransitive());
            if (extension.getSupportedPredicate().isPresent()) {
                var serial = closureToPredicate(extension.getSupportedPredicate().get());
                task.getSupportedPredicate().set(serial);
            }

            if (extension.getNameResolver().isPresent()) {
                var serial = closureToFunction(extension.getNameResolver().get());
                task.getNameResolver().set(serial);
            }
        });
    }

    private Provider<SourceSet> getDefaultSourceSet(Project project) {
        return project.getProviders().provider(() ->
                Optional.ofNullable(project.getExtensions().findByType(SourceSetContainer.class))
                        .map(s -> s.findByName(MAIN_SOURCE_SET_NAME))
                        .orElseThrow(() -> new IllegalArgumentException("Source set was not provided and there was no source set named '%s'".formatted(MAIN_SOURCE_SET_NAME))));
    }

    private Provider<RegularFile> getDefaultOutput(Project project) {
        return project.getLayout().getBuildDirectory().file("generated/java2ts/types.d.ts");
    }

    private SerializableFunction<Class<?>, String> closureToFunction(Closure<String> closure) {
        Closure<String> dehydrated = closure.dehydrate();
        return dehydrated::call;
    }

    private SerializablePredicate<Class<?>> closureToPredicate(Closure<Boolean> closure) {
        Closure<Boolean> dehydrated = closure.dehydrate();
        return dehydrated::call;
    }
}
