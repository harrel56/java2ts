package org.harrel.java2ts;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public enum SimpleType implements TsType {
    STRING("string"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    VOID("void"),
    ANY("any");

    record NullableSimpleType(SimpleType type) implements TsType {

        NullableSimpleType {
            Objects.requireNonNull(type);
        }

        @Override
        public String getTypeName() {
            return type.getTypeName();
        }
    }

    private final String name;

    SimpleType(String name) {
        this.name = name;
    }

    @Override
    public String getTypeName() {
        return name;
    }

    @Override
    public String getNullableTypeName() {
        return getTypeName();
    }

    private static final Map<Class<?>, TsType> mapping = new HashMap<>();

    static {
        mapping.put(char.class, STRING);
        mapping.put(Character.class, new NullableSimpleType(STRING));
        mapping.put(CharSequence.class, new NullableSimpleType(STRING));

        mapping.put(short.class, NUMBER);
        mapping.put(int.class, NUMBER);
        mapping.put(long.class, NUMBER);
        mapping.put(float.class, NUMBER);
        mapping.put(double.class, NUMBER);
        mapping.put(byte.class, NUMBER);
        mapping.put(Number.class, new NullableSimpleType(NUMBER));

        mapping.put(boolean.class, BOOLEAN);
        mapping.put(Boolean.class, new NullableSimpleType(BOOLEAN));

        mapping.put(void.class, VOID);
        mapping.put(Void.class, VOID);
    }

    public static Optional<TsType> fromClass(Class<?> clazz) {
        if (mapping.containsKey(clazz)) {
            return Optional.of(mapping.get(clazz));
        }

        return mapping.entrySet().stream()
                .filter(e -> e.getKey().isAssignableFrom(clazz))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
