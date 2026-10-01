package minilang.vm;

import minilang.algo.AlgoLib;
import minilang.compiler.Bytecode;
import minilang.compiler.FunctionInfo;
import minilang.compiler.Instruction;
import minilang.compiler.Program;
import minilang.graphics.GraphicsWindow;
import minilang.plot.PlotEngine;
import minilang.turtle.TurtleEngine;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Stack-based Virtual Machine for MiniLang 2.0.
 * Includes security guards against stack overflow, memory leak prevention,
 * dynamic method/class dispatch, and integrated 2D/OpenGL graphics.
 */
public class VM {

    public static final int MAX_CALL_STACK_DEPTH = 1000;

    private static record ExceptionHandler(int targetIp, int callStackDepth, int stackSize, int errorSlot) {}

    private final Program program;
    private final Map<Integer, FunctionInfo> functions;
    private final Deque<Value> stack = new ArrayDeque<>();
    private final Deque<CallFrame> callStack = new ArrayDeque<>();
    private final Deque<ExceptionHandler> exceptionHandlers = new ArrayDeque<>();
    private final PrintStream out;
    private final Scanner scanner;

    public VM(Program program) {
        this(program, System.out, System.in);
    }

    public VM(Program program, PrintStream out) {
        this(program, out, System.in);
    }

    public VM(Program program, PrintStream out, InputStream in) {
        this.program = program;
        this.functions = program.functions();
        this.out = out;
        this.scanner = new Scanner(in);
    }

