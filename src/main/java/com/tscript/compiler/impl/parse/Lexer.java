package com.tscript.compiler.impl.parse;

/**
 * The lexical analyzer maps an input stream consisting of
 * ASCII characters and Unicode escapes into a token sequence.
 *
 * @since 1.0
 * @author Lennart Köhler
 * @param <T> the tokens tag type
 */
public interface Lexer<T> {

    /**
     * Returns the current Token without consuming it.
     * @return the current token
     */
    Token<T> peek();

    /**
     * Returns the current Token and moves to the next one.
     * @return the current token
     */
    Token<T> consume();

    /**
     * Pushes a token back to the lexer. The token will be the new current token.
     * The previous current token will pe accessible afterward.
     * @param token the new current token
     * @throws UnsupportedOperationException by default
     */
    default void pushBack(Token<T> token){
        throw new UnsupportedOperationException("pushBack");
    }

    /**
     * Skips n tokens
     * @param n the amount of tokens to skip
     */
    default void skip(int n){
        for (; n >= 0; n--)
            consume();
    }

    /**
     * Returns weather the lexer has a current token to peek or consume.
     * @return true if the lexer has a next token, else false
     */
    boolean hasNext();

}
