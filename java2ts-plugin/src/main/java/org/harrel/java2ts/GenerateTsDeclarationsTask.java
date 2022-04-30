package org.harrel.java2ts;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

public abstract class GenerateTsDeclarationsTask extends DefaultTask {

    @Input
    public abstract SetProperty<File> getSourceFiles();

    @Input
    public abstract SetProperty<String> getTypes();

    @Optional
    @OutputFile
    public abstract RegularFileProperty getOutput();

    @TaskAction
    public void generate() {
        URL[] urls = getSourceFiles().get().stream()
                .map(this::toURL)
                .toArray(URL[]::new);

        try (URLClassLoader ucl = new URLClassLoader(urls, ClassLoader.getSystemClassLoader())) {
            TsGenerator gen = new TsGenerator();
            for (String typeName : getTypes().get()) {
                gen.registerType(ucl.loadClass(typeName));
            }
            System.out.println(gen.getAllDeclarations());
            try(var writer = Files.newBufferedWriter(getOutput().get().getAsFile().toPath())) {
                writer.write(gen.getAllDeclarations());
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }

    }

    private URL toURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
