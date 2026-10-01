package minilang.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record ArrayValue(List<Value> elements) implements Value {

    public ArrayValue() {
        this(new ArrayList<>());
    }

    public Value get(int index) {
        if (index < 0 || index >= elements.size()) {
            throw new RuntimeError("Array index out of bounds: " + index + " (length: " + elements.size() + ")", 0);
        }
        return elements.get(index);
    }

    public void set(int index, Value value) {
        if (index < 0 || index >= elements.size()) {
            throw new RuntimeError("Array index out of bounds: " + index + " (length: " + elements.size() + ")", 0);
        }
        elements.set(index, value);
    }

    public void push(Value value) {
        elements.add(value);
    }

    public Value pop() {
        if (elements.isEmpty()) {
            throw new RuntimeError("pop() called on empty array", 0);
        }
        return elements.remove(elements.size() - 1);
    }

    public int size() {
        return elements.size();
    }

    @Override
    public String toString() {
        return "[" + elements.stream().map(Value::toString).collect(Collectors.joining(", ")) + "]";
    }
}
