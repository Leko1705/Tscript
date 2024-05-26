package tscriptc.parse;

import tscriptc.log.Logger;
import tscriptc.tree.*;
import tscriptc.util.Diagnostics;
import tscriptc.util.Location;
import tscriptc.util.Phase;

import java.util.*;

public class TscriptParser implements Parser {

    private final Lexer lexer;
    private final Logger log;

    private final Set<Integer> breakPoints;

    public TscriptParser(Lexer lexer, Logger logger, Set<Integer> breakPoints) {
        this.lexer = lexer;
        this.log = logger;
        this.breakPoints = breakPoints;
    }

    private void error(String msg, Token token) {
        log.error(new Diagnostics.Error(msg, token.getLocation(), Phase.PARSING));
    }

    private ExpressionTree unwrap(ExpressionTree exp, Token token) {
        if (exp == null)
            error("expression expected", token);
        return exp;
    }

    @Override
    public RootTree parseProgram() {

        RootTree rootTree = new Trees.BasicRootTree();
        rootTree.getDefinitions().addAll(Builtins.getBuiltins());

        Token token = lexer.peek();
        while (!token.hasTag(TokenKind.EOF)) {

            DefinitionTree def = parseDefinition();
            if (def != null) {
                rootTree.getDefinitions().add(def);
                token = lexer.peek();
                continue;
            }
            StatementTree stmt = parseStatement();
            if (stmt != null) {
                rootTree.getStatements().add(stmt);
                if (requireBreakPoint(token))
                    rootTree.getStatements().add(new Trees.BasicBreakPointTree());
            }

            token = lexer.peek();
        }

        return rootTree;
    }

    private DefinitionTree parseDefinition() {
        Token token = lexer.peek();

        if (token.hasTag(TokenKind.FUNCTION)) {
            return parseFunctionDef();
        } else if (token.hasTag(TokenKind.NATIVE)) {
            return parseNativeFunctionDef();
        } else if (token.hasTag(TokenKind.CLASS)) {
            return parseClass();
        } else if (token.hasTag(TokenKind.ABSTRACT)) {
            lexer.consume();
            ClassTree classTree = parseClass();
            classTree.getModifiers().add(Modifier.ABSTRACT);
            return classTree;
        } else if (token.hasTag(TokenKind.NAMESPACE)) {
            return parseNamespace();
        }

        return null;
    }

    private NamespaceTree parseNamespace() {
        lexer.consume();
        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.IDENTIFIER))
            error("identifier expected", token);
        Trees.BasicNamespaceTree nTree = new Trees.BasicNamespaceTree(token.getLocation(), token.getLexem());
        token = lexer.consume();
        if (!token.hasTag(TokenKind.CURVED_OPEN))
            error("missing '{'", token);

        token = lexer.peek();

        if (!token.hasTag(TokenKind.CURVED_CLOSED)) {
            do {
                DefinitionTree def = parseDefinition();
                if (def == null) break;
                nTree.getDefinitions().add(def);
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(TokenKind.EOF) || !token.hasTag(TokenKind.CURVED_CLOSED))
            error("missing '}'", token);

        return nTree;
    }

    private ClassTree parseClass(){
        lexer.consume();
        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.IDENTIFIER))
            error("identifier expected", token);
        Trees.BasicClassTree classNode = new Trees.BasicClassTree(token.getLocation(), token.getLexem());

        token = lexer.peek();
        if (token.hasTag(TokenKind.COLON)){
            lexer.consume();
            token = lexer.consume();
            if (!token.hasTag(TokenKind.IDENTIFIER))
                error("identifier expected", token);
            classNode.superName = token.getLexem();
        }
        return parseClassBody(classNode);
    }

    private ClassTree parseClassBody(Trees.BasicClassTree classTree){
        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.CURVED_OPEN))
            error("missing '{'", token);

        token = lexer.peek();
        TokenKind visibility = null;

        boolean isStatic = false;

        DefinitionTree defTree = null;

        if (!token.hasTag(TokenKind.CURVED_CLOSED, TokenKind.EOF)) {
            do {

                if (visibility == null || isVisibility((TokenKind) token.getTag())) {
                    visibility = parseVisibility();
                }
                else if (token.hasTag(TokenKind.STATIC)){
                    isStatic = true;
                    lexer.consume();
                }
                else if (token.hasTag(TokenKind.VAR, TokenKind.CONST)) {
                    lexer.consume();
                    defTree = parseVarDec(token.getTag() == TokenKind.CONST);
                }
                else if (token.hasTag(TokenKind.FUNCTION)) {
                    defTree = parseFunctionDef();
                }
                else if (token.hasTag(TokenKind.NATIVE)){
                    defTree = parseNativeFunctionDef();
                }
                else if (token.hasTag(TokenKind.ABSTRACT)){
                    defTree = parseAbstractMethodDef();
                }
                else if (token.hasTag(TokenKind.CONSTRUCTOR)){
                    if (classTree.getConstructor() != null)
                        error("can not have multiple constructors in class", token);
                    else
                        classTree.constructor = parseConstructor();
                    if (classTree.constructor != null) {
                        classTree.constructor.getModifiers().add(Modifier.of(visibility.name));
                        if (isStatic)
                            error("constructor can not be static", token);
                    }
                }
                else {
                    error("class member definition expected", token);
                }

                if (defTree != null) {
                    Set<Modifier> modifiers = new HashSet<>();
                    if (isStatic) modifiers.add(Modifier.STATIC);
                    modifiers.add(Modifier.of(visibility.name));

                    if (defTree instanceof MultiVarDecTree m)
                        applyClassMemberModifiers(classTree, modifiers, m.getDeclarations());
                    else
                        applyClassMemberModifiers( classTree, modifiers, List.of(defTree));

                    if (isStatic) isStatic = false;
                }

                token = lexer.peek();
                defTree = null;

            } while (!token.hasTag(TokenKind.CURVED_CLOSED, TokenKind.EOF));
        }

        if (!token.hasTag(TokenKind.CURVED_CLOSED))
            error("missing '}'", token);

        lexer.consume();
        return classTree;
    }

    private void applyClassMemberModifiers(ClassTree classTree, Set<Modifier> modifiers, Collection<? extends DefinitionTree> defs){
        for (DefinitionTree definitionTree : defs) {
            definitionTree.getModifiers().addAll(modifiers);
            classTree.getDefinitions().add(definitionTree);
        }
    }

    private ConstructorTree parseConstructor(){
        Trees.BasicConstructorTree constructor = new Trees.BasicConstructorTree(lexer.consume().getLocation());

        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.PARENTHESES_OPEN))
            error("missing '('", token);

        token = lexer.peek();
        if (!token.hasTag(TokenKind.PARENTHESES_CLOSED, TokenKind.EOF)){
            do {
                ParameterTree param = parseParam();
                constructor.parameters.add(param);

                token = lexer.peek();
                if (token.hasTag(TokenKind.COMMA)){
                    lexer.consume();
                    continue;
                }

                break;
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(TokenKind.EOF) || !token.hasTag(TokenKind.PARENTHESES_CLOSED))
            error("missing ')'", token);

        token = lexer.peek();

        if (token.hasTag(TokenKind.COLON)){
            lexer.consume();
            token = lexer.consume();
            if (!token.hasTag(TokenKind.SUPER))
                error("missing keyword 'super'", token);
            token = lexer.consume();
            if (!token.hasTag(TokenKind.PARENTHESES_OPEN))
                error("missing '('", token);

            token = lexer.peek();

            if (!token.hasTag(TokenKind.PARENTHESES_CLOSED)){
                do {
                    token = lexer.peek();
                    ExpressionTree exp = unwrap(parseExpression(), token);
                    Trees.BasicArgumentTree arg = new Trees.BasicArgumentTree(exp.getLocation(), exp);
                    constructor.superArguments.add(arg);

                    token = lexer.peek();
                    if (token.hasTag(TokenKind.COMMA)){
                        lexer.consume();
                        continue;
                    }

                    break;
                } while (true);
            }

            token = lexer.consume();
            if (token.hasTag(TokenKind.EOF)|| !token.hasTag(TokenKind.PARENTHESES_CLOSED))
                error("missing ')'", token);
        }

        token = lexer.peek();
        boolean requireBreakPoint = requireBreakPoint(token);
        constructor.body = parseBlock();
        if (requireBreakPoint) constructor.body.getStatements().add(0, new Trees.BasicBreakPointTree());

        return constructor;
    }

    private boolean isVisibility(TokenKind type){
        return type == TokenKind.PUBLIC
                || type == TokenKind.PRIVATE
                || type == TokenKind.PROTECTED;
    }

    private TokenKind parseVisibility(){
        Token token = lexer.consume();
        TokenKind visibility = (TokenKind) token.getTag();
        if (!isVisibility(visibility))
            error("missing visibility", token);

        token = lexer.consume();
        if (!token.hasTag(TokenKind.COLON))
            error("missing ':'", token);
        return visibility;
    }

    private FunctionTree parseFunctionDef(){

        lexer.consume();
        Token token = lexer.consume();

        if (!token.hasTag(TokenKind.IDENTIFIER))
            error("identifier expected", token);

        Trees.BasicFunctionTree functionDefNode = new Trees.BasicFunctionTree(token.getLocation(), token.getLexem());

        token = lexer.consume();
        if (!token.hasTag(TokenKind.PARENTHESES_OPEN))
            error("missing '('", token);

        token = lexer.peek();
        if (!token.hasTag(TokenKind.PARENTHESES_CLOSED, TokenKind.EOF)){
            do {
                ParameterTree param = parseParam();
                functionDefNode.parameters.add(param);

                token = lexer.peek();
                if (token.hasTag(TokenKind.COMMA)){
                    lexer.consume();
                    continue;
                }

                break;
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(TokenKind.EOF) || !token.hasTag(TokenKind.PARENTHESES_CLOSED))
            error("missing ')'", token);

        token = lexer.peek();
        boolean requireBreakPoint = requireBreakPoint(token);
        functionDefNode.body = parseBlock();
        if (requireBreakPoint) functionDefNode.body.getStatements().add(0, new Trees.BasicBreakPointTree());

        Trees.BasicReturnTree returnNode = new Trees.BasicReturnTree(functionDefNode.location, new Trees.BasicNullLiteralTree(functionDefNode.location));
        functionDefNode.body.getStatements().add(returnNode);

        return functionDefNode;
    }

    private NativeFunctionTree parseNativeFunctionDef(){
        lexer.consume();
        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.FUNCTION))
            error("keyword 'function' expected", token);

        token = lexer.consume();
        if (!token.hasTag(TokenKind.IDENTIFIER))
            error("identifier expected", token);

        Trees.BasicNativeFunctionTree functionDefNode = new Trees.BasicNativeFunctionTree(token.getLocation(), token.getLexem());
        parseEOS();

        return functionDefNode;
    }

    private AbstractMethodTree parseAbstractMethodDef(){
        lexer.consume();
        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.FUNCTION))
            error("keyword 'function' expected", token);

        token = lexer.consume();
        if (!token.hasTag(TokenKind.IDENTIFIER))
            error("identifier expected", token);

        Trees.BasicAbstractMethodTree functionDefNode = new Trees.BasicAbstractMethodTree(token.getLocation(), token.getLexem());
        parseEOS();

        return functionDefNode;
    }

    private ParameterTree parseParam(){
        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.IDENTIFIER))
            error("identifier expected", token);

        Trees.BasicParameterTree paramNode = new Trees.BasicParameterTree(token.getLocation(), false, token.getLexem());

        token = lexer.peek();
        if (token.hasTag(TokenKind.EQ_ASSIGN)){
            token = lexer.consume();
            paramNode.initializer = unwrap(parseExpression(), token);
        }

        return paramNode;
    }

    @Override
    public StatementTree parseStatement() {
        Token token = lexer.peek();

        if (token.hasTag(TokenKind.SEMI)){
            lexer.consume();
            return null;
        }

        else if (token.hasTag(TokenKind.VAR, TokenKind.CONST)){
            lexer.consume();
            return parseVarDec(token.getTag() == TokenKind.CONST);
        }
        else if (token.hasTag(TokenKind.IF)){
            return parseIfElse();
        }
        else if (token.hasTag(TokenKind.WHILE)){
            return parseWhileDo();
        }
        else if (token.hasTag(TokenKind.DO)){
            return parseDoWhile();
        }
        else if (token.hasTag(TokenKind.FOR)){
            return parseForLoop();
        }
        else if (token.hasTag(TokenKind.BREAK)){
            BreakTree breakTree = new Trees.BasicBreakTree(lexer.consume().getLocation());
            parseEOS();
            return breakTree;
        }
        else if (token.hasTag(TokenKind.CONTINUE)){
            ContinueTree continueTree = new Trees.BasicContinueTree(lexer.consume().getLocation());
            parseEOS();
            return continueTree;
        }
        else if (token.hasTag(TokenKind.RETURN)){
            return parseReturn();
        }
        else if (token.hasTag(TokenKind.CURVED_OPEN)){
            return parseBlock();
        }
        else if (token.hasTag(TokenKind.THROW)){
            return parseThrow();
        }
        else if (token.hasTag(TokenKind.TRY)){
            return parseTryCatch();
        }
        else if (token.hasTag(TokenKind.IMPORT)){
            return parseImport();
        }
        else if (token.hasTag(TokenKind.USE)){
            return parseUse();
        }
        else {
            ExpressionTree exp = parseExpression();
            if (exp == null)
                error("not a statement", token);
            else {
                parseEOS();
                return new Trees.BasicExpressionStatementTree(exp);
            }
        }


        return null;
    }

    private ImportTree parseImport(){
        Location location = lexer.consume().getLocation();
        StringBuilder sb = new StringBuilder();

        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.IDENTIFIER))
            error("identifier expected", token);
        sb.append(token.getLexem());

        token = lexer.consume();
        if (!token.hasTag(TokenKind.DOT))
            error("missing '.'", token);
        sb.append('.');

        token = lexer.consume();

        if (!token.hasTag(TokenKind.IDENTIFIER))
            error("identifier expected", token);
        sb.append(token.getLexem());

        while (lexer.peek().hasTag(TokenKind.DOT)){
            lexer.consume();
            sb.append('.');

            token = lexer.consume();
            if (!token.hasTag(TokenKind.IDENTIFIER))
                error("identifier expected", token);
            sb.append(token.getLexem());
        }

        parseEOS();

        String[] path = sb.toString().split("[.]");
        return new Trees.BasicImportTree(location, path);
    }

    private UseTree parseUse(){
        Token token = lexer.consume();
        Location location = token.getLocation();

        token = lexer.consume();
        if (!token.hasTag(TokenKind.IDENTIFIER))
            error("identifier expected", token);

        parseEOS();
        return new Trees.BasicUseTree(location, token.getLexem());
    }

    private MultiVarDecTree parseVarDec(boolean isConstant){

        List<VarDecTree> varDeclarations = new ArrayList<>();

        Token ident;
        do {
            ident = lexer.consume();
            if (!ident.hasTag(TokenKind.IDENTIFIER))
                error("identifier expected", ident);

            Trees.BasicVarDecTree varNode = new Trees.BasicVarDecTree(ident.getLocation(), isConstant, ident.getLexem());
            if (isConstant) varNode.getModifiers().add(Modifier.IMMUTABLE);

            ident = lexer.peek();
            if (ident.hasTag(TokenKind.EQ_ASSIGN)) {
                lexer.consume();
                varNode.initializer = unwrap(parseExpression(), ident);
            }

            varDeclarations.add(varNode);

            ident = lexer.peek();
            if (ident.hasTag(TokenKind.COMMA)) {
                lexer.consume();
                continue;
            }

            break;
        }while (true);

        parseEOS();

        return new Trees.BasicMultiVarDecTree(varDeclarations.get(0).getLocation(), varDeclarations);
    }

    private IfElseTree parseIfElse(){
        Trees.BasicIfElseTree ifElseNode = new Trees.BasicIfElseTree(lexer.consume().getLocation());
        ifElseNode.condition = unwrap(parseExpression(), lexer.peek());

        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.THEN))
            error("missing keyword 'then'", token);

        ifElseNode.ifBody = parseStatement();

        if (lexer.peek().hasTag(TokenKind.ELSE)){
            lexer.consume();
            ifElseNode.elseBody = parseStatement();
        }

        return ifElseNode;
    }

    private DoWhileTree parseDoWhile(){
        Trees.BasicDoWhileTree doWhileNode = new Trees.BasicDoWhileTree(lexer.consume().getLocation());
        doWhileNode.body = parseStatement();

        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.WHILE))
            error("missing keyword 'while'", token);

        doWhileNode.condition = unwrap(parseExpression(), lexer.peek());
        parseEOS();
        return doWhileNode;
    }

    private WhileDoTree parseWhileDo(){
        Trees.BasicWhileDoTree whileDoNode = new Trees.BasicWhileDoTree(lexer.consume().getLocation());

        whileDoNode.condition = unwrap(parseExpression(), lexer.peek());

        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.DO))
            error("missing keyword 'do'", token);

        whileDoNode.body = parseStatement();

        return whileDoNode;
    }

    private ForLoopTree parseForLoop(){
        Token loopToken = lexer.peek();
        lexer.consume();
        Token token = lexer.peek();
        Trees.BasicForLoopTree loopNode;

        if (token.hasTag(TokenKind.VAR)){
            lexer.consume();
            token = lexer.consume();
            if (!token.hasTag(TokenKind.IDENTIFIER))
                error("identifier expected", token);
            loopNode = new Trees.BasicForLoopTree(loopToken.getLocation(), true, token.getLexem());
            token = lexer.consume();
            if (!token.hasTag(TokenKind.IN))
                error("keyword 'in' expected", token);
            loopNode.iterable = unwrap(parseExpression(), lexer.peek());
        }
        else {
            ExpressionTree first = unwrap(parseExpression(), token);
            token = lexer.peek();
            if (token.hasTag(TokenKind.IN)){
                lexer.consume();
                String name = null;
                if (!(first instanceof IdentifierTree))
                    error("identifier or variable declaration expected", token);
                else
                    name = token.getLexem();
                ExpressionTree iterable = unwrap(parseExpression(), lexer.peek());
                loopNode = new Trees.BasicForLoopTree(loopToken.getLocation(), false, name, iterable);
            }
            else {
                loopNode = new Trees.BasicForLoopTree(loopToken.getLocation(), first);
            }
        }

        token = lexer.consume();
        if (!token.hasTag(TokenKind.DO))
            error("missing keyword 'do'", token);

        loopNode.body = parseStatement();

        return loopNode;
    }

    private ReturnTree parseReturn(){
        Trees.BasicReturnTree returnNode = new Trees.BasicReturnTree(lexer.consume().getLocation());
        if (lexer.peek().hasTag(TokenKind.SEMI)){
            returnNode.expression = new Trees.BasicNullLiteralTree(lexer.consume().getLocation());
            return returnNode;
        }
        returnNode.expression = unwrap(parseExpression(), lexer.peek());
        parseEOS();
        return returnNode;
    }

    private ThrowTree parseThrow(){
        Trees.BasicThrowTree throwTree =  new Trees.BasicThrowTree(lexer.consume().getLocation(), null);
        throwTree.thrown = unwrap(parseExpression(), lexer.peek());
        parseEOS();
        return throwTree;
    }

    private TryCatchTree parseTryCatch(){
        Trees.BasicTryCatchTree tryCatchTree = new Trees.BasicTryCatchTree(lexer.consume().getLocation());
        tryCatchTree.tryBody = parseStatement();
        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.CATCH))
            error("missing keyword 'catch'", token);
        token = lexer.consume();
        if (!token.hasTag(TokenKind.VAR))
            error("missing keyword 'var'", token);
        token = lexer.consume();
        if (!token.hasTag(TokenKind.IDENTIFIER))
            error("identifier expected", token);
        tryCatchTree.exVarName = token.getLexem();
        token = lexer.consume();
        if (!token.hasTag(TokenKind.DO))
            error("missing keyword 'do'", token);
        tryCatchTree.catchBody = parseStatement();
        return tryCatchTree;
    }

    private BlockTree parseBlock(){
        BlockTree streamNode = new Trees.BasicBlockTree();

        Token token = lexer.consume();
        if (!token.hasTag(TokenKind.CURVED_OPEN))
            error("missing '{'", token);

        token = lexer.peek();
        while (!token.hasTag(TokenKind.SEMI, TokenKind.CURVED_CLOSED)) {
            StatementTree stmtNode = parseStatement();
            if (stmtNode != null)
                streamNode.getStatements().add(stmtNode);

            if (requireBreakPoint(token))
                streamNode.getStatements().add(new Trees.BasicBreakPointTree());

            token = lexer.peek();
        }

        if (token.hasTag(TokenKind.EOF))
            error("missing '}'", token);

        lexer.consume();

        return streamNode;
    }

    @Override
    public ExpressionTree parseExpression() {
        return parseExpression(parsePrimaryExpression(true), 0, true);

    }

    private ExpressionTree parseExpression(boolean allowRange){
        return parseExpression(parsePrimaryExpression(allowRange), 0, false);
    }

    private ExpressionTree parseExpression(ExpressionTree lhs, int minPrecedence, boolean allowRange){
        Token lookahead = lexer.peek();

        while (isBinaryOperator(lookahead) && precedenceOf(lookahead) >= minPrecedence){
            final Token op = lexer.consume();
            ExpressionTree rhs = unwrap(parsePrimaryExpression(allowRange), op);
            lookahead = lexer.peek();

            while (isBinaryOperator(lookahead)
                    && precedenceOf(lookahead) > precedenceOf(op)){
                int offs = precedenceOf(lookahead) > precedenceOf(op) ? 1 : 0;
                rhs = parseExpression(rhs, precedenceOf(op) + offs, allowRange);
                lookahead = lexer.peek();
            }

            lhs = PrecedenceCalculator.apply(op, lhs, rhs);
        }

        return lhs;
    }

    private boolean isBinaryOperator(Token token){
        return PrecedenceCalculator.isBinaryOperator(token);
    }

    private int precedenceOf(Token token){
        return PrecedenceCalculator.calculate(token);
    }

    private ExpressionTree parsePrimaryExpression(boolean allowRange){
        ExpressionTree expNode = null;

        Token token = lexer.peek();

        if (token.hasTag(TokenKind.INTEGER)) {
            expNode = new Trees.BasicIntegerLiteralTree(lexer.consume().getLocation(), Integer.parseInt(token.getLexem()));
        }
        if (token.hasTag(TokenKind.FLOAT)) {
            expNode = new Trees.BasicFloatLiteralTree(lexer.consume().getLocation(), Double.parseDouble(token.getLexem()));
        }
        else if (token.hasTag(TokenKind.STRING)){
            expNode = new Trees.BasicStringLiteralTree(lexer.consume().getLocation(), token.getLexem());
        }
        else if (token.hasTag(TokenKind.NULL)){
            expNode = new Trees.BasicNullLiteralTree(lexer.consume().getLocation());
        }
        else if (token.hasTag(TokenKind.TRUE, TokenKind.FALSE)){
            expNode = new Trees.BasicBooleanLiteralTree(lexer.consume().getLocation(), Boolean.parseBoolean(token.getLexem()));
        }
        else if (token.hasTag(TokenKind.IDENTIFIER)){
            expNode = new Trees.BasicIdentifierTree(lexer.consume().getLocation(), token.getLexem());
        }
        else if (token.hasTag(TokenKind.THIS)){
            expNode = new Trees.BasicThisTree(lexer.consume().getLocation());
        }
        else if (token.hasTag(TokenKind.FUNCTION)){
            expNode = parseLambda();
        }
        else if (token.hasTag(TokenKind.BRACKET_OPEN)){
            expNode = parseArray();
        }
        else if (token.hasTag(TokenKind.CURVED_OPEN)){
            expNode = parseDictionary();
        }
        else if (token.hasTag(TokenKind.NOT)){
            lexer.consume();
            expNode = new Trees.BasicNotTree(token.getLocation(), unwrap(parsePrimaryExpression(true), token));
        }
        else if (token.hasTag(TokenKind.SUPER)){
            lexer.consume();
            Token memberToken = lexer.peek();
            if (!memberToken.hasTag(TokenKind.DOT))
                error("missing '.'", memberToken);
            lexer.consume();
            memberToken = lexer.peek();
            if (!memberToken.hasTag(TokenKind.IDENTIFIER))
                error("identifier expected", memberToken);
            expNode = new Trees.BasicSuperTree(token.getLocation(), memberToken.getLexem());
            lexer.consume();
        }
        else if (token.hasTag(TokenKind.PLUS) || token.hasTag(TokenKind.MINUS)){
            Trees.BasicSignTree signNode = new Trees.BasicSignTree(token.getLocation());
            lexer.consume();
            signNode.expression = unwrap(parsePrimaryExpression(true), token);
            signNode.isNegation = token.getTag() == TokenKind.MINUS;
            expNode = signNode;
        }
        else if (token.hasTag(TokenKind.TYPEOF)){
            lexer.consume();
            expNode = new Trees.BasicGetTypeTree(token.getLocation(), unwrap(parseExpression(), token));
        }
        else if (token.hasTag(TokenKind.PARENTHESES_OPEN)){
            lexer.consume();
            expNode = parseExpression();
            if (expNode == null)
                error("expression expected", token);
            token = lexer.consume();
            if (!token.hasTag(TokenKind.PARENTHESES_CLOSED))
                error("missing ')'", token);
        }


        while (expNode != null) {
            token = lexer.peek();

            if (token.hasTag(TokenKind.PARENTHESES_OPEN)) {
                expNode = parseFunctionCall(expNode);
                continue;
            }

            else if (token.hasTag(TokenKind.COLON) && allowRange) {
                expNode = new Trees.BasicRangeTree(lexer.consume().getLocation(), expNode, unwrap(parseExpression(), token));
            }

            else if (token.hasTag(TokenKind.DOT)){
                lexer.consume();
                token = lexer.consume();
                if (!token.hasTag(TokenKind.IDENTIFIER))
                    error("identifier expected", token);
                expNode = new Trees.BasicMemberAccessTree(token.getLocation(), expNode, token.getLexem());
                continue;
            }

            else if (token.hasTag(TokenKind.BRACKET_OPEN)) {
                lexer.consume();
                Trees.BasicContainerAccessTree accessNode = new Trees.BasicContainerAccessTree(token.getLocation());
                accessNode.expression = expNode;
                accessNode.key = unwrap(parseExpression(), token);
                expNode = accessNode;
                token = lexer.consume();
                if (!token.hasTag(TokenKind.BRACKET_CLOSED))
                    error("missing ']'", token);
                continue;
            }

            break;
        }

        return expNode;
    }

    private LambdaTree parseLambda() {
        Trees.BasicLambdaTree lambdaNode = new Trees.BasicLambdaTree(lexer.consume().getLocation());

        Token token = lexer.consume();

        if (token.hasTag(TokenKind.BRACKET_OPEN)) {
            token = lexer.peek();
            if (!token.hasTag(TokenKind.BRACKET_CLOSED, TokenKind.EOF)) {
                do {

                    ParameterTree param = parseParam();
                    Trees.BasicClosureTree closure = new Trees.BasicClosureTree(param.getLocation(), param.getName());
                    closure.expression = param.getInitializer();
                    lambdaNode.closures.add(closure);

                    token = lexer.peek();
                    if (token.hasTag(TokenKind.COMMA)) {
                        lexer.consume();
                        continue;
                    }

                    break;
                } while (true);
            }
            lexer.consume();
            if (token.hasTag(TokenKind.EOF) || !token.hasTag(TokenKind.BRACKET_CLOSED))
                error("missing ']'", token);
            token = lexer.consume();
        }

        if (!token.hasTag(TokenKind.PARENTHESES_OPEN))
            error("missing '('", token);

        token = lexer.peek();
        if (!token.hasTag(TokenKind.PARENTHESES_CLOSED, TokenKind.EOF)){
            do {
                ParameterTree param = parseParam();
                lambdaNode.parameters.add(param);

                token = lexer.peek();
                if (token.hasTag(TokenKind.COMMA)){
                    lexer.consume();
                    continue;
                }

                break;
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(TokenKind.EOF) || !token.hasTag(TokenKind.PARENTHESES_CLOSED))
            error("missing ')'", token);

        lambdaNode.body = parseBlock();

        ReturnTree returnNode =
                new Trees.BasicReturnTree(lambdaNode.getLocation(), new Trees.BasicNullLiteralTree(lambdaNode.getLocation()));
        lambdaNode.body.getStatements().add(returnNode);

        return lambdaNode;
    }

    private ArrayTree parseArray() {
        ArrayTree arrayNode = new Trees.BasicArrayTree(lexer.consume().getLocation());

        Token token = lexer.peek();

        if (!token.hasTag(TokenKind.BRACKET_CLOSED)){
            do {
                token = lexer.peek();

                ExpressionTree arg = unwrap(parseExpression(), token);
                arrayNode.getContent().add(arg);

                token = lexer.peek();
                if (token.hasTag(TokenKind.COMMA)){
                    lexer.consume();
                    continue;
                }

                break;
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(TokenKind.EOF) || !token.hasTag(TokenKind.BRACKET_CLOSED))
            error("missing ']'", token);

        return arrayNode;
    }

    private DictionaryTree parseDictionary() {
        DictionaryTree dictionaryNode = new Trees.BasicDictionaryTree(lexer.consume().getLocation());
        Token token = lexer.peek();

        if (!token.hasTag(TokenKind.CURVED_CLOSED)){
            do {
                token = lexer.peek();

                ExpressionTree key = unwrap(parseExpression(false), token);
                token = lexer.consume();
                if (!token.hasTag(TokenKind.COLON))
                    error("missing ':'", token);

                ExpressionTree value = unwrap(parseExpression(), token);

                dictionaryNode.getKeys().add(key);
                dictionaryNode.getValues().add(value);

                token = lexer.peek();
                if (token.hasTag(TokenKind.COMMA)){
                    lexer.consume();
                    continue;
                }

                break;
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(TokenKind.EOF) || !token.hasTag(TokenKind.CURVED_CLOSED))
            error("missing '}'", token);

        return dictionaryNode;
    }

    private CallTree parseFunctionCall(ExpressionTree exp){
        Trees.BasicCallTree callNode = new Trees.BasicCallTree(lexer.consume().getLocation(), exp);

        Token token = lexer.peek();

        if (!token.hasTag(TokenKind.PARENTHESES_CLOSED)){
            do {
                String ref = null;
                token = lexer.peek();

                if (token.hasTag(TokenKind.IDENTIFIER))
                {
                    Token dummy = lexer.consume();
                    token = lexer.peek();
                    if (token.hasTag(TokenKind.EQ_ASSIGN)) {
                        ref = dummy.getLexem();
                        lexer.consume();
                        token = lexer.peek();
                    }
                    else {
                        lexer.pushBack(dummy);
                        token = dummy;
                    }
                }

                exp = unwrap(parseExpression(), token);
                Trees.BasicArgumentTree arg = new Trees.BasicArgumentTree(exp.getLocation(), exp);
                arg.referenceName = ref;
                callNode.arguments.add(arg);

                token = lexer.peek();
                if (token.hasTag(TokenKind.COMMA)){
                    lexer.consume();
                    continue;
                }

                break;
            } while (true);
        }

        token = lexer.consume();
        if (token.hasTag(TokenKind.EOF)|| !token.hasTag(TokenKind.PARENTHESES_CLOSED))
            error("missing ')'", token);

        return callNode;
    }

    private void parseEOS(){
        Token token = lexer.peek();
        if (!token.hasTag(TokenKind.SEMI))
            error("missing ';'", token);
        else
            lexer.consume();
    }

    private boolean requireBreakPoint(Token token){
        Location location = token.getLocation();
        boolean required = breakPoints.contains(location.line()+1);
        breakPoints.remove(location.line());
        return required;
    }
}
