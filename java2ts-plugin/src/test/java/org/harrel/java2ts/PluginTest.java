package org.harrel.java2ts;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginTest {

    @TempDir
    private Path testProjectDir;
    private Path buildFile;

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

    @Test
    void createsOutputFile() throws IOException {
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations")
                .build();

        System.out.println(result.getOutput());
        Path out = testProjectDir.resolve(Path.of("build", "generated", "java2ts", "types.d.ts"));

        assertTrue(Files.exists(out));
        try (var reader = Files.newBufferedReader(out)) {
            long match = reader.lines()
                    .filter(l -> l.contains("export declare interface Sample {"))
                    .count();
            assertEquals(1L, match);
        }
    }

    private void writeFile(Path dest, String content) throws IOException {
        try (var out = Files.newBufferedWriter(dest)) {
            out.write(content);
        }
    }

    private void copyDir(Path sourceDir, Path destDir) throws IOException {
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