package org.harrel.java2ts;

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
}
