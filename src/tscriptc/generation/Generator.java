package tscriptc.generation;

import tscriptc.scope.*;
import tscriptc.tree.*;
import tscriptc.util.Conversion;
import tscriptc.util.Location;
import tscriptc.util.TreeScanner;

import java.util.*;

public class Generator extends TreeScanner<Scope, Void> {

    private final Compiled compiled = new Compiled();

    public Compiled getCompiled() {
        return compiled;
    }

    private final Map<String, ClassScope> classes = new HashMap<>();

    private ClassScope currentClass = null;

    private boolean inStaticArea = false;

    private int lastLine = 0;


    private void newLine(Tree tree){
        Location location = tree.getLocation();
        if (location.line() != lastLine) {
            lastLine = location.line();
            compiled.addInstruction(new Instruction(Opcode.NEW_LINE, Conversion.getBytes(lastLine)));
        }
    }

    private void registerDefinitions(Scope scope, Collection<DefinitionTree> defs){
        List<ClassTree> addedClasses = new ArrayList<>();

        for (DefinitionTree def : defs){

            if (def instanceof ClassTree c){
                ClassScope cs = new ClassScope(scope, c);
                classes.put(c.getName(), cs);
                scope.putIfAbsent(SymbolKind.CLASS, c.getName(), c.getModifiers());
                addedClasses.add(c);
            }
            else if (def instanceof NamespaceTree namespaceTree){
                Trees.BasicClassTree c = new Trees.BasicClassTree(
                        namespaceTree.getLocation(),
                        namespaceTree.getName(),
                        namespaceTree.getModifiers());
                c.definitions.addAll(namespaceTree.getDefinitions());
                scope.putIfAbsent(SymbolKind.CLASS, c.getName(), c.getModifiers());
                addedClasses.add(c);
            }
            else if (def instanceof CallableTree f){
                scope.putIfAbsent(SymbolKind.FUNCTION, f.getName(), f.getModifiers());
                compiled.putFunction(fullNameOf(f.getName()));
                compiled.registerFunction(fullNameOf(f.getName()), f.getParameters().size());
            }
            else if (def instanceof NativeFunctionTree f){
                scope.putIfAbsent(SymbolKind.FUNCTION, f.getName(), f.getModifiers());
                compiled.putNative(f.getName());
            }
            else if (def instanceof AbstractMethodTree f){
                ClassScope classScope = (ClassScope) scope;
                Set<Modifier> modifiers = f.getModifiers();
                modifiers.add(Modifier.ABSTRACT);
                classScope.putAbstractMethod(f.getName(), modifiers);
                compiled.putUTF8(f.getName());
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
                ClassScope childClass = classes.get(classTree.getName());
                ClassScope superClass = classes.get(classTree.getSuper());
                childClass.setSuperClass(superClass);
            }
        }
    }

    private void setupGlobalFunctions(List<DefinitionTree> globalDefinitions, GlobalScope scope){
        // load global functions into their global registers
        for (DefinitionTree def : globalDefinitions) {
            if (def instanceof CallableTree c) {
                int globalAddr = scope.get(def.getName()).getAddress();
                int poolAddr = compiled.putFunction(c.getName());
                compiled.addInstruction(new Instruction(Opcode.LOAD_CONST, (byte) poolAddr));
                compiled.addInstruction(new Instruction(Opcode.STORE_GLOBAL, (byte) globalAddr));
                compiled.stackGrows();
                compiled.stackGrows(-1);
            }
            else if (def instanceof NativeFunctionTree c) {
                int globalAddr = scope.get(def.getName()).getAddress();
                int poolAddr = compiled.putNative(c.getName());
                compiled.addInstruction(new Instruction(Opcode.LOAD_CONST, (byte) poolAddr));
                compiled.addInstruction(new Instruction(Opcode.STORE_GLOBAL, (byte) globalAddr));
                compiled.stackGrows();
                compiled.stackGrows(-1);
            }
        }
    }

    private String fullNameOf(String name, Scope scope){
        StringBuilder fullName = new StringBuilder(name);
        while (scope instanceof ClassScope classScope){
            fullName.insert(0, classScope.getName() + "$");
            scope = scope.getEnclosingScope();
        }
        return fullName.toString();
    }

    private String fullNameOf(String name){
        return fullNameOf(name, currentClass);
    }




    @Override
    public Void visitRootTree(RootTree rootTree, Scope scope) {
        GlobalScope globalScope = new GlobalScope();

        registerDefinitions(globalScope, rootTree.getDefinitions());
        setupGlobalFunctions(rootTree.getDefinitions(), globalScope);

        scan(rootTree.getStatements(), globalScope);
        compiled.addInstruction(new Instruction(Opcode.PUSH_NULL));
        compiled.addInstruction(new Instruction(Opcode.RETURN));
        compiled.stackGrows();
        compiled.stackGrows(-1);

        compiled.setGlobalRegs(globalScope.getGlobals());
        compiled.setLocals(globalScope.getLocals());
        compiled.completeFunction();

        scan(rootTree.getDefinitions(), globalScope);

        genTypes();
        return null;
    }

    @Override
    public Void visitNamespaceTree(NamespaceTree namespaceTree, Scope scope) {

        Trees.BasicClassTree classTree = new Trees.BasicClassTree(
                namespaceTree.getLocation(),
                namespaceTree.getName(),
                namespaceTree.getModifiers());
        classTree.definitions.addAll(namespaceTree.getDefinitions());

        ClassTree prevTree = this.classTree;
        this.classTree = classTree;

        ClassScope prev = currentClass;
        currentClass = new ClassScope(scope, classTree);

        registerDefinitions(currentClass, classTree.getDefinitions());
        genStaticBlock(currentClass);

        for (DefinitionTree def : classTree.getDefinitions()) {
            def.getModifiers().addAll(List.of(Modifier.STATIC, Modifier.PUBLIC));
            scan(def, currentClass);
        }
        compiled.putType(classTree.getName());

        Type type = new Type(namespaceTree.getName(), null, -1, currentClass.getStaticBlockID());
        type.setAbstract(true);
        for (Symbol sym : currentClass){
            Set<Modifier> modifiers = sym.getModifiers();
            modifiers.addAll(List.of(Modifier.STATIC, Modifier.PUBLIC));
            type.add(sym.getName(), modifiers);
        }
        compiled.addType(type);

        currentClass = prev;
        this.classTree = prevTree;

        return null;
    }

    @Override
    public Void visitClassTree(ClassTree classTree, Scope scope) {
        ClassScope currentClass = this.currentClass;
        this.currentClass = classes.get(classTree.getName());
        registerDefinitions(this.currentClass, classTree.getDefinitions());

        ConstructorTree constructor = classTree.getConstructor() != null
                ? classTree.getConstructor()
                : new Trees.BasicConstructorTree(classTree.getLocation());

        this.classTree = classTree;
        scan(constructor, this.currentClass);
        genStaticBlock(this.currentClass);
        for (DefinitionTree def : classTree.getDefinitions()) {
            if (!(def instanceof VarDecTree))
                scan(def, this.currentClass);
        }
        this.currentClass = currentClass;
        compiled.putType(fullNameOf(classTree.getName()));
        return null;
    }

    private void genTypes(){
        for (ClassScope clazz : classes.values()){
            ClassScope superScope = clazz.getSuperClass();
            String superClass = superScope != null ? superScope.getName() : null;
            String name = fullNameOf(clazz.getName(), clazz.getEnclosingScope());
            Type type = new Type(name, superClass, clazz.getConstructorID(), clazz.getStaticBlockID());
            type.setAbstract(clazz.getTree().getModifiers().contains(Modifier.ABSTRACT));
            for (Symbol sym : clazz){
                if (sym.getOwner() != clazz && sym.getModifiers().contains(Modifier.STATIC))
                    continue;
                type.add(sym.getName(), sym.getModifiers());
            }
            compiled.addType(type);
        }
    }

