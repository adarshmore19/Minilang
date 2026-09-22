package minilang.vm;

/**
 * Runtime error for MiniLang.
 *
 * Errors include line numbers where possible and clear messages, avoiding
 * giant Java stack traces for normal language errors.
 */
public class RuntimeError extends RuntimeException {
    private final int line;

    public RuntimeError(String message, int line) {
        super(message);
        this.line = line;
    }

    public int line() { return line; }

    @Override
    public String toString() {
        return "Runtime Error at line " + line + ":\n" + getMessage();
    }
}