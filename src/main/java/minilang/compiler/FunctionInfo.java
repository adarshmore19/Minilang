package minilang.compiler;

import minilang.vm.Value;

/**
 * Metadata and bytecode for a compiled function.
 */
public record FunctionInfo(int id, String name, int paramCount, Bytecode bytecode) implements Value {
    @Override
    public String toString() {
        return "<function " + name + ">";
    }
}
