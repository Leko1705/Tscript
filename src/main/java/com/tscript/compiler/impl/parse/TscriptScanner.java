package com.tscript.compiler.impl.parse;

import com.tscript.compiler.impl.utils.Errors;
import com.tscript.compiler.source.utils.Location;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * A Lexer for the <em>Tscript programming language</em>.
 * @since 1.0
 * @author Lennart Köhler
 */
public class TscriptScanner implements Lexer<TscriptTokenType> {

    private final Set<Character> BINARY_DIGITS = Set.of('0', '1');
    private final Set<Character> OCTAL_DIGITS = Set.of('0', '1', '2', '3', '4', '5', '6', '7');
    private final Set<Character> DECIMAL_DIGITS = Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    private final Set<Character> HEX_DIGITS =
            Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F');

    private int line = 1;

    private int startPos = 0, endPos = 0;


    private final UnicodeReader reader;

    private final Deque<Token<TscriptTokenType>> queue = new ArrayDeque<>();

    public TscriptScanner(UnicodeReader reader) {
        this.reader = reader;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Token<TscriptTokenType> peek() {
        if (queue.isEmpty())
            queue.add(scan());
        return queue.peek();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Token<TscriptTokenType> consume() {
        if (queue.isEmpty())
            queue.add(scan());
        return queue.poll();
    }

    /**
     * {@inheritDoc}
     * @param token the new current token
     */
    @Override
    public void pushBack(Token<TscriptTokenType> token) {
        queue.push(token);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return !peek().hasTag(TscriptTokenType.EOF);
    }


    private Location createLocation(){
        return new Location(startPos, endPos, line);
    }

    private Token<TscriptTokenType> getEOF(){
        int start = endPos != 0 ? endPos-1 : 0;
        return new SimpleToken<>(new Location(start, endPos, line), null, TscriptTokenType.EOF);
    }

    private Token<TscriptTokenType> scan(){
        skipWhitespace();

        if (!reader.hasNext())
            return getEOF();

        if (nextIsNumeric())
            return scanNumber();

        else if (nextIsString())
            return scanString();

        else if (nextIsLetter())
            return scanIdentifierOrKeyword();

        Token<TscriptTokenType> token = scanSpecial();
        if (token != null) return token;

        Location location = new Location(endPos-1, endPos, line);
        char c = consumeChar();
        throw  Errors.unexpectedToken(location, c);
    }

    private char peekChar(){
        return reader.peek();
    }

    private char consumeChar(){
        char c = reader.consume();
        endPos++;
        return c;
    }

    private void skipWhitespace(){
        char c = peekChar();
        while (Character.isWhitespace(c)) {
            if (c == '\n') line++;
            consumeChar();
            c = reader.peek();
        }
        startPos = endPos;
    }

    private Token<TscriptTokenType> scanSpecial(){
        char c = peekChar();
        return switch (c){
            case '[', ']', '(', ')', '{', '}', ';', ',', ':' -> {
                consumeChar();
                String lexeme = Character.toString(c);
                yield new SimpleToken<>(createLocation(), lexeme, TscriptTokenType.fromLexeme(lexeme));
            }
            case '+', '-', '*', '/', '%', '^', '<', '>', '=', '!', '.'
                // all characters where a '=' can follow e.g. '+=' or '!='
                    -> scanPossibleExtendedOperation(c);
            default -> null;
        };
    }

    private Token<TscriptTokenType> scanPossibleExtendedOperation(char c){
        String lexeme = Character.toString(c);
        consumeChar();

        if (c == '.' && nextIsNumeric()){
            return scanNumberByRadixSet(new StringBuilder("0."), DECIMAL_DIGITS, false);
        }
        else if (c == '<' && peekChar() == '<') {
            lexeme += consumeChar();
            if (peekChar() == '<')
                lexeme += consumeChar();
        }
        else if (c == '>' && peekChar() == '>'){
            lexeme += consumeChar();
            if (peekChar() == '>') {
                lexeme += consumeChar();
                if (peekChar() == '>')
                    lexeme += consumeChar();
            }
        }
        else if (c == '/' && peekChar() == '/') {
            lexeme += consumeChar();
        }
        else if (c == '+' && peekChar() == '+') {
            consumeChar();
            return new SimpleToken<>(createLocation(), "++", TscriptTokenType.PLUS_PLUS);
        }
        else if (c == '-' && peekChar() == '-') {
            consumeChar();
            return new SimpleToken<>(createLocation(), "--", TscriptTokenType.MINUS_MINUS);
        }

        if (peekChar() == '=')
            lexeme += consumeChar();

        return new SimpleToken<>(createLocation(), lexeme, TscriptTokenType.fromLexeme(lexeme));
    }

    private boolean nextIsLetter(){
        char c = peekChar();
        return Character.isLetter(c) || c == '_';
    }

    private Token<TscriptTokenType> scanIdentifierOrKeyword(){
        StringBuilder buffer = new StringBuilder();
        char c = peekChar();

        while (Character.isLetter(c) || c == '_'){
            buffer.append(c);
            consumeChar();
            if (!reader.hasNext()) break;
            c = peekChar();
        }

        String lexeme = buffer.toString();
        return new SimpleToken<>(createLocation(), lexeme, TscriptTokenType.fromLexeme(lexeme));
    }

    private boolean nextIsString(){
        return peekChar() == '"';
    }

    private Token<TscriptTokenType> scanString(){
        consumeChar();
        StringBuilder buffer = new StringBuilder();
        char c;

        do {
            char next = scanNextStringChar();
            if (next == '"')
                return new SimpleToken<>(createLocation(), buffer.toString(), TscriptTokenType.STRING);
            buffer.append(next);
            if (!reader.hasNext())
                throw Errors.missingSymbol(new Location(endPos-1, endPos, line), "\"");
            c = peekChar();
        } while (c != '"');

        consumeChar();

        return new SimpleToken<>(createLocation(), buffer.toString(), TscriptTokenType.STRING);
    }

    private char scanNextStringChar() {
        char c = consumeChar();
        if (c == '\\'){
            c = consumeChar();
            if (c == 'n') c = '\n';
            else if (c == 'b') c = '\b';
            else if (c == 't') c = '\t';
            else if (c != '\\' && c != '"') {
                throw Errors.invalidEscapeCharacter(new Location(endPos-1, startPos, line));
            }
        }
        return c;
    }

    private boolean nextIsNumeric(){
        return Character.isDigit(peekChar());
    }

    private Token<TscriptTokenType> scanNumber(){
        StringBuilder buffer = new StringBuilder();
        Set<Character> radixSet = DECIMAL_DIGITS;

        char c = consumeChar();

        if (c == '0' && isValidRadixIdentifier(peekChar())){
            radixSet = getDigitSet(consumeChar());
            c = scanDigit(radixSet);
        }

        buffer.append(c);

        return scanNumberByRadixSet(buffer, radixSet, true);
    }

    private Token<TscriptTokenType> scanNumberByRadixSet(StringBuilder buffer, Set<Character> radixSet, boolean allowFraction){
        boolean fractionFound = !allowFraction;
        char c = peekChar();
        while (radixSet.contains(c) || c == '_' || c == '.'){

            if (c == '_'){
                consume();
                c = peekChar();
            }

            if (c == '.'){
                if (radixSet != DECIMAL_DIGITS) {
                    throw Errors.invalidFraction(createLocation());
                }
                if (fractionFound) break;
                fractionFound = true;
            }

            buffer.append(c);
            consumeChar();
            c = peekChar();
        }

        return completeToNumericToken(buffer, fractionFound, radixSet);
    }

    private Token<TscriptTokenType> completeToNumericToken(StringBuilder buffer, boolean fractionFound, Set<Character> radixSet){
        Location location = createLocation();

        if(radixSet == DECIMAL_DIGITS && fractionFound)
            return new SimpleToken<>(location, buffer.toString(), TscriptTokenType.FLOAT);

        int radix = radixSet.size();
        try {
            int parsed = Integer.parseInt(buffer.toString(), radix);
            return new SimpleToken<>(location, Integer.toString(parsed), TscriptTokenType.INTEGER);
        }catch (NumberFormatException e){
            BigInteger parsed = new BigInteger(buffer.toString(), radix);
            return new SimpleToken<>(location, parsed.toString(), TscriptTokenType.FLOAT);
        }
    }

    private char scanDigit(Set<Character> radixSet){
        char c = peekChar();
        if (c != '_'){
            if (!HEX_DIGITS.contains(c)) {
                throw Errors.missingDigitOnRadixSpecs(createLocation());
            }
            if (!radixSet.contains(c)){
                throw Errors.invalidDigitOnRadixSpecs(createLocation());
            }
        }
        consumeChar();
        return c;
    }

    private boolean isValidRadixIdentifier(char c){
        return switch (c){
            case 'b', 'B', 'x', 'X', 'o', 'O' -> true;
            default -> false;
        };
    }

    private Set<Character> getDigitSet(char c){
        return switch (c){
            case 'b', 'B' -> BINARY_DIGITS;
            case 'o', 'O' -> OCTAL_DIGITS;
            case 'x', 'X' -> HEX_DIGITS;
            default -> DECIMAL_DIGITS;
        };
    }
}