    private ClassTree classTree;
    @Override
    public Void visitConstructorTree(ConstructorTree constructorTree, Scope scope) {
        String name = "constructor of " + fullNameOf(currentClass.getName(), scope.getEnclosingScope());

        int poolID = compiled.putFunction(name);
        compiled.registerFunction(name, constructorTree.getParameters().size());
        compiled.enterFunction(name);

        boolean inStaticArea = this.inStaticArea;
        this.inStaticArea = false;

        FunctionScope functionScope = new FunctionScope(name, scope);
        scan(constructorTree.getParameters(), functionScope);

        if (classTree.getSuper() != null) {
            List<ArgumentTree> args = constructorTree.getSuperArguments();
            scan(args, functionScope);
            compiled.addInstruction(new Instruction(Opcode.CALL_SUPER, (byte) args.size()));
            compiled.stackGrows(-args.size());
            compiled.stackGrows(1);
        }

        for (DefinitionTree def : classTree.getDefinitions()){
            if (def instanceof AbstractMethodTree
                    || def.getModifiers().contains(Modifier.STATIC))
                continue;

            String defName = def.getName();
            int address = currentClass.get(defName).getAddress();

            if (def instanceof VarDecTree v) {
                scan(v.getInitializer() != null ? v.getInitializer() : new Trees.BasicNullLiteralTree(def.getLocation()), functionScope);
            }
            else if (def instanceof FunctionTree){
                int index = compiled.putFunction(fullNameOf(defName));
                compiled.addInstruction(new Instruction(Opcode.LOAD_CONST, (byte) index));
                compiled.stackGrows(1);
            }
            else if (def instanceof NativeFunctionTree){
                int index = compiled.putNative(defName);
                compiled.addInstruction(new Instruction(Opcode.LOAD_CONST, (byte) index));
                compiled.stackGrows(1);
            }

            compiled.addInstruction(new Instruction(Opcode.STORE_MEMBER_FAST, (byte) address));
            compiled.stackGrows(-1);
        }

        scan(constructorTree.getBody(), functionScope);

        scan(new Trees.BasicReturnTree(constructorTree.getLocation(), new Trees.BasicThisTree(constructorTree.getLocation())), functionScope);

        this.inStaticArea = inStaticArea;

        compiled.setLocals(functionScope.getLocals());
        compiled.completeFunction();

        currentClass.setConstructorID(poolID);
        return null;
    }

    private void genStaticBlock(Scope scope){
        String name =  fullNameOf(currentClass.getName(), scope.getEnclosingScope()) + "@static";

        int poolID = compiled.putFunction(name);
        compiled.registerFunction(name, 0);
        compiled.enterFunction(name);

        boolean inStaticArea = this.inStaticArea;
        this.inStaticArea = false;

        FunctionScope functionScope = new FunctionScope(name, scope);

        for (DefinitionTree def : classTree.getDefinitions()){
            if (!def.getModifiers().contains(Modifier.STATIC))
                continue;

            String defName = def.getName();
            int address = currentClass.get(defName).getAddress();

            if (def instanceof ClassTree){
                int index = compiled.putType(fullNameOf(defName));
                compiled.addInstruction(new Instruction(Opcode.LOAD_CONST, (byte) index));
                compiled.stackGrows(1);
            }
            if (def instanceof VarDecTree v) {
                scan(v.getInitializer() != null ? v.getInitializer() : new Trees.BasicNullLiteralTree(def.getLocation()), functionScope);
            }
            else if (def instanceof FunctionTree){
                int index = compiled.putFunction(fullNameOf(defName));
                compiled.addInstruction(new Instruction(Opcode.LOAD_CONST, (byte) index));
                compiled.stackGrows(1);
            }
            else if (def instanceof NativeFunctionTree){
                int index = compiled.putNative(defName);
                compiled.addInstruction(new Instruction(Opcode.LOAD_CONST, (byte) index));
                compiled.stackGrows(1);
            }

            compiled.addInstruction(new Instruction(Opcode.STORE_MEMBER_FAST, (byte) address));
            compiled.stackGrows(-1);
        }

        scan(new Trees.BasicReturnTree(null, new Trees.BasicNullLiteralTree(null)), functionScope);

        this.inStaticArea = inStaticArea;

        compiled.setLocals(functionScope.getLocals());
        compiled.completeFunction();

        currentClass.setStaticBlockID(poolID);
    }

