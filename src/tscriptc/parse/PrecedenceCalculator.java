package tscriptc.parse;

import tscriptc.tree.ExpressionTree;
import tscriptc.tree.Operation;
import tscriptc.tree.Trees;

import java.util.HashMap;

public final class PrecedenceCalculator {

    private static final HashMap<Object, Integer> precedences = new HashMap<>();

    static {
        precedences.put(TokenKind.DOT, 100);
        precedences.put(TokenKind.POW, 71);
        precedences.put(TokenKind.MUL, 70);
        precedences.put(TokenKind.IDIV, 70);
        precedences.put(TokenKind.DIV, 70);
        precedences.put(TokenKind.MOD, 70);
        precedences.put(TokenKind.PLUS, 60);
        precedences.put(TokenKind.MINUS, 60);
        precedences.put(TokenKind.SHIFT_AL, 50);
        precedences.put(TokenKind.SHIFT_AR, 50);
        precedences.put(TokenKind.SHIFT_LR, 50);
        precedences.put(TokenKind.GREATER, 40);
        precedences.put(TokenKind.LESS, 40);
        precedences.put(TokenKind.GREATER_EQ, 40);
        precedences.put(TokenKind.LESS_EQ, 40);
        precedences.put(TokenKind.TYPEOF, 40);
        precedences.put(TokenKind.EQUALS, 30);
        precedences.put(TokenKind.NOT_EQUALS, 30);
        precedences.put(TokenKind.XOR, 20);
        precedences.put(TokenKind.OR, 19);
        precedences.put(TokenKind.AND, 18);
        precedences.put(TokenKind.EQ_ASSIGN, 0);
        precedences.put(TokenKind.ADD_ASSIGN, 0);
        precedences.put(TokenKind.SUB_ASSIGN, 0);
        precedences.put(TokenKind.MUL_ASSIGN, 0);
        precedences.put(TokenKind.DIV_ASSIGN, 0);
        precedences.put(TokenKind.IDIV_ASSIGN, 0);
        precedences.put(TokenKind.MOD_ASSIGN, 0);
        precedences.put(TokenKind.POW_ASSIGN, 0);
        precedences.put(TokenKind.SHIFT_AL_ASSIGN, 0);
        precedences.put(TokenKind.SHIFT_AR_ASSIGN, 0);
        precedences.put(TokenKind.SHIFT_LR_ASSIGN, 0);
    }

    private PrecedenceCalculator(){
    }

    public static int calculate(Token token){
        if (!isBinaryOperator(token))
            throw new IllegalStateException(token.getTag() + " is not a binary expression");
        return precedences.get(token.getTag());
    }

    public static boolean isBinaryOperator(Token type){
        return precedences.containsKey(type.getTag());
    }

    public static ExpressionTree apply(Token op, ExpressionTree lhs, ExpressionTree rhs) {
        TokenKind tag = (TokenKind) op.getTag();
        return switch (tag) {

            case EQ_ASSIGN -> new Trees.BasicAssignTree(op.getLocation(), lhs, rhs);

            case ADD_ASSIGN, SUB_ASSIGN,
                    MUL_ASSIGN, DIV_ASSIGN,
                    IDIV_ASSIGN, MOD_ASSIGN,
                    POW_ASSIGN, SHIFT_LR_ASSIGN,
                    SHIFT_AL_ASSIGN, SHIFT_AR_ASSIGN
                    -> new Trees.BasicAssignTree(
                            op.getLocation(),
                            lhs,
                            new Trees.BasicBinaryOperationTree(op.getLocation(), lhs, rhs, Operation.withoutAssign(tag.name)));

            case PLUS, MINUS, MUL, DIV,
                    IDIV, MOD, POW, SHIFT_AL,
                    SHIFT_AR , SHIFT_LR, AND, OR,
                    XOR, TYPEOF, GREATER, LESS, GREATER_EQ, NOT_EQUALS,
                    EQUALS, LESS_EQ -> new Trees.BasicBinaryOperationTree(op.getLocation(), lhs, rhs, Operation.of(tag.name));

            default -> throw new IllegalStateException(tag + " is not a binary expression");
        };
    }

}
