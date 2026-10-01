package minilang.vm;

public record BoolValue(boolean value) implements Value {
    @Override
    public String toString() {
        return value ? "true" : "false";
    }
}