    @Override
    public Void visitFunctionTree(FunctionTree functionTree, Scope scope) {
        compiled.enterFunction(fullNameOf(functionTree.getName()));
        FunctionScope functionScope = new FunctionScope(functionTree.getName(), scope);
        boolean inStaticArea = this.inStaticArea;
        this.inStaticArea = functionTree.getModifiers().contains(Modifier.STATIC);

        scan(functionTree.getParameters(), functionScope);
        scan(functionTree.getBody(), functionScope);

        this.inStaticArea = inStaticArea;
        compiled.setLocals(functionScope.getLocals());
        compiled.completeFunction();
        return null;
    }

    @Override
    public Void visitIfElseTree(IfElseTree ifElseTree, Scope scope) {
        scan(ifElseTree.getCondition(), scope);

        Instruction branchIf = new Instruction(Opcode.BRANCH_IF_FALSE, (byte) 0);
        compiled.addInstruction(branchIf);

        compiled.stackGrows(-1);

        LocalScope localScope = new LocalScope(scope);
        scan(ifElseTree.getIfBody(), localScope);

        if (ifElseTree.getElseBody() == null){
            int jmpAddr = compiled.getInstructionStreamSize();
            branchIf.bytes = Conversion.toJumpAddress(jmpAddr);
        }
        else {
            Instruction goto_ = new Instruction(Opcode.GOTO, (byte) 0);
            compiled.addInstruction(goto_);
            int ifEndIndex = compiled.getInstructionStreamSize();

            localScope = new LocalScope(scope);
            scan(ifElseTree.getElseBody(), localScope);
            int elseEndIndex = compiled.getInstructionStreamSize();

            branchIf.bytes = Conversion.toJumpAddress(ifEndIndex);
            goto_.bytes = Conversion.toJumpAddress(elseEndIndex);
        }

        return null;
    }

    @Override
    public Void visitDoWhileTree(DoWhileTree doWhileTree, Scope scope) {
        int jmpAddr = compiled.getInstructionStreamSize();
        LocalScope localScope = new LocalScope(scope);
        scan(doWhileTree.getBody(), localScope);
        scan(doWhileTree.getCondition(), localScope);
        compiled.addInstruction(new Instruction(Opcode.BRANCH_IF_TRUE, Conversion.toJumpAddress(jmpAddr)));
        compiled.stackGrows(-1);
        return null;
    }

    @Override
    public Void visitWhileDoTree(WhileDoTree whileDoTree, Scope scope) {

        Trees.BasicIfElseTree ifElseTree = new Trees.BasicIfElseTree(whileDoTree.getLocation());
        DoWhileTree doWhileTree = new Trees.BasicDoWhileTree(whileDoTree.getLocation(), whileDoTree.getBody(), whileDoTree.getCondition());
        ifElseTree.condition = whileDoTree.getCondition();
        ifElseTree.ifBody = doWhileTree;

        scan(ifElseTree, scope);
        return null;
    }

    @Override
    public Void visitForLoopTree(ForLoopTree forLoopTree, Scope scope) {
        newLine(forLoopTree);

        scan(forLoopTree.getIterable(), scope);
        compiled.addInstruction(new Instruction(Opcode.GET_ITR));
        compiled.stackGrows();

        int jumpBackAddr = compiled.getInstructionStreamSize();
        Instruction branchItr = new Instruction(Opcode.BRANCH_ITR, (byte) 0);
        compiled.addInstruction(branchItr);

        LocalScope localScope = new LocalScope(scope);

        if (forLoopTree.getName() != null) {

            if (forLoopTree.isDeclaration())
                // declare variable before usage
                localScope.putIfAbsent(SymbolKind.VARIABLE, forLoopTree.getName(), Set.of());

            compiled.addInstruction(new Instruction(Opcode.ITR_NEXT));
            compiled.stackGrows();

            // use identifierTree to compile correct access (LOAD_LOCAL vs LOAD_GLOBAL)
            IdentifierTree tree = new Trees.BasicIdentifierTree(forLoopTree.getLocation(), forLoopTree.getName());
            genAssignToIdentifier(tree, localScope);
        }
        else {
            compiled.addInstruction(new Instruction(Opcode.ITR_NEXT));
            compiled.stackGrows();
            compiled.addInstruction(new Instruction(Opcode.POP));
            compiled.stackGrows(-1);
        }

        scan(forLoopTree.getBody(), localScope);

        compiled.addInstruction(new Instruction(Opcode.GOTO, Conversion.toJumpAddress(jumpBackAddr)));
        int jumpItrFailsAddr = compiled.getInstructionStreamSize();
        branchItr.bytes = Conversion.toJumpAddress(jumpItrFailsAddr);
        compiled.stackGrows(-1); // pop the iterator
        return null;
    }

    @Override
    public Void visitReturnTree(ReturnTree returnTree, Scope scope) {
        scan(returnTree.getExpression(), scope);
        compiled.addInstruction(new Instruction(Opcode.RETURN));
        compiled.stackGrows(-1);
        return null;
    }

    @Override
    public Void visitExpressionStatementTree(ExpressionStatementTree expressionStatementTree, Scope scope) {
        ExpressionTree exp = expressionStatementTree.getExpression();
        if (!(exp instanceof UsefulExpression)) return null;
        scan(exp, scope);
        if (exp instanceof CallTree) {
            compiled.addInstruction(new Instruction(Opcode.POP));
            compiled.stackGrows(-1);
        }
        return null;
    }

    @Override
    public Void visitTryCatchTree(TryCatchTree tryCatchTree, Scope scope) {

        Instruction enterTry =  new Instruction(Opcode.ENTER_TRY, (byte) 0);
        compiled.addInstruction(enterTry);

        LocalScope localScope = new LocalScope(scope);
        scan(tryCatchTree.getTryBody(), localScope);
        int tryEndIndex = compiled.getInstructionStreamSize()+1;
        enterTry.bytes = new byte[]{(byte) (tryEndIndex+1)};
        compiled.addInstruction(new Instruction(Opcode.LEAVE_TRY));
        Instruction goto_ = new Instruction(Opcode.GOTO, (byte) 0);
        compiled.addInstruction(goto_);

        localScope = new LocalScope(scope);
        localScope.putIfAbsent(SymbolKind.VARIABLE, tryCatchTree.getExceptionName(), Set.of());
        int exVarAddr = localScope.get(tryCatchTree.getExceptionName()).getAddress();
        compiled.stackGrows();
        compiled.addInstruction(new Instruction(Opcode.STORE_LOCAL, (byte) exVarAddr));
        compiled.stackGrows(-1);

        scan(tryCatchTree.getCatchBody(), localScope);
        int catchEndIndex = compiled.getInstructionStreamSize();
        goto_.bytes = Conversion.toJumpAddress(catchEndIndex);

        return null;
    }

    @Override
    public Void visitThrowTree(ThrowTree throwTree, Scope scope) {
        scan(throwTree.getExpression(), scope);
        newLine(throwTree);
        compiled.addInstruction(new Instruction(Opcode.THROW));
        compiled.stackGrows(-1);
        return null;
    }

    @Override
    public Void visitBlockTree(BlockTree blockTree, Scope scope) {
        LocalScope localScope = new LocalScope(scope);
        scan(blockTree.getStatements(), localScope);
        return null;
    }



    @Override
    public Void visitNullTree(NullLiteralTree tree, Scope scope) {
        compiled.addInstruction(new Instruction(Opcode.PUSH_NULL));
        compiled.stackGrows();
        return null;
    }

    @Override
    public Void visitIntegerTree(IntegerLiteralTree tree, Scope scope) {
        int i = tree.get();
        if (i < Byte.MIN_VALUE || i > Byte.MAX_VALUE)
        {
            int index = compiled.putInt(i);
            compiled.addInstruction(new Instruction(Opcode.LOAD_CONST, (byte) index));
        }
        else {
            compiled.addInstruction(new Instruction(Opcode.PUSH_INT, (byte) i));
        }
        compiled.stackGrows();
        return null;
    }

    @Override
    public Void visitFloatTree(FloatLiteralTree tree, Scope scope) {
        double d = tree.get();
        int index = compiled.putReal(d);
        compiled.addInstruction(new Instruction(Opcode.LOAD_CONST, (byte) index));
        compiled.stackGrows();
        return null;
    }

    @Override
    public Void visitBooleanTree(BooleanLiteralTree tree, Scope scope) {
        int b = tree.get() ? 1 : 0;
        compiled.addInstruction(new Instruction(Opcode.PUSH_BOOL, (byte) b));
        compiled.stackGrows();
        return null;
    }


    @Override
    public Void visitStringTree(StringLiteralTree tree, Scope scope) {
        String s = tree.get();
        int index = compiled.putStr(s);
        compiled.addInstruction(new Instruction(Opcode.LOAD_CONST, (byte) index));
        compiled.stackGrows();
        return null;
    }

    @Override
    public Void visitThisTree(ThisTree thisTree, Scope scope) {
        compiled.addInstruction(new Instruction(Opcode.PUSH_THIS));
        compiled.stackGrows();
        return null;
    }

    @Override
    public Void visitSuperTree(SuperTree superTree, Scope scope) {
        ClassScope superClass = currentClass.getSuperClass();
        Symbol symbol = superClass.accept(new ClassMemberSearcher(superTree.getName()), null);
        compiled.addInstruction(new Instruction(Opcode.LOAD_MEMBER_FAST, (byte) symbol.getAddress()));
        compiled.stackGrows();
        return null;
    }

    @Override
    public Void visitArrayTree(ArrayTree arrayTree, Scope scope) {
        List<ExpressionTree> content = arrayTree.getContent();
        for (int i = content.size()-1; i >= 0; i --)
            scan(content.get(i), scope);
        compiled.addInstruction(new Instruction(Opcode.MAKE_ARRAY, (byte) content.size()));
        compiled.stackGrows(-content.size() + 1);
        return null;
    }

    @Override
    public Void visitDictionaryTree(DictionaryTree dictionaryTree, Scope scope) {
        List<ExpressionTree> keys = dictionaryTree.getKeys();
        List<ExpressionTree> values = dictionaryTree.getValues();

        for (int i = values.size()-1; i >= 0; i--){
            scan(values.get(i), scope);
            scan(keys.get(i), scope);
        }

        compiled.addInstruction(new Instruction(Opcode.MAKE_DICT, (byte) keys.size()));
        compiled.stackGrows(keys.size() * -2 + 1);
        return null;
    }

    @Override
    public Void visitRangeTree(RangeTree rangeTree, Scope scope) {
        scan(rangeTree.getFrom(), scope);
        scan(rangeTree.getTo(), scope);
        newLine(rangeTree);
        compiled.addInstruction(new Instruction(Opcode.MAKE_RANGE));
        compiled.stackGrows(-1);
        return null;
    }

    int nextLambdaId = 0;
    @Override
    public Void visitLambdaTree(LambdaTree lambdaTree, Scope scope) {

        String name = "Lambda@" + (nextLambdaId++);
        int poolId = compiled.putFunction(name);
        compiled.registerFunction(name, lambdaTree.getParameters().size());
        compiled.enterFunction(name);

        boolean inStaticArea = this.inStaticArea;

        this.inStaticArea = false;

        LambdaScope lambdaScope = new LambdaScope(scope);
        super.visitLambdaTree(lambdaTree, lambdaScope);

        this.inStaticArea = inStaticArea;

        compiled.setLocals(lambdaScope.getLocals());
        compiled.completeFunction();

        compiled.addInstruction(new Instruction(Opcode.LOAD_CONST, (byte) poolId));
        compiled.stackGrows();
        return null;
    }

    @Override
    public Void visitContainerAccessTree(ContainerAccessTree accessTree, Scope scope) {
        scan(accessTree.getKey(), scope);
        scan(accessTree.getExpression(), scope);
        newLine(accessTree);
        compiled.addInstruction(new Instruction(Opcode.CONTAINER_READ));
        compiled.stackGrows(-2);
        return null;
    }

    @Override
    public Void visitGetTypeTree(GetTypeTree getTypeTree, Scope scope) {
        scan(getTypeTree.getExpression(), scope);
        compiled.addInstruction(new Instruction(Opcode.GET_TYPE));
        compiled.stackGrows();
        return null;
    }

    @Override
    public Void visitCallTree(CallTree callTree, Scope scope) {
        List<ArgumentTree> args = callTree.getArguments();
        for (int i = args.size()-1; i >= 0; i--)
            scan(args.get(i), scope);
        scan(callTree.getExpression(), scope);
        int argc = callTree.getArguments().size();
        newLine(callTree);
        compiled.addInstruction(new Instruction(Opcode.CALL, (byte) argc));
        compiled.stackGrows(-argc);
        return null;
    }

