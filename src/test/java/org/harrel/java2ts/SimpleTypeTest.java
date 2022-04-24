package org.harrel.java2ts;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SimpleTypeTest {

    @Test
    void numberFromClass() {
        assertEquals(SimpleType.NUMBER, SimpleType.fromClass(byte.class).orElseThrow());
        assertEquals(SimpleType.NUMBER, SimpleType.fromClass(float.class).orElseThrow());
        assertEquals(SimpleType.NUMBER, SimpleType.fromClass(int.class).orElseThrow());
        assertEquals(SimpleType.NUMBER, SimpleType.fromClass(long.class).orElseThrow());
        assertEquals(new SimpleType.NullableSimpleType(SimpleType.NUMBER), SimpleType.fromClass(BigDecimal.class).orElseThrow());
        assertEquals(new SimpleType.NullableSimpleType(SimpleType.NUMBER), SimpleType.fromClass(Number.class).orElseThrow());
    }

    @Test
    void stringFromClass() {
        assertEquals(SimpleType.STRING, SimpleType.fromClass(char.class).orElseThrow());
        assertEquals(new SimpleType.NullableSimpleType(SimpleType.STRING), SimpleType.fromClass(StringBuilder.class).orElseThrow());
        assertEquals(new SimpleType.NullableSimpleType(SimpleType.STRING), SimpleType.fromClass(String.class).orElseThrow());
    }

    @Test
    void booleanFromClass() {
        assertEquals(SimpleType.BOOLEAN, SimpleType.fromClass(boolean.class).orElseThrow());
        assertEquals(new SimpleType.NullableSimpleType(SimpleType.BOOLEAN), SimpleType.fromClass(Boolean.class).orElseThrow());
    }

    @Test
    void voidFromClass() {
        assertEquals(SimpleType.VOID, SimpleType.fromClass(void.class).orElseThrow());
        assertEquals(SimpleType.VOID, SimpleType.fromClass(Void.class).orElseThrow());
    }
}