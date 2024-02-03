package tscriptc.analysis;

import tscriptc.scope.*;
import tscriptc.tree.*;
import tscriptc.util.Errors;

import java.util.*;

public class DefinitionChecker extends Checker<Scope, Void> {

    private final Map<String, ClassScope> classes = new HashMap<>();

    private ClassScope currentClass = null;

    private boolean inStaticArea = false;

    private boolean useActivated = false;

    private void registerDefinitions(Scope scope, Collection<DefinitionTree> defs){
        List<ClassTree> addedClasses = new ArrayList<>();

        List<DefinitionTree> copy = new ArrayList<>(defs);

        for (int i = 0; i < copy.size(); i++){
            DefinitionTree def = copy.get(i);

            if (def instanceof MultiVarDecTree m){
                copy.addAll(m.getDeclarations());
                continue;
            }

            if (scope.has(def.getName())) {
                report(Errors.alreadyDefinedError(def.getName(), def.getLocation()));
                continue;
            }

            if (def instanceof ClassTree c){
                ClassScope cs = new ClassScope(scope, c);
                classes.put(c.getName(), cs);
                scope.putIfAbsent(SymbolKind.CLASS, c.getName(), c.getModifiers());
                addedClasses.add(c);
            }
            if (def instanceof NamespaceTree namespaceTree){
                Trees.BasicClassTree c = new Trees.BasicClassTree(
                        namespaceTree.getLocation(),
                        namespaceTree.getName(),
                        namespaceTree.getModifiers());
                c.definitions.addAll(namespaceTree.getDefinitions());
                ClassScope cs = new ClassScope(scope, c);
                classes.put(c.getName(), cs);
                scope.putIfAbsent(SymbolKind.CLASS, c.getName(), c.getModifiers());
                addedClasses.add(c);
            }
            else if (def instanceof CallableTree f){
                scope.putIfAbsent(SymbolKind.FUNCTION, f.getName(), f.getModifiers());
            }
            else if (def instanceof NativeFunctionTree f){
                scope.putIfAbsent(SymbolKind.FUNCTION, f.getName(), f.getModifiers());
            }
            else if (def instanceof AbstractMethodTree f){
                ClassScope classScope = (ClassScope) scope;
                Set<Modifier> modifiers = f.getModifiers();
                modifiers.add(Modifier.ABSTRACT);
                classScope.putAbstractMethod(f.getName(), modifiers);
            }
            else if (def instanceof VarDecTree v){
                SymbolKind kind = v.isConstant() ? SymbolKind.CONSTANT : SymbolKind.VARIABLE;
                scope.putIfAbsent(kind, v.getName(), v.getModifiers());
            }
        }

        applyInheritanceDependencies(addedClasses);
    }


    private void applyInheritanceDependencies(Collection<ClassTree> classTrees){
        for (ClassTree classTree : classTrees){
            if (classTree.getSuper() != null){
                if (!classes.containsKey(classTree.getSuper())){
                    report(Errors.canNotFindClass(classTree.getSuper(), classTree.getLocation()));
                }
                else {
                    ClassScope childClass = classes.get(classTree.getName());
                    ClassScope superClass = classes.get(classTree.getSuper());
                    childClass.setSuperClass(superClass);
                }
            }
        }
    }


    @Override
    public Void visitRootTree(RootTree rootTree, Scope unused) {
        GlobalScope globalScope = new GlobalScope();
        registerDefinitions(globalScope, rootTree.getDefinitions());
        scan(rootTree.getStatements(), globalScope);
        scan(rootTree.getDefinitions(), globalScope);
        return null;
    }

    @Override
    public Void visitClassTree(ClassTree classTree, Scope scope) {
        ClassScope classScope = classes.get(classTree.getName());

        ClassScope enclosingClassScope = this.currentClass;
        this.currentClass = classScope;

        registerDefinitions(classScope, classTree.getDefinitions());
        super.visitClassTree(classTree, classScope);

        this.currentClass = enclosingClassScope;
        return null;
    }

    @Override
    public Void visitNamespaceTree(NamespaceTree namespaceTree, Scope scope) {
        ClassScope classScope = classes.get(namespaceTree.getName());

        ClassScope enclosingClassScope = this.currentClass;
        this.currentClass = classScope;

        registerDefinitions(classScope, namespaceTree.getDefinitions());

        for (DefinitionTree def : classScope.getTree().getDefinitions()) {
            def.getModifiers().addAll(List.of(Modifier.STATIC, Modifier.PUBLIC));
            scan(def, currentClass);
        }

        this.currentClass = enclosingClassScope;
        return null;
    }

    @Override
    public Void visitConstructorTree(ConstructorTree constructorTree, Scope scope) {
        FunctionScope functionScope = new FunctionScope("constructor of " + currentClass.getName(), scope);

        boolean inStaticArea = this.inStaticArea;
        this.inStaticArea = false;

        super.visitConstructorTree(constructorTree, functionScope);

        this.inStaticArea = inStaticArea;
        return null;
    }

    @Override
    public Void visitFunctionTree(FunctionTree functionTree, Scope scope) {
        FunctionScope functionScope = new FunctionScope(functionTree.getName(), scope);

        boolean inStaticArea = this.inStaticArea;
        this.inStaticArea = functionTree.getModifiers().contains(Modifier.STATIC);

        super.visitFunctionTree(functionTree, functionScope);

        this.inStaticArea = inStaticArea;
        return null;
    }

    @Override
    public Void visitAbstractMethodTree(AbstractMethodTree abstractMethodTree, Scope scope) {
        Set<Modifier> modifiers = abstractMethodTree.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE))
            report(Errors.canNotUsePrivateOnAbstract(abstractMethodTree.getLocation()));
        if (modifiers.contains(Modifier.STATIC))
            report(Errors.canNotUseStaticOnAbstract(abstractMethodTree.getLocation()));
        return null;
    }

    @Override
    public Void visitLambdaTree(LambdaTree lambdaTree, Scope scope) {
        LambdaScope lambdaScope = new LambdaScope(scope);
        boolean inStaticArea = this.inStaticArea;
        ClassScope currentClass = this.currentClass;
        this.inStaticArea = false;
        this.currentClass = null;
        super.visitLambdaTree(lambdaTree, lambdaScope);
        this.inStaticArea = inStaticArea;
        this.currentClass = currentClass;
        return null;
    }

    @Override
    public Void visitParameterTree(ParameterTree parameterTree, Scope scope) {

        boolean alreadyDeclared = scope.accept(new AlreadyDeclaredChecker(), parameterTree.getName());
        if (alreadyDeclared)
            report(Errors.alreadyDefinedError(parameterTree.getName(), parameterTree.getLocation()));

        SymbolKind kind = parameterTree.isConstant() ? SymbolKind.CONSTANT : SymbolKind.VARIABLE;
        scope.putIfAbsent(kind, parameterTree.getName(), Set.of());

        return null;
    }

    @Override
    public Void visitSuperTree(SuperTree superTree, Scope scope) {

        if (inStaticArea) {
            report(Errors.canNotUseSuperFromStaticContext(superTree.getLocation()));
        }
        else if (currentClass != null){
            ClassScope superClass = currentClass.getSuperClass();
            if (superClass == null){
                report(Errors.noSuperClassFound(superTree.getLocation(), currentClass.getName()));
            }
            else {
                Symbol symbol = superClass.accept(new ClassMemberSearcher(superTree.getName()), null);
                if (symbol == null)
                    report(Errors.noSuchMemberFound(superTree.getLocation(), superClass.getName(), superTree.getName()));
                else if (symbol.getModifiers().contains(Modifier.PRIVATE))
                    report(Errors.memberIsNotVisible(superTree.getLocation(), symbol.getVisibility(), symbol.getName()));
            }
        }

        return null;
    }

    @Override
    public Void visitThisTree(ThisTree thisTree, Scope scope) {
        if (inStaticArea)
            report(Errors.canNotUseThisFromStaticContext(thisTree.getLocation()));
        return null;
    }

    @Override
    public Void visitMemberAccessTree(MemberAccessTree accessTree, Scope scope) {

        ExpressionTree exp = accessTree.getExpression();
        String memberName = accessTree.getMemberName();

        scan(exp, scope);

        if (exp instanceof ThisTree && currentClass != null){
            Symbol sym = currentClass.get(memberName);
            if (sym == null)
                report(Errors.noSuchMemberFound(accessTree.getLocation(), currentClass.getName(), memberName));
        }

        return null;
    }

    @Override
    public Void visitIdentifierTree(IdentifierTree identifierTree, Scope scope) {

        Symbol symbol = scope.accept(new SimpleSymbolSearcher(identifierTree.getName()), null);

        if (symbol == null && !useActivated)
            report(Errors.canNotFindSymbol(identifierTree.getName(), identifierTree.getLocation()));

        else if (!useActivated && inStaticArea && !symbol.getModifiers().contains(Modifier.STATIC) && symbol.getOwner() instanceof ClassScope)
            report(Errors.canNotAccessFromStaticContext(identifierTree.getLocation()));

        return null;
    }

    @Override
    public Void visitVarDecTree(VarDecTree varDecTree, Scope scope) {
        scan(varDecTree.getInitializer(), scope);

        if (!(scope instanceof ClassScope)) {
            boolean alreadyDeclared = scope.accept(new AlreadyDeclaredChecker(), varDecTree.getName());
            if (alreadyDeclared)
                report(Errors.alreadyDefinedError(varDecTree.getName(), varDecTree.getLocation()));

            SymbolKind kind = varDecTree.isConstant() ? SymbolKind.CONSTANT : SymbolKind.VARIABLE;
            scope.putIfAbsent(kind, varDecTree.getName(), varDecTree.getModifiers());

            if (kind == SymbolKind.CONSTANT && varDecTree.getInitializer() == null){
                report(Errors.constantMustBeInitialized(varDecTree.getLocation()));
            }
        }

        return null;
    }

    @Override
    public Void visitImportTree(ImportTree importTree, Scope scope) {

        String[] path = importTree.getPath();
        String imported = path[path.length-1];

        boolean alreadyDeclared = scope.accept(new AlreadyDeclaredChecker(), imported);
        if (alreadyDeclared)
            report(Errors.alreadyDefinedError(imported, importTree.getLocation()));

        scope.putIfAbsent(SymbolKind.UNKNOWN, imported, Set.of());
        return null;
    }

    @Override
    public Void visitBlockTree(BlockTree blockTree, Scope scope) {
        boolean useActivated = this.useActivated;
        LocalScope localScope = new LocalScope(scope);
        super.visitBlockTree(blockTree, localScope);
        this.useActivated = useActivated;
        return null;
    }

    @Override
    public Void visitIfElseTree(IfElseTree ifElseTree, Scope scope) {
        scan(ifElseTree.getCondition(), scope);

        boolean useActivated = this.useActivated;
        LocalScope body = new LocalScope(scope);
        scan(ifElseTree.getIfBody(), body);
        this.useActivated = useActivated;

        body = new LocalScope(scope);
        scan(ifElseTree.getElseBody(), body);
        this.useActivated = useActivated;

        return null;
    }

    @Override
    public Void visitDoWhileTree(DoWhileTree doWhileTree, Scope scope) {
        LocalScope localScope = new LocalScope(scope);
        boolean useActivated = this.useActivated;
        scan(doWhileTree.getBody(), localScope);
        this.useActivated = useActivated;
        scan(doWhileTree.getCondition(), localScope);
        return null;
    }

    @Override
    public Void visitWhileDoTree(WhileDoTree whileDoTree, Scope scope) {
        scan(whileDoTree.getCondition(), scope);
        boolean useActivated = this.useActivated;
        LocalScope localScope = new LocalScope(scope);
        scan(whileDoTree.getBody(), localScope);
        this.useActivated = useActivated;
        return null;
    }

    @Override
    public Void visitForLoopTree(ForLoopTree forLoopTree, Scope scope) {
        scan(forLoopTree.getIterable(), scope);

        LocalScope localScope = new LocalScope(scope);

        if (forLoopTree.getName() != null) {
            Tree runVarTree = forLoopTree.isDeclaration()
                    ? new Trees.BasicVarDecTree(forLoopTree.getLocation(), false, forLoopTree.getName())
                    : new Trees.BasicIdentifierTree(forLoopTree.getLocation(), forLoopTree.getName());
            scan(runVarTree, localScope);
        }

        boolean useActivated = this.useActivated;
        scan(forLoopTree.getBody(), localScope);
        this.useActivated = useActivated;

        return null;
    }

    @Override
    public Void visitTryCatchTree(TryCatchTree tryCatchTree, Scope scope) {

        LocalScope localScope = new LocalScope(scope);

        boolean useActivated = this.useActivated;
        scan(tryCatchTree.getTryBody(), localScope);
        this.useActivated = useActivated;

        localScope = new LocalScope(scope);
        VarDecTree exVarDecTree = new Trees.BasicVarDecTree(tryCatchTree.getLocation(), false, tryCatchTree.getExceptionName());
        scan(exVarDecTree, localScope);

        scan(tryCatchTree.getCatchBody(), localScope);
        this.useActivated = useActivated;
        return null;
    }

    @Override
    public Void visitUseTree(UseTree useTree, Scope scope) {
        Symbol symbol = scope.accept(new SimpleSymbolSearcher(useTree.getName()), null);

        if (symbol == null && !useActivated)
            report(Errors.canNotFindSymbol(useTree.getName(), useTree.getLocation()));
        else
            useActivated = true;

        return null;
    }

    @Override
    public Void visitAssignTree(AssignTree assignTree, Scope scope) {
        boolean useActivated = this.useActivated;
        this.useActivated = false;
        scan(assignTree.getLeft(), scope);
        this.useActivated = useActivated;
        scan(assignTree.getRight(), scope);
        return null;
    }
}