    public void run() {
        CallFrame mainFrame = new CallFrame("<main>", program.mainBytecode());
        callStack.push(mainFrame);

        while (!callStack.isEmpty()) {
            CallFrame frame = callStack.peek();
            if (frame.ip >= frame.bytecode.instructions().size()) {
                frame.locals.clear(); // Free memory
                callStack.pop();
                continue;
            }

            Instruction inst = frame.bytecode.instructions().get(frame.ip++);
            try {
                switch (inst.opcode()) {
                case PUSH_CONST -> {
                    Value constant = frame.bytecode.getConstant(inst.argument());
                    stack.push(constant);
                }

                case LOAD_LOCAL -> {
                    int slot = inst.argument();
                    if (slot < 0 || slot >= frame.locals.size() || frame.locals.get(slot) == null) {
                        throw new RuntimeError("Variable at local slot " + slot + " is uninitialized.", 0);
                    }
                    stack.push(frame.locals.get(slot));
                }

                case STORE_LOCAL -> {
                    int slot = inst.argument();
                    Value val = popStack("STORE_LOCAL");
                    while (frame.locals.size() <= slot) {
                        frame.locals.add(null);
                    }
                    frame.locals.set(slot, val);
                }

                case ADD -> {
                    Value b = popStack("ADD");
                    Value a = popStack("ADD");
                    if (a instanceof StringValue || b instanceof StringValue) {
                        stack.push(new StringValue(a.toString() + b.toString()));
                    } else if (a instanceof FloatValue fa && b instanceof FloatValue fb) {
                        stack.push(new FloatValue(fa.value() + fb.value()));
                    } else if (a instanceof FloatValue fa && b instanceof IntValue ib) {
                        stack.push(new FloatValue(fa.value() + ib.value()));
                    } else if (a instanceof IntValue ia && b instanceof FloatValue fb) {
                        stack.push(new FloatValue(ia.value() + fb.value()));
                    } else if (a instanceof IntValue ia && b instanceof IntValue ib) {
                        stack.push(new IntValue(ia.value() + ib.value()));
                    } else {
                        throw new RuntimeError("Operands for '+' must be numbers or strings: " + a + ", " + b, 0);
                    }
                }

                case SUB -> {
                    Value b = popStack("SUB");
                    Value a = popStack("SUB");
                    if (isNumber(a) && isNumber(b)) {
                        if (a instanceof FloatValue || b instanceof FloatValue) {
                            stack.push(new FloatValue(asDouble(a) - asDouble(b)));
                        } else {
                            stack.push(new IntValue(((IntValue) a).value() - ((IntValue) b).value()));
                        }
                    } else {
                        throw new RuntimeError("Operands for '-' must be numbers: " + a + ", " + b, 0);
                    }
                }

                case MUL -> {
                    Value b = popStack("MUL");
                    Value a = popStack("MUL");
                    if (isNumber(a) && isNumber(b)) {
                        if (a instanceof FloatValue || b instanceof FloatValue) {
                            stack.push(new FloatValue(asDouble(a) * asDouble(b)));
                        } else {
                            stack.push(new IntValue(((IntValue) a).value() * ((IntValue) b).value()));
                        }
                    } else if (a instanceof StringValue sv && isNumber(b)) {
                        int times = Math.max(0, (int) asDouble(b));
                        stack.push(new StringValue(sv.value().repeat(times)));
                    } else if (isNumber(a) && b instanceof StringValue sv) {
                        int times = Math.max(0, (int) asDouble(a));
                        stack.push(new StringValue(sv.value().repeat(times)));
                    } else {
                        throw new RuntimeError("Operands for '*' must be numbers or string repetition: " + a + ", " + b, 0);
                    }
                }

                case DIV -> {
                    Value b = popStack("DIV");
                    Value a = popStack("DIV");
                    if (isNumber(a) && isNumber(b)) {
                        double divisor = asDouble(b);
                        if (divisor == 0.0) {
                            throw new RuntimeError("Division by zero.", 0);
                        }
                        if (a instanceof FloatValue || b instanceof FloatValue) {
                            stack.push(new FloatValue(asDouble(a) / divisor));
                        } else {
                            stack.push(new IntValue(((IntValue) a).value() / ((IntValue) b).value()));
                        }
                    } else {
                        throw new RuntimeError("Operands for '/' must be numbers: " + a + ", " + b, 0);
                    }
                }

                case MOD -> {
                    Value b = popStack("MOD");
                    Value a = popStack("MOD");
                    if (isNumber(a) && isNumber(b)) {
                        double divisor = asDouble(b);
                        if (divisor == 0.0) {
                            throw new RuntimeError("Modulo by zero.", 0);
                        }
                        if (a instanceof FloatValue || b instanceof FloatValue) {
                            stack.push(new FloatValue(asDouble(a) % divisor));
                        } else {
                            stack.push(new IntValue(((IntValue) a).value() % ((IntValue) b).value()));
                        }
                    } else {
                        throw new RuntimeError("Operands for '%' must be numbers: " + a + ", " + b, 0);
                    }
                }

                case EQ -> {
                    Value b = popStack("EQ");
                    Value a = popStack("EQ");
                    stack.push(new BoolValue(valuesEqual(a, b)));
                }

                case NE -> {
                    Value b = popStack("NE");
                    Value a = popStack("NE");
                    stack.push(new BoolValue(!valuesEqual(a, b)));
                }

                case LT -> {
                    Value b = popStack("LT");
                    Value a = popStack("LT");
                    if (isNumber(a) && isNumber(b)) {
                        stack.push(new BoolValue(asDouble(a) < asDouble(b)));
                    } else {
                        throw new RuntimeError("Operands for '<' must be numbers.", 0);
                    }
                }

                case LE -> {
                    Value b = popStack("LE");
                    Value a = popStack("LE");
                    if (isNumber(a) && isNumber(b)) {
                        stack.push(new BoolValue(asDouble(a) <= asDouble(b)));
                    } else {
                        throw new RuntimeError("Operands for '<=' must be numbers.", 0);
                    }
                }

                case GT -> {
                    Value b = popStack("GT");
                    Value a = popStack("GT");
                    if (isNumber(a) && isNumber(b)) {
                        stack.push(new BoolValue(asDouble(a) > asDouble(b)));
                    } else {
                        throw new RuntimeError("Operands for '>' must be numbers.", 0);
                    }
                }

                case GE -> {
                    Value b = popStack("GE");
                    Value a = popStack("GE");
                    if (isNumber(a) && isNumber(b)) {
                        stack.push(new BoolValue(asDouble(a) >= asDouble(b)));
                    } else {
                        throw new RuntimeError("Operands for '>=' must be numbers.", 0);
                    }
                }

                case AND -> {
                    Value b = popStack("AND");
                    Value a = popStack("AND");
                    stack.push(new BoolValue(isTruthy(a) && isTruthy(b)));
                }

                case OR -> {
                    Value b = popStack("OR");
                    Value a = popStack("OR");
                    stack.push(new BoolValue(isTruthy(a) || isTruthy(b)));
                }

                case NOT -> {
                    Value a = popStack("NOT");
                    stack.push(new BoolValue(!isTruthy(a)));
                }

                case JUMP -> {
                    frame.ip = inst.argument();
                }

                case JUMP_IF_FALSE -> {
                    Value cond = popStack("JUMP_IF_FALSE");
                    if (!isTruthy(cond)) {
                        frame.ip = inst.argument();
                    }
                }

                case CALL -> {
                    checkCallStackDepth();
                    int funcId = inst.argument();
                    FunctionInfo fn = functions.get(funcId);
                    if (fn == null) {
                        throw new RuntimeError("Unknown function id: " + funcId, 0);
                    }
                    Value[] args = new Value[fn.paramCount()];
                    for (int i = fn.paramCount() - 1; i >= 0; i--) {
                        args[i] = popStack("CALL " + fn.name());
                    }
                    CallFrame newFrame = new CallFrame(fn.name(), fn.bytecode());
                    for (Value arg : args) {
                        newFrame.locals.add(arg);
                    }
                    callStack.push(newFrame);
                }

                case CALL_VALUE -> {
                    int argCount = inst.argument();
                    Value[] args = new Value[argCount];
                    for (int i = argCount - 1; i >= 0; i--) {
                        args[i] = popStack("CALL_VALUE argument");
                    }
                    Value callable = popStack("CALL_VALUE target");

                    if (callable instanceof MLClass klass) {
                        MLInstance instance = new MLInstance(klass);
                        FunctionInfo initMethod = klass.findMethod("init");
                        if (initMethod != null) {
                            checkCallStackDepth();
                            CallFrame initFrame = new CallFrame(klass.name() + ".init", initMethod.bytecode());
                            initFrame.initInstance = instance;
                            initFrame.locals.add(instance); // slot 0 = this/self
                            for (Value arg : args) {
                                initFrame.locals.add(arg);
                            }
                            callStack.push(initFrame);
                        } else {
                            stack.push(instance);
                        }
                    } else if (callable instanceof BoundMethod bm) {
                        checkCallStackDepth();
                        CallFrame methodFrame = new CallFrame(bm.method().name(), bm.method().bytecode());
                        methodFrame.locals.add(bm.instance()); // slot 0 = this/self
                        for (Value arg : args) {
                            methodFrame.locals.add(arg);
                        }
                        callStack.push(methodFrame);
                    } else if (callable instanceof FunctionInfo fn) {
                        checkCallStackDepth();
                        CallFrame fnFrame = new CallFrame(fn.name(), fn.bytecode());
                        for (Value arg : args) {
                            fnFrame.locals.add(arg);
                        }
                        callStack.push(fnFrame);
                    } else {
                        throw new RuntimeError("Cannot call non-callable value: " + callable, 0);
                    }
                }

                case GET_PROPERTY -> {
                    Value target = popStack("GET_PROPERTY");
                    String propName = frame.bytecode.getConstant(inst.argument()).toString();
                    if (target instanceof MLInstance instance) {
                        stack.push(instance.get(propName));
                    } else {
                        throw new RuntimeError("Only instances have properties, got: " + target, 0);
                    }
                }

                case SET_PROPERTY -> {
                    String propName = frame.bytecode.getConstant(inst.argument()).toString();
                    Value val = popStack("SET_PROPERTY value");
                    Value target = popStack("SET_PROPERTY target");
                    if (target instanceof MLInstance instance) {
                        instance.set(propName, val);
                        stack.push(val);
                    } else {
                        throw new RuntimeError("Only instances have fields, got: " + target, 0);
                    }
                }

                case RETURN -> {
                    Value retVal = stack.isEmpty() ? NullValue.INSTANCE : stack.pop();
                    if (frame.initInstance != null) {
                        retVal = frame.initInstance;
                    }
                    frame.locals.clear(); // Free memory
                    callStack.pop();
                    if (!callStack.isEmpty()) {
                        stack.push(retVal);
                    }
                }

                case PRINT -> {
                    Value val = popStack("PRINT");
                    out.println(val);
                }

                case POP -> {
                    if (!stack.isEmpty()) {
                        stack.pop();
                    }
                }

                case HALT -> {
                    for (CallFrame f : callStack) {
                        f.locals.clear();
                    }
                    callStack.clear();
                    stack.clear();
                }

                case BUILD_ARRAY -> {
                    int count = inst.argument();
                    List<Value> elements = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        elements.add(null);
                    }
                    for (int i = count - 1; i >= 0; i--) {
                        elements.set(i, popStack("BUILD_ARRAY"));
                    }
                    stack.push(new ArrayValue(elements));
                }

                case INDEX_GET -> {
                    Value indexVal = popStack("INDEX_GET");
                    Value targetVal = popStack("INDEX_GET");

                    if (targetVal instanceof ArrayValue arr) {
                        if (!(indexVal instanceof IntValue iv)) {
                            throw new RuntimeError("Array index must be an integer, got " + indexVal, 0);
                        }
                        stack.push(arr.get(iv.value()));
                    } else if (targetVal instanceof StringValue sv) {
                        if (!(indexVal instanceof IntValue iv)) {
                            throw new RuntimeError("String index must be an integer, got " + indexVal, 0);
                        }
                        int idx = iv.value();
                        String s = sv.value();
                        if (idx < 0 || idx >= s.length()) {
                            throw new RuntimeError("String index out of bounds: " + idx + " (length: " + s.length() + ")", 0);
                        }
                        stack.push(new StringValue(String.valueOf(s.charAt(idx))));
                    } else if (targetVal instanceof MapValue mv) {
                        stack.push(mv.get(indexVal));
                    } else {
                        throw new RuntimeError("Cannot index into non-indexable type: " + targetVal, 0);
                    }
                }

                case INDEX_SET -> {
                    Value value = popStack("INDEX_SET");
                    Value indexVal = popStack("INDEX_SET");
                    Value targetVal = popStack("INDEX_SET");

                    if (targetVal instanceof ArrayValue arr) {
                        if (!(indexVal instanceof IntValue iv)) {
                            throw new RuntimeError("Array index must be an integer, got " + indexVal, 0);
                        }
                        arr.set(iv.value(), value);
                        stack.push(value);
                    } else if (targetVal instanceof MapValue mv) {
                        mv.set(indexVal, value);
                        stack.push(value);
                    } else {
                        throw new RuntimeError("Cannot set index on non-indexable type: " + targetVal, 0);
                    }
                }

                case BUILD_MAP -> {
                    int pairCount = inst.argument();
                    Map<Value, Value> map = new LinkedHashMap<>();
                    Value[] keys = new Value[pairCount];
                    Value[] values = new Value[pairCount];
                    for (int i = pairCount - 1; i >= 0; i--) {
                        values[i] = popStack("BUILD_MAP value");
                        keys[i] = popStack("BUILD_MAP key");
                    }
                    for (int i = 0; i < pairCount; i++) {
                        map.put(keys[i], values[i]);
                    }
                    stack.push(new MapValue(map));
                }

                case PUSH_TRY -> {
                    int targetIp = inst.argument() & 0xFFFF;
                    int slot = (inst.argument() >>> 16) - 1;
                    exceptionHandlers.push(new ExceptionHandler(targetIp, callStack.size(), stack.size(), slot));
                }

                case POP_TRY -> {
                    if (!exceptionHandlers.isEmpty()) {
                        exceptionHandlers.pop();
                    }
                }

                case CALL_BUILTIN -> {
                    int builtinId = inst.argument() & 0xFFFF;
                    int argCount = inst.argument() >>> 16;
                    executeBuiltin(builtinId, argCount);
                }
            }
        } catch (RuntimeError e) {
            if (!exceptionHandlers.isEmpty()) {
                ExceptionHandler handler = exceptionHandlers.pop();
                while (callStack.size() > handler.callStackDepth()) {
                    CallFrame popped = callStack.pop();
                    popped.locals.clear();
                }
                while (stack.size() > handler.stackSize()) {
                    stack.pop();
                }
                CallFrame currentFrame = callStack.peek();
                if (handler.errorSlot() >= 0 && currentFrame != null) {
                    while (currentFrame.locals.size() <= handler.errorSlot()) {
                        currentFrame.locals.add(null);
                    }
                    currentFrame.locals.set(handler.errorSlot(), new StringValue(e.getMessage()));
                }
                if (currentFrame != null) {
                    currentFrame.ip = handler.targetIp();
                }
            } else {
                throw e;
            }
        }
    }
}

    private void checkCallStackDepth() {
        if (callStack.size() >= MAX_CALL_STACK_DEPTH) {
            throw new RuntimeError("Call stack overflow: maximum call stack depth (" + MAX_CALL_STACK_DEPTH + ") exceeded.", 0);
        }
    }

    private void executeBuiltin(int id, int argCount) {
        GraphicsWindow win = GraphicsWindow.getInstance();

        switch (id) {
            case Builtins.LEN -> {
                Value val = popStack("len");
                if (val instanceof ArrayValue arr) {
                    stack.push(new IntValue(arr.size()));
                } else if (val instanceof StringValue sv) {
                    stack.push(new IntValue(sv.value().length()));
                } else if (val instanceof MapValue mv) {
                    stack.push(new IntValue(mv.size()));
                } else {
                    throw new RuntimeError("len() requires an array, string, or map, got " + val, 0);
                }
            }

            case Builtins.PUSH -> {
                Value item = popStack("push");
                Value arrVal = popStack("push");
                if (arrVal instanceof ArrayValue arr) {
                    arr.push(item);
                    stack.push(arr);
                } else {
                    throw new RuntimeError("push() requires an array as first argument", 0);
                }
            }

            case Builtins.POP -> {
                Value arrVal = popStack("pop");
                if (arrVal instanceof ArrayValue arr) {
                    stack.push(arr.pop());
                } else {
                    throw new RuntimeError("pop() requires an array", 0);
                }
            }

            case Builtins.CLOCK -> {
                stack.push(new FloatValue(System.currentTimeMillis() / 1000.0));
            }

            case Builtins.INPUT -> {
                if (scanner.hasNextLine()) {
                    stack.push(new StringValue(scanner.nextLine()));
                } else {
                    stack.push(new StringValue(""));
                }
            }

            case Builtins.STR -> {
                Value val = popStack("str");
                stack.push(new StringValue(val.toString()));
            }

            case Builtins.INT -> {
                Value val = popStack("int");
                if (val instanceof IntValue) {
                    stack.push(val);
                } else if (val instanceof FloatValue fv) {
                    stack.push(new IntValue((int) fv.value()));
                } else if (val instanceof StringValue sv) {
                    try {
                        stack.push(new IntValue(Integer.parseInt(sv.value().trim())));
                    } catch (NumberFormatException e) {
                        throw new RuntimeError("Cannot parse integer from: " + sv.value(), 0);
                    }
                } else {
                    throw new RuntimeError("Cannot convert " + val + " to int", 0);
                }
            }

            case Builtins.FLOAT -> {
                Value val = popStack("float");
                if (val instanceof FloatValue) {
                    stack.push(val);
                } else if (val instanceof IntValue iv) {
                    stack.push(new FloatValue(iv.value()));
                } else if (val instanceof StringValue sv) {
                    try {
                        stack.push(new FloatValue(Double.parseDouble(sv.value().trim())));
                    } catch (NumberFormatException e) {
                        throw new RuntimeError("Cannot parse float from: " + sv.value(), 0);
                    }
                } else {
                    throw new RuntimeError("Cannot convert " + val + " to float", 0);
                }
            }

            case Builtins.TYPE -> {
                Value val = popStack("type");
                String typeName;
                if (val instanceof IntValue) typeName = "int";
                else if (val instanceof FloatValue) typeName = "float";
                else if (val instanceof BoolValue) typeName = "bool";
                else if (val instanceof StringValue) typeName = "string";
                else if (val instanceof ArrayValue) typeName = "array";
                else if (val instanceof NullValue) typeName = "null";
                else if (val instanceof MLClass) typeName = "class";
                else if (val instanceof MLInstance) typeName = "instance";
                else if (val instanceof BoundMethod) typeName = "method";
                else typeName = "unknown";
                stack.push(new StringValue(typeName));
            }

            case Builtins.SQRT -> {
                Value val = popStack("sqrt");
                stack.push(new FloatValue(Math.sqrt(asDouble(val))));
            }

            case Builtins.ABS -> {
                Value val = popStack("abs");
                if (val instanceof IntValue iv) {
                    stack.push(new IntValue(Math.abs(iv.value())));
                } else {
                    stack.push(new FloatValue(Math.abs(asDouble(val))));
                }
            }

            case Builtins.MIN -> {
                Value b = popStack("min");
                Value a = popStack("min");
                if (a instanceof IntValue ia && b instanceof IntValue ib) {
                    stack.push(new IntValue(Math.min(ia.value(), ib.value())));
                } else {
                    stack.push(new FloatValue(Math.min(asDouble(a), asDouble(b))));
                }
            }

            case Builtins.MAX -> {
                Value b = popStack("max");
                Value a = popStack("max");
                if (a instanceof IntValue ia && b instanceof IntValue ib) {
                    stack.push(new IntValue(Math.max(ia.value(), ib.value())));
                } else {
                    stack.push(new FloatValue(Math.max(asDouble(a), asDouble(b))));
                }
            }

            case Builtins.FLOOR -> {
                Value val = popStack("floor");
                stack.push(new IntValue((int) Math.floor(asDouble(val))));
            }

            case Builtins.CEIL -> {
                Value val = popStack("ceil");
                stack.push(new IntValue((int) Math.ceil(asDouble(val))));
            }

            case Builtins.ROUND -> {
                Value val = popStack("round");
                stack.push(new IntValue((int) Math.round(asDouble(val))));
            }

            case Builtins.POW -> {
                Value exp = popStack("pow");
                Value base = popStack("pow");
                stack.push(new FloatValue(Math.pow(asDouble(base), asDouble(exp))));
            }

            case Builtins.RANDOM -> {
                stack.push(new FloatValue(Math.random()));
            }

            case Builtins.SIN -> {
                Value val = popStack("sin");
                stack.push(new FloatValue(Math.sin(asDouble(val))));
            }

            case Builtins.COS -> {
                Value val = popStack("cos");
                stack.push(new FloatValue(Math.cos(asDouble(val))));
            }

            // --- Graphics & OpenGL Builtins ---
            case Builtins.GL_WINDOW -> {
                Value title = popStack("glWindow");
                Value h = popStack("glWindow");
                Value w = popStack("glWindow");
                win.openWindow(title.toString(), (int) asDouble(w), (int) asDouble(h));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_IS_OPEN -> {
                stack.push(new BoolValue(win.isOpen()));
            }

            case Builtins.GL_UPDATE -> {
                win.update();
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_CLEAR -> {
                Value b = popStack("glClear");
                Value g = popStack("glClear");
                Value r = popStack("glClear");
                win.clear(asDouble(r), asDouble(g), asDouble(b));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_COLOR -> {
                Value b = popStack("glColor");
                Value g = popStack("glColor");
                Value r = popStack("glColor");
                win.color(asDouble(r), asDouble(g), asDouble(b));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_RECT -> {
                Value h = popStack("glRect");
                Value w = popStack("glRect");
                Value y = popStack("glRect");
                Value x = popStack("glRect");
                win.rect(asDouble(x), asDouble(y), asDouble(w), asDouble(h));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_FILL_RECT -> {
                Value h = popStack("glFillRect");
                Value w = popStack("glFillRect");
                Value y = popStack("glFillRect");
                Value x = popStack("glFillRect");
                win.fillRect(asDouble(x), asDouble(y), asDouble(w), asDouble(h));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_CIRCLE -> {
                Value r = popStack("glCircle");
                Value y = popStack("glCircle");
                Value x = popStack("glCircle");
                win.circle(asDouble(x), asDouble(y), asDouble(r));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_FILL_CIRCLE -> {
                Value r = popStack("glFillCircle");
                Value y = popStack("glFillCircle");
                Value x = popStack("glFillCircle");
                win.fillCircle(asDouble(x), asDouble(y), asDouble(r));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_LINE -> {
                Value y2 = popStack("glLine");
                Value x2 = popStack("glLine");
                Value y1 = popStack("glLine");
                Value x1 = popStack("glLine");
                win.line(asDouble(x1), asDouble(y1), asDouble(x2), asDouble(y2));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_TEXT -> {
                Value size = popStack("glText");
                Value y = popStack("glText");
                Value x = popStack("glText");
                Value msg = popStack("glText");
                win.text(msg.toString(), asDouble(x), asDouble(y), (int) asDouble(size));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_BEGIN -> {
                Value mode = popStack("glBegin");
                win.glBegin(mode.toString());
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_VERTEX -> {
                Value y = popStack("glVertex");
                Value x = popStack("glVertex");
                win.glVertex(asDouble(x), asDouble(y));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_END -> {
                win.glEnd();
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.GL_KEY_DOWN -> {
                Value key = popStack("glKeyDown");
                stack.push(new BoolValue(win.isKeyDown(key.toString())));
            }

            case Builtins.GL_MOUSE_X -> {
                stack.push(new IntValue(win.getMouseX()));
            }

            case Builtins.GL_MOUSE_Y -> {
                stack.push(new IntValue(win.getMouseY()));
            }

            case Builtins.GL_MOUSE_DOWN -> {
                stack.push(new BoolValue(win.isMouseDown()));
            }

            case Builtins.GL_CLOSE -> {
                win.close();
                stack.push(NullValue.INSTANCE);
            }

            // Algorithms
            case Builtins.RANGE -> {
                Value[] args = popArgs(argCount, "range");
                if (args.length == 1) {
                    stack.push(AlgoLib.range((int) asDouble(args[0])));
                } else if (args.length == 2) {
                    stack.push(AlgoLib.range((int) asDouble(args[0]), (int) asDouble(args[1])));
                } else if (args.length == 3) {
                    stack.push(AlgoLib.range((int) asDouble(args[0]), (int) asDouble(args[1]), (int) asDouble(args[2])));
                } else {
                    throw new RuntimeError("range() takes 1 to 3 arguments, got " + args.length, 0);
                }
            }

            case Builtins.SUM -> {
                Value[] args = popArgs(argCount, "sum");
                if (args.length != 1 || !(args[0] instanceof ArrayValue arr)) {
                    throw new RuntimeError("sum() requires an array argument", 0);
                }
                stack.push(AlgoLib.sum(arr));
            }

            case Builtins.MEAN -> {
                Value[] args = popArgs(argCount, "mean");
                if (args.length != 1 || !(args[0] instanceof ArrayValue arr)) {
                    throw new RuntimeError("mean() requires an array argument", 0);
                }
                stack.push(AlgoLib.mean(arr));
            }

            case Builtins.MEDIAN -> {
                Value[] args = popArgs(argCount, "median");
                if (args.length != 1 || !(args[0] instanceof ArrayValue arr)) {
                    throw new RuntimeError("median() requires an array argument", 0);
                }
                stack.push(AlgoLib.median(arr));
            }

            case Builtins.SORT -> {
                Value[] args = popArgs(argCount, "sort");
                if (args.length == 0 || !(args[0] instanceof ArrayValue arr)) {
                    throw new RuntimeError("sort() requires an array as first argument", 0);
                }
                boolean ascending = args.length < 2 || isTruthy(args[1]);
                stack.push(AlgoLib.sort(arr, ascending));
            }

            case Builtins.REVERSE -> {
                Value[] args = popArgs(argCount, "reverse");
                if (args.length != 1) {
                    throw new RuntimeError("reverse() requires 1 argument", 0);
                }
                if (args[0] instanceof ArrayValue arr) {
                    stack.push(AlgoLib.reverse(arr));
                } else if (args[0] instanceof StringValue sv) {
                    stack.push(AlgoLib.reverse(sv));
                } else {
                    throw new RuntimeError("reverse() requires an array or string", 0);
                }
            }

            case Builtins.BINARY_SEARCH -> {
                Value[] args = popArgs(argCount, "binarySearch");
                if (args.length != 2 || !(args[0] instanceof ArrayValue arr)) {
                    throw new RuntimeError("binarySearch(array, target) requires an array and a target", 0);
                }
                stack.push(new IntValue(AlgoLib.binarySearch(arr, args[1])));
            }

            // Plotting
            case Builtins.PLOT_LINE -> {
                Value[] args = popArgs(argCount, "plotLine");
                PlotEngine pe = PlotEngine.getInstance();
                if (args.length == 1 && args[0] instanceof ArrayValue yArr) {
                    List<Double> yData = toDoubleList(yArr);
                    List<Double> xData = new ArrayList<>();
                    for (int i = 0; i < yData.size(); i++) xData.add((double) i);
                    pe.addLine(xData, yData, "Series", null);
                } else if (args.length >= 2 && args[0] instanceof ArrayValue xArr && args[1] instanceof ArrayValue yArr) {
                    List<Double> xData = toDoubleList(xArr);
                    List<Double> yData = toDoubleList(yArr);
                    String label = args.length >= 3 ? args[2].toString() : "Series";
                    pe.addLine(xData, yData, label, null);
                } else {
                    throw new RuntimeError("plotLine requires array of y values or (x, y) arrays", 0);
                }
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.PLOT_SCATTER -> {
                Value[] args = popArgs(argCount, "plotScatter");
                PlotEngine pe = PlotEngine.getInstance();
                if (args.length >= 2 && args[0] instanceof ArrayValue xArr && args[1] instanceof ArrayValue yArr) {
                    List<Double> xData = toDoubleList(xArr);
                    List<Double> yData = toDoubleList(yArr);
                    String label = args.length >= 3 ? args[2].toString() : "Scatter";
                    pe.addScatter(xData, yData, label, null);
                } else {
                    throw new RuntimeError("plotScatter requires (x, y) arrays", 0);
                }
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.PLOT_BAR -> {
                Value[] args = popArgs(argCount, "plotBar");
                PlotEngine pe = PlotEngine.getInstance();
                if (args.length >= 2 && args[0] instanceof ArrayValue catArr && args[1] instanceof ArrayValue valArr) {
                    List<String> categories = toStringList(catArr);
                    List<Double> values = toDoubleList(valArr);
                    String label = args.length >= 3 ? args[2].toString() : "Bar";
                    pe.addBar(categories, values, label, null);
                } else {
                    throw new RuntimeError("plotBar requires (categories, values) arrays", 0);
                }
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.PLOT_TITLE -> {
                Value[] args = popArgs(argCount, "plotTitle");
                if (args.length >= 1) {
                    PlotEngine.getInstance().setTitle(args[0].toString());
                }
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.PLOT_X_LABEL -> {
                Value[] args = popArgs(argCount, "plotXLabel");
                if (args.length >= 1) {
                    PlotEngine.getInstance().setXLabel(args[0].toString());
                }
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.PLOT_Y_LABEL -> {
                Value[] args = popArgs(argCount, "plotYLabel");
                if (args.length >= 1) {
                    PlotEngine.getInstance().setYLabel(args[0].toString());
                }
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.PLOT_GRID -> {
                Value[] args = popArgs(argCount, "plotGrid");
                boolean show = args.length == 0 || isTruthy(args[0]);
                PlotEngine.getInstance().setGrid(show);
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.PLOT_SHOW -> {
                Value[] args = popArgs(argCount, "plotShow");
                int w = args.length >= 1 ? (int) asDouble(args[0]) : 700;
                int h = args.length >= 2 ? (int) asDouble(args[1]) : 500;
                PlotEngine.getInstance().show(w, h);
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.PLOT_SAVE -> {
                Value[] args = popArgs(argCount, "plotSave");
                if (args.length < 1) {
                    throw new RuntimeError("plotSave(path) requires a filename", 0);
                }
                String path = args[0].toString();
                int w = args.length >= 2 ? (int) asDouble(args[1]) : 700;
                int h = args.length >= 3 ? (int) asDouble(args[2]) : 500;
                boolean ok = PlotEngine.getInstance().save(path, w, h);
                stack.push(new BoolValue(ok));
            }

            case Builtins.PLOT_CLEAR -> {
                popArgs(argCount, "plotClear");
                PlotEngine.getInstance().clear();
                stack.push(NullValue.INSTANCE);
            }

            // Turtle Graphics
            case Builtins.TURTLE_INIT -> {
                Value[] args = popArgs(argCount, "turtleInit");
                int w = args.length >= 1 ? (int) asDouble(args[0]) : 600;
                int h = args.length >= 2 ? (int) asDouble(args[1]) : 600;
                String title = args.length >= 3 ? args[2].toString() : "MiniLang Turtle";
                TurtleEngine.getInstance().initCanvas(w, h, title);
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_FORWARD -> {
                Value[] args = popArgs(argCount, "forward");
                if (args.length < 1) throw new RuntimeError("forward(dist) requires distance", 0);
                TurtleEngine.getInstance().forward(asDouble(args[0]));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_BACKWARD -> {
                Value[] args = popArgs(argCount, "backward");
                if (args.length < 1) throw new RuntimeError("backward(dist) requires distance", 0);
                TurtleEngine.getInstance().backward(asDouble(args[0]));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_RIGHT -> {
                Value[] args = popArgs(argCount, "turnRight");
                if (args.length < 1) throw new RuntimeError("turnRight(deg) requires degrees", 0);
                TurtleEngine.getInstance().turnRight(asDouble(args[0]));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_LEFT -> {
                Value[] args = popArgs(argCount, "turnLeft");
                if (args.length < 1) throw new RuntimeError("turnLeft(deg) requires degrees", 0);
                TurtleEngine.getInstance().turnLeft(asDouble(args[0]));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_PEN_UP -> {
                popArgs(argCount, "penUp");
                TurtleEngine.getInstance().penUp();
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_PEN_DOWN -> {
                popArgs(argCount, "penDown");
                TurtleEngine.getInstance().penDown();
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_PEN_COLOR -> {
                Value[] args = popArgs(argCount, "penColor");
                if (args.length >= 3) {
                    TurtleEngine.getInstance().setPenColor((int) asDouble(args[0]), (int) asDouble(args[1]), (int) asDouble(args[2]));
                }
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_PEN_SIZE -> {
                Value[] args = popArgs(argCount, "penSize");
                if (args.length >= 1) {
                    TurtleEngine.getInstance().setPenSize((int) asDouble(args[0]));
                }
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_CIRCLE -> {
                Value[] args = popArgs(argCount, "turtleCircle");
                if (args.length < 1) throw new RuntimeError("turtleCircle(radius) requires radius", 0);
                TurtleEngine.getInstance().circle(asDouble(args[0]));
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_CLEAR -> {
                popArgs(argCount, "turtleClear");
                TurtleEngine.getInstance().clear();
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_SPEED -> {
                Value[] args = popArgs(argCount, "turtleSpeed");
                if (args.length >= 1) {
                    TurtleEngine.getInstance().setSpeed((int) asDouble(args[0]));
                }
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_UPDATE -> {
                popArgs(argCount, "turtleUpdate");
                TurtleEngine.getInstance().update();
                stack.push(NullValue.INSTANCE);
            }

            case Builtins.TURTLE_SAVE -> {
                Value[] args = popArgs(argCount, "turtleSave");
                if (args.length < 1) throw new RuntimeError("turtleSave(path) requires path", 0);
                boolean ok = TurtleEngine.getInstance().save(args[0].toString());
                stack.push(new BoolValue(ok));
            }

            // Dictionaries / HashMaps
            case Builtins.KEYS -> {
                Value[] args = popArgs(argCount, "keys");
                if (args.length != 1 || !(args[0] instanceof MapValue mv)) {
                    throw new RuntimeError("keys(map) requires a map argument", 0);
                }
                stack.push(mv.keys());
            }

            case Builtins.VALUES -> {
                Value[] args = popArgs(argCount, "values");
                if (args.length != 1 || !(args[0] instanceof MapValue mv)) {
                    throw new RuntimeError("values(map) requires a map argument", 0);
                }
                stack.push(mv.values());
            }

            case Builtins.HAS -> {
                Value[] args = popArgs(argCount, "has");
                if (args.length != 2 || !(args[0] instanceof MapValue mv)) {
                    throw new RuntimeError("has(map, key) requires a map and key", 0);
                }
                stack.push(new BoolValue(mv.has(args[1])));
            }

            // File I/O
            case Builtins.READ_FILE -> {
                Value[] args = popArgs(argCount, "readFile");
                if (args.length < 1) throw new RuntimeError("readFile(path) requires a path", 0);
                try {
                    String content = Files.readString(Paths.get(args[0].toString()));
                    stack.push(new StringValue(content));
                } catch (Exception e) {
                    throw new RuntimeError("Failed to read file: " + e.getMessage(), 0);
                }
            }

            case Builtins.WRITE_FILE -> {
                Value[] args = popArgs(argCount, "writeFile");
                if (args.length < 2) throw new RuntimeError("writeFile(path, content) requires path and content", 0);
                try {
                    Files.writeString(Paths.get(args[0].toString()), args[1].toString());
                    stack.push(new BoolValue(true));
                } catch (Exception e) {
                    throw new RuntimeError("Failed to write file: " + e.getMessage(), 0);
                }
            }

            case Builtins.APPEND_FILE -> {
                Value[] args = popArgs(argCount, "appendFile");
                if (args.length < 2) throw new RuntimeError("appendFile(path, content) requires path and content", 0);
                try {
                    Files.writeString(Paths.get(args[0].toString()), args[1].toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    stack.push(new BoolValue(true));
                } catch (Exception e) {
                    throw new RuntimeError("Failed to append file: " + e.getMessage(), 0);
                }
            }

            case Builtins.FILE_EXISTS -> {
                Value[] args = popArgs(argCount, "fileExists");
                if (args.length < 1) throw new RuntimeError("fileExists(path) requires a path", 0);
                stack.push(new BoolValue(Files.exists(Paths.get(args[0].toString()))));
            }

            case Builtins.DELETE_FILE -> {
                Value[] args = popArgs(argCount, "deleteFile");
                if (args.length < 1) throw new RuntimeError("deleteFile(path) requires a path", 0);
                try {
                    boolean ok = Files.deleteIfExists(Paths.get(args[0].toString()));
                    stack.push(new BoolValue(ok));
                } catch (Exception e) {
                    stack.push(new BoolValue(false));
                }
            }

            default -> throw new RuntimeError("Unknown builtin id: " + id, 0);
        }
    }

    private Value[] popArgs(int count, String op) {
        Value[] args = new Value[count];
        for (int i = count - 1; i >= 0; i--) {
            args[i] = popStack(op);
        }
        return args;
    }

    private List<Double> toDoubleList(ArrayValue arr) {
        List<Double> list = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            list.add(asDouble(arr.get(i)));
        }
        return list;
    }

    private List<String> toStringList(ArrayValue arr) {
        List<String> list = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            list.add(arr.get(i).toString());
        }
        return list;
    }

    public boolean isTruthy(Value v) {
        if (v == null || v instanceof NullValue) return false;
        if (v instanceof BoolValue bv) return bv.value();
        if (v instanceof IntValue iv) return iv.value() != 0;
        if (v instanceof FloatValue fv) return fv.value() != 0.0;
        if (v instanceof StringValue sv) return !sv.value().isEmpty();
        if (v instanceof ArrayValue av) return av.size() > 0;
        if (v instanceof MapValue mv) return mv.size() > 0;
        return true;
    }

    private boolean isNumber(Value v) {
        return v instanceof IntValue || v instanceof FloatValue;
    }

    private double asDouble(Value v) {
        if (v instanceof IntValue iv) return iv.value();
        if (v instanceof FloatValue fv) return fv.value();
        throw new RuntimeError("Expected number, got: " + v, 0);
    }

    private boolean valuesEqual(Value a, Value b) {
        if (a instanceof IntValue ia && b instanceof IntValue ib) {
            return ia.value() == ib.value();
        }
        if (isNumber(a) && isNumber(b)) {
            return asDouble(a) == asDouble(b);
        }
        if (a instanceof BoolValue ba && b instanceof BoolValue bb) {
            return ba.value() == bb.value();
        }
        if (a instanceof StringValue sa && b instanceof StringValue sb) {
            return sa.value().equals(sb.value());
        }
        if (a instanceof NullValue && b instanceof NullValue) {
            return true;
        }
        if (a instanceof ArrayValue aa && b instanceof ArrayValue ab) {
            if (aa.size() != ab.size()) return false;
            for (int i = 0; i < aa.size(); i++) {
                if (!valuesEqual(aa.get(i), ab.get(i))) return false;
            }
            return true;
        }
        return a == b;
    }

    private Value popStack(String op) {
        if (stack.isEmpty()) {
            throw new RuntimeError("Stack underflow during " + op, 0);
        }
        return stack.pop();
    }

    public Value peekStack() {
        return stack.peek();
    }
}