    @Override
    public Void visitArgumentTree(ArgumentTree argumentTree, Scope scope) {
        scan(argumentTree.getExpression(), scope);
        String refName = argumentTree.getReferencedName();
        if (refName != null){
            int poolAddr = compiled.putUTF8(refName);
            compiled.addInstruction(new Instruction(Opcode.WRAP_ARGUMENT, (byte) poolAddr));
        }
        return null;
    }

    @Override
    public Void visitSignTree(SignTree signTree, Scope scope) {
        scan(signTree.getExpression(), scope);

        Opcode opcode = signTree.isNegation()
                ? Opcode.NEG
                : Opcode.POS;

        newLine(signTree);
        compiled.addInstruction(new Instruction(opcode));
        return null;
    }

    @Override
    public Void visitNotTree(NotTree notTree, Scope scope) {
        scan(notTree.getExpression(), scope);
        newLine(notTree);
        compiled.addInstruction(new Instruction(Opcode.NOT));
        return null;
    }

    @Override
    public Void visitOperationTree(BinaryOperationTree operationTree, Scope scope) {
        scan(operationTree.getLeft(), scope);
        scan(operationTree.getRight(), scope);
        Opcode opcode = Opcode.of(operationTree.getOperation());
        newLine(operationTree);
        compiled.addInstruction(new Instruction(opcode));
        compiled.stackGrows(-1);
        return null;
    }

    @Override
    public Void visitIdentifierTree(IdentifierTree identifierTree, Scope scope) {

        Symbol symbol = scope.accept(new SimpleSymbolSearcher(identifierTree.getName()), null);

        if (symbol.getKind() == SymbolKind.CLASS){
            String s = identifierTree.getName();
            int index = compiled.putType(s);
            newLine(identifierTree);
            compiled.addInstruction(new Instruction(Opcode.LOAD_CONST, (byte) index));
        }
        else {
            if (symbol.getOwner() instanceof GlobalScope) {
                compiled.addInstruction(new Instruction(Opcode.LOAD_GLOBAL, (byte) symbol.getAddress()));
            } else if (symbol.getOwner() instanceof ClassScope) {
                if (inStaticArea || symbol.getModifiers().contains(Modifier.STATIC)) {
                    compiled.addInstruction(new Instruction(Opcode.LOAD_STATIC, (byte) compiled.putUTF8(symbol.getName())));
                }
                else if (symbol.getModifiers().contains(Modifier.ABSTRACT)){
                    int poolAddress = compiled.putUTF8(symbol.getName());
                    newLine(identifierTree);
                    compiled.addInstruction(new Instruction(Opcode.LOAD_ABSTRACT_IMPL, (byte) poolAddress));
                }
                else {
                    compiled.addInstruction(new Instruction(Opcode.LOAD_MEMBER_FAST, (byte) symbol.getAddress()));
                }
            } else {
                compiled.addInstruction(new Instruction(Opcode.LOAD_LOCAL, (byte) symbol.getAddress()));
            }

        }
        compiled.stackGrows();
        return null;
    }

    @Override
    public Void visitMemberAccessTree(MemberAccessTree accessTree, Scope scope) {

        ExpressionTree exp = accessTree.getExpression();
        String memberName = accessTree.getMemberName();

        if (exp instanceof ThisTree){
            Symbol sym = currentClass.get(memberName);
            compiled.addInstruction(new Instruction(Opcode.LOAD_MEMBER_FAST, (byte) sym.getAddress()));
            compiled.stackGrows();
        }
        else {
            scan(exp, scope);
            int utf8Addr = compiled.putUTF8(memberName);
            newLine(accessTree);
            compiled.addInstruction(new Instruction(Opcode.LOAD_MEMBER, (byte) utf8Addr));
        }

        return null;
    }

    @Override
    public Void visitVarDecTree(VarDecTree varDecTree, Scope scope) {

        if (varDecTree.getInitializer() != null)
            varDecTree.getInitializer().accept(this, scope);
        else
            compiled.addInstruction(new Instruction(Opcode.PUSH_NULL));

        SymbolKind kind = varDecTree.isConstant() ? SymbolKind.CONSTANT : SymbolKind.VARIABLE;
        scope.putIfAbsent(kind, varDecTree.getName(), varDecTree.getModifiers());

        int varAddr = scope.get(varDecTree.getName()).getAddress();
        Opcode opcode = scope instanceof GlobalScope ? Opcode.STORE_GLOBAL : Opcode.STORE_LOCAL;
        compiled.addInstruction(new Instruction(opcode, (byte) varAddr));
        compiled.stackGrows(-1);

        return null;
    }

    @Override
    public Void visitParameterTree(ParameterTree parameterTree, Scope scope) {
        compiled.addParameter(parameterTree.getName());

        SymbolKind kind = parameterTree.isConstant() ? SymbolKind.CONSTANT : SymbolKind.VARIABLE;
        scope.putIfAbsent(kind, parameterTree.getName(), Set.of());

        int varAddr = scope.get(parameterTree.getName()).getAddress();
        compiled.stackGrows(1);
        compiled.addInstruction(new Instruction(Opcode.STORE_LOCAL, (byte) varAddr));
        compiled.stackGrows(-1);
        return null;
    }

    @Override
    public Void visitAssignTree(AssignTree assignTree, Scope scope) {

        scan(assignTree.getRight(), scope);

        ExpressionTree left = assignTree.getLeft();

        if (left instanceof IdentifierTree identifierTree){
            genAssignToIdentifier(identifierTree, scope);
        }
        else if (left instanceof SuperTree superTree){
            genAssignToSuper(superTree);
        }
        else if (left instanceof ContainerAccessTree accessTree){
            genAssignToContainerEntry(accessTree, scope);
        }
        else if (left instanceof MemberAccessTree accessTree){
            genMemberAccessWrite(accessTree, scope);
        }
        else throw new UnsupportedOperationException();

        return null;
    }

    private void genAssignToIdentifier(IdentifierTree identifierTree, Scope scope){
        Symbol symbol = scope.accept(new SimpleSymbolSearcher(identifierTree.getName()), null);

        if (symbol.getOwner() instanceof GlobalScope) {
            compiled.addInstruction(new Instruction(Opcode.STORE_GLOBAL, (byte) symbol.getAddress()));
        }
        else if (symbol.getOwner() instanceof ClassScope) {
            if (inStaticArea || symbol.getModifiers().contains(Modifier.STATIC))
                compiled.addInstruction(new Instruction(Opcode.STORE_STATIC, (byte) compiled.putUTF8(symbol.getName())));
            else
                compiled.addInstruction(new Instruction(Opcode.STORE_MEMBER_FAST, (byte) symbol.getAddress()));
        }
        else {
            compiled.addInstruction(new Instruction(Opcode.STORE_LOCAL, (byte) symbol.getAddress()));
        }
        compiled.stackGrows(-1);
    }

    private void genAssignToSuper(SuperTree superTree){
        ClassScope superClass = currentClass.getSuperClass();
        Symbol symbol = superClass.accept(new ClassMemberSearcher(superTree.getName()), null);
        compiled.addInstruction(new Instruction(Opcode.STORE_MEMBER_FAST, (byte) symbol.getAddress()));
        compiled.stackGrows(-1);
    }

    private void genAssignToContainerEntry(ContainerAccessTree accessTree, Scope scope){
        scan(accessTree.getKey(), scope);
        scan(accessTree.getExpression(), scope);
        newLine(accessTree);
        compiled.addInstruction(new Instruction(Opcode.CONTAINER_WRITE));
        compiled.stackGrows(-3);
    }

    private void genMemberAccessWrite(MemberAccessTree accessTree, Scope scope){
        ExpressionTree exp = accessTree.getExpression();
        String memberName = accessTree.getMemberName();

        if (exp instanceof ThisTree){
            Symbol sym = currentClass.get(memberName);
            compiled.addInstruction(new Instruction(Opcode.STORE_MEMBER_FAST, (byte) sym.getAddress()));
        }
        else {
            scan(exp, scope);
            int utf8Addr = compiled.putUTF8(memberName);
            newLine(accessTree);
            compiled.addInstruction(new Instruction(Opcode.STORE_MEMBER, (byte) utf8Addr));
        }
        compiled.stackGrows(-1);
    }


}
