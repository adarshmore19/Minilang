package minilang.compiler;

/**
 * Instruction is a single bytecode instruction with an opcode and optional argument.
 *
 * For simplicity this project uses an instruction object rather than raw bytes,
 * which makes the disassembler and compiler much easier to read.
 */
public record Instruction(Opcode opcode, int argument) {
    public Instruction(Opcode opcode) {
        this(opcode, 0);
    }

    @Override
    public String toString() {
        if (argument != 0 || opcode == Opcode.PUSH_CONST || opcode == Opcode.LOAD_LOCAL
                || opcode == Opcode.STORE_LOCAL || opcode == Opcode.JUMP
                || opcode == Opcode.JUMP_IF_FALSE || opcode == Opcode.CALL) {
            return opcode + " " + argument;
        }
        return opcode.toString();
    }
}