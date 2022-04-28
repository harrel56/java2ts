package org.harrel.java2ts;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TsGenerator {

    private final Set<Type> unsupportedTypes = Set.of(Object.class, Class.class);
    private final Map<Class<?>, ComplexType> typesCache = new LinkedHashMap<>();
    private final Map<TypeVariable<?>, GenericType> genericsCache = new HashMap<>();

    private Function<Class<?>, String> nameResolver = clazz -> {
        if (clazz.getPackageName().startsWith("java.")) {
            return "J" + clazz.getSimpleName();
        }
        return clazz.getSimpleName();
    };

    public String getAllDeclarations() {
        return typesCache.values().stream()
                .map(ComplexType::getTypeDeclaration)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public void registerType(Class<?> clazz) {
        resolveClassType(clazz);
    }

    public void setNameResolver(Function<Class<?>, String> resolver) {
        Objects.requireNonNull(resolver);
        this.nameResolver = resolver;
    }

    private List<TsType> resolveTypes(Type[] types) {
        return Arrays.stream(types)
                .map(this::resolveType)
                .toList();
    }

    private TsType resolveType(Type mainType) {
        return switch (mainType) {
            case ParameterizedType type && unsupportedTypes.contains(type.getRawType()) -> SimpleType.ANY;
            case ParameterizedType type ->
                    new ParamType(resolveType(type.getRawType()), resolveTypes(type.getActualTypeArguments()));
            case GenericArrayType type -> new ArrayType(resolveType(type.getGenericComponentType()));
            case WildcardType type -> resolveWildcardType(type);
            case TypeVariable<?> type -> resolveGenericType(type);
            case Class<?> type -> resolveClassType(type);
            default -> throw new IllegalStateException();
        };
    }

    private TsType resolveWildcardType(WildcardType type) {
        return Stream.of(type.getLowerBounds(), type.getUpperBounds())
                .flatMap(Arrays::stream)
                .map(this::resolveType)
                .findFirst()
                .orElseThrow();
    }

    private GenericType resolveGenericType(TypeVariable<?> type) {
        if (genericsCache.containsKey(type)) {
            return genericsCache.get(type);
        }
        List<TsType> tsBounds = new ArrayList<>(1);
        GenericType genericType = new GenericType(type.toString(), tsBounds);
        genericsCache.put(type, genericType);
        tsBounds.addAll(resolveTypes(getGenericBounds(type.getBounds())));
        return genericType;
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
        if (typesCache.containsKey(clazz)) {
            return typesCache.get(clazz);
        }

        List<TsType> tsGenericTypes = new ArrayList<>();
        List<TsType> tsSuperTypes = new ArrayList<>();
        Map<String, TsType> tsFields = new LinkedHashMap<>();
        Map<String, TsType> tsFunctions = new LinkedHashMap<>();
        ComplexType complexType = new ComplexType(nameResolver.apply(clazz), tsGenericTypes, tsSuperTypes, tsFields, tsFunctions);
        typesCache.put(clazz, complexType);

        tsGenericTypes.addAll(resolveTypes(clazz.getTypeParameters()));
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
            List<TsType> tsMethodGenericTypes = resolveTypes(method.getTypeParameters());
            tsFunctions.put(method.getName(), new FunctionType(returnType, tsMethodGenericTypes, tsArguments));
        }

        return complexType;
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

    private Type[] getGenericBounds(Type[] bounds) {
        return Arrays.stream(bounds)
                .filter(b -> b != Object.class)
                .toArray(Type[]::new);
    }
}
