package minilang.compiler;

/**
 * Instruction is a single bytecode instruction with an opcode and optional argument.
 */
public record Instruction(Opcode opcode, int argument) {
    public Instruction(Opcode opcode) {
        this(opcode, 0);
    }

    @Override
    public String toString() {
        if (argument != 0 || opcode == Opcode.PUSH_CONST || opcode == Opcode.LOAD_LOCAL
                || opcode == Opcode.STORE_LOCAL || opcode == Opcode.JUMP
                || opcode == Opcode.JUMP_IF_FALSE || opcode == Opcode.CALL
                || opcode == Opcode.BUILD_ARRAY || opcode == Opcode.CALL_BUILTIN
                || opcode == Opcode.CLASS || opcode == Opcode.METHOD
                || opcode == Opcode.GET_PROPERTY || opcode == Opcode.SET_PROPERTY
                || opcode == Opcode.CALL_VALUE) {
            return opcode + " " + argument;
        }
        return opcode.toString();
    }
}