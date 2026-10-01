package minilang.compiler;

import minilang.vm.Builtins;
import minilang.vm.Value;
import java.util.ArrayList;
import java.util.List;

/**
 * Bytecode is a container for the compiled instruction sequence and constant pool.
 */
public class Bytecode {
    private final List<Instruction> instructions = new ArrayList<>();
    private final List<Value> constants = new ArrayList<>();

    public void emit(Instruction instruction) {
        instructions.add(instruction);
    }

    public List<Instruction> instructions() {
        return instructions;
    }

    public int addConstant(Value value) {
        for (int i = 0; i < constants.size(); i++) {
            if (constants.get(i).equals(value)) {
                return i;
            }
        }
        constants.add(value);
        return constants.size() - 1;
    }

    public Value getConstant(int index) {
        return constants.get(index);
    }

    public List<Value> constants() {
        return constants;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < instructions.size(); i++) {
            Instruction inst = instructions.get(i);
            if (inst.opcode() == Opcode.CALL_BUILTIN) {
                sb.append(String.format("%04d  %s (%s)%n", i, inst, Builtins.getName(inst.argument() & 0xFFFF)));
            } else if (inst.opcode() == Opcode.PUSH_CONST && inst.argument() >= 0 && inst.argument() < constants.size()) {
                sb.append(String.format("%04d  %s (%s)%n", i, inst, constants.get(inst.argument())));
            } else {
                sb.append(String.format("%04d  %s%n", i, inst));
            }
        }
        return sb.toString();
    }
}