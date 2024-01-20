package tscriptc.generation;

import tscriptc.tree.Operation;

import java.util.HashMap;
import java.util.Map;

public enum Opcode {

    PUSH_NULL(0), PUSH_INT(1), PUSH_BOOL(1),
    LOAD_CONST(1), PUSH_THIS(0),

    POP(0),

    NEW_LINE(4),

    LOAD_GLOBAL(1), STORE_GLOBAL(1),
    LOAD_LOCAL(1), STORE_LOCAL(1),
    LOAD_MEMBER_FAST(1), STORE_MEMBER_FAST(1),
    LOAD_STATIC(1), STORE_STATIC(1),
    LOAD_MEMBER(1), STORE_MEMBER(1),
    CONTAINER_READ(0), CONTAINER_WRITE(0),
    LOAD_ABSTRACT_IMPL(1),

    ADD(0), SUB(0),
    MUL(0),
    DIV(0), IDIV(0),
    MOD(0),
    POW(0), AND(0),
    OR(0),
    XOR(0), NOT(0),
    LT(0),
    GT(0), LEQ(0),
    GEQ(0),
    SLA(0), SRA(0),
    SRL(0),
    EQUALS(0), NOT_EQUALS(0),
    NEG(0), POS(0),
    GET_TYPE(0),

    MAKE_ARRAY(1), MAKE_DICT(1),
    MAKE_RANGE(0),

    GOTO(2),
    BRANCH_IF_TRUE(2), BRANCH_IF_FALSE(2),

    GET_ITR(0), BRANCH_ITR(2), ITR_NEXT(0),

    ENTER_TRY(1), LEAVE_TRY(0),
    THROW(0),

    CALL(1), WRAP_ARGUMENT(1), RETURN(0),
    CALL_SUPER(1),

    BREAK_POINT(0),

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


    public static Opcode of(Operation o){
        return switch (o){
            case ADD -> ADD;
            case SUB -> SUB;
            case MUL -> MUL;
            case DIV -> DIV;
            case IDIV -> IDIV;
            case MOD -> MOD;
            case POW -> POW;
            case AND -> AND;
            case OR -> OR;
            case XOR -> XOR;
            case SHIFT_AL -> SLA;
            case SHIFT_AR -> SRA;
            case SHIFT_LR -> SRL;
            case LESS -> LT;
            case GREATER -> GT;
            case LESS_EQ -> LEQ;
            case GREATER_EQ -> GEQ;
            case EQUALS -> EQUALS;
            case NOT_EQUALS -> NOT_EQUALS;
        };
    }

    public static Opcode of(byte b){
        return opcodeMap.get(b);
    }
}
