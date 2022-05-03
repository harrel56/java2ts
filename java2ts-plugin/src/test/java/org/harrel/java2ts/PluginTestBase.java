package org.harrel.java2ts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

abstract class PluginTestBase {

    @TempDir
    Path testProjectDir;
    Path buildFile;

    @BeforeEach
    void setup() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        URL templateUrl = cl.getResource("buildTemplate.gradle");
        Objects.requireNonNull(templateUrl);
        Path resourceDir = Path.of(templateUrl.toURI()).getParent();
        copyDir(resourceDir, testProjectDir);

        Path propertiesFile = testProjectDir.resolve("gradle.properties");
        writeFile(propertiesFile, "org.gradle.jvmargs =--enable-preview");
        Path settingsFile = testProjectDir.resolve("settings.gradle");
        writeFile(settingsFile, "rootProject.name = 'plugin-test'");
        buildFile = testProjectDir.resolve("build.gradle");
        try (var in = templateUrl.openStream()) {
            in.transferTo(Files.newOutputStream(buildFile));
        }

    }

    void assertFileContains(Path file, String content) throws IOException {
        assertTrue(Files.exists(file));
        try (var reader = Files.newBufferedReader(file)) {
            String fileContent = reader.lines().collect(Collectors.joining("\n"));
            System.out.println(fileContent);
            assertTrue(fileContent.contains(content));
        }
    }

    void assertFileNotContains(Path file, String content) throws IOException {
        assertTrue(Files.exists(file));
        try (var reader = Files.newBufferedReader(file)) {
            String fileContent = reader.lines().collect(Collectors.joining("\n"));
            System.out.println(fileContent);
            assertFalse(fileContent.contains(content));
        }
    }

    void assertFileContainsExactly(Path file, String content) throws IOException {
        assertTrue(Files.exists(file));
        try (var reader = Files.newBufferedReader(file)) {
            String fileContent = reader.lines().collect(Collectors.joining("\n"));
            System.out.println(fileContent);
            assertEquals(content, fileContent);
        }
    }

    void writeFile(Path dest, String content) throws IOException {
        try (var out = Files.newBufferedWriter(dest)) {
            out.write(content);
        }
    }

    void appendFile(Path dest, String content) throws IOException {
        try (var out = Files.newBufferedWriter(dest, StandardOpenOption.APPEND)) {
            out.write(content);
        }
    }

    void copyDir(Path sourceDir, Path destDir) throws IOException {
        String src = sourceDir.toString();
        String dest = destDir.toString();
        try (var pathStream = Files.walk(sourceDir)) {
            pathStream
                    .filter(p -> p != sourceDir)
                    .filter(p -> !p.toString().endsWith(".gradle"))
                    .forEach(path -> {
                        Path destPath = Paths.get(dest, path.toString().substring(src.length()));
                        try {
                            Files.copy(path, destPath);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }
}
