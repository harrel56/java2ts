package dev.harrel.java2ts;

import groovy.lang.Closure;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.SourceSet;

public abstract class GenerateExtension {

    public abstract Property<SourceSet> getSourceSet();

    public abstract SetProperty<String> getIncludeTypes();

    public abstract SetProperty<String> getExcludeTypes();

    public abstract Property<Boolean> getSorting();

    public abstract Property<Boolean> getRecursive();

    public abstract Property<Closure<Boolean>> getSupportedPredicate();

    public abstract Property<Closure<String>> getNameResolver();

    public abstract RegularFileProperty getOutput();


}
