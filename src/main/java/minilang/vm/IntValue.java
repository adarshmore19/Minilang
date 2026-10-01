package minilang.vm;

public record IntValue(int value) implements Value {
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
