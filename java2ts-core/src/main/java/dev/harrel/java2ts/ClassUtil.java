package dev.harrel.java2ts;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

class ClassUtil {

    public static List<Field> getPublicFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .toList();
    }

    public static List<Method> getPublicMethods(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .filter(m -> !isCovariantDuplicate(m))
                .toList();
    }

    public static Method getFunctionalMethod(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .filter(m -> !m.isDefault())
                .findFirst()
                .orElseThrow();
    }

    private static boolean isCovariantDuplicate(Method method) {
        try {
            Method declaredMethod = method.getDeclaringClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
            return method.getReturnType() != declaredMethod.getReturnType();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
