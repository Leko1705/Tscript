package com.tscript.tscriptc.parse;

public enum TokenKind {
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
    IMPORT("import"),
    USE("use"),
    NAMESPACE("namespace"),
    EOF,
    ERROR,
    IDENTIFIER;

    TokenKind(){
        this(null);
    }
    TokenKind(String name){
        this.name = name;
    }
    public final String name;

    public static TokenKind fromLexem(String lexem){
        for (TokenKind kind : TokenKind.values())
            if (kind.name != null && kind.name.equals(lexem))
                return kind;
        return IDENTIFIER;
    }
}
