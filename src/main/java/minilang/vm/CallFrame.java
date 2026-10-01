package minilang.vm;

import minilang.compiler.Bytecode;
import java.util.ArrayList;
import java.util.List;

/**
 * CallFrame represents a single function invocation on the VM call stack.
 *
 * Why a call frame: when a function is called, the VM must save the caller's
 * state (instruction pointer and local variables) and start a fresh execution
 * context for the callee. When the callee returns, the caller's frame is
 * restored and the return value pushed onto the operand stack.
 */
public class CallFrame {
    public final String functionName;
    public final Bytecode bytecode;
    public int ip = 0; // instruction pointer
    public final List<Value> locals = new ArrayList<>();
    public MLInstance initInstance = null; // Set when invoking class constructor (init)

    public CallFrame(String functionName, Bytecode bytecode) {
        this.functionName = functionName;
        this.bytecode = bytecode;
    }
}