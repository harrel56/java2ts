package org.harrel.java2ts;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TsGenerator {

    private final Set<Class<?>> unsupportedTypes = Set.of(Object.class, Class.class);
    private final Map<Class<?>, ComplexType> completeTypes = new LinkedHashMap<>();
    private final Map<Class<?>, ComplexType> incompleteTypes = new HashMap<>();

    public String getAllDeclarations() {
        System.out.println(completeTypes.size());
        return completeTypes.values().stream()
                .map(ComplexType::getTypeDeclaration)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public void registerType(Class<?> clazz) {
        getTsType(clazz);
    }

    private TsType getTsType(Class<?> clazz) {
        return getTsType(clazz, null);
    }

    private TsType getTsType(Class<?> clazz, Type genericType) {
        if (completeTypes.containsKey(clazz)) {
            return completeTypes.get(clazz);
        }

        return computeTsType(clazz);
    }


    private TsType computeTsType(Class<?> clazz) {
        return computeUnsupportedType(clazz)
                .or(() -> SimpleType.fromClass(clazz))
                .or(() -> computeArrayType(clazz))
                .orElseGet(() -> computeComplexType(clazz));
    }

    private Optional<TsType> computeUnsupportedType(Class<?> clazz) {
        return unsupportedTypes.contains(clazz) ? Optional.of(SimpleType.ANY) : Optional.empty();
    }

    private Optional<TsType> computeArrayType(Class<?> clazz) {
        if (clazz.isArray()) {
            TsType elementType = getTsType(clazz.getComponentType());
            return Optional.of(new ArrayType(elementType));
        } else {
            return Optional.empty();
        }
    }

    private ComplexType computeComplexType(Class<?> clazz) {
        if (incompleteTypes.containsKey(clazz)) {
            return incompleteTypes.get(clazz);
        }

        List<GenericType> tsGenericTypes = getGenericTypes(clazz.getTypeParameters());
        List<ComplexType> tsSuperTypes = new ArrayList<>();
        Map<String, TsType> tsFields = new LinkedHashMap<>();
        Map<String, FunctionType> tsFunctions = new LinkedHashMap<>();
        ComplexType complexType = new ComplexType(clazz.getSimpleName(), tsGenericTypes, tsSuperTypes, tsFields, tsFunctions);
        incompleteTypes.put(clazz, complexType);

        for (Class<?> superType : getSuperTypes(clazz)) {
            tsSuperTypes.add(computeComplexType(superType));
        }

        for (Field field : ClassUtil.getPublicFields(clazz)) {
            tsFields.put(field.getName(), getTsType(field.getType(), field.getGenericType()));
        }

        for (Method method : ClassUtil.getPublicMethods(clazz)) {
            Map<String, TsType> tsArguments = new LinkedHashMap<>();
            for (Parameter param : method.getParameters()) {
                tsArguments.put(param.getName(), getParameterType(param));
            }
            TsType returnType = getReturnType(method);
            tsFunctions.put(method.getName(), new FunctionType(returnType, tsArguments));
        }

        incompleteTypes.remove(clazz);
        completeTypes.put(clazz, complexType);
        return complexType;
    }

    private List<Class<?>> getSuperTypes(Class<?> clazz) {
        List<Class<?>> res = new ArrayList<>();
        if (clazz.getSuperclass() != null && !unsupportedTypes.contains(clazz.getSuperclass())) {
            res.add(clazz.getSuperclass());
        }
        for (Class<?> inter : clazz.getInterfaces()) {
            if (!unsupportedTypes.contains(inter)) {
                res.add(inter);
            }
        }
        return res;
    }

    private TsType getParameterType(Parameter param) {
        Type genericType = param.getParameterizedType();
        if (genericType instanceof TypeVariable<?>) {
            return new GenericType(genericType.toString());
        } else if (genericType instanceof ParameterizedType pType) {
            return new ParamType(getTsType((Class<?>) pType.getRawType()), getGenericTypes(pType.getActualTypeArguments()));
        } else {
            return getTsType(param.getType());
        }
    }

    private TsType getReturnType(Method method) {
        Type genericType = method.getGenericReturnType();
        if (genericType instanceof TypeVariable<?>) {
            return new GenericType(genericType.toString());
        } else if (genericType instanceof ParameterizedType pType) {
            return new ParamType(getTsType((Class<?>) pType.getRawType()), getGenericTypes(pType.getActualTypeArguments()));
        } else {
            return getTsType(method.getReturnType());
        }
    }

    private List<GenericType> getGenericTypes(Type[] types) {
        return Arrays.stream(types).map(t -> new GenericType(genericTypeToString(t))).toList();
    }

    private String genericTypeToString(Type type) {
        if(type instanceof WildcardType wType) {
            return Stream.of(wType.getLowerBounds(), wType.getUpperBounds())
                    .flatMap(Arrays::stream)
                    .map(this::genericTypeToString)
                    .filter(s -> !s.isEmpty())
                    .findFirst()
                    .orElseThrow();
        } else if (type instanceof TypeVariable<?>) {
            return type.toString();
        } else {
            return "";
        }
    }
}
