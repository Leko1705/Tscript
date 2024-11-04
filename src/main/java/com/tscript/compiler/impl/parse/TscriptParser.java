package com.tscript.compiler.impl.parse;

import com.tscript.compiler.source.utils.CompileException;
import com.tscript.compiler.source.utils.Location;
import com.tscript.compiler.impl.utils.*;
import com.tscript.compiler.impl.utils.TCTree.*;
import com.tscript.compiler.source.tree.*;

import java.io.InputStream;
import java.util.*;

import static com.tscript.compiler.impl.parse.TscriptTokenType.*;

public class TscriptParser implements Parser {

    public static TscriptParser getDefaultSetup(InputStream in){
        UnicodeReader reader = new UnicodeReader(in);
        Lexer<TscriptTokenType> lexer = new TscriptScanner(reader);
        return new TscriptParser(lexer);
    }


    private final Lexer<TscriptTokenType> lexer;
    private final TreeMaker F = new TreeMaker();

    public TscriptParser(Lexer<TscriptTokenType> lexer) {
        this.lexer = lexer;
    }

    private void error(String msg, Token<TscriptTokenType> token) {
        throw new CompileException(msg, token.getLocation(), Phase.PARSING);
    }

    private TCExpressionTree unwrap(TCExpressionTree exp, Token<TscriptTokenType> token) {
        if (exp == null)
            error("expression expected", token);
        return exp;
    }

    @Override
    public TCRootTree parseProgram() {
        List<TCDefinitionTree> definitions = new ArrayList<>();
        List<TCStatementTree> statements = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.peek();
        String moduleName = null;

        if (token.hasTag(MODULE)){
            lexer.consume();
            List<String> moduleNamePath = parseAccessChain();
            StringJoiner sb = new StringJoiner(".");
            for (String name : moduleNamePath){
                sb.add(name);
            }
            moduleName = sb.toString();
            parseEOS();
        }

        List<? extends TCTree> imports = parseImports();

        token = lexer.peek();
        while (!token.hasTag(EOF)) {

            TCStatementTree stmt = parseStatement();

            if (stmt instanceof TCDefinitionTree def){
                definitions.add(def);
            }
            else if (stmt != null) {
                statements.add(stmt);
            }

            token = lexer.peek();
        }

        return F.RootTree(null, moduleName, imports, definitions, statements);
    }

    private List<? extends TCTree> parseImports(){
        List<TCTree> imports = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.peek();
        while (token.hasTag(IMPORT, FROM)){

            if (token.hasTag(IMPORT))
                imports.add(parseImport());
            else
                imports.add(parseFromImport());

            token = lexer.peek();
        }

        return imports;
    }

    private TCDefinitionTree parseDefinition() {
        Token<TscriptTokenType> token = lexer.peek();

        if (token.hasTag(FUNCTION)) {
            return parseFunctionDef();
        }
        else if (token.hasTag(NATIVE)) {
            return parseDeclaredFunction(Modifier.NATIVE);
        }
        else if (token.hasTag(CLASS)) {
            return parseClass();
        }
        else if (token.hasTag(ENUM)){
            return parseEnum();
        }
        else if (token.hasTag(ABSTRACT)) {
            lexer.consume();
            token = lexer.peek();
            if (token.hasTag(CLASS)) {
                return parseClass(Modifier.ABSTRACT);
            }
            else if (token.hasTag(FUNCTION)){
                return parseFunctionDef(Modifier.ABSTRACT);
            }
        }
        else if (token.hasTag(NAMESPACE)) {
            return parseNamespace();
        }

        return null;
    }

    private TCNamespaceTree parseNamespace(Modifier... modifiers) {
        Location location = lexer.consume().getLocation();
        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(IDENTIFIER))
            error("identifier expected", token);

        String name = token.getLexeme();

        List<TCTree> definitions = new ArrayList<>();

        token = lexer.consume();
        if (!token.hasTag(CURVED_OPEN))
            error("missing '{'", token);

        token = lexer.peek();

        if (!token.hasTag(CURVED_CLOSED)) {
            do {
                TCTree def = parseDefinition();
                if (def == null) {
                    if (token.hasTag(VAR)){
                        def = parseVarDec(Modifier.STATIC);
                    }
                    else if (token.hasTag(CONST)){
                        def = parseVarDec(Modifier.STATIC, Modifier.CONSTANT);
                    }
                    else {
                        break;
                    }
                }
                definitions.add(def);
                token = lexer.peek();
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(EOF) || !token.hasTag(CURVED_CLOSED))
            error("missing '}'", token);

        return F.NamespaceTree(location, F.ModifiersTree(location, Set.of(modifiers)), name, definitions, new ArrayList<>());
    }

    private TCClassTree parseClass(Modifier... modifiers){
        Location location = lexer.consume().getLocation();
        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(IDENTIFIER))
            error("identifier expected", token);

        String name = token.getLexeme();
        List<String> superName = null;

        token = lexer.peek();
        if (token.hasTag(COLON)){
            lexer.consume();
            superName = parseAccessChain();
        }
        return parseClassBody(location, Set.of(modifiers), name, superName);
    }

    private TCClassTree parseClassBody(Location location, Set<Modifier> classModifiers,
                                     String className, List<String> superName){

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(CURVED_OPEN))
            error("missing '{'", token);


        TCConstructorTree constructor = null;
        Modifier visibility = null;
        boolean isStatic = false;
        boolean isOverridden = false;
        List<TCTree> members = new ArrayList<>();
        TCTree defTree = null;


        token = lexer.peek();
        if (!token.hasTag(CURVED_CLOSED, EOF)) {
            do {
                if (visibility == null || isVisibility(token.getTag())) {
                    visibility = parseVisibility();
                    token = lexer.peek();
                    continue;
                }
                else if (token.hasTag(STATIC)){
                    if (isOverridden) {
                        error("static functions can not be overridden", token);
                        continue;
                    }
                    isStatic = true;
                    lexer.consume();
                    token = lexer.peek();
                    continue;
                }
                else if (token.hasTag(OVERRIDDEN)){
                    if (isStatic) {
                        error("static functions can not be overridden", token);
                        continue;
                    }
                    isOverridden = true;
                    lexer.consume();
                    token = lexer.peek();
                    continue;
                }

                else if (token.hasTag(VAR)) {
                    if (isStatic) defTree = parseVarDec(visibility, Modifier.STATIC);
                    else defTree = parseVarDec(visibility);
                }
                else if (token.hasTag(CONST)) {
                    if (isStatic) defTree = parseVarDec(visibility, Modifier.CONSTANT, Modifier.STATIC);
                    else defTree = parseVarDec(visibility, Modifier.CONSTANT);
                }
                else if (token.hasTag(FUNCTION)) {
                    if (isOverridden) defTree = parseFunctionDef(visibility, Modifier.OVERRIDDEN);
                    else if (isStatic) defTree = parseFunctionDef(visibility, Modifier.STATIC);
                    else defTree = parseFunctionDef(visibility);
                }
                else if (token.hasTag(CLASS)){
                    if (isStatic) defTree = parseClass(visibility, Modifier.STATIC);
                    else defTree = parseClass(visibility);
                }
                else if (token.hasTag(ENUM)){
                    if (isStatic) defTree = parseEnum(visibility, Modifier.STATIC);
                    else defTree = parseEnum(visibility);
                }
                else if (token.hasTag(NAMESPACE)){
                    defTree = parseNamespace(visibility, Modifier.STATIC);
                }
                else if (token.hasTag(NATIVE)){
                    if (isOverridden) defTree = parseDeclaredFunction(visibility, Modifier.NATIVE, Modifier.OVERRIDDEN);
                    else if (isStatic) defTree = parseDeclaredFunction(visibility, Modifier.NATIVE, Modifier.STATIC);
                    else defTree = parseDeclaredFunction(visibility, Modifier.NATIVE);
                }
                else if (token.hasTag(ABSTRACT)){
                    if (isOverridden) defTree = parseDeclaredFunction(visibility, Modifier.ABSTRACT, Modifier.OVERRIDDEN);
                    else if (isStatic) defTree = parseDeclaredFunction(visibility, Modifier.ABSTRACT, Modifier.STATIC);
                    else defTree = parseDeclaredFunction(visibility, Modifier.ABSTRACT);
                }
                else if (token.hasTag(CONSTRUCTOR)){

                    if (constructor != null)
                        error("can not have multiple constructors in class", token);
                    else
                        constructor = parseConstructor(visibility);

                    if (isStatic){
                        error("constructor can not be static", token);
                    }

                    defTree = constructor;
                }
                else {
                    error("class member definition expected", token);
                }

                members.add(defTree);

                if (defTree != null)
                    isStatic = false;
                defTree = null;

                token = lexer.peek();

            } while (!token.hasTag(CURVED_CLOSED, EOF));
        }

        if (!token.hasTag(CURVED_CLOSED))
            error("missing '}'", token);

        lexer.consume();
        return F.ClassTree(location, F.ModifiersTree(location, classModifiers), className, superName, members);
    }

    /**
     * compiles to: <pre>
     *     {@code
     *     class EnumName {
     *         public:
     *         static const
     *              ENUM1 = EnumName("ENUM1"),
     *              ENUM2 = EnumName("ENUM2");
     *
 *         private:
     *         var name;
     *         constructor(n){
     *             name = n;
     *         }
     *         function __str__(){
     *             return name;
     *         }
     *     }
     *     }
     * </pre>
     * @param modifiers additional modifiers for this enum
     * @return the class tree for this enum
     */
    private TCClassTree parseEnum(Modifier... modifiers){
        Location location = lexer.consume().getLocation();

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(IDENTIFIER))
            error("identifier expected", token);
        String name = token.getLexeme();

        token = lexer.consume();
        if (!token.hasTag(CURVED_OPEN))
            error("missing '{'", token);

        List<TCVarDefTree> enums = new ArrayList<>();

        token = lexer.consume();
        if (!token.hasTag(CURVED_CLOSED, EOF)) {
            do {

                if (!token.hasTag(IDENTIFIER))
                    error("identifier expected", token);

                enums.add(F.VarDefTree(
                        token.getLocation(),
                        token.getLexeme(),
                        F.CallTree(
                                token.getLocation(),
                                F.ThisTree(token.getLocation()),
                                List.of(F.ArgumentTree(
                                        token.getLocation(),
                                        null,
                                        F.StringTree(token.getLocation(), token.getLexeme())))
                        )));

                token = lexer.consume();
                if (token.hasTag(CURVED_CLOSED, EOF)) {
                    break;
                }

                if (!token.hasTag(COMMA))
                    error("missing '}'", token);

                token = lexer.consume();
            } while (true);
        }

        if (!token.hasTag(CURVED_CLOSED))
            error("missing '}'", token);

        TCVarDefsTree enumList = F.VarDefsTree(
                location,
                F.ModifiersTree(location, Set.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.CONSTANT)),
                enums
        );

        TCConstructorTree constructor = F.ConstructorTree(
                location,
                F.ModifiersTree(location, Set.of(Modifier.PRIVATE)),
                List.of(F.ParameterTree(location, "n", F.ModifiersTree(location, Set.of()), null)),
                List.of(),
                F.BlockTree(location, List.of(
                        F.ExpressionStatementTree(location,
                                F.AssignTree(
                                        location,
                                        F.VariableTree(location, "name"),
                                        F.VariableTree(location, "n")))
                ))
        );

        TCVarDefsTree nameField = F.VarDefsTree(
                location,
                F.ModifiersTree(location, Set.of(Modifier.PRIVATE)),
                List.of(
                        F.VarDefTree(location, "name", null)
                ));

        TCFunctionTree toString = F.FunctionTree(
                location,
                F.ModifiersTree(location, Set.of()),
                "__str__",
                List.of(),
                F.BlockTree(location, List.of(
                        F.ReturnTree(location, F.VariableTree(location, "name"))))
        );

        return F.ClassTree(location,
                F.ModifiersTree(location, Set.of(modifiers)),
                name,
                null,
                List.of(nameField, constructor, enumList, toString));
    }

    private TCConstructorTree parseConstructor(Modifier... modifiers){
        Location location = lexer.consume().getLocation();

        List<TCParameterTree> parameters = new ArrayList<>();
        List<TCArgumentTree> superArgs = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(PARENTHESES_OPEN))
            error("missing '('", token);

        token = lexer.peek();
        if (!token.hasTag(PARENTHESES_CLOSED, EOF)){
            do {
                TCParameterTree param = parseParam();
                parameters.add(param);

                token = lexer.peek();
                if (token.hasTag(COMMA)){
                    lexer.consume();
                    continue;
                }

                break;
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(EOF) || !token.hasTag(PARENTHESES_CLOSED))
            error("missing ')'", token);

        token = lexer.peek();

        if (token.hasTag(COLON)){
            lexer.consume();
            token = lexer.consume();
            if (!token.hasTag(SUPER))
                error("missing keyword 'super'", token);
            token = lexer.consume();
            if (!token.hasTag(PARENTHESES_OPEN))
                error("missing '('", token);

            token = lexer.peek();

            if (!token.hasTag(PARENTHESES_CLOSED)){
                do {
                    TCArgumentTree arg = parseArgument();
                    superArgs.add(arg);

                    token = lexer.peek();
                    if (token.hasTag(COMMA)){
                        lexer.consume();
                        continue;
                    }

                    break;
                } while (true);
            }

            token = lexer.consume();
            if (token.hasTag(EOF)|| !token.hasTag(PARENTHESES_CLOSED))
                error("missing ')'", token);
        }

        TCBlockTree body = parseBlock();

        return F.ConstructorTree(location, F.ModifiersTree(location, Set.of(modifiers)), parameters, superArgs, body);
    }

    private boolean isVisibility(TscriptTokenType type){
        return type == PUBLIC
                || type == PRIVATE
                || type == PROTECTED;
    }

    private Modifier parseVisibility(){
        Token<TscriptTokenType> token = lexer.consume();
        TscriptTokenType visibility = token.getTag();
        if (!isVisibility(visibility))
            error("missing visibility", token);

        token = lexer.consume();
        if (!token.hasTag(COLON))
            error("missing ':'", token);

        return switch (visibility){
            case PUBLIC -> Modifier.PUBLIC;
            case PRIVATE -> Modifier.PRIVATE;
            case PROTECTED -> Modifier.PROTECTED;
            default -> throw new AssertionError();
        };
    }

    private TCFunctionTree parseFunctionDef(Modifier... modifiers){

        Location location = lexer.consume().getLocation();
        Token<TscriptTokenType> token = lexer.consume();

        if (!token.hasTag(IDENTIFIER))
            error("identifier expected", token);

        String name = token.getLexeme();

        List<TCParameterTree> parameters = new ArrayList<>();

        token = lexer.consume();
        if (!token.hasTag(PARENTHESES_OPEN))
            error("missing '('", token);

        token = lexer.peek();
        if (!token.hasTag(PARENTHESES_CLOSED, EOF)){
            do {
                TCParameterTree param = parseParam();
                parameters.add(param);

                token = lexer.peek();
                if (token.hasTag(COMMA)){
                    lexer.consume();
                    continue;
                }

                break;
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(EOF) || !token.hasTag(PARENTHESES_CLOSED))
            error("missing ')'", token);

        TCBlockTree body = parseBlock();

        TCReturnTree returnNode = F.ReturnTree(location, F.NullTree(location));
        List<TCStatementTree> statements = new ArrayList<>(body.statements);
        statements.add(returnNode);
        body = F.BlockTree(body.getLocation(), statements);

        return F.FunctionTree(location, F.ModifiersTree(location, Set.of(modifiers)), name, parameters, body);
    }

    private TCFunctionTree parseDeclaredFunction(Modifier... modifiers){
        Location location = lexer.consume().getLocation();
        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(FUNCTION))
            error("keyword 'function' expected", token);

        token = lexer.consume();
        if (!token.hasTag(IDENTIFIER))
            error("identifier expected", token);

        String name = token.getLexeme();
        parseEOS();

        return F.FunctionTree(location, F.ModifiersTree(location, Set.of(modifiers)), name, List.of(), null);
    }

    private TCParameterTree parseParam(){
        Set<Modifier> modifiers = new HashSet<>();

        Token<TscriptTokenType> token = lexer.consume();
        if (token.hasTag(CONST)){
            modifiers.add(Modifier.CONSTANT);
            token = lexer.consume();
        }
        if (!token.hasTag(IDENTIFIER))
            error("identifier expected", token);

        Location location = token.getLocation();
        String name = token.getLexeme();
        TCExpressionTree initializer = null;

        token = lexer.peek();
        if (token.hasTag(EQ_ASSIGN)){
            token = lexer.consume();
            initializer = unwrap(parseExpression(), token);
        }

        return F.ParameterTree(location, name, F.ModifiersTree(location, modifiers), initializer);
    }

    @Override
    public TCStatementTree parseStatement() {
        TCDefinitionTree candidate = parseDefinition();
        if (candidate != null) return candidate;

        Token<TscriptTokenType> token = lexer.peek();

        if (token.hasTag(SEMI)){
            lexer.consume();
            return null;
        }

        else if (token.hasTag(VAR)){
            return parseVarDec();
        }
        else if (token.hasTag(CONST)){
            return parseVarDec(Modifier.CONSTANT);
        }
        else if (token.hasTag(IF)){
            return parseIfElse();
        }
        else if (token.hasTag(WHILE)){
            return parseWhileDo();
        }
        else if (token.hasTag(DO)){
            return parseDoWhile();
        }
        else if (token.hasTag(FOR)){
            return parseForLoop();
        }
        else if (token.hasTag(BREAK)){
            TCBreakTree breakTree = F.BreakTree(lexer.consume().getLocation());
            parseEOS();
            return breakTree;
        }
        else if (token.hasTag(CONTINUE)){
            TCContinueTree continueTree = F.ContinueTree(lexer.consume().getLocation());
            parseEOS();
            return continueTree;
        }
        else if (token.hasTag(RETURN)){
            return parseReturn();
        }
        else if (token.hasTag(CURVED_OPEN)){
            return parseBlock();
        }
        else if (token.hasTag(THROW)){
            return parseThrow();
        }
        else if (token.hasTag(TRY)){
            return parseTryCatch();
        }
        else if (token.hasTag(USE)) {
            return parseUse();
        }
        else if (token.hasTag(FROM)) {
            return parseFromUse();
        }
        else {
            TCExpressionTree exp = parseExpression();
            if (exp == null)
                error("not a statement", token);
            else {
                parseEOS();
                return F.ExpressionStatementTree(exp.getLocation(), exp);
            }
        }


        return null;
    }

    private TCVarDefsTree parseVarDec(Modifier... modifiers){

        List<TCVarDefTree> varDefs = new ArrayList<>();
        Location location = lexer.peek().getLocation();

        do {
            lexer.consume();
            Token<TscriptTokenType> ident = lexer.consume();
            if (!ident.hasTag(IDENTIFIER))
                error("identifier expected", ident);

            Location varLocation = ident.getLocation();
            String name = ident.getLexeme();
            TCExpressionTree initializer = null;

            Token<TscriptTokenType> next = lexer.peek();
            if (!next.hasTag(SEMI)) {
                next = lexer.consume();
                if (!next.hasTag(EQ_ASSIGN))
                    error("'=' expected", next);
                initializer = unwrap(parseExpression(), next);
            }

            TCVarDefTree varDefTree = F.VarDefTree(varLocation, name, initializer);
            varDefs.add(varDefTree);

        } while (lexer.peek().hasTag(COMMA));

        parseEOS();

        return F.VarDefsTree(location, F.ModifiersTree(location, Set.of(modifiers)), varDefs);
    }

    private TCIfElseTree parseIfElse(){
        Location location = lexer.consume().getLocation();

        TCExpressionTree condition = unwrap(parseExpression(), lexer.peek());

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(THEN))
            error("missing keyword 'then'", token);

        TCStatementTree ifBody = parseStatement();
        TCStatementTree elseBody = null;

        if (lexer.peek().hasTag(ELSE)){
            lexer.consume();
            elseBody = parseStatement();
        }

        return F.IfElseTree(location, condition, ifBody, elseBody);
    }

    private TCDoWhileTree parseDoWhile(){
        Location location = lexer.consume().getLocation();

        TCStatementTree body = parseStatement();

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(WHILE))
            error("missing keyword 'while'", token);

        TCExpressionTree condition = unwrap(parseExpression(), lexer.peek());
        parseEOS();
        return F.DoWhileTree(location, body, condition);
    }

    private TCWhileDoTree parseWhileDo(){
        Location location = lexer.consume().getLocation();

        TCExpressionTree condition = unwrap(parseExpression(), lexer.peek());

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(DO))
            error("missing keyword 'do'", token);

        TCStatementTree body = parseStatement();

        return F.WhileDoTree(location, condition, body);
    }

    private TCForLoopTree parseForLoop(){
        Token<TscriptTokenType> loopToken = lexer.peek();
        final Location location = loopToken.getLocation();

        lexer.consume();
        Token<TscriptTokenType> token = lexer.peek();

        TCVarDefTree runVar;
        TCExpressionTree iterable;

        if (token.hasTag(VAR)){
            lexer.consume();
            token = lexer.consume();
            if (!token.hasTag(IDENTIFIER))
                error("identifier expected", token);
            runVar = F.VarDefTree(token.getLocation(), token.getLexeme(), null);
            token = lexer.consume();
            if (!token.hasTag(IN))
                error("keyword 'in' expected", token);
            iterable = unwrap(parseExpression(), lexer.peek());
        }
        else {
            TCExpressionTree first = unwrap(parseExpression(), token);
            token = lexer.peek();
            if (token.hasTag(IN)){
                lexer.consume();
                if (!(first instanceof VariableTree v)) {
                    error("identifier or variable declaration expected", token);
                    runVar = null;
                    iterable = null;
                }
                else {
                    iterable = unwrap(parseExpression(), lexer.peek());
                    runVar = F.VarDefTree(v.getLocation(), v.getName(), null);
                }
            }
            else {
                runVar = null;
                iterable = first;
            }
        }

        token = lexer.consume();
        if (!token.hasTag(DO))
            error("missing keyword 'do'", token);

        TCStatementTree body = parseStatement();

        return F.ForLoopTree(location, runVar, iterable, body);
    }

    private TCReturnTree parseReturn(){
        Location location = lexer.consume().getLocation();
        TCExpressionTree returned;

        if (lexer.peek().hasTag(SEMI)){
            returned = F.NullTree(lexer.consume().getLocation());
        }
       else {
           returned = unwrap(parseExpression(), lexer.peek());
           parseEOS();
        }

        return F.ReturnTree(location, returned);
    }

    private TCThrowTree parseThrow(){
        Token<TscriptTokenType> token = lexer.consume();
        TCExpressionTree thrown = unwrap(parseExpression(), token);
        parseEOS();
        return F.ThrowTree(token.getLocation(), thrown);
    }

    private TCTryCatchTree parseTryCatch(){
        Location location = lexer.consume().getLocation();

        TCStatementTree tryBody = parseStatement();
        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(CATCH))
            error("missing keyword 'catch'", token);
        token = lexer.consume();
        if (!token.hasTag(VAR, CONST))
            error("missing keyword 'var' or 'const'", token);

        token = lexer.consume();
        if (!token.hasTag(IDENTIFIER))
            error("identifier expected", token);

        TCVarDefTree exVarDef = F.VarDefTree(token.getLocation(), token.getLexeme(), null);

        token = lexer.consume();
        if (!token.hasTag(DO))
            error("missing keyword 'do'", token);

        TCStatementTree catchBody = parseStatement();

        return F.TryCatchTree(location, tryBody, exVarDef, catchBody);
    }

    private TCUseTree parseUse() {
        Token<TscriptTokenType> token = lexer.consume();
        TCExpressionTree exp = unwrap(parseExpression(), token);
        TCUseTree useTree = F.UseTree(token.getLocation(), exp, null);
        parseEOS();
        return useTree;
    }

    private TCUseTree parseFromUse() {
        Location location = lexer.consume().getLocation();
        TCExpressionTree from = parseExpression();
        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(USE))
            error("missing keyword 'use'", token);

        List<String> useChain = parseAccessChain();
        Iterator<String> itr = useChain.iterator();

        do {
            from = F.MemberAccessTree(location, from, itr.next());
        } while (itr.hasNext());

        parseEOS();
        return F.UseTree(location, from, useChain.get(useChain.size() - 1));
    }

    private TCImportTree parseImport(){
        Location location = lexer.consume().getLocation();
        List<String> accessChain = parseAccessChain();
        parseEOS();
        return F.ImportTree(location, accessChain);
    }

    private TCFromImportTree parseFromImport(){
        Location location = lexer.consume().getLocation();
        List<String> fromAccessChain = parseAccessChain();
        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(IMPORT))
            error("missing keyword 'import'", token);
        List<String> importAccessChain = parseAccessChain();
        parseEOS();
        return F.FromImportTree(location, fromAccessChain, importAccessChain);
    }

    private List<String> parseAccessChain(){
        List<String> accessChain = new ArrayList<>();
        Token<TscriptTokenType> token;

        do {
            token = lexer.consume();
            if (!token.hasTag(IDENTIFIER))
                error("identifier expected", token);
            accessChain.add(token.getLexeme());

            token = lexer.peek();

            if (token.hasTag(DOT)) {
                lexer.consume();
                continue;
            }

            break;
        }
        while (true);

        return accessChain;
    }

    private TCBlockTree parseBlock(){
        Location location = lexer.peek().getLocation();
        List<TCStatementTree> statements = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(CURVED_OPEN))
            error("missing '{'", token);

        token = lexer.peek();
        while (!token.hasTag(SEMI, CURVED_CLOSED)) {
            TCStatementTree stmtNode = parseStatement();
            if (stmtNode != null)
                statements.add(stmtNode);

            token = lexer.peek();
        }

        if (token.hasTag(EOF))
            error("missing '}'", token);

        lexer.consume();

        return F.BlockTree(location, statements);
    }

    @Override
    public TCExpressionTree parseExpression() {
        return parseExpression(parsePrimaryExpression(true), 0, true);

    }

    private TCExpressionTree parseExpression(TCExpressionTree lhs, int minPrecedence, boolean allowRange){
        Token<TscriptTokenType> lookahead = lexer.peek();

        while (isBinaryOperator(lookahead) && precedenceOf(lookahead) >= minPrecedence){
            final Token<TscriptTokenType> op = lexer.consume();
            TCExpressionTree rhs = unwrap(parsePrimaryExpression(true), op);
            lookahead = lexer.peek();

            while (isBinaryOperator(lookahead)
                    && precedenceOf(lookahead) > precedenceOf(op)){
                int offs = precedenceOf(lookahead) > precedenceOf(op) ? 1 : 0;
                rhs = parseExpression(rhs, precedenceOf(op) + offs, allowRange);
                lookahead = lexer.peek();
            }

            lhs = TscriptPrecedenceCalculator.apply(F, op, lhs, rhs);
        }

        return lhs;
    }

    private boolean isBinaryOperator(Token<TscriptTokenType> token){
        return TscriptPrecedenceCalculator.isBinaryOperator(token);
    }

    private int precedenceOf(Token<TscriptTokenType> token){
        return TscriptPrecedenceCalculator.calculate(token);
    }

    private TCExpressionTree parsePrimaryExpression(boolean allowRange){
        TCExpressionTree expNode = null;

        Token<TscriptTokenType> token = lexer.peek();

        if (token.hasTag(INTEGER)) {
            expNode = F.IntegerTree(lexer.consume().getLocation(), Integer.parseInt(token.getLexeme()));
        }
        if (token.hasTag(FLOAT)) {
            expNode = F.FloatTree(lexer.consume().getLocation(), Double.parseDouble(token.getLexeme()));
        }
        else if (token.hasTag(STRING)){
            expNode = F.StringTree(lexer.consume().getLocation(), token.getLexeme());
        }
        else if (token.hasTag(NULL)){
            expNode = F.NullTree(lexer.consume().getLocation());
        }
        else if (token.hasTag(TRUE, FALSE)){
            expNode = F.BooleanTree(lexer.consume().getLocation(), Boolean.parseBoolean(token.getLexeme()));
        }
        else if (token.hasTag(IDENTIFIER)){
            expNode = F.VariableTree(lexer.consume().getLocation(), token.getLexeme());
        }
        else if (token.hasTag(THIS)){
            expNode = F.ThisTree(lexer.consume().getLocation());
        }
        else if (token.hasTag(FUNCTION)){
            expNode = parseLambda();
        }
        else if (token.hasTag(BRACKET_OPEN)){
            expNode = parseArray();
        }
        else if (token.hasTag(CURVED_OPEN)){
            expNode = parseDictionary();
        }
        else if (token.hasTag(NOT)){
            lexer.consume();
            expNode = F.NotTree(token.getLocation(), unwrap(parsePrimaryExpression(true), token));
        }
        else if (token.hasTag(SUPER)){
            lexer.consume();
            Token<TscriptTokenType> memberToken = lexer.peek();
            if (!memberToken.hasTag(DOT))
                error("missing '.'", memberToken);
            lexer.consume();
            memberToken = lexer.peek();
            if (!memberToken.hasTag(IDENTIFIER))
                error("identifier expected", memberToken);
            expNode = F.SuperTree(token.getLocation(), memberToken.getLexeme());
            lexer.consume();
        }
        else if (token.hasTag(PLUS) || token.hasTag(MINUS)){
            lexer.consume();

            TCExpressionTree op = unwrap(parsePrimaryExpression(true), token);
            boolean isNegation = token.getTag() == MINUS;
            expNode = F.SignTree(token.getLocation(), isNegation, op);
        }
        else if (token.hasTag(TYPEOF)){
            lexer.consume();
            expNode = F.GetTypeTree(token.getLocation(), unwrap(parsePrimaryExpression(true), token));
        }
        else if (token.hasTag(PARENTHESES_OPEN)){
            lexer.consume();
            expNode = parseExpression();
            if (expNode == null)
                error("expression expected", token);
            token = lexer.consume();
            if (!token.hasTag(PARENTHESES_CLOSED))
                error("missing ')'", token);
        }


        while (expNode != null) {
            token = lexer.peek();

            if (token.hasTag(PARENTHESES_OPEN)) {
                expNode = parseFunctionCall(expNode);
                continue;
            }

            else if (token.hasTag(COLON) && allowRange) {
                expNode = F.RangeTree(lexer.consume().getLocation(), expNode, unwrap(parseExpression(), token));
                continue;
            }

            else if (token.hasTag(DOT)){
                lexer.consume();
                token = lexer.consume();
                if (!token.hasTag(IDENTIFIER))
                    error("identifier expected", token);
                expNode = F.MemberAccessTree(token.getLocation(), expNode, token.getLexeme());
                continue;
            }

            else if (token.hasTag(BRACKET_OPEN)) {
                lexer.consume();
                TCExpressionTree key = unwrap(parseExpression(), token);
                expNode = F.ContainerAccessTree(token.getLocation(), expNode, key);
                token = lexer.consume();
                if (!token.hasTag(BRACKET_CLOSED))
                    error("missing ']'", token);
                continue;
            }

            break;
        }

        return expNode;
    }

    private TCLambdaTree parseLambda() {
        Location location = lexer.consume().getLocation();

        List<TCClosureTree> closures = new ArrayList<>();
        List<TCParameterTree> parameters = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.consume();

        if (token.hasTag(BRACKET_OPEN)) {
            token = lexer.peek();
            if (!token.hasTag(BRACKET_CLOSED, EOF)) {
                do {
                    TCParameterTree param = parseParam();
                    TCExpressionTree init = param.defaultValue;
                    if (init == null){
                        init = F.VariableTree(param.location, param.name);
                    }
                    closures.add(F.ClosureTree(param.getLocation(), param.getName(), init));

                    token = lexer.peek();
                    if (token.hasTag(COMMA)) {
                        lexer.consume();
                        continue;
                    }

                    break;
                } while (true);
            }
            lexer.consume();
            if (token.hasTag(EOF) || !token.hasTag(BRACKET_CLOSED))
                error("missing ']'", token);
            token = lexer.consume();
        }

        if (!token.hasTag(PARENTHESES_OPEN))
            error("missing '('", token);

        token = lexer.peek();
        if (!token.hasTag(PARENTHESES_CLOSED, EOF)){
            do {
                TCParameterTree param = parseParam();
                parameters.add(param);

                token = lexer.peek();
                if (token.hasTag(COMMA)){
                    lexer.consume();
                    continue;
                }

                break;
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(EOF) || !token.hasTag(PARENTHESES_CLOSED))
            error("missing ')'", token);

        TCBlockTree block = parseBlock();

        TCReturnTree returnNode = F.ReturnTree(location, F.NullTree(location));

        List<TCStatementTree> statements = new ArrayList<>(block.statements);
        statements.add(returnNode);

        return F.LambdaTree(location, closures, parameters, F.BlockTree(block.getLocation(), statements));
    }

    private TCArrayTree parseArray() {
        Location location = lexer.consume().getLocation();
        List<TCExpressionTree> content = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.peek();

        if (!token.hasTag(BRACKET_CLOSED)){
            do {
                token = lexer.peek();

                TCExpressionTree arg = unwrap(parseExpression(), token);
                content.add(arg);

                token = lexer.peek();
                if (token.hasTag(COMMA)){
                    lexer.consume();
                    continue;
                }

                break;
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(EOF) || !token.hasTag(BRACKET_CLOSED))
            error("missing ']'", token);

        return F.ArrayTree(location, content);
    }

    private TCDictionaryTree parseDictionary() {
        Location location = lexer.consume().getLocation();
        List<TCExpressionTree> keys = new ArrayList<>();
        List<TCExpressionTree> values = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.peek();

        if (!token.hasTag(CURVED_CLOSED)){
            do {
                token = lexer.peek();

                TCExpressionTree key = unwrap(parsePrimaryExpression(false), token);
                token = lexer.consume();
                if (!token.hasTag(COLON))
                    error("missing ':'", token);

                TCExpressionTree value = unwrap(parsePrimaryExpression(false), token);

                keys.add(key);
                values.add(value);

                token = lexer.peek();
                if (token.hasTag(COMMA)){
                    lexer.consume();
                    continue;
                }

                break;
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(EOF) || !token.hasTag(CURVED_CLOSED))
            error("missing '}'", token);

        return F.DictionaryTree(location, keys, values);
    }

    private TCCallTree parseFunctionCall(TCExpressionTree called){
        Location location = lexer.consume().getLocation();

        List<TCArgumentTree> arguments = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.peek();

        if (!token.hasTag(PARENTHESES_CLOSED)){
            do {

                TCArgumentTree arg = parseArgument();
                arguments.add(arg);

                token = lexer.peek();
                if (token.hasTag(COMMA)){
                    lexer.consume();
                    continue;
                }

                break;
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(EOF)|| !token.hasTag(PARENTHESES_CLOSED))
            error("missing ')'", token);

        return F.CallTree(location, called, arguments);
    }

    private TCArgumentTree parseArgument(){
        String ref = null;
        Token<TscriptTokenType> token = lexer.peek();

        if (token.hasTag(IDENTIFIER))
        {
            Token<TscriptTokenType> dummy = lexer.consume();
            token = lexer.peek();
            if (token.hasTag(EQ_ASSIGN)) {
                ref = dummy.getLexeme();
                lexer.consume();
                token = lexer.peek();
            }
            else {
                lexer.pushBack(dummy);
                token = dummy;
            }
        }

        TCExpressionTree exp = unwrap(parseExpression(), token);
        return F.ArgumentTree(exp.getLocation(), ref, exp);
    }

    private void parseEOS(){
        Token<TscriptTokenType> token = lexer.peek();
        if (!token.hasTag(SEMI))
            error("missing ';'", token);
        else
            lexer.consume();
    }

}
