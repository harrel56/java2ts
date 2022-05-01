package org.harrel.java2ts;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}

public abstract class GenerateTsDeclarationsTask extends DefaultTask {


    @Input
    public abstract SetProperty<File> getSourceFiles();

    @Input
    public abstract SetProperty<String> getTypes();

    @Input
    @Optional
    public abstract Property<Boolean> getSorting();

    @Input
    @Optional
    public abstract Property<Boolean> getRecursive();

    @Input
    @Optional
    public abstract SetProperty<String> getUnsupportedTypes();

    @Input
    @Optional
    public abstract Property<SerializableFunction<Class<?>, String>> getNameResolver();

    @OutputFile
    @Optional
    public abstract RegularFileProperty getOutput();

    @TaskAction
    public void generate() {
        URL[] urls = getSourceFiles().get().stream()
                .map(this::toURL)
                .toArray(URL[]::new);

        try (URLClassLoader ucl = new URLClassLoader(urls, ClassLoader.getSystemClassLoader())) {
            TsGenerator gen = createTsGenerator(ucl);
            for (String typeName : getTypes().get()) {
                gen.registerType(loadClass(ucl, typeName));
            }
            writeToOutput(gen);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

    }

    private TsGenerator createTsGenerator(URLClassLoader ucl) {
        TsGenerator gen = new TsGenerator();
        gen.setSortingEnabled(Boolean.TRUE.equals(getSorting().getOrElse(true)));
        if (!getUnsupportedTypes().getOrElse(Set.of()).isEmpty()) {
            var clazzSet = getUnsupportedTypes().get().stream()
                    .map(name -> loadClass(ucl, name))
                    .collect(Collectors.toSet());
            gen.setUnsupportedTypes(clazzSet);
        }
        if (getNameResolver().isPresent()) {
            gen.setNameResolver(getNameResolver().get());
        }
        return gen;
    }

    private void writeToOutput(TsGenerator gen) throws IOException {
        try (var writer = Files.newBufferedWriter(getOutput().get().getAsFile().toPath())) {
            if (Boolean.TRUE.equals(getRecursive().getOrElse(true))) {
                writer.write(gen.getAllDeclarations());
            } else {
                writer.write(gen.getRegisteredDeclarations());
            }
        }
    }

    private URL toURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Class<?> loadClass(URLClassLoader ucl, String name) {
        try {
            return ucl.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
