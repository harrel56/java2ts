package org.harrel.java2ts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericMethodsTest {

    @Test
    void simple() {
        interface Simple<T> {
            T t(T t);
            void y(T t);
            T u();
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t(t: T | null): T | null\n"), out);
        assertTrue(out.contains("y(t: T | null): void\n"), out);
        assertTrue(out.contains("u(): T | null\n"), out);
    }

    @Test
    void simpleMulti() {
        interface Simple<T, Y, U> {
            T t(Y t);
            Y y(U t);
            U u(T t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t(t: Y | null): T | null\n"), out);
        assertTrue(out.contains("y(t: U | null): Y | null\n"), out);
        assertTrue(out.contains("u(t: T | null): U | null\n"), out);
    }

    @Test
    void array() {
        interface Simple<T> {
            T[] t(T[] t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t(t: T[] | null): T[] | null\n"), out);
    }

    @Test
    void arrayMulti() {
        interface Simple<T, Y, U> {
            T[] t(Y[][][] t);
            Y[][] y(U[][] t);
            U[][][] u(T[] t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t(t: Y[][][] | null): T[] | null\n"), out);
        assertTrue(out.contains("y(t: U[][] | null): Y[][] | null\n"), out);
        assertTrue(out.contains("u(t: T[] | null): U[][][] | null\n"), out);
    }

    @Test
    void nested() {
        interface Simple<T> {
            Simple<T> t(Simple<T> t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t(t: Simple<T> | null): Simple<T> | null\n"), out);
    }

    @Test
    void nestedMulti() {
        interface Simple<T, Y, U> {
            Simple<T, Y, U> t(Simple<Y, T, U> t);
            Simple<Y, U, T> y(Simple<U, U, U> t);
            Simple<U, T, T> u(Simple<T, Y, Y> t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t(t: Simple<Y, T, U> | null): Simple<T, Y, U> | null\n"), out);
        assertTrue(out.contains("y(t: Simple<U, U, U> | null): Simple<Y, U, T> | null\n"), out);
        assertTrue(out.contains("u(t: Simple<T, Y, Y> | null): Simple<U, T, T> | null\n"), out);
    }

    @Test
    void nestedRecursive() {
        interface Simple<T> {
            Simple<Simple<Simple<Simple<T>>>> t(Simple<Simple<T>> t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t(t: Simple<Simple<T>> | null): Simple<Simple<Simple<Simple<T>>>> | null\n"), out);
    }

    @Test
    void nestedRecursiveMulti() {
        interface Simple<T, Y> {
            Simple<Y, Simple<Y, Simple<T, T>>> t(Simple<Simple<Y, Y>, Simple<T, T>> t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t(t: Simple<Simple<Y, Y>, Simple<T, T>> | null): Simple<Y, Simple<Y, Simple<T, T>>> | null\n"), out);
    }

    @Test
    void generic() {
        interface Simple {
            <T> T t(T t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t<T>(t: T | null): T | null\n"), out);
    }

    @Test
    void genericMulti() {
        interface Simple {
            <T, Y, U> T t(T t, Y y, U u);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t<T, Y, U>(t: T | null, y: Y | null, u: U | null): T | null\n"), out);
    }

    @Test
    void genericNested() {
        interface Simple<S> {
            <T> Simple<T> t(Simple<S> t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t<T>(t: Simple<S> | null): Simple<T> | null\n"), out);
    }
}
