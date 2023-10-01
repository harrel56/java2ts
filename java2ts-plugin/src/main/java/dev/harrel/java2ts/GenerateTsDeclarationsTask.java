package dev.harrel.java2ts;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@CacheableTask
public abstract class GenerateTsDeclarationsTask extends DefaultTask {

    @InputFiles
    @Classpath
    public abstract Property<FileCollection> getRuntimeClasspath();

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract Property<FileCollection> getCompiledSources();

    @Input
    @Optional
    public abstract SetProperty<String> getIncludeTypes();

    @Input
    @Optional
    public abstract SetProperty<String> getExcludeTypes();

    @Input
    @Optional
    public abstract Property<Boolean> getSorting();

    @Input
    @Optional
    public abstract Property<Boolean> getTransitive();

    @Input
    @Optional
    public abstract Property<SerializablePredicate<Class<?>>> getSupportedPredicate();

    @Input
    @Optional
    public abstract Property<SerializableFunction<Class<?>, String>> getNameResolver();

    @OutputFile
    @Optional
    public abstract RegularFileProperty getOutput();

    @TaskAction
    public void generate() {
        FileCollection compiledSources = getCompiledSources().get();
        Set<String> allTypes = getIncludeTypes().get();
        if (allTypes.isEmpty()) {
            allTypes = getAllTypes(compiledSources);
        }
        if (!getExcludeTypes().get().isEmpty()) {
            allTypes = new HashSet<>(allTypes);
            allTypes.removeAll(getExcludeTypes().get());
        }

        FileCollection allFiles = getRuntimeClasspath().get().plus(compiledSources);
        URL[] urls = allFiles.getFiles().stream()
                .map(this::toURL)
                .toArray(URL[]::new);

        try (URLClassLoader ucl = new URLClassLoader(urls, ClassLoader.getSystemClassLoader())) {
            TsGenerator gen = createTsGenerator();
            for (String typeName : allTypes) {
                gen.registerType(loadClass(ucl, typeName));
            }
            writeToOutput(gen);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    private Set<String> getAllTypes(FileCollection fileCollection) {
        String basePath = fileCollection.getAsPath();
        return fileCollection.getAsFileTree().getFiles().stream()
                .map(file -> file.getAbsolutePath().substring(basePath.length() + 1))
                .filter(path -> path.endsWith(".class"))
                .map(path -> path.substring(0, path.length() - ".class".length()))
                .map(path -> path.replace(File.separator, "."))
                .collect(Collectors.toSet());
    }

    private TsGenerator createTsGenerator() {
        TsGenerator gen = new TsGenerator();
        if (getSorting().isPresent()) {
            gen.setSortingEnabled(getSorting().get());
        }
        if (getSupportedPredicate().isPresent()) {
            gen.setSupportedPredicate(getSupportedPredicate().get());
        }
        if (getNameResolver().isPresent()) {
            gen.setNameResolver(getNameResolver().get());
        }
        return gen;
    }

    private void writeToOutput(TsGenerator gen) throws IOException {
        try (var writer = Files.newBufferedWriter(getOutput().get().getAsFile().toPath())) {
            if (Boolean.TRUE.equals(getTransitive().getOrElse(true))) {
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
