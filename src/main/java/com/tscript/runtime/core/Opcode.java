package com.tscript.runtime.core;

import java.util.HashMap;
import java.util.Map;

public enum Opcode {

    PUSH_NULL(0),
    PUSH_INT(1),
    PUSH_BOOL(1),
    LOAD_CONST(2),
    PUSH_THIS(0),
    LOAD_NATIVE(2),
    LOAD_VIRTUAL(2),
    LOAD_BUILTIN(2),
    SET_OWNER(0),
    EXTEND(2),

    LOAD_TYPE(2),
    BUILD_TYPE(2),

    POP(0),
    NEW_LINE(4),

    LOAD_GLOBAL(1),
    STORE_GLOBAL(1),

    LOAD_LOCAL(1),
    STORE_LOCAL(1),

    LOAD_EXTERNAL(2),
    STORE_EXTERNAL(2),

    LOAD_INTERNAL(2),
    STORE_INTERNAL(2),

    LOAD_SUPER(2),
    STORE_SUPER(2),

    LOAD_STATIC(2),
    STORE_STATIC(2),

    CONTAINER_READ(0),
    CONTAINER_WRITE(0),

    LOAD_ABSTRACT(2),

    ADD(0), SUB(0), MUL(0), DIV(0), IDIV(0), MOD(0), POW(0),
    AND(0), OR(0), XOR(0), NOT(0),
    LT(0), GT(0), LEQ(0), GEQ(0), SLA(0), SRA(0), SRL(0),
    EQUALS(0), NOT_EQUALS(0),
    NEG(0), POS(0),
    GET_TYPE(0),

    MAKE_ARRAY(1), MAKE_DICT(1), MAKE_RANGE(0),

    GOTO(2),
    BRANCH_IF_TRUE(2), BRANCH_IF_FALSE(2),

    GET_ITR(0),
    BRANCH_ITR(2),
    ITR_NEXT(0),

    ENTER_TRY(2), LEAVE_TRY(0), THROW(0),

    CALL_INPLACE(1), CALL_MAPPED(1),
    TO_MAP_ARG(2), TO_INP_ARG(0),
    RETURN(0), CALL_SUPER(1),

    IMPORT(2),

    USE(2),
    USE_MEMBERS(0),
    LOAD_NAME(2),
    DUP(0),

    ;Opcode(int argc){
        this.b = (byte) this.ordinal();
        this.argc = argc;
    }
    public final byte b; // the byte associated with this opcode
    public final int argc;

    private static final Map<Byte, Opcode> opcodeMap = new HashMap<>();

    static {
        for (Opcode opcode : values())
            opcodeMap.put(opcode.b, opcode);
    }

    public static Opcode of(byte b){
        return opcodeMap.get(b);
    }

}
