package minilang.vm;

public record StringValue(String value) implements Value {
    @Override
    public String toString() {
        return value;
    }
}
