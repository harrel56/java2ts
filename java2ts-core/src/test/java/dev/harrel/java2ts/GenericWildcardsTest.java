package dev.harrel.java2ts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericWildcardsTest {

    @Test
    void any() {
        class Simple<T> {
            public Simple<?> t;
            public Simple<?> m(Simple<?> t) {return null;}
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: Simple<any> | null\n"), out);
        assertTrue(out.contains("m(t: Simple<any> | null): Simple<any> | null\n"), out);
    }

    @Test
    void lowerBound() {
        class Simple<T> {
            public Simple<? super String> t;
            public Simple<? super Number> m(Simple<? super Object> t) {return null;}
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: Simple<string> | null\n"), out);
        assertTrue(out.contains("m(t: Simple<any> | null): Simple<number> | null\n"), out);
    }

    @Test
    void upperBound() {
        class Simple<T> {
            public Simple<? extends String> t;
            public Simple<? extends Number> m(Simple<? extends Object> t) {return null;}
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: Simple<string> | null\n"), out);
        assertTrue(out.contains("m(t: Simple<any> | null): Simple<number> | null\n"), out);
    }

    @Test
    void nested() {
        class Simple<T> {
            public Simple<Simple<?>> t;
            public Simple<Simple<?>> m(Simple<Simple<Simple<?>>> t) {return null;}
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t: Simple<Simple<any>> | null\n"), out);
        assertTrue(out.contains("m(t: Simple<Simple<Simple<any>>> | null): Simple<Simple<any>> | null\n"), out);
    }

    @Test
    void multiple() {
        class Simple<T, Y> {
            public Simple<?, ? extends Simple<?, ? super Simple<?, ?>>> x;
            public Simple<? extends String, ? super Long> t;
            public Simple<? super Number, Simple<?, ? extends String>> m(Simple<? super Object, ?> t) {return null;}
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("x: Simple<any, Simple<any, Simple<any, any>>> | null\n"), out);
        assertTrue(out.contains("t: Simple<string, number> | null\n"), out);
        assertTrue(out.contains("m(t: Simple<any, any> | null): Simple<number, Simple<any, string>> | null\n"), out);
    }
}
