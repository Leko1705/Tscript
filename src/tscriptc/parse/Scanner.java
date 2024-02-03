package tscriptc.parse;

import tscriptc.log.Logger;
import tscriptc.util.Errors;
import tscriptc.util.Location;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

public class Scanner implements Lexer {
    private static final Set<Character> binaries = Set.of('0', '1');
    private static final Set<Character> octal = Set.of('0', '1', '2', '3', '4', '5', '6', '7');
    private static final Set<Character> decimals = Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    private static final Set<Character> hexadecimals =
            Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F');


    private final UnicodeReader reader;
    private final Logger log;

    private int line = 1;

    private int startPos = 0, endPos = 0;

    private Token current = null;

    private final ArrayDeque<Token> pushedBack = new ArrayDeque<>();

    public Scanner(UnicodeReader reader, Logger log) {
        this.reader = reader;
        this.log = log;
    }

    @Override
    public Token peek() {
        if (current == null) current = scan();
        return current;
    }

    @Override
    public Token consume() {
        if (current == null) return current = scan();
        Token ret = current;
        current = pushedBack.isEmpty() ? scan() : pushedBack.pop();
        return ret;
    }

    @Override
    public void pushBack(Token token) {
        pushedBack.push(current);
        current = token;
    }

    @Override
    public boolean hasNext() {
        return peek().hasTag(TokenKind.EOF);
    }

    private Location createLocation(){
        return new Location(startPos, endPos, line);
    }

    private Token getEOF(){
        int start = endPos != 0 ? endPos-1 : 0;
        return new BasicToken(new Location(start, endPos, line), TokenKind.EOF, null);
    }

    private Token scan(){
        skipWhitespaceAndComments();

        if (!reader.hasNext())
            return getEOF();

        if (nextIsNumeric())
            return scanNumber();

        else if (nextIsString())
            return scanString();

        else if (nextIsLetter())
            return scanIdentifierOrKeyword();

        Token token = scanSpecial();
        if (token != null) return token;

        Location location = new Location(endPos-1, endPos, line);
        char c = consumeChar();
        log.error(Errors.unexpectedToken(location, c));
        return new BasicToken(location, TokenKind.ERROR, Character.toString(c));
    }

    private char peekChar(){
        return reader.peek();
    }

    private char consumeChar(){
        char c = reader.consume();
        endPos++;
        return c;
    }

    private void skipWhitespaceAndComments(){
        char c = peekChar();
        while (Character.isWhitespace(c) || c == '#') {
            while (Character.isWhitespace(c)) {
                if (c == '\n') line++;
                consumeChar();
                c = reader.peek();
            }
            if (c == '#') {
                consumeChar();
                c = consumeChar();
                if (c == '*'){
                    c = consumeChar();
                    while (true){
                        if (c == '*'){
                            c = consumeChar();
                            if (c == '#'){
                                c = consumeChar();
                                break;
                            }
                        }
                        c = consumeChar();
                    }
                }
                else {
                    while (c != '\n' && reader.hasNext()) {
                        c = consumeChar();
                    }
                }
            }
        }
        startPos = endPos;
    }

    private Token scanSpecial(){
        char c = peekChar();
        return switch (c){
            case '[', ']', '(', ')', '{', '}', ';', ',', '.', ':' -> {
                consumeChar();
                String lexem = Character.toString(c);
                yield new BasicToken(createLocation(), TokenKind.fromLexem(lexem), lexem);
            }
            case '+', '-', '*', '/', '%', '^', '<', '>', '=', '!'
                    // all characters where a '=' can follow e.g. '+=' or '!='
                    -> scanPossibleEqualOperation(c);
            default -> null;
        };
    }

    private Token scanPossibleEqualOperation(char c){
        String lexem = Character.toString(c);
        consumeChar();

        if (c == '<' && peekChar() == '<') {
            lexem += consumeChar();
            if (peekChar() == '<')
                lexem += consumeChar();
        }
        else if (c == '>' && peekChar() == '>'){
            lexem += consumeChar();
            if (peekChar() == '>') {
                lexem += consumeChar();
                if (peekChar() == '>')
                    lexem += consumeChar();
            }
        }
        else if (c == '/' && peekChar() == '/')
            lexem += consumeChar();

        if (peekChar() == '=')
            lexem += consumeChar();

        return new BasicToken(createLocation(), TokenKind.fromLexem(lexem), lexem);
    }

    private boolean nextIsLetter(){
        char c = peekChar();
        return Character.isLetter(c) || c == '_';
    }

    private Token scanIdentifierOrKeyword(){
        StringBuilder buffer = new StringBuilder();
        char c = peekChar();

        while (Character.isLetter(c) || c == '_' || Character.isDigit(c)){
            buffer.append(c);
            consumeChar();
            if (!reader.hasNext()) break;
            c = peekChar();
        }

        String lexem = buffer.toString();
        return new BasicToken(createLocation(), TokenKind.fromLexem(lexem), lexem);
    }

    private boolean nextIsString(){
        return peekChar() == '"';
    }

    private Token scanString(){
        consumeChar();
        StringBuilder buffer = new StringBuilder();
        char c;

        do {
            char next = scanNextStringChar();
            if (next == '"')
                return new BasicToken(createLocation(), TokenKind.STRING, buffer.toString());
            buffer.append(next);
            if (!reader.hasNext())
                log.error(Errors.missingSymbol(new Location(endPos-1, endPos, line), "\""));
            c = peekChar();
        } while (c != '"');

        consumeChar();

        return new BasicToken(createLocation(), TokenKind.STRING, buffer.toString());
    }

    private char scanNextStringChar() {
        char c = consumeChar();
        if (c == '\\'){
            c = consumeChar();
            if (c == 'n') c = '\n';
            else if (c == 'b') c = '\b';
            else if (c == 't') c = '\t';
            else if (c != '\\' && c != '"') {
                log.error(Errors.invalidEscapeCharacter(new Location(endPos-1, startPos, line)));
                c = Character.MIN_VALUE;
            }
        }
        return c;
    }

    private boolean nextIsNumeric(){
        return Character.isDigit(peekChar());
    }

    private Token scanNumber(){
        StringBuilder buffer = new StringBuilder();
        Set<Character> radixSet = decimals;

        char c = consumeChar();
        if (c == '0' && isValidRadixIdentifier(peekChar())){
            radixSet = getDigitSet(consumeChar());
            c = scanDigit(radixSet);
        }

        buffer.append(c);

        return scanNumberByRadixSet(buffer, radixSet);
    }

    private Token scanNumberByRadixSet(StringBuilder buffer, Set<Character> radixSet){
        boolean fractionFound = false;
        char c = peekChar();
        while (radixSet.contains(c) || c == '_' || c == '.'){

            if (c == '_'){
                consume();
                c = peekChar();
            }

            if (c == '.'){
                if (radixSet != decimals) {
                    log.error(Errors.invalidFraction(createLocation()));
                    continue;
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

    private Token completeToNumericToken(StringBuilder buffer, boolean fractionFound, Set<Character> radixSet){
        Location location = createLocation();

        if(radixSet == decimals && fractionFound)
            return new BasicToken(location, TokenKind.FLOAT, buffer.toString());

        int radix = radixSet.size();
        try {
            int parsed = Integer.parseInt(buffer.toString(), radix);
            return new BasicToken(location, TokenKind.INTEGER, Integer.toString(parsed));
        }catch (NumberFormatException e){
            BigInteger parsed = new BigInteger(buffer.toString(), radix);
            return new BasicToken(location, TokenKind.FLOAT, parsed.toString());
        }
    }

    private char scanDigit(Set<Character> radixSet){
        char c = peekChar();
        if (c != '_'){
            if (!hexadecimals.contains(c)) {
                log.error(Errors.missingDigitOnRadixSpecs(createLocation()));
                return 0;
            }
            if (!radixSet.contains(c)){
                log.error(Errors.invalidDigitOnRadixSpecs(createLocation()));
                return 0;
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
            case 'b', 'B' -> binaries;
            case 'o', 'O' -> octal;
            case 'x', 'X' -> hexadecimals;
            default -> decimals;
        };
    }


}
