package org.harrel.java2ts;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.SourceSet;

public abstract class GenerateExtension {

    public abstract Property<SourceSet> getSourceSet();

    public abstract SetProperty<String> getTypes();

    public abstract RegularFileProperty getOutput();

}
