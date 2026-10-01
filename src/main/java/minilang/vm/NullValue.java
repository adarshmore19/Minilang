package minilang.vm;

public record NullValue() implements Value {
    public static final NullValue INSTANCE = new NullValue();

    @Override
    public String toString() {
        return "null";
    }
}
