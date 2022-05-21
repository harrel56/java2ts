package org.harrel.java2ts;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.function.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FunctionalInterfacesTest {

    @Test
    void runnable() {
        class Simple {
            public Runnable runnable;
        }
        TsGenerator gen = new TsGenerator();
        gen.setNameResolver(Class::getSimpleName);
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("type Runnable = () => void"), out);
    }

    @Test
    void callable() {
        interface Simple {
            void fun(Callable<String> call);
        }
        TsGenerator gen = new TsGenerator();
        gen.setNameResolver(Class::getSimpleName);
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("type Callable<V> = () => V | null"), out);
    }

    @Test
    void predicate() {
        interface Simple<T> {
            void fun(Predicate<T> call);
        }
        TsGenerator gen = new TsGenerator();
        gen.setNameResolver(Class::getSimpleName);
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("type Predicate<T> = (arg0: T | null) => boolean"), out);
    }

    @Test
    void function() {
        interface Simple<T> {
            void fun(Function<T, Predicate<String>> call);
        }
        TsGenerator gen = new TsGenerator();
        gen.setNameResolver(Class::getSimpleName);
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("type Predicate<T> = (arg0: T | null) => boolean"), out);
        assertTrue(out.contains("type Function<T, R> = (arg0: T | null) => R | null"), out);
    }

    @Test
    void consumer() {
        interface Simple<T> {
            void fun(Consumer<Consumer<T>> call);
        }
        TsGenerator gen = new TsGenerator();
        gen.setNameResolver(Class::getSimpleName);
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("type Consumer<T> = (arg0: T | null) => void"), out);
    }

    @Test
    void supplier() {
        interface Simple<T> {
            void fun(Supplier<T> call);
        }
        TsGenerator gen = new TsGenerator();
        gen.setNameResolver(Class::getSimpleName);
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("type Supplier<T> = () => T | null"), out);
    }

    @Test
    void biFunction() {
        interface Simple<T> {
            void fun(BiFunction<String, String, T> call);
        }
        TsGenerator gen = new TsGenerator();
        gen.setNameResolver(Class::getSimpleName);
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("type BiFunction<T, U, R> = (arg0: T | null, arg1: U | null) => R | null"), out);
    }

    @Test
    void toDoubleBiFunction() {
        interface Simple<T> {
            void fun(ToDoubleBiFunction<String, T> call);
        }
        TsGenerator gen = new TsGenerator();
        gen.setNameResolver(Class::getSimpleName);
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("type ToDoubleBiFunction<T, U> = (arg0: T | null, arg1: U | null) => number"), out);
    }

    @Test
    void longBinaryOperator() {
        interface Simple {
            void fun(LongBinaryOperator call);
        }
        TsGenerator gen = new TsGenerator();
        gen.setNameResolver(Class::getSimpleName);
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("type LongBinaryOperator = (arg0: number, arg1: number) => number"), out);
    }

    @Test
    void customFunctionalInterface() {
        @FunctionalInterface
        interface Simple<T, R> {
            R fun(Simple<T, String> call);
        }
        TsGenerator gen = new TsGenerator();
        gen.setNameResolver(Class::getSimpleName);
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("type Simple<T, R> = (call: Simple<T, string> | null) => R | null"), out);
    }
}
