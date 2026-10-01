package minilang.vm;

public record FloatValue(double value) implements Value {
    @Override
    public String toString() {
        if (value == (long) value) {
            return String.format("%d.0", (long) value);
        }
        return String.valueOf(value);
    }
}
