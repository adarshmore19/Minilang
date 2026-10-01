package minilang.vm;

import java.util.*;
import java.util.stream.Collectors;

public record MapValue(Map<Value, Value> entries) implements Value {

    public MapValue() {
        this(new LinkedHashMap<>());
    }

    public Value get(Value key) {
        Value val = entries.get(key);
        return val != null ? val : NullValue.INSTANCE;
    }

    public void set(Value key, Value value) {
        entries.put(key, value);
    }

    public boolean has(Value key) {
        return entries.containsKey(key);
    }

    public Value remove(Value key) {
        Value val = entries.remove(key);
        return val != null ? val : NullValue.INSTANCE;
    }

    public ArrayValue keys() {
        return new ArrayValue(new ArrayList<>(entries.keySet()));
    }

    public ArrayValue values() {
        return new ArrayValue(new ArrayList<>(entries.values()));
    }

    public int size() {
        return entries.size();
    }

    @Override
    public String toString() {
        return "{" + entries.entrySet().stream()
                .map(e -> e.getKey().toString() + ": " + e.getValue().toString())
                .collect(Collectors.joining(", ")) + "}";
    }
}
