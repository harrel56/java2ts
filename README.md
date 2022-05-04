[![pipeline status](https://gitlab.com/org.harrel/java2ts/badges/master/pipeline.svg)](https://gitlab.com/org.harrel/java2ts/-/commits/master)
# Java2ts

Lightweight library (with gradle plugin) which generates typescript types definitions from java classes. Supports:
* fields (only instance, public),
* methods (only instance, public),
* inheritance,
* generics.

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
    id 'org.harrel.java2ts' version '1.0'
}
// ...
generateTsDeclarations {
    // required. SourceSet from which output we want to load classes
    sourceSet = project.sourceSets.getByName('main')
    // required. Fully qualified names of types you want to register for generation
    types = ['org.testing.Child']

    // file to which declarations will be written
    // default value: ./build/generated/java2ts/types.d.ts
    output = project.layout.projectDirectory.file("custom.txt")
    
    // sorting of type members (alphabetically), otherwise order is unpredictable
    // default value: true
    sorting = false

    // predicate for Class<?> class, return true if type should be processed (unsupported types will use 'any' type)
    // default value: supports all except Object.class and Class.class
    supportedPredicate = { type -> type.getPackageName().contains("java.") }
    
    // function which will be used to map Class objects to typescript type names
    // please don't use stateful Closures to ensure its serializability
    // default value: prefixes classes from 'java.*' package with 'J'
    nameResolver = { type -> type.getSimpleName() + 'xxx' }
    
    // determines if declarations for transitive types should also be generated
    // default value: true
    recursive = false
}
```

