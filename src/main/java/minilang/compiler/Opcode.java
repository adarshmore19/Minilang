package minilang.compiler;

/**
 * Opcode defines the custom bytecode instruction set for MiniLang.
 *
 * Why custom bytecode (not JVM): the spec explicitly requires a custom
 * stack-based VM using our own instruction encoding. This makes the
 * pipeline transparent and educational.
 *
 * Instructions are conceptually grouped:
 *   Stack: PUSH_CONST, POP
 *   Locals: LOAD_LOCAL, STORE_LOCAL
 *   Arithmetic: ADD, SUB, MUL, DIV, MOD
 *   Comparisons: EQ, NE, LT, LE, GT, GE
 *   Boolean: AND, OR, NOT
 *   Jumps: JUMP, JUMP_IF_FALSE
 *   Functions: CALL, RETURN, LOAD_FUNC
 *   I/O / Control: PRINT, HALT
 */
public enum Opcode {
    PUSH_CONST,
    LOAD_LOCAL,
    STORE_LOCAL,
    ADD,
    SUB,
    MUL,
    DIV,
    MOD,
    EQ,
    NE,
    LT,
    LE,
    GT,
    GE,
    AND,
    OR,
    NOT,
    JUMP,
    JUMP_IF_FALSE,
    CALL,
    RETURN,
    PRINT,
    POP,
    HALT
}