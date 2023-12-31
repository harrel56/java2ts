# Java2ts
[![build](https://github.com/harrel56/java2ts/actions/workflows/build.yml/badge.svg)](https://github.com/harrel56/java2ts/actions/workflows/build.yml)

Java library with gradle plugin, which generates typescript types definitions from java classes. It requires java classes to be loaded in runtime. It does **NOT** support generation from non-compiled java source files.

Supports:
* fields (only instance, public),
* methods (only instance, public),
* inheritance,
* generics,
* functional interfaces as lambda types.

### Use case

GraalVM interoperability feature can use *Java* objects in *JavaScript* environment ([Doc](https://www.graalvm.org/22.0/reference-manual/js/#interoperability-with-java)). Unfortunately, seems there is no way to run *TypeScript* code in GraalVM directly.

### Example

From:
```java
class Child extends Parent<Long> {
    public Long childField;
}
class Parent<T> {
    public T parentField;
    public T[] method(String... args) { return null; }
}
```
To:
```typescript
export declare interface Child extends Parent<number> {
    childField: number | null
}
export declare interface Parent<T> {
    parentField: T | null
    method(args: string[] | null): T[] | null
}
```
## java2ts-core
Usage:
```java
TsGenerator gen = new TsGenerator();
// sorting of type members (alphabetically), otherwise order is unpredictable
// default value: true
gen.setSortingEnabled(false);

// predicate for Class<?> class, return true if type should be processed (unsupported types will use 'any' type)
// default value: supports all except Object.class and Class.class
gen.setSupportedPredicate(t -> t.getPackageName().contains("java."));

// function which will be used to map Class objects to typescript type names
// default value: prefixes classes from 'java.*' package with 'J'
gen.setNameResolver(type -> "Prefix" + type.getSimpleName());

// register classes which you want generated
gen.registerType(YourCustomClass.class);

// get only registered types declarations
String out1 = gen.getRegisteredDeclarations();
// get all all types declarations which were processed transitively
String out2 = gen.getAllDeclarations();
```
## java2ts-plugin

Usage in _build.gradle_ file:
```groovy
plugins {
    id 'java'
    id 'dev.harrel.java2ts' version '1.0'
}
// ...
generateTsDeclarations {
    // SourceSet from which output we want to load classes
    // default value: 'main' source set provided by 'java' plugin
    sourceSet = project.sourceSets.getByName('main')
    
    // fully qualified names of types you want to register for generation
    // default value: empty collection which means all types from source set output
    includeTypes = ['org.testing.Child']

    // fully qualified names of types you want to exclude
    // default value: empty collection
    excludeTypes = ['org.testing.Internal']

    // file to which declarations will be written
    // default value: ./build/generated/java2ts/types.d.ts
    output = project.layout.projectDirectory.file("custom.txt")
    
    // sorting of type members (alphabetically), otherwise order is unpredictable
    // default value: true
    sorting = false

    // predicate for Class<?> class, return true if type should be processed (unsupported types will use 'any' type)
    // please don't use stateful Closures to ensure its serializability
    // default value: supports all except Object.class and Class.class
    supportedPredicate = { type -> type.getPackageName().contains("java.") }
    
    // function which will be used to map Class objects to typescript type names
    // please don't use stateful Closures to ensure its serializability
    // default value: prefixes classes from 'java.*' package with 'J'
    nameResolver = { type -> type.getSimpleName() + 'xxx' }
    
    // determines if declarations for transitive types should also be generated
    // default value: true
    transitive = false
}
```

