# CASerializable
This is an interface used when you want to reflectively load options from the config.
Normal usage is fairly straight forward, but there are some things to know before usage.

## Serialization/Deserialization methods
The `onDeserialize()` and `onSerialize()` methods are called appropriately after serialization/deserialization.

## Options
The options annotations should be placed on the class that directly implements the [CASerializable](CASerializable.java) interface.
### flatten
See documentation for `flatten()` in [CASerializable](CASerializable.java).

### canDelegate
See documentation for `canDelegate()` in [CASerializable](CASerializable.java).

### typeResolverEnum
To deal with polymorphism and usability for end users, this feature was added.
Instead of using class names, an intermediary `TypeResolver` enum class should be used.
```java
public enum PolymorphicEnum implements CASerializable.TypeResolver {
    TYPE_1(Type1Serializable.class),
    TYPE_2(Type2Serializable.class),
    ;

    Class<?> type;
    PolymorphicEnum(Class<?> type) {
        this.type = type;
    }

    @Override
    public Class<?> getType() {
        return type;
    }
}
```

The type will be serialized alongside normal values like so:
```yaml
type: TYPE_1
data1: 20
data2: 10
```
