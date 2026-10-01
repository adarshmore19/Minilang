package minilang.compiler;

import minilang.lexer.*;
import minilang.parser.*;
import minilang.vm.*;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

/**
 * Compiler transforms AST into bytecode instructions with constant folding,
 * OOP support, loop control backpatching, and optimization.
 */
public class Compiler {

    private static class LoopContext {
        final int loopStart;
        final int continueTarget;
        final List<Integer> breakJumps = new ArrayList<>();
        final List<Integer> continueJumps = new ArrayList<>();

        LoopContext(int loopStart, int continueTarget) {
            this.loopStart = loopStart;
            this.continueTarget = continueTarget;
        }
    }

    private final Map<String, Integer> functionIds = new HashMap<>();
    private final Map<String, FunctionDeclaration> functionDefs = new HashMap<>();
    private final Map<Integer, FunctionInfo> compiledFunctions = new HashMap<>();
    private final Map<String, MLClass> compiledClasses = new HashMap<>();
    private final Deque<LoopContext> loopStack = new ArrayDeque<>();
    private final boolean optimize;
    private int syntheticVarCounter = 0;
    private final Set<String> importedFiles = new HashSet<>();

    private Bytecode currentBytecode;
    private Map<String, Integer> currentLocals;

    public Compiler() {
        this(false);
    }

    public Compiler(boolean optimize) {
        this.optimize = optimize;
    }

    public Program compile(List<Stmt> statements) {
        // Expand imports into a complete statement list
        List<Stmt> allStatements = new ArrayList<>(statements);
        for (int i = 0; i < allStatements.size(); i++) {
            Stmt stmt = allStatements.get(i);
            if (stmt instanceof ImportStmt imp) {
                String target = imp.path();
                if (target != null && (target.endsWith(".ml") || new File(target).exists() || new File(target + ".ml").exists())) {
                    String fullPath = target.endsWith(".ml") ? target : target + ".ml";
                    File f = new File(fullPath);
                    if (f.exists()) {
                        String canonical = f.getAbsolutePath();
                        if (!importedFiles.contains(canonical)) {
                            importedFiles.add(canonical);
                            try {
                                String src = Files.readString(f.toPath());
                                List<Stmt> importedStmts = new Parser(new Lexer(src)).parse();
                                allStatements.addAll(importedStmts);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }

        // Pass 1: Collect all top-level function and class declarations
        int funcIdCounter = 0;
        for (Stmt stmt : allStatements) {
            if (stmt instanceof FunctionDeclaration fn) {
                String name = fn.name().lexeme();
                functionIds.put(name, funcIdCounter);
                functionDefs.put(name, fn);
                funcIdCounter++;
            } else if (stmt instanceof ClassDeclaration cd) {
                String name = cd.name().lexeme();
                compiledClasses.put(name, new MLClass(name, new HashMap<>()));
            }
        }

        // Pass 2: Compile each top-level function
        for (Map.Entry<String, FunctionDeclaration> entry : functionDefs.entrySet()) {
            String name = entry.getKey();
            FunctionDeclaration fn = entry.getValue();
            int id = functionIds.get(name);

            Bytecode fnBytecode = new Bytecode();
            this.currentBytecode = fnBytecode;
            this.currentLocals = new LinkedHashMap<>();

            // Parameter slots (slot 0, slot 1, ...)
            for (Token param : fn.params()) {
                allocateSlot(param.lexeme());
            }

            // Compile body
            for (Stmt s : fn.body().statements()) {
                compileStmt(s);
            }

            // Ensure function returns
            ensureReturn(fnBytecode, NullValue.INSTANCE);
            compiledFunctions.put(id, new FunctionInfo(id, name, fn.params().size(), fnBytecode));
        }

        // Pass 3: Compile top-level code into main bytecode
        Bytecode mainBytecode = new Bytecode();
        this.currentBytecode = mainBytecode;
        this.currentLocals = new LinkedHashMap<>();

        for (Stmt stmt : allStatements) {
            if (!(stmt instanceof FunctionDeclaration)) {
                compileStmt(stmt);
            }
        }

        mainBytecode.emit(new Instruction(Opcode.HALT));
        return new Program(mainBytecode, compiledFunctions);
    }

    private int allocateSlot(String name) {
        if (currentLocals.containsKey(name)) {
            return currentLocals.get(name);
        }
        int nextSlot = 0;
        for (int s : currentLocals.values()) {
            if (s >= nextSlot) {
                nextSlot = s + 1;
            }
        }
        currentLocals.put(name, nextSlot);
        return nextSlot;
    }

    private void compileStmt(Stmt stmt) {
        if (stmt instanceof ClassDeclaration classDecl) {
            compileClass(classDecl);
        } else if (stmt instanceof VarDeclaration varDecl) {
            if (varDecl.initializer() != null) {
                compileExpr(varDecl.initializer());
            } else {
                int nullIdx = currentBytecode.addConstant(NullValue.INSTANCE);
                currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, nullIdx));
            }
            int slot = allocateSlot(varDecl.name().lexeme());
            currentBytecode.emit(new Instruction(Opcode.STORE_LOCAL, slot));
        } else if (stmt instanceof ExpressionStmt exprStmt) {
            Expr expr = exprStmt.expression();
            if (isPrintCall(expr)) {
                Call call = (Call) expr;
                if (!call.arguments().isEmpty()) {
                    compileExpr(call.arguments().get(0));
                } else {
                    int emptyIdx = currentBytecode.addConstant(new StringValue(""));
                    currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, emptyIdx));
                }
                currentBytecode.emit(new Instruction(Opcode.PRINT));
            } else {
                compileExpr(expr);
                currentBytecode.emit(new Instruction(Opcode.POP));
            }
        } else if (stmt instanceof Block block) {
            for (Stmt s : block.statements()) {
                compileStmt(s);
            }
        } else if (stmt instanceof If ifStmt) {
            compileExpr(ifStmt.condition());
            int jumpIfFalseIdx = currentBytecode.instructions().size();
            currentBytecode.emit(new Instruction(Opcode.JUMP_IF_FALSE, 0));

            compileStmt(ifStmt.thenBranch());

            if (ifStmt.elseBranch() != null) {
                int jumpIdx = currentBytecode.instructions().size();
                currentBytecode.emit(new Instruction(Opcode.JUMP, 0));

                int elseTarget = currentBytecode.instructions().size();
                currentBytecode.instructions().set(jumpIfFalseIdx, new Instruction(Opcode.JUMP_IF_FALSE, elseTarget));

                compileStmt(ifStmt.elseBranch());

                int afterElseTarget = currentBytecode.instructions().size();
                currentBytecode.instructions().set(jumpIdx, new Instruction(Opcode.JUMP, afterElseTarget));
            } else {
                int afterThenTarget = currentBytecode.instructions().size();
                currentBytecode.instructions().set(jumpIfFalseIdx, new Instruction(Opcode.JUMP_IF_FALSE, afterThenTarget));
            }
        } else if (stmt instanceof While whileStmt) {
            int loopStart = currentBytecode.instructions().size();
            LoopContext loopCtx = new LoopContext(loopStart, loopStart);
            loopStack.push(loopCtx);

            compileExpr(whileStmt.condition());

            int exitJumpIdx = currentBytecode.instructions().size();
            currentBytecode.emit(new Instruction(Opcode.JUMP_IF_FALSE, 0));

            compileStmt(whileStmt.body());
            currentBytecode.emit(new Instruction(Opcode.JUMP, loopStart));

            int loopEnd = currentBytecode.instructions().size();
            currentBytecode.instructions().set(exitJumpIdx, new Instruction(Opcode.JUMP_IF_FALSE, loopEnd));

            for (int breakIdx : loopCtx.breakJumps) {
                currentBytecode.instructions().set(breakIdx, new Instruction(Opcode.JUMP, loopEnd));
            }
            for (int contIdx : loopCtx.continueJumps) {
                currentBytecode.instructions().set(contIdx, new Instruction(Opcode.JUMP, loopStart));
            }
            loopStack.pop();
        } else if (stmt instanceof For forStmt) {
            if (forStmt.initializer() != null) {
                compileStmt(forStmt.initializer());
            }

            int loopStart = currentBytecode.instructions().size();
            LoopContext loopCtx = new LoopContext(loopStart, -1);
            loopStack.push(loopCtx);

            int exitJumpIdx = -1;
            if (forStmt.condition() != null) {
                compileExpr(forStmt.condition());
                exitJumpIdx = currentBytecode.instructions().size();
                currentBytecode.emit(new Instruction(Opcode.JUMP_IF_FALSE, 0));
            }

            compileStmt(forStmt.body());

            int incrementStart = currentBytecode.instructions().size();
            if (forStmt.increment() != null) {
                compileExpr(forStmt.increment());
                currentBytecode.emit(new Instruction(Opcode.POP));
            }
            currentBytecode.emit(new Instruction(Opcode.JUMP, loopStart));

            int loopEnd = currentBytecode.instructions().size();
            if (exitJumpIdx != -1) {
                currentBytecode.instructions().set(exitJumpIdx, new Instruction(Opcode.JUMP_IF_FALSE, loopEnd));
            }
            for (int breakIdx : loopCtx.breakJumps) {
                currentBytecode.instructions().set(breakIdx, new Instruction(Opcode.JUMP, loopEnd));
            }
            for (int contIdx : loopCtx.continueJumps) {
                currentBytecode.instructions().set(contIdx, new Instruction(Opcode.JUMP, incrementStart));
            }
            loopStack.pop();
        } else if (stmt instanceof Break brk) {
            if (loopStack.isEmpty()) {
                throw new RuntimeException("Cannot use 'break' outside of a loop at line " + brk.keyword().line());
            }
            int jumpIdx = currentBytecode.instructions().size();
            currentBytecode.emit(new Instruction(Opcode.JUMP, 0));
            loopStack.peek().breakJumps.add(jumpIdx);
        } else if (stmt instanceof Continue cont) {
            if (loopStack.isEmpty()) {
                throw new RuntimeException("Cannot use 'continue' outside of a loop at line " + cont.keyword().line());
            }
            int jumpIdx = currentBytecode.instructions().size();
            currentBytecode.emit(new Instruction(Opcode.JUMP, 0));
            loopStack.peek().continueJumps.add(jumpIdx);
        } else if (stmt instanceof Return ret) {
            if (ret.value() != null) {
                compileExpr(ret.value());
            } else {
                int nullIdx = currentBytecode.addConstant(NullValue.INSTANCE);
                currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, nullIdx));
            }
            currentBytecode.emit(new Instruction(Opcode.RETURN));
        } else if (stmt instanceof Repeat rep) {
            compileExpr(rep.count());
            int limitSlot = allocateSlot("__rep_limit_" + (syntheticVarCounter++));
            currentBytecode.emit(new Instruction(Opcode.STORE_LOCAL, limitSlot));

            int iSlot = allocateSlot("__rep_i_" + (syntheticVarCounter++));
            int zeroIdx = currentBytecode.addConstant(new IntValue(0));
            currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, zeroIdx));
            currentBytecode.emit(new Instruction(Opcode.STORE_LOCAL, iSlot));

            int loopStart = currentBytecode.instructions().size();
            currentBytecode.emit(new Instruction(Opcode.LOAD_LOCAL, iSlot));
            currentBytecode.emit(new Instruction(Opcode.LOAD_LOCAL, limitSlot));
            currentBytecode.emit(new Instruction(Opcode.LT));

            int exitJumpIdx = currentBytecode.instructions().size();
            currentBytecode.emit(new Instruction(Opcode.JUMP_IF_FALSE, 0));

            LoopContext loopCtx = new LoopContext(loopStart, loopStart);
            loopStack.push(loopCtx);

            compileStmt(rep.body());

            int incrementTarget = currentBytecode.instructions().size();
            currentBytecode.emit(new Instruction(Opcode.LOAD_LOCAL, iSlot));
            int oneIdx = currentBytecode.addConstant(new IntValue(1));
            currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, oneIdx));
            currentBytecode.emit(new Instruction(Opcode.ADD));
            currentBytecode.emit(new Instruction(Opcode.STORE_LOCAL, iSlot));
            currentBytecode.emit(new Instruction(Opcode.JUMP, loopStart));

            int afterLoopTarget = currentBytecode.instructions().size();
            currentBytecode.instructions().set(exitJumpIdx, new Instruction(Opcode.JUMP_IF_FALSE, afterLoopTarget));

            for (int breakJump : loopCtx.breakJumps) {
                currentBytecode.instructions().set(breakJump, new Instruction(Opcode.JUMP, afterLoopTarget));
            }
            for (int contJump : loopCtx.continueJumps) {
                currentBytecode.instructions().set(contJump, new Instruction(Opcode.JUMP, incrementTarget));
            }
            loopStack.pop();

        } else if (stmt instanceof ForEach forEach) {
            compileExpr(forEach.collection());
            int colSlot = allocateSlot("__col_" + (syntheticVarCounter++));
            currentBytecode.emit(new Instruction(Opcode.STORE_LOCAL, colSlot));

            // len(__col)
            currentBytecode.emit(new Instruction(Opcode.LOAD_LOCAL, colSlot));
            currentBytecode.emit(new Instruction(Opcode.CALL_BUILTIN, Builtins.LEN | (1 << 16)));
            int lenSlot = allocateSlot("__len_" + (syntheticVarCounter++));
            currentBytecode.emit(new Instruction(Opcode.STORE_LOCAL, lenSlot));

            int idxSlot = allocateSlot("__idx_" + (syntheticVarCounter++));
            int zeroIdx = currentBytecode.addConstant(new IntValue(0));
            currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, zeroIdx));
            currentBytecode.emit(new Instruction(Opcode.STORE_LOCAL, idxSlot));

            int loopStart = currentBytecode.instructions().size();
            currentBytecode.emit(new Instruction(Opcode.LOAD_LOCAL, idxSlot));
            currentBytecode.emit(new Instruction(Opcode.LOAD_LOCAL, lenSlot));
            currentBytecode.emit(new Instruction(Opcode.LT));

            int exitJumpIdx = currentBytecode.instructions().size();
            currentBytecode.emit(new Instruction(Opcode.JUMP_IF_FALSE, 0));

            LoopContext loopCtx = new LoopContext(loopStart, loopStart);
            loopStack.push(loopCtx);

            // var = __col[__idx]
            int varSlot = allocateSlot(forEach.variable().lexeme());
            currentBytecode.emit(new Instruction(Opcode.LOAD_LOCAL, colSlot));
            currentBytecode.emit(new Instruction(Opcode.LOAD_LOCAL, idxSlot));
            currentBytecode.emit(new Instruction(Opcode.INDEX_GET));
            currentBytecode.emit(new Instruction(Opcode.STORE_LOCAL, varSlot));

            compileStmt(forEach.body());

            int incrementTarget = currentBytecode.instructions().size();
            currentBytecode.emit(new Instruction(Opcode.LOAD_LOCAL, idxSlot));
            int oneIdx = currentBytecode.addConstant(new IntValue(1));
            currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, oneIdx));
            currentBytecode.emit(new Instruction(Opcode.ADD));
            currentBytecode.emit(new Instruction(Opcode.STORE_LOCAL, idxSlot));
            currentBytecode.emit(new Instruction(Opcode.JUMP, loopStart));

            int afterLoopTarget = currentBytecode.instructions().size();
            currentBytecode.instructions().set(exitJumpIdx, new Instruction(Opcode.JUMP_IF_FALSE, afterLoopTarget));

            for (int breakJump : loopCtx.breakJumps) {
                currentBytecode.instructions().set(breakJump, new Instruction(Opcode.JUMP, afterLoopTarget));
            }
            for (int contJump : loopCtx.continueJumps) {
                currentBytecode.instructions().set(contJump, new Instruction(Opcode.JUMP, incrementTarget));
            }
            loopStack.pop();

        } else if (stmt instanceof TryCatch tc) {
            int pushTryIp = currentBytecode.instructions().size();
            currentBytecode.emit(new Instruction(Opcode.PUSH_TRY, 0));

            compileStmt(tc.tryBlock());

            currentBytecode.emit(new Instruction(Opcode.POP_TRY));
            int jumpOverCatch = currentBytecode.instructions().size();
            currentBytecode.emit(new Instruction(Opcode.JUMP, 0));

            int catchTarget = currentBytecode.instructions().size();
            int errorSlot = -1;
            if (tc.errorVar() != null) {
                errorSlot = allocateSlot(tc.errorVar().lexeme());
            }
            int encodedSlot = errorSlot >= 0 ? (errorSlot + 1) : 0;
            int pushTryArg = (encodedSlot << 16) | (catchTarget & 0xFFFF);
            currentBytecode.instructions().set(pushTryIp, new Instruction(Opcode.PUSH_TRY, pushTryArg));

            compileStmt(tc.catchBlock());

            int endCatch = currentBytecode.instructions().size();
            currentBytecode.instructions().set(jumpOverCatch, new Instruction(Opcode.JUMP, endCatch));

        } else if (stmt instanceof ImportStmt) {
            // Handled during compilation pass expansion
        } else if (stmt instanceof FunctionDeclaration) {
            // Top-level functions handled in Pass 2
        } else {
            throw new RuntimeException("Unsupported statement type: " + stmt.getClass().getSimpleName());
        }
    }

    private void compileClass(ClassDeclaration classDecl) {
        String className = classDecl.name().lexeme();
        MLClass classValue = compiledClasses.computeIfAbsent(className, k -> new MLClass(className, new HashMap<>()));
        Map<String, FunctionInfo> methods = classValue.methods();

        Bytecode savedBytecode = this.currentBytecode;
        Map<String, Integer> savedLocals = this.currentLocals;

        for (FunctionDeclaration method : classDecl.methods()) {
            String methodName = method.name().lexeme();
            Bytecode methodBytecode = new Bytecode();
            this.currentBytecode = methodBytecode;
            this.currentLocals = new LinkedHashMap<>();

            // Slot 0 is this/self
            currentLocals.put("this", 0);
            currentLocals.put("self", 0);

            // Method parameter slots start at 1
            for (Token param : method.params()) {
                allocateSlot(param.lexeme());
            }

            for (Stmt s : method.body().statements()) {
                compileStmt(s);
            }

            ensureReturn(methodBytecode, NullValue.INSTANCE);

            methods.put(methodName, new FunctionInfo(0, className + "." + methodName, method.params().size() + 1, methodBytecode));
        }

        this.currentBytecode = savedBytecode;
        this.currentLocals = savedLocals;

        int classIdx = currentBytecode.addConstant(classValue);
        currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, classIdx));
        int slot = allocateSlot(className);
        currentBytecode.emit(new Instruction(Opcode.STORE_LOCAL, slot));
    }

    private void compileExpr(Expr expr) {
        // 1. Constant folding optimization (when enabled)
        if (optimize && expr instanceof Binary bin) {
            Expr folded = tryFoldBinary(bin);
            if (folded != null) {
                compileExpr(folded);
                return;
            }
        }

        if (expr instanceof Literal lit) {
            Object val = lit.value();
            Value value;
            if (val instanceof Integer i) {
                value = new IntValue(i);
            } else if (val instanceof Double d) {
                value = new FloatValue(d);
            } else if (val instanceof Float f) {
                value = new FloatValue(f.doubleValue());
            } else if (val instanceof Boolean b) {
                value = new BoolValue(b);
            } else if (val instanceof String s) {
                value = new StringValue(s);
            } else if (val == null) {
                value = NullValue.INSTANCE;
            } else {
                value = new StringValue(String.valueOf(val));
            }
            int idx = currentBytecode.addConstant(value);
            currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, idx));
        } else if (expr instanceof ArrayLiteral arrayLit) {
            for (Expr elem : arrayLit.elements()) {
                compileExpr(elem);
            }
            currentBytecode.emit(new Instruction(Opcode.BUILD_ARRAY, arrayLit.elements().size()));
        } else if (expr instanceof MapLiteral mapLit) {
            for (int i = 0; i < mapLit.keys().size(); i++) {
                compileExpr(mapLit.keys().get(i));
                compileExpr(mapLit.values().get(i));
            }
            currentBytecode.emit(new Instruction(Opcode.BUILD_MAP, mapLit.keys().size()));
        } else if (expr instanceof Variable var) {
            String name = var.name().lexeme();
            Integer slot = currentLocals.get(name);
            if (slot != null) {
                currentBytecode.emit(new Instruction(Opcode.LOAD_LOCAL, slot));
            } else if (compiledClasses.containsKey(name)) {
                int classIdx = currentBytecode.addConstant(compiledClasses.get(name));
                currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, classIdx));
            } else {
                throw new RuntimeException("Undefined variable '" + name + "' at line " + var.name().line());
            }
        } else if (expr instanceof ThisExpr thisExpr) {
            Integer slot = currentLocals.get(thisExpr.keyword().lexeme());
            if (slot == null) {
                throw new RuntimeException("Cannot use '" + thisExpr.keyword().lexeme() + "' outside of a class method at line " + thisExpr.keyword().line());
            }
            currentBytecode.emit(new Instruction(Opcode.LOAD_LOCAL, slot));
        } else if (expr instanceof Assignment assign) {
            compileExpr(assign.value());
            int slot = allocateSlot(assign.name().lexeme());
            currentBytecode.emit(new Instruction(Opcode.STORE_LOCAL, slot));
            currentBytecode.emit(new Instruction(Opcode.LOAD_LOCAL, slot));
        } else if (expr instanceof IndexGet indexGet) {
            compileExpr(indexGet.array());
            compileExpr(indexGet.index());
            currentBytecode.emit(new Instruction(Opcode.INDEX_GET));
        } else if (expr instanceof IndexSet indexSet) {
            compileExpr(indexSet.array());
            compileExpr(indexSet.index());
            compileExpr(indexSet.value());
            currentBytecode.emit(new Instruction(Opcode.INDEX_SET));
        } else if (expr instanceof GetExpr getExpr) {
            compileExpr(getExpr.object());
            int nameIdx = currentBytecode.addConstant(new StringValue(getExpr.name().lexeme()));
            currentBytecode.emit(new Instruction(Opcode.GET_PROPERTY, nameIdx));
        } else if (expr instanceof SetExpr setExpr) {
            compileExpr(setExpr.object());
            int nameIdx = currentBytecode.addConstant(new StringValue(setExpr.name().lexeme()));
            compileExpr(setExpr.value());
            currentBytecode.emit(new Instruction(Opcode.SET_PROPERTY, nameIdx));
        } else if (expr instanceof Binary bin) {
            compileExpr(bin.left());
            compileExpr(bin.right());
            Opcode op = switch (bin.operator().type()) {
                case PLUS -> Opcode.ADD;
                case MINUS -> Opcode.SUB;
                case STAR -> Opcode.MUL;
                case SLASH -> Opcode.DIV;
                case PERCENT -> Opcode.MOD;
                case EQ -> Opcode.EQ;
                case BANG_EQ -> Opcode.NE;
                case LT -> Opcode.LT;
                case LE -> Opcode.LE;
                case GT -> Opcode.GT;
                case GE -> Opcode.GE;
                case AND -> Opcode.AND;
                case OR -> Opcode.OR;
                default -> throw new RuntimeException("Unknown binary operator: " + bin.operator().lexeme());
            };
            currentBytecode.emit(new Instruction(op));
        } else if (expr instanceof Unary unary) {
            if (unary.operator().type() == TokenType.BANG) {
                compileExpr(unary.right());
                currentBytecode.emit(new Instruction(Opcode.NOT));
            } else if (unary.operator().type() == TokenType.MINUS) {
                int zeroIdx = currentBytecode.addConstant(new IntValue(0));
                currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, zeroIdx));
                compileExpr(unary.right());
                currentBytecode.emit(new Instruction(Opcode.SUB));
            } else {
                throw new RuntimeException("Unknown unary operator: " + unary.operator().lexeme());
            }
        } else if (expr instanceof Call call) {
            if (isPrintCall(call)) {
                if (!call.arguments().isEmpty()) {
                    compileExpr(call.arguments().get(0));
                } else {
                    int emptyIdx = currentBytecode.addConstant(new StringValue(""));
                    currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, emptyIdx));
                }
                currentBytecode.emit(new Instruction(Opcode.PRINT));
                int nullIdx = currentBytecode.addConstant(NullValue.INSTANCE);
                currentBytecode.emit(new Instruction(Opcode.PUSH_CONST, nullIdx));
                return;
            }

            if (call.callee() instanceof Variable calleeVar) {
                String name = calleeVar.name().lexeme();
                if (Builtins.isBuiltin(name)) {
                    for (Expr arg : call.arguments()) {
                        compileExpr(arg);
                    }
                    int combinedArg = Builtins.getId(name) | (call.arguments().size() << 16);
                    currentBytecode.emit(new Instruction(Opcode.CALL_BUILTIN, combinedArg));
                    return;
                }

                if (functionIds.containsKey(name)) {
                    for (Expr arg : call.arguments()) {
                        compileExpr(arg);
                    }
                    currentBytecode.emit(new Instruction(Opcode.CALL, functionIds.get(name)));
                    return;
                }

                // If not in top-level functions, might be a Class constructor or variable callable
                compileExpr(calleeVar);
                for (Expr arg : call.arguments()) {
                    compileExpr(arg);
                }
                currentBytecode.emit(new Instruction(Opcode.CALL_VALUE, call.arguments().size()));
                return;
            }

            // General callable (method invocation, e.g. obj.method(args), or higher-order function)
            compileExpr(call.callee());
            for (Expr arg : call.arguments()) {
                compileExpr(arg);
            }
            currentBytecode.emit(new Instruction(Opcode.CALL_VALUE, call.arguments().size()));
        } else {
            throw new RuntimeException("Unsupported expression type: " + expr.getClass().getSimpleName());
        }
    }

    private Expr tryFoldBinary(Binary bin) {
        Expr left = bin.left();
        Expr right = bin.right();

        if (left instanceof Binary bLeft) {
            Expr folded = tryFoldBinary(bLeft);
            if (folded != null) left = folded;
        }
        if (right instanceof Binary bRight) {
            Expr folded = tryFoldBinary(bRight);
            if (folded != null) right = folded;
        }

        if (left instanceof Literal l && right instanceof Literal r) {
            Object lv = l.value();
            Object rv = r.value();

            if (lv instanceof Number ln && rv instanceof Number rn) {
                double a = ln.doubleValue();
                double b = rn.doubleValue();
                boolean isInt = (lv instanceof Integer) && (rv instanceof Integer);

                switch (bin.operator().type()) {
                    case PLUS -> {
                        return isInt ? new Literal((int) (a + b)) : new Literal(a + b);
                    }
                    case MINUS -> {
                        return isInt ? new Literal((int) (a - b)) : new Literal(a - b);
                    }
                    case STAR -> {
                        return isInt ? new Literal((int) (a * b)) : new Literal(a * b);
                    }
                    case SLASH -> {
                        if (b != 0) {
                            return isInt ? new Literal((int) (a / b)) : new Literal(a / b);
                        }
                    }
                    case PERCENT -> {
                        if (b != 0) {
                            return isInt ? new Literal((int) (a % b)) : new Literal(a % b);
                        }
                    }
                    case LT -> { return new Literal(a < b); }
                    case LE -> { return new Literal(a <= b); }
                    case GT -> { return new Literal(a > b); }
                    case GE -> { return new Literal(a >= b); }
                    case EQ -> { return new Literal(a == b); }
                    case BANG_EQ -> { return new Literal(a != b); }
                    default -> {}
                }
            } else if (lv instanceof String || rv instanceof String) {
                if (bin.operator().type() == TokenType.PLUS) {
                    return new Literal(String.valueOf(lv) + String.valueOf(rv));
                }
            } else if (lv instanceof Boolean lb && rv instanceof Boolean rb) {
                switch (bin.operator().type()) {
                    case AND -> { return new Literal(lb && rb); }
                    case OR -> { return new Literal(lb || rb); }
                    case EQ -> { return new Literal(lb.equals(rb)); }
                    case BANG_EQ -> { return new Literal(!lb.equals(rb)); }
                    default -> {}
                }
            }
        }

        if (left != bin.left() || right != bin.right()) {
            return new Binary(left, bin.operator(), right);
        }

        return null;
    }

    private void ensureReturn(Bytecode bytecode, Value value) {
        List<Instruction> instructions = bytecode.instructions();
        if (instructions.isEmpty() || instructions.get(instructions.size() - 1).opcode() != Opcode.RETURN) {
            int idx = bytecode.addConstant(value);
            bytecode.emit(new Instruction(Opcode.PUSH_CONST, idx));
            bytecode.emit(new Instruction(Opcode.RETURN));
        }
    }

    private boolean isPrintCall(Expr expr) {
        if (expr instanceof Call call && call.callee() instanceof Variable var) {
            String name = var.name().lexeme();
            return name.equals("print") || name.equals("say") || name.equals("show") || var.name().type() == TokenType.PRINT;
        }
        return false;
    }
}
