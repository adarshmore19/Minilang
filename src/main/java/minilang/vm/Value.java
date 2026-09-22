package minilang.vm;

/**
 * Runtime value representation for MiniLang.
 *
 * Why a sealed-like approach with explicit types: the language only supports
 * int, bool, and string. By explicitly distinguishing types at runtime, we
 * get meaningful errors (e.g., "Invalid operand types: Int + Bool") instead
 * of silent coercion bugs.
 */
public interface Value {
    /** For error messages and disassembly output. */
    String toString();
}

public record IntValue(int value) implements Value {
    public String toString() { return String.valueOf(value); }
}

public record BoolValue(boolean value) implements Value {
    public String toString() { return value ? "true" : "false"; }
}

public record StringValue(String value) implements Value {
    public String toString() { return "\"" + value + "\""; }
}