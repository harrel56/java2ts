package org.harrel.java2ts;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PluginTest extends PluginTestBase {

    @Test
    void createDefaultOutputFile() throws IOException {
        String ext = """
                generateTsDeclarations {
                    sourceSet = project.sourceSets.getByName('main')
                    types = ['org.testing.Sample']
                }""";
        appendFile(buildFile, ext);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations")
                .build();

        System.out.println(result.getOutput());
        Path out = testProjectDir.resolve(Path.of("build", "generated", "java2ts", "types.d.ts"));

        assertFileContains(out, "export declare interface Sample {");
    }

    @Test
    void createCustomOutputFile() throws IOException {
        String ext = """
                generateTsDeclarations {
                    sourceSet = project.sourceSets.getByName('main')
                    types = ['org.testing.Sample']
                    output = project.layout.projectDirectory.file("custom")
                }""";
        appendFile(buildFile, ext);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations")
                .build();

        System.out.println(result.getOutput());
        Path out = testProjectDir.resolve(Path.of("custom"));

        assertFileContains(out, "export declare interface Sample {");
    }

    @Test
    void recursive() throws IOException {
        String ext = """
                generateTsDeclarations {
                    sourceSet = project.sourceSets.getByName('main')
                    types = ['org.testing.Child']
                }""";
        appendFile(buildFile, ext);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations")
                .build();

        System.out.println(result.getOutput());
        Path out = testProjectDir.resolve(Path.of("build", "generated", "java2ts", "types.d.ts"));

        assertFileContains(out, "export declare interface Parent {");
    }

    @Test
    void nonRecursive() throws IOException {
        String ext = """
                generateTsDeclarations {
                    sourceSet = project.sourceSets.getByName('main')
                    types = ['org.testing.Child']
                    recursive = false
                }""";
        appendFile(buildFile, ext);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations")
                .build();

        System.out.println(result.getOutput());
        Path out = testProjectDir.resolve(Path.of("build", "generated", "java2ts", "types.d.ts"));

        assertFileNotContains(out, "export declare interface Parent {");
    }

    @Test
    void unsupportedTypes() throws IOException {
        String ext = """
                generateTsDeclarations {
                    sourceSet = project.sourceSets.getByName('main')
                    types = ['org.testing.Child']
                    unsupportedTypes = ['org.testing.Parent']
                }""";
        appendFile(buildFile, ext);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations")
                .build();

        System.out.println(result.getOutput());
        Path out = testProjectDir.resolve(Path.of("build", "generated", "java2ts", "types.d.ts"));

        assertFileContains(out, "export declare interface Child {");
        assertFileNotContains(out, "export declare interface Parent {");
    }

    @Test
    void failForEmptyTypes() throws IOException {
        String ext = """
                generateTsDeclarations {
                    sourceSet = project.sourceSets.getByName('main')
                }""";
        appendFile(buildFile, ext);

        GradleRunner runner = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations");
        assertThrows(UnexpectedBuildFailure.class, runner::build);
    }

    @Test
    void failForEmptySourceSet() throws IOException {
        String ext = """
                generateTsDeclarations {
                    types = ['org.testing.Child']
                }""";
        appendFile(buildFile, ext);

        GradleRunner runner = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations");
        assertThrows(UnexpectedBuildFailure.class, runner::build);
    }

    @Test
    void nameResolver() throws IOException {
        String ext = """
                generateTsDeclarations {
                    sourceSet = project.sourceSets.getByName('main')
                    types = ['org.testing.Child']
                    nameResolver = { type -> type.getSimpleName() + 'xxx' }
                }""";
        appendFile(buildFile, ext);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations", "--stacktrace")
                .build();

        System.out.println(result.getOutput());
        Path out = testProjectDir.resolve(Path.of("build", "generated", "java2ts", "types.d.ts"));

        assertFileContains(out, "export declare interface Parentxxx {");
    }

    @Test
    void sorting() throws IOException {
        String ext = """
                generateTsDeclarations {
                    sourceSet = project.sourceSets.getByName('main')
                    types = ['org.testing.Sorted']
                }""";
        appendFile(buildFile, ext);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations")
                .build();

        System.out.println(result.getOutput());
        Path out = testProjectDir.resolve(Path.of("build", "generated", "java2ts", "types.d.ts"));

        assertFileContainsExactly(out, """
                                        export declare interface Sorted {
                                            a(): number
                                            b(): void
                                            c(arg0: string | null): string | null
                                            d(): void
                                            e(): void
                                            f(): void
                                            g(): void
                                        }""");
    }
}