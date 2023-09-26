package dev.harrel.java2ts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericFieldsTest {

    @Test
    void simple() {
        class Simple<T> {
            public T t;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: T | null\n"), out);
    }

    @Test
    void simpleMulti() {
        class Simple<T, Y, U> {
            public T t;
            public Y y;
            public U u;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: T | null\n"), out);
        assertTrue(out.contains("y: Y | null\n"), out);
        assertTrue(out.contains("u: U | null\n"), out);
    }

    @Test
    void array() {
        class Simple<T> {
            public T[] t;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: T[] | null\n"), out);
    }

    @Test
    void arrayMulti() {
        class Simple<T, Y, U> {
            public T[] t;
            public Y[] y;
            public U[] u;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: T[] | null\n"), out);
        assertTrue(out.contains("y: Y[] | null\n"), out);
        assertTrue(out.contains("u: U[] | null\n"), out);
    }

    @Test
    void array2D() {
        class Simple<T> {
            public T[][] t;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: T[][] | null\n"), out);
    }

    @Test
    void array2DMulti() {
        class Simple<T, Y, U> {
            public T[][] t;
            public Y[][][] y;
            public U[][][][] u;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: T[][] | null\n"), out);
        assertTrue(out.contains("y: Y[][][] | null\n"), out);
        assertTrue(out.contains("u: U[][][][] | null\n"), out);
    }

    @Test
    void nested() {
        interface Gen<T> {
        }
        class Simple<T> {
            public Gen<T> t;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: Gen<T> | null\n"), out);
    }

    @Test
    void nestedMulti() {
        interface Gen<T> {
        }
        class Simple<T, Y, U> {
            public Gen<T> t;
            public Gen<Y> y;
            public Gen<U> u;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: Gen<T> | null\n"), out);
        assertTrue(out.contains("y: Gen<Y> | null\n"), out);
        assertTrue(out.contains("u: Gen<U> | null\n"), out);
    }

    @Test
    void nestedRecursive() {
        interface Gen<T> {
        }
        class Simple<T> {
            public Gen<Simple<Gen<Simple<T>>>> t;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: Gen<Simple<Gen<Simple<T>>>> | null\n"), out);
    }

    @Test
    void nestedRecursiveMulti() {
        interface Gen<T> {
        }
        class Simple<T, Y, U> {
            public Gen<Simple<Gen<U>, Gen<T>, Simple<U, Y, T>>> t;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: Gen<Simple<Gen<U>, Gen<T>, Simple<U, Y, T>>> | null\n"), out);
    }
}
