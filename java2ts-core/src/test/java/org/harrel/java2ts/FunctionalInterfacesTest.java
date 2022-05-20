package org.harrel.java2ts;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FunctionalInterfacesTest {

    @Test
    void asField() {
        class Simple {
            public Runnable runnable;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("runnable: (() => void) | null\n"), out);
    }

    @Test
    void asArgument() {
        interface Simple {
            void fun(Callable<String> call);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("fun(call: (() => string) | null\n"), out);
    }

    @Test
    void predicate() {

    }

    @Test
    void function() {

    }

    @Test
    void consumer() {

    }

    @Test
    void supplier() {

    }

    @Test
    void biFunction() {

    }

    @Test
    void toDoubleBiFunction() {

    }

    @Test
    void longBinaryOperator() {

    }

    @Test
    void customFunctionalInterface() {

    }
}
