package org.harrel.java2ts;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TsGeneratorTest {

    @Test
    void simpleFields() {
        class Simple {
            public int x;
            public long y;
            public char ch;
            public boolean bool;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("x: number\n"));
        assertTrue(out.contains("y: number\n"));
        assertTrue(out.contains("ch: string\n"));
        assertTrue(out.contains("bool: boolean\n"));
    }

    @Test
    void nullableSimpleFields() {
        class Simple {
            public Integer x;
            public BigDecimal y;
            public String str;
            public Boolean bool;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("x: number | null\n"));
        assertTrue(out.contains("y: number | null\n"));
        assertTrue(out.contains("str: string | null\n"));
        assertTrue(out.contains("bool: boolean | null\n"));
    }

    @Test
    void arrayFields() {
        class Simple {
            public int[] x;
            public BigDecimal[] y;
            public CharSequence[][] str;
            public boolean[][][] bool;
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("x: number[] | null\n"));
        assertTrue(out.contains("y: number[] | null\n"));
        assertTrue(out.contains("str: string[][] | null\n"));
        assertTrue(out.contains("bool: boolean[][][] | null\n"));
    }

    @Test
    void methods() {
        interface Simple {
            int[] m1();

            long m2(long a1);

            String m3(String a1, String a2);

            void m4(Object a1);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("m1(): number[] | null\n"));
        assertTrue(out.contains("m2(a1: number): number\n"));
        assertTrue(out.contains("m3(a1: string | null, a2: string | null): string | null\n"));
        assertTrue(out.contains("m4(a1: any): void\n"));
    }

    @Test
    void methodsOverloaded() {
        interface Simple {
            int m1();
            int m1(int i);
            int m1(String s);
            int m1(int x, long... l);
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Simple.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("m1(): number\n"), out);
        assertTrue(out.contains("m1(s: string | null): number\n"), out);
        assertTrue(out.contains("m1(i: number): number\n"), out);
        assertTrue(out.contains("m1(x: number, l: number[] | null): number\n"), out);
    }

    @Test
    void covariantOverride() {
        interface Parent {
            Number m();
        }
        interface Child extends Parent {
            Long m();
        }
        TsGenerator gen = new TsGenerator();
        gen.registerType(Child.class);
        String out = gen.getAllDeclarations();
        assertTrue(out.contains("""
                interface Child extends Parent {
                    m(): number | null
                }"""), out);
    }
}