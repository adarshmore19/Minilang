package minilang.compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Bytecode is a container for the compiled instruction sequence.
 */
public class Bytecode {
    private final List<Instruction> instructions = new ArrayList<>();

    public void emit(Instruction instruction) {
        instructions.add(instruction);
    }

    public List<Instruction> instructions() {
        return instructions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < instructions.size(); i++) {
            sb.append(String.format("%04d  %s%n", i, instructions.get(i)));
        }
        return sb.toString();
    }
}