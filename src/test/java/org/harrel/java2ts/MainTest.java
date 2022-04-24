package org.harrel.java2ts;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

class MainTest {
//    public Integer integerField;
//    public String[] someArray;
    public List<String> li;
//    public List<String>[] li2;
//    public List<List<String>> li3;
//    public List<List<List<String>>> li4;
//    public List<List<List<List<List<List<String>>>>>> li5;
//    public List<?> li6;
    public Gen<String, String, String> gen;

    @Test
    void main() {
        TsGenerator gen = new TsGenerator();
        gen.registerType(List.class);
        System.out.println(gen.getAllDeclarations());
    }


    interface Gen<T, V, Y> {
        T genM(T t);
        String str();
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
