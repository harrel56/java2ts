package org.harrel.java2ts;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TsGenerator {

    private final List<Class<?>> registeredTypes = new ArrayList<>();
    private final Map<Class<?>, ComplexType> typesCache = new LinkedHashMap<>();
    private final Map<TypeVariable<?>, GenericType> genericsCache = new HashMap<>();

    private boolean sortingEnabled = true;
    private Predicate<Class<?>> supportedPredicate = type -> !Set.of(Object.class, Class.class).contains(type);
    private Function<Class<?>, String> nameResolver = clazz -> {
        if (clazz.getPackageName().startsWith("java.")) {
            return "J" + clazz.getSimpleName();
        }
        return clazz.getSimpleName();
    };

    public String getRegisteredDeclarations() {
        return registeredTypes.stream()
                .map(typesCache::get)
                .map(ComplexType::getTypeDeclaration)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public String getAllDeclarations() {
        return typesCache.values().stream()
                .map(ComplexType::getTypeDeclaration)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public void registerType(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        registeredTypes.add(clazz);
        resolveClassType(clazz);
    }

    public void setSupportedPredicate(Predicate<Class<?>> predicate) {
        Objects.requireNonNull(predicate);
        this.supportedPredicate = predicate;
    }

    public void setSortingEnabled(boolean sortingEnabled) {
        this.sortingEnabled = sortingEnabled;
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
            case ParameterizedType type && resolveUnsupportedType(type.getRawType()).isPresent() -> SimpleType.ANY;
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
                .or(() -> resolveFunctionalInterfaceType(clazz))
                .orElseGet(() -> resolveComplexType(clazz));
    }

    private Optional<TsType> resolveUnsupportedType(Type type) {
        if (type instanceof Class<?> clazz && !supportedPredicate.test(clazz)) {
            return Optional.of(SimpleType.ANY);
        } else {
            return Optional.empty();
        }
    }

    private Optional<TsType> resolveArrayType(Class<?> clazz) {
        if (clazz.isArray()) {
            TsType elementType = resolveClassType(clazz.getComponentType());
            return Optional.of(new ArrayType(elementType));
        } else {
            return Optional.empty();
        }
    }

    private Optional<TsType> resolveFunctionalInterfaceType(Class<?> clazz) {
        if (clazz.isAnnotationPresent(FunctionalInterface.class)) {
            Method method = ClassUtil.getFunctionalMethod(clazz);
            return Optional.of(resolveFunctionType(method));
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
        List<NamedProperty> tsFields = new ArrayList<>();
        List<NamedProperty> tsFunctions = new ArrayList<>();
        ComplexType complexType = new ComplexType(nameResolver.apply(clazz), tsGenericTypes, tsSuperTypes, tsFields, tsFunctions);
        typesCache.put(clazz, complexType);

        tsGenericTypes.addAll(resolveTypes(clazz.getTypeParameters()));
        for (Type superType : getSuperTypes(clazz)) {
            tsSuperTypes.add(resolveType(superType));
        }

        for (Field field : ClassUtil.getPublicFields(clazz)) {
            tsFields.add(new NamedProperty(field.getName(), resolveType(field.getGenericType())));
        }

        for (Method method : ClassUtil.getPublicMethods(clazz)) {
            tsFunctions.add(new NamedProperty(method.getName(), resolveFunctionType(method)));
        }

        if (sortingEnabled) {
            Collections.sort(tsFields);
            Collections.sort(tsFunctions);
        }

        return complexType;
    }

    private FunctionType resolveFunctionType(Method method) {
        List<NamedProperty> tsArguments = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            tsArguments.add(new NamedProperty(param.getName(), resolveType(param.getParameterizedType())));
        }
        TsType returnType = resolveType(method.getGenericReturnType());
        List<TsType> tsMethodGenericTypes = resolveTypes(method.getTypeParameters());
        return new FunctionType(returnType, tsMethodGenericTypes, tsArguments);
    }

    private List<Type> getSuperTypes(Class<?> clazz) {
        return Stream.concat(Stream.ofNullable(clazz.getGenericSuperclass()), Arrays.stream(clazz.getGenericInterfaces()))
                .filter(t -> resolveUnsupportedType(t).isEmpty())
                .toList();
    }

    private Type[] getGenericBounds(Type[] bounds) {
        return Arrays.stream(bounds)
                .filter(b -> b != Object.class)
                .toArray(Type[]::new);
    }
}
