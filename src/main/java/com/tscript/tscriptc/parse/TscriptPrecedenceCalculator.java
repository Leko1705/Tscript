package com.tscript.tscriptc.parse;

import com.tscript.tscriptc.tree.ExpressionTree;
import com.tscript.tscriptc.tree.Operation;
import com.tscript.tscriptc.utils.TreeFactory;

import java.util.HashMap;

public final class TscriptPrecedenceCalculator {

    private static final HashMap<Object, Integer> precedences = new HashMap<>();

    static {
        precedences.put(TscriptTokenType.DOT, 100);
        precedences.put(TscriptTokenType.POW, 71);
        precedences.put(TscriptTokenType.MUL, 70);
        precedences.put(TscriptTokenType.IDIV, 70);
        precedences.put(TscriptTokenType.DIV, 70);
        precedences.put(TscriptTokenType.MOD, 70);
        precedences.put(TscriptTokenType.PLUS, 60);
        precedences.put(TscriptTokenType.MINUS, 60);
        precedences.put(TscriptTokenType.SHIFT_AL, 50);
        precedences.put(TscriptTokenType.SHIFT_AR, 50);
        precedences.put(TscriptTokenType.SHIFT_LR, 50);
        precedences.put(TscriptTokenType.GREATER, 40);
        precedences.put(TscriptTokenType.LESS, 40);
        precedences.put(TscriptTokenType.GREATER_EQ, 40);
        precedences.put(TscriptTokenType.LESS_EQ, 40);
        precedences.put(TscriptTokenType.TYPEOF, 40);
        precedences.put(TscriptTokenType.EQUALS, 30);
        precedences.put(TscriptTokenType.NOT_EQUALS, 30);
        precedences.put(TscriptTokenType.XOR, 20);
        precedences.put(TscriptTokenType.OR, 19);
        precedences.put(TscriptTokenType.AND, 18);
        precedences.put(TscriptTokenType.EQ_ASSIGN, 0);
        precedences.put(TscriptTokenType.ADD_ASSIGN, 0);
        precedences.put(TscriptTokenType.SUB_ASSIGN, 0);
        precedences.put(TscriptTokenType.MUL_ASSIGN, 0);
        precedences.put(TscriptTokenType.DIV_ASSIGN, 0);
        precedences.put(TscriptTokenType.IDIV_ASSIGN, 0);
        precedences.put(TscriptTokenType.MOD_ASSIGN, 0);
        precedences.put(TscriptTokenType.POW_ASSIGN, 0);
        precedences.put(TscriptTokenType.SHIFT_AL_ASSIGN, 0);
        precedences.put(TscriptTokenType.SHIFT_AR_ASSIGN, 0);
        precedences.put(TscriptTokenType.SHIFT_LR_ASSIGN, 0);
    }

    private TscriptPrecedenceCalculator(){
    }

    public static int calculate(Token<TscriptTokenType> token){
        if (!isBinaryOperator(token))
            throw new IllegalStateException(token.getTag() + " is not a binary expression");
        return precedences.get(token.getTag());
    }

    public static boolean isBinaryOperator(Token<TscriptTokenType> type){
        return precedences.containsKey(type.getTag());
    }

    public static ExpressionTree apply(TreeFactory factory, Token<TscriptTokenType> op, ExpressionTree lhs, ExpressionTree rhs) {
        TscriptTokenType tag = (TscriptTokenType) op.getTag();
        return switch (tag) {

            case EQ_ASSIGN -> factory.AssignTree(op.getLocation(), lhs, rhs);

            case ADD_ASSIGN, SUB_ASSIGN,
                    MUL_ASSIGN, DIV_ASSIGN,
                    IDIV_ASSIGN, MOD_ASSIGN,
                    POW_ASSIGN, SHIFT_LR_ASSIGN,
                    SHIFT_AL_ASSIGN, SHIFT_AR_ASSIGN
                    -> factory.AssignTree(
                            op.getLocation(),
                            lhs,
                            factory.BinaryOperationTree(op.getLocation(), lhs, rhs, Operation.withoutAssign(tag.name)));

            case PLUS, MINUS, MUL, DIV,
                    IDIV, MOD, POW, SHIFT_AL,
                    SHIFT_AR , SHIFT_LR, AND, OR,
                    XOR, TYPEOF, GREATER, LESS, GREATER_EQ, NOT_EQUALS,
                    EQUALS, LESS_EQ -> factory.BinaryOperationTree(op.getLocation(), lhs, rhs, Operation.of(tag.name));

            default -> throw new IllegalStateException(tag + " is not a binary expression");
        };
    }

}
