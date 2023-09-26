package dev.harrel.java2ts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericClassesTest {

    @Test
    void concreteClass() {
        class Simple<T> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple<T> {\n"), out);
    }

    @Test
    void concreteClassMulti() {
        class Simple<T, Y, U, I> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple<T, Y, U, I> {\n"), out);
    }

    @Test
    void interfaceType() {
        interface Simple<T> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple<T> {\n"), out);
    }

    @Test
    void interfaceTypeMulti() {
        interface Simple<T, R, E> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple<T, R, E> {\n"), out);
    }

    @Test
    void abstractClass() {
        abstract class Simple<T> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple<T> {\n"), out);
    }

    @Test
    void abstractClassMulti() {
        abstract class Simple<T, R, E> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple<T, R, E> {\n"), out);
    }

    @Test
    void rawClass() {
        abstract class Simple<T> {
        }
        abstract class Simple2 {
            public Simple s;
            public abstract void x(Simple s);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple2.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("s: Simple<any> | null\n"), out);
        assertTrue(out.contains("x(s: Simple<any> | null): void\n"), out);
    }
}
