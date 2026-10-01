package minilang.vm;

/**
 * Runtime value representation for MiniLang.
 *
 * Why an explicit value hierarchy: the language supports int, bool, and string.
 * Distinguishing types at runtime provides meaningful type errors (e.g. "Cannot add Int and Bool")
 * instead of silent coercion bugs.
 */
public interface Value {
    /** For error messages, printing, and disassembly output. */
    String toString();
}