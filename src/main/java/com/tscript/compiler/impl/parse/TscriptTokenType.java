package com.tscript.compiler.impl.parse;

/**
 * Token types that are used by {@link TscriptParser}.
 * @since 1.0
 * @author Lennart Köhler
 */
public enum TscriptTokenType {

    INTEGER,
    FLOAT,
    TRUE("true"),
    FALSE("false"),

    STRING,
    NULL("null"),
    FUNCTION("function"),
    RETURN("return"),
    NATIVE("native"),
    CLASS("class"),
    ENUM("enum"),
    CONSTRUCTOR("constructor"),
    PUBLIC("public"),
    PRIVATE("private"),
    PROTECTED("protected"),
    ABSTRACT("abstract"),
    OVERRIDDEN("overridden"),
    STATIC("static"),
    THIS("this"),
    SUPER("super"),
    VAR("var"),
    CONST("const"),
    EQ_ASSIGN("="),
    ADD_ASSIGN("+="),
    SUB_ASSIGN("-="),
    MUL_ASSIGN("*="),
    DIV_ASSIGN("/="),
    IDIV_ASSIGN("//="),
    MOD_ASSIGN("%="),
    POW_ASSIGN("^="),
    AND_ASSIGN("&="),
    OR_ASSIGN("|="),
    SHIFT_AL_ASSIGN("<<="),
    SHIFT_AR_ASSIGN(">>="),
    SHIFT_LR_ASSIGN(">>>="),
    IF("if"),
    SWITCH("switch"),
    CASE("case"),
    DEFAULT("default"),
    THEN("then"),
    ELSE("else"),
    WHILE("while"),
    DO("do"),
    FOR("for"),
    IN("in"),
    BREAK("break"),
    CONTINUE("continue"),
    TRY("try"),
    CATCH("catch"),
    THROW("throw"),
    TYPEOF("typeof"),
    PLUS("+"),
    MINUS("-"),
    MUL("*"),
    DIV("/"),
    IDIV("//"),
    MOD("%"),
    POW("^"),
    AND("and"),
    OR("or"),
    XOR("xor"),
    NOT("not"),
    SHIFT_AL("<<"),
    SHIFT_AR(">>"),
    SHIFT_LR(">>>"),
    EQUALS("=="),
    NOT_EQUALS("!="),
    LESS("<"),
    GREATER(">"),
    LESS_EQ("<="),
    GREATER_EQ(">="),
    CURVED_OPEN("{"),
    CURVED_CLOSED("}"),
    BRACKET_OPEN("["),
    BRACKET_CLOSED("]"),
    PARENTHESES_OPEN("("),
    PARENTHESES_CLOSED(")"),
    DOT("."),
    COMMA(","),
    SEMI(";"),
    COLON(":"),
    PLUS_PLUS("++"),
    MINUS_MINUS("--"),
    MODULE("module"),
    IMPORT("import"),
    FROM("from"),
    USE("use"),
    NAMESPACE("namespace"),
    EOF,
    ERROR,
    IDENTIFIER;

    TscriptTokenType(){
        this(null);
    }

    TscriptTokenType(String name){
        this.name = name;
    }

    public final String name;

    /**
     * Returns the matching token type for a given lexeme.
     * Returns {@link #IDENTIFIER} if no match was made.
     * @param lexeme the lexeme to match the type for
     * @return the matching token type
     */
    public static TscriptTokenType fromLexeme(String lexeme){
        for (TscriptTokenType kind : TscriptTokenType.values())
            if (kind.name != null && kind.name.equals(lexeme))
                return kind;
        return IDENTIFIER;
    }

}
