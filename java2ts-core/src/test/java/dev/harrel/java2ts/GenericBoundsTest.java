package dev.harrel.java2ts;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericBoundsTest {

    @Test
    void concreteClass() {
        class Simple<T extends String> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple<T extends string> {\n"), out);
    }

    @Test
    void concreteClassNested() {
        class Simple<T extends Simple<T>> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple<T extends Simple<T>> {\n"), out);
    }

    @Test
    void concreteClassNestedMultiple() {
        class Simple<T extends Simple<T, Y>, Y extends Simple<Y, T>> {
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple<T extends Simple<T, Y>, Y extends Simple<Y, T>> {\n"), out);
    }

    @Test
    void method() {
        interface Simple {
            <T extends Long> T t(T t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t<T extends number>(t: T | null): T | null\n"), out);
    }

    @Test
    void methodMultiple() {
        interface Simple {
            <T extends Long, Y extends String, U> Y t(U t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t<T extends number, Y extends string, U>(t: U | null): Y | null\n"), out);
    }

    @Test
    void methodNested() {
        interface Simple<X> {
            <T extends Simple<Simple<T>>> T t(T t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t<T extends Simple<Simple<T>>>(t: T | null): T | null\n"), out);
    }

    @Test
    void withWildcards() {
        interface Simple<X> {
            <T extends Simple<Simple<? super T>>> T t(T t);
            <T extends Simple<Simple<? extends T>>> T y(T t);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("t<T extends Simple<Simple<T>>>(t: T | null): T | null\n"), out);
        assertTrue(out.contains("y<T extends Simple<Simple<T>>>(t: T | null): T | null\n"), out);
    }

    @Test
    void multipleBounds() {
        interface Simple<T extends Serializable & IntConsumer> {
            <U extends Runnable & Callable<U> & DoubleSupplier> U m();
        }
        TsGenerator gen = new TsGenerator();
        gen.setNameResolver(Class::getSimpleName);
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("interface Simple<T extends Serializable & IntConsumer> {\n"), out);
        assertTrue(out.contains("m<U extends Runnable & Callable<U> & DoubleSupplier>(): U | null\n"), out);
    }
}
