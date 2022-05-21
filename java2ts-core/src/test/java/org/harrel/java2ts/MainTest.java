package org.harrel.java2ts;

import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.LongStream;

class MainTest {
//    public Integer integerField;
//    public String[] someArray;
//    public List<String> li;
//    public List<String>[] li2;
//    public List<List<String>> li3;
//    public List<List<List<String>>> li4;
//    public List<List<List<List<List<List<String>>>>>> li5;
//    public List<?> li6;
    public String str;
    public Gen<String, String, String> gen;

    @Test
    void main() throws IOException {
        TsGenerator gen = new TsGenerator();
//        gen.setSupportedPredicate(c -> !c.getPackageName().contains("java.reflection"));
        gen.registerType(List.class);
        System.out.println(gen.getAllDeclarations());
    }

    @FunctionalInterface
    interface Run1 extends Runnable {}

    interface Ov {
        Ov m();
    }

    interface Ov2 extends Ov {
        Ov2 m();
        void f(Fun f);
    }

    interface Ov3 {
        void f(Fun f);
    }

    @FunctionalInterface
    interface Fun<T> {
        void hello(T x);
    }

interface Gen4<T extends Gen4<? super T>> {}

    interface Gen<T extends String, V, Y> {
        T genM(Gen2<T> t);
        T[] genA(Gen2<T> t);
        <R extends String> R genX(Gen<?, R, ?> t);
        String str(String s);
    }

    interface Gen2<T extends String> {
        Gen<T, ? extends Gen<T, ? extends Gen2<?>, T>, ? super Long> genT();
    }

    interface Gen3<T extends String> extends Gen2<T>, Serializable {
        T t();
    }

//    static class GClass implements Gen<Long> {
//
//        @Override
//        public Long genM(Long aLong) {
//            return null;
//        }
//    }

    static class SimpleClass extends ParentClass implements Inter1 {
        public SimpleClass simp;
        public MainTest main;
        public String m1() {return null;}
        public String m2(Number num) {return null;}
        public boolean m3(String[] arr, String str) {return false;}
        public void m4() {}

    }

    static class ParentClass implements Marker {
        public String parentField;
    }

    interface Marker {}
    interface Inter1 {
        default void hello() {}
    }
}
