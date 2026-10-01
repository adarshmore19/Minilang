package minilang.compiler;

/**
 * Opcode defines the custom bytecode instruction set for MiniLang.
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
    HALT,

    // Data structures & Stdlib
    BUILD_ARRAY,
    BUILD_MAP,
    INDEX_GET,
    INDEX_SET,
    CALL_BUILTIN,

    // OOP & Dynamic Dispatch
    CLASS,
    METHOD,
    GET_PROPERTY,
    SET_PROPERTY,
    CALL_VALUE,

    // Exception handling
    PUSH_TRY,
    POP_TRY
}