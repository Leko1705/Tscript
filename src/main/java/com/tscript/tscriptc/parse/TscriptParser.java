package com.tscript.tscriptc.parse;

import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.tscript.tscriptc.parse.TscriptTokenType.*;

public class TscriptParser implements Parser {

    private final Lexer<TscriptTokenType> lexer;
    private final TreeFactory F;

    public TscriptParser(Lexer<TscriptTokenType> lexer, TreeFactory treeFactory) {
        this.lexer = lexer;
        this.F = treeFactory;
    }

    private void error(String msg, Token<TscriptTokenType> token) {
        throw new CompileException(msg, token.getLocation(), Phase.PARSING);
    }

    private ExpressionTree unwrap(ExpressionTree exp, Token<TscriptTokenType> token) {
        if (exp == null)
            error("expression expected", token);
        return exp;
    }

    @Override
    public RootTree parseProgram() {
        List<DefinitionTree> definitions = new ArrayList<>();
        List<StatementTree> statements = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.peek();
        while (!token.hasTag(EOF)) {

            DefinitionTree def = parseDefinition();
            if (def != null) {
                definitions.add(def);
                token = lexer.peek();
                continue;
            }
            StatementTree stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }

            token = lexer.peek();
        }

        return F.RootTree(null, definitions, statements);
    }

    private DefinitionTree parseDefinition() {
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
        else if (token.hasTag(ABSTRACT)) {
            lexer.consume();
            return parseClass(Modifier.ABSTRACT);
        }
        else if (token.hasTag(CONST)) {
            final Token<TscriptTokenType> candidate = lexer.consume();

            token = lexer.peek();
            if (token.hasTag(ABSTRACT)){
                lexer.consume();
                return parseClass(Modifier.CONSTANT, Modifier.ABSTRACT);
            }
            else if (token.hasTag(CLASS)){
                lexer.consume();
                return parseClass(Modifier.CONSTANT);
            }
            else {
                lexer.pushBack(candidate);
            }
        }
        else if (token.hasTag(NAMESPACE)) {
            return parseNamespace();
        }

        return null;
    }

    private NamespaceTree parseNamespace() {
        Location location = lexer.consume().getLocation();
        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(IDENTIFIER))
            error("identifier expected", token);

        String name = token.getLexeme();

        List<DefinitionTree> definitions = new ArrayList<>();

        token = lexer.consume();
        if (!token.hasTag(CURVED_OPEN))
            error("missing '{'", token);

        token = lexer.peek();

        if (!token.hasTag(CURVED_CLOSED)) {
            do {
                DefinitionTree def = parseDefinition();
                if (def == null) break;
                definitions.add(def);
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(EOF) || !token.hasTag(CURVED_CLOSED))
            error("missing '}'", token);

        return F.NamespaceTree(location, F.ModifiersTree(location, Set.of()), name, definitions, new ArrayList<>());
    }

    private ClassTree parseClass(Modifier... modifiers){
        Location location = lexer.consume().getLocation();
        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(IDENTIFIER))
            error("identifier expected", token);

        String name = token.getLexeme();
        String superName = null;

        token = lexer.peek();
        if (token.hasTag(COLON)){
            lexer.consume();
            token = lexer.consume();
            if (!token.hasTag(IDENTIFIER))
                error("identifier expected", token);
            superName = token.getLexeme();
        }
        return parseClassBody(location, Set.of(modifiers), name, superName);
    }

    private ClassTree parseClassBody(Location location, Set<Modifier> classModifiers,
                                     String className, String superName){

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(CURVED_OPEN))
            error("missing '{'", token);


        ConstructorTree constructor = null;
        Modifier visibility = null;
        boolean isStatic = false;
        boolean isOverridden = false;
        List<ClassMemberTree> members = new ArrayList<>();
        ClassMemberTree defTree = null;


        token = lexer.peek();
        if (!token.hasTag(CURVED_CLOSED, EOF)) {
            do {
                if (visibility == null || isVisibility(token.getTag())) {
                    visibility = parseVisibility();
                    continue;
                }
                else if (token.hasTag(STATIC)){
                    if (isOverridden) {
                        error("static functions can not be overridden", token);
                        continue;
                    }
                    isStatic = true;
                    lexer.consume();
                    continue;
                }
                else if (token.hasTag(OVERRIDDEN)){
                    if (isStatic) {
                        error("static functions can not be overridden", token);
                        continue;
                    }
                    isOverridden = true;
                    lexer.consume();
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
                else if (token.hasTag(NATIVE)){
                    if (isOverridden) defTree = parseDeclaredFunction(visibility, Modifier.NATIVE, Modifier.OVERRIDDEN);
                    else if (isStatic) defTree = parseFunctionDef(visibility, Modifier.NATIVE, Modifier.STATIC);
                    else defTree = parseFunctionDef(visibility, Modifier.NATIVE);
                }
                else if (token.hasTag(ABSTRACT)){
                    if (isOverridden) defTree = parseDeclaredFunction(visibility, Modifier.ABSTRACT, Modifier.OVERRIDDEN);
                    else if (isStatic) defTree = parseFunctionDef(visibility, Modifier.ABSTRACT, Modifier.STATIC);
                    else defTree = parseFunctionDef(visibility, Modifier.ABSTRACT);
                }
                else if (token.hasTag(CONSTRUCTOR)){

                    if (constructor != null)
                        error("can not have multiple constructors in class", token);
                    else
                        constructor = parseConstructor(visibility);

                    if (isStatic){
                        error("constructor can not be static", token);
                    }
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
        return F.ClassTree(location, F.ModifiersTree(location, classModifiers), className, superName, constructor, members);
    }

    private ConstructorTree parseConstructor(Modifier... modifiers){
        Location location = lexer.consume().getLocation();

        List<ParameterTree> parameters = new ArrayList<>();
        List<ArgumentTree> superArgs = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(PARENTHESES_OPEN))
            error("missing '('", token);

        token = lexer.peek();
        if (!token.hasTag(PARENTHESES_CLOSED, EOF)){
            do {
                ParameterTree param = parseParam();
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
                    ParameterTree param = parseParam();
                    ArgumentTree arg = F.ArgumentTree(param.getLocation(), param.getName(), param.getDefaultValue());
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

        BlockTree body = parseBlock();

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

    private FunctionTree parseFunctionDef(Modifier... modifiers){

        Location location = lexer.consume().getLocation();
        Token<TscriptTokenType> token = lexer.consume();

        if (!token.hasTag(IDENTIFIER))
            error("identifier expected", token);

        String name = token.getLexeme();

        List<ParameterTree> parameters = new ArrayList<>();

        token = lexer.consume();
        if (!token.hasTag(PARENTHESES_OPEN))
            error("missing '('", token);

        token = lexer.peek();
        if (!token.hasTag(PARENTHESES_CLOSED, EOF)){
            do {
                ParameterTree param = parseParam();
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

        BlockTree body = parseBlock();

        ReturnTree returnNode = F.ReturnTree(location, F.NullTree(location));
        List<StatementTree> statements = new ArrayList<>(body.getStatements());
        statements.add(returnNode);
        body = F.BlockTree(body.getLocation(), statements);

        return F.FunctionTree(location, F.ModifiersTree(location, Set.of(modifiers)), name, parameters, body);
    }

    private FunctionTree parseDeclaredFunction(Modifier... modifiers){
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

    private ParameterTree parseParam(){
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
        ExpressionTree initializer = null;

        token = lexer.peek();
        if (token.hasTag(EQ_ASSIGN)){
            token = lexer.consume();
            initializer = unwrap(parseExpression(), token);
        }

        return F.ParameterTree(location, name, F.ModifiersTree(location, modifiers), initializer);
    }

    @Override
    public StatementTree parseStatement() {
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
            BreakTree breakTree = F.BreakTree(lexer.consume().getLocation());
            parseEOS();
            return breakTree;
        }
        else if (token.hasTag(CONTINUE)){
            ContinueTree continueTree = F.ContinueTree(lexer.consume().getLocation());
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
        else {
            ExpressionTree exp = parseExpression();
            if (exp == null)
                error("not a statement", token);
            else {
                parseEOS();
                return F.ExpressionStatementTree(exp.getLocation(), exp);
            }
        }


        return null;
    }

    private VarDefsTree parseVarDec(Modifier... modifiers){

        List<VarDefTree> varDefs = new ArrayList<>();
        Location location = lexer.peek().getLocation();

        do {
            lexer.consume();
            Token<TscriptTokenType> ident = lexer.consume();
            if (!ident.hasTag(IDENTIFIER))
                error("identifier expected", ident);

            Location varLocation = ident.getLocation();
            String name = ident.getLexeme();
            ExpressionTree initializer = null;

            Token<TscriptTokenType> next = lexer.peek();
            if (!next.hasTag(SEMI)) {
                next = lexer.consume();
                if (!next.hasTag(EQ_ASSIGN))
                    error("'=' expected", next);
                initializer = unwrap(parseExpression(), next);
            }

            VarDefTree varDefTree = F.VarDefTree(varLocation, name, initializer);
            varDefs.add(varDefTree);

        } while (lexer.peek().hasTag(COMMA));

        return F.VarDefsTree(location, F.ModifiersTree(location, Set.of(modifiers)), varDefs);
    }

    private IfElseTree parseIfElse(){
        Location location = lexer.consume().getLocation();

        ExpressionTree condition = unwrap(parseExpression(), lexer.peek());

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(THEN))
            error("missing keyword 'then'", token);

        StatementTree ifBody = parseStatement();
        StatementTree elseBody = null;

        if (lexer.peek().hasTag(ELSE)){
            lexer.consume();
            elseBody = parseStatement();
        }

        return F.IfElseTree(location, condition, ifBody, elseBody);
    }

    private DoWhileTree parseDoWhile(){
        Location location = lexer.consume().getLocation();

        StatementTree body = parseStatement();

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(WHILE))
            error("missing keyword 'while'", token);

        ExpressionTree condition = unwrap(parseExpression(), lexer.peek());
        parseEOS();
        return F.DoWhileTree(location, body, condition);
    }

    private WhileDoTree parseWhileDo(){
        Location location = lexer.consume().getLocation();

        ExpressionTree condition = unwrap(parseExpression(), lexer.peek());

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(DO))
            error("missing keyword 'do'", token);

        StatementTree body = parseStatement();

        return F.WhileDoTree(location, condition, body);
    }

    private ForLoopTree parseForLoop(){
        Token<TscriptTokenType> loopToken = lexer.peek();
        final Location location = loopToken.getLocation();

        lexer.consume();
        Token<TscriptTokenType> token = lexer.peek();

        VarDefTree runVar;
        ExpressionTree iterable;

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
            ExpressionTree first = unwrap(parseExpression(), token);
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

        StatementTree body = parseStatement();

        return F.ForLoopTree(location, runVar, iterable, body);
    }

    private ReturnTree parseReturn(){
        Location location = lexer.consume().getLocation();
        ExpressionTree returned;

        if (lexer.peek().hasTag(SEMI)){
            returned = F.NullTree(lexer.consume().getLocation());
        }
       else {
           returned = unwrap(parseExpression(), lexer.peek());
            parseEOS();
        }

        return F.ReturnTree(location, returned);
    }

    private ThrowTree parseThrow(){
        ExpressionTree thrown = unwrap(parseExpression(), lexer.peek());
        parseEOS();
        return F.ThrowTree(lexer.consume().getLocation(), thrown);
    }

    private TryCatchTree parseTryCatch(){
        Location location = lexer.consume().getLocation();

        StatementTree tryBody = parseStatement();
        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(CATCH))
            error("missing keyword 'catch'", token);
        token = lexer.consume();
        if (!token.hasTag(VAR, CONST))
            error("missing keyword 'var' or 'const'", token);

        token = lexer.consume();
        if (!token.hasTag(IDENTIFIER))
            error("identifier expected", token);

        VarDefTree exVarDef = F.VarDefTree(token.getLocation(), token.getLexeme(), null);

        token = lexer.consume();
        if (!token.hasTag(DO))
            error("missing keyword 'do'", token);

        StatementTree catchBody = parseStatement();

        return F.TryCatchTree(location, tryBody, exVarDef, catchBody);
    }

    private BlockTree parseBlock(){
        Location location = lexer.peek().getLocation();
        List<StatementTree> statements = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.consume();
        if (!token.hasTag(CURVED_OPEN))
            error("missing '{'", token);

        token = lexer.peek();
        while (!token.hasTag(SEMI, CURVED_CLOSED)) {
            StatementTree stmtNode = parseStatement();
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
    public ExpressionTree parseExpression() {
        return parseExpression(parsePrimaryExpression(), 0, true);

    }

    private ExpressionTree parseExpression(ExpressionTree lhs, int minPrecedence, boolean allowRange){
        Token<TscriptTokenType> lookahead = lexer.peek();

        while (isBinaryOperator(lookahead) && precedenceOf(lookahead) >= minPrecedence){
            final Token<TscriptTokenType> op = lexer.consume();
            ExpressionTree rhs = unwrap(parsePrimaryExpression(), op);
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

    private ExpressionTree parsePrimaryExpression(){
        ExpressionTree expNode = null;

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
            expNode = F.NotTree(token.getLocation(), unwrap(parseExpression(), token));
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

            ExpressionTree op = unwrap(parseExpression(), token);
            boolean isNegation = token.getTag() == MINUS;
            expNode = F.SignTree(token.getLocation(), isNegation, op);
        }
        else if (token.hasTag(TYPEOF)){
            lexer.consume();
            expNode = F.GetTypeTree(token.getLocation(), unwrap(parseExpression(), token));
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

            else if (token.hasTag(COLON)) {
                expNode = F.RangeTree(lexer.consume().getLocation(), expNode, unwrap(parseExpression(), token));
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
                ExpressionTree key = unwrap(parseExpression(), token);
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

    private LambdaTree parseLambda() {
        Location location = lexer.consume().getLocation();

        List<ClosureTree> closures = new ArrayList<>();
        List<ParameterTree> parameters = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.consume();

        if (token.hasTag(BRACKET_OPEN)) {
            token = lexer.peek();
            if (!token.hasTag(BRACKET_CLOSED, EOF)) {
                do {
                    ParameterTree param = parseParam();
                    closures.add(F.ClosureTree(param.getLocation(), param.getName(), param.getDefaultValue()));

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
                ParameterTree param = parseParam();
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

        BlockTree block = parseBlock();

        ReturnTree returnNode = F.ReturnTree(location, F.NullTree(location));

        List<StatementTree> statements = new ArrayList<>(block.getStatements());
        statements.add(returnNode);

        return F.LambdaTree(location, closures, parameters, F.BlockTree(block.getLocation(), statements));
    }

    private ArrayTree parseArray() {
        Location location = lexer.consume().getLocation();
        List<ExpressionTree> content = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.peek();

        if (!token.hasTag(BRACKET_CLOSED)){
            do {
                token = lexer.peek();

                ExpressionTree arg = unwrap(parseExpression(), token);
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

    private DictionaryTree parseDictionary() {
        Location location = lexer.consume().getLocation();
        List<ExpressionTree> keys = new ArrayList<>();
        List<ExpressionTree> values = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.peek();

        if (!token.hasTag(CURVED_CLOSED)){
            do {
                token = lexer.peek();

                ExpressionTree key = unwrap(parsePrimaryExpression(), token);
                token = lexer.consume();
                if (!token.hasTag(COLON))
                    error("missing ':'", token);

                ExpressionTree value = unwrap(parseExpression(), token);

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

    private CallTree parseFunctionCall(ExpressionTree exp){
        Location location = lexer.consume().getLocation();

        List<ArgumentTree> arguments = new ArrayList<>();

        Token<TscriptTokenType> token = lexer.peek();

        if (!token.hasTag(PARENTHESES_CLOSED)){
            do {
                String ref = null;
                token = lexer.peek();

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

                exp = unwrap(parseExpression(), token);
                ArgumentTree arg = F.ArgumentTree(exp.getLocation(), ref, exp);
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

        return F.CallTree(location, exp, arguments);
    }

    private void parseEOS(){
        Token<TscriptTokenType> token = lexer.peek();
        if (!token.hasTag(SEMI))
            error("missing ';'", token);
        else
            lexer.consume();
    }

}
