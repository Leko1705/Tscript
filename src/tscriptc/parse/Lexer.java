package tscriptc.parse;

public interface Lexer {

    Token peek();

    Token consume();

    default void pushBack(Token token){
        throw new UnsupportedOperationException("pushBack");
    }

    default void skip(int n){
        for (; n >= 0; n--)
            consume();
    }

    boolean hasNext();

}
