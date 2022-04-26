package org.harrel.java2ts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericInheritanceTest {

    @Test
    void simple() {
        interface Gen<T> {}
        class CGen<T> {}
        class Simple<T> extends CGen<T> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple<T> extends CGen<T> {\n"), out);
    }

    @Test
    void simpleMultiple() {
        interface Gen<T> {}
        interface Gen2<T> {}
        class CGen<T> {}
        class Simple<T, Y, U> extends CGen<T> implements Gen<Y>, Gen2<U> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple<T, Y, U> extends CGen<T>, Gen<Y>, Gen2<U> {\n"), out);
    }

    @Test
    void concreteMultiple() {
        interface Gen<T> {}
        interface Gen2<T> {}
        class CGen<T> {}
        class Simple extends CGen<String> implements Gen<Long>, Gen2<Boolean> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple extends CGen<string>, Gen<number>, Gen2<boolean> {\n"), out);
    }

    @Test
    void leveled() {
        interface Gen<T> {}
        interface Gen2<T> extends Gen<String> {}
        interface Gen3<T, Y> extends Gen2<T> {}
        class CGen<T> implements Gen3<Long, T> {}
        class Simple extends CGen<String> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple extends CGen<string> {\n"), out);
        assertTrue(out.contains("interface CGen<T> extends Gen3<number, T> {\n"), out);
        assertTrue(out.contains("interface Gen3<T, Y> extends Gen2<T> {\n"), out);
        assertTrue(out.contains("interface Gen2<T> extends Gen<string> {\n"), out);
    }
}
