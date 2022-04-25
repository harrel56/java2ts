package org.harrel.java2ts;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TsGenerator {

    private final Set<Type> unsupportedTypes = Set.of(Object.class, Class.class);
    private final Map<Class<?>, ComplexType> typeCache = new LinkedHashMap<>();

    public String getAllDeclarations() {
        System.out.println(typeCache.size());
        return typeCache.values().stream()
                .map(ComplexType::getTypeDeclaration)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public void registerType(Class<?> clazz) {
        resolveClassType(clazz);
    }

    private List<TsType> resolveTypes(Type[] types) {
        return Arrays.stream(types)
                .map(this::resolveType)
                .toList();
    }

    private TsType resolveType(Type mainType) {
        return switch (mainType) {
            case WildcardType type ->
                    Stream.of(type.getLowerBounds(), type.getUpperBounds())
                            .flatMap(Arrays::stream)
                            .map(this::resolveType)
                            .findFirst()
                            .orElseThrow();
            case TypeVariable<?> type -> new GenericType(type.toString());
            case ParameterizedType type && unsupportedTypes.contains(type.getRawType()) -> SimpleType.ANY;
            case ParameterizedType type -> new ParamType(resolveType(type.getRawType()), resolveTypes(type.getActualTypeArguments()));
            case GenericArrayType type -> new ArrayType(resolveType(type.getGenericComponentType()));
            case Class<?> type -> resolveClassType(type);
            default -> throw new IllegalStateException();
        };
    }

    private TsType resolveClassType(Class<?> clazz) {
        return resolveUnsupportedType(clazz)
                .or(() -> SimpleType.fromClass(clazz))
                .or(() -> resolveArrayType(clazz))
                .orElseGet(() -> resolveComplexType(clazz));
    }

    private Optional<TsType> resolveUnsupportedType(Class<?> clazz) {
        return unsupportedTypes.contains(clazz) ? Optional.of(SimpleType.ANY) : Optional.empty();
    }

    private Optional<TsType> resolveArrayType(Class<?> clazz) {
        if (clazz.isArray()) {
            TsType elementType = resolveClassType(clazz.getComponentType());
            return Optional.of(new ArrayType(elementType));
        } else {
            return Optional.empty();
        }
    }

    private ComplexType resolveComplexType(Class<?> clazz) {
        if (typeCache.containsKey(clazz)) {
            return typeCache.get(clazz);
        }

        List<GenericType> tsGenericTypes = toGenericTypes(clazz.getTypeParameters());
        List<TsType> tsSuperTypes = new ArrayList<>();
        Map<String, TsType> tsFields = new LinkedHashMap<>();
        Map<String, FunctionType> tsFunctions = new LinkedHashMap<>();
        ComplexType complexType = new ComplexType(getClassName(clazz), tsGenericTypes, tsSuperTypes, tsFields, tsFunctions);
        typeCache.put(clazz, complexType);

        for (Type superType : getSuperTypes(clazz)) {
            tsSuperTypes.add(resolveType(superType));
        }

        for (Field field : ClassUtil.getPublicFields(clazz)) {
            tsFields.put(field.getName(), resolveType(field.getGenericType()));
        }

        for (Method method : ClassUtil.getPublicMethods(clazz)) {
            Map<String, TsType> tsArguments = new LinkedHashMap<>();
            for (Parameter param : method.getParameters()) {
                tsArguments.put(param.getName(), resolveType(param.getParameterizedType()));
            }
            TsType returnType = resolveType(method.getGenericReturnType());
            List<GenericType> tsMethodGenericTypes = toGenericTypes(method.getTypeParameters());
            tsFunctions.put(method.getName(), new FunctionType(returnType, tsMethodGenericTypes, tsArguments));
        }

        return complexType;
    }

    private String getClassName(Class<?> clazz) {
        if (clazz.getPackageName().startsWith("java.")) {
            return "J_" + clazz.getSimpleName();
        }
        return clazz.getSimpleName();
    }

    private List<Type> getSuperTypes(Class<?> clazz) {
        List<Type> res = new ArrayList<>();
        if (clazz.getSuperclass() != null && !unsupportedTypes.contains(clazz.getSuperclass())) {
            res.add(clazz.getGenericSuperclass());
        }
        for (Type type : clazz.getGenericInterfaces()) {
            if (!unsupportedTypes.contains(type)) {
                res.add(type);
            }
        }
        return res;
    }

    private List<GenericType> toGenericTypes(Type[] types) {
        return Arrays.stream(types)
                .map(Objects::toString)
                .map(GenericType::new)
                .toList();
    }
}
