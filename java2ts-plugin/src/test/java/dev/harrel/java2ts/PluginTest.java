package dev.harrel.java2ts;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

class PluginTest extends PluginTestBase {

    @Test
    void worksForDefaults() throws IOException {
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations", "--stacktrace")
                .build();

        System.out.println(result.getOutput());
        Path out = testProjectDir.resolve(Path.of("build", "generated", "java2ts", "types.d.ts"));

        assertFileContains(out, "export declare interface Sample {");
    }

    @Test
    void excludesTypes() throws IOException {
        String ext = """
                generateTsDeclarations {
                    excludeTypes = ['org.testing.Sample']
                }""";
        appendFile(buildFile, ext);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations", "--stacktrace")
                .build();

        System.out.println(result.getOutput());
        Path out = testProjectDir.resolve(Path.of("build", "generated", "java2ts", "types.d.ts"));

        assertFileNotContains(out, "export declare interface Sample {");
    }

    @Test
    void createsCustomOutputFile() throws IOException {
        String ext = """
                generateTsDeclarations {
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
    void transitive() throws IOException {
        String ext = """
                generateTsDeclarations {
                    includeTypes = ['org.testing.Child']
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
    void nonTransitive() throws IOException {
        String ext = """
                generateTsDeclarations {
                    includeTypes = ['org.testing.Child']
                    transitive = false
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
    void supportedPredicate() throws IOException {
        String ext = """
                generateTsDeclarations {
                    includeTypes = ['org.testing.Child']
                    supportedPredicate = { type -> !"Parent".equals(type.getSimpleName()) }
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
    void nameResolver() throws IOException {
        String ext = """
                generateTsDeclarations {
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
                    includeTypes = ['org.testing.Sorted']
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

    @Test
    void externalDependencies() throws IOException {
        String ext = """
                sourceSets {
                    main {
                        java {
                            srcDir 'src/external'
                        }
                    }
                }
                                
                repositories {
                    mavenCentral()
                }
                                
                dependencies {
                    implementation 'com.google.guava:guava:31.1-jre'
                }
                                
                generateTsDeclarations {
                    includeTypes = ['org.testing.External']
                }""";
        appendFile(buildFile, ext);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .withArguments("generateTsDeclarations", "--stacktrace")
                .build();

        System.out.println(result.getOutput());
        Path out = testProjectDir.resolve(Path.of("build", "generated", "java2ts", "types.d.ts"));

        assertFileContains(out, "guavaOptional: Optional<string> | null");
    }
}