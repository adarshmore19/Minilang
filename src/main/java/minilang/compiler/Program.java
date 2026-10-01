package minilang.compiler;

import java.util.Map;

/**
 * Program encapsulates the compiled top-level bytecode and all compiled functions.
 */
public record Program(Bytecode mainBytecode, Map<Integer, FunctionInfo> functions) {
    public String disassemble() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== <main> ===\n");
        sb.append(mainBytecode.toString());
        for (FunctionInfo fn : functions.values()) {
            sb.append("\n=== fn ").append(fn.name()).append(" (id: ").append(fn.id())
              .append(", params: ").append(fn.paramCount()).append(") ===\n");
            sb.append(fn.bytecode().toString());
        }
        return sb.toString();
    }
}
