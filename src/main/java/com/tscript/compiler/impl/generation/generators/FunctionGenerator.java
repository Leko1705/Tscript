package com.tscript.compiler.impl.generation.generators;

import com.tscript.compiler.impl.generation.compiled.CompiledFunction;
import com.tscript.compiler.impl.generation.compiled.instruction.*;
import com.tscript.compiler.impl.generation.generators.impls.CompFunc;
import com.tscript.compiler.impl.generation.generators.impls.LambdaFunction;
import com.tscript.compiler.impl.generation.generators.impls.PoolPutter;
import com.tscript.compiler.impl.utils.Scope;
import com.tscript.compiler.impl.utils.Symbol;
import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.source.tree.*;
import com.tscript.compiler.source.utils.Location;
import com.tscript.compiler.source.utils.TreeScanner;

import java.util.*;

public class FunctionGenerator extends TreeScanner<Scope, Void> {

    public static int generate(Context context, FunctionTree node, Scope scope){
        FunctionGenerator generator = new FunctionGenerator(context, node);
        generator.handle(node, scope);
        return generator.func.getIndex();
    }

    private final Context context;

    public final CompFunc func;

    private int maxStackSize = 0;
    private int currentStackSize = maxStackSize;

    private int maxLocals = 0;
    private final List<String> currentLocals = new ArrayList<>();

    private final Deque<List<LoopFlowAction>> loopControlFlowStack = new ArrayDeque<>();

    private int line;

    public FunctionGenerator(Context context, FunctionTree node) {
        this.context = context;
        func = new CompFunc(context.getNextFunctionIndex(), node.getName());
    }

    public void addPreInstructions(List<Instruction> instruction){
        func.getInstructions().addAll(0, instruction);
    }

    public void handle(FunctionTree node, Scope scope){
        newLine(node);

        List<? extends ParameterTree> params = node.getParameters();
        stackGrows(params.size());
        stackShrinks(params.size());
        scan(params, scope);

        if (node.getName().equals("__main__")) {
            for (StatementTree stmt : node.getBody().getStatements()){
                scan(stmt, scope);
            }
        }
        else {
            scan(node.getBody(), scope);
        }

        if (!isExplicitReturn()){
            func.getInstructions().add(new PushNull());
            func.getInstructions().add(new Return());
        }

        func.stackSize = maxStackSize;
        func.locals = maxLocals;

        context.getFile().functions.add(func);
    }

    private void newLine(Tree tree){
        int newLine = tree.getLocation().line();
        if (newLine > line){
            line = newLine;
            func.getInstructions().add(new NewLine(line));
        }
    }

    @Override
    public Void visitFunction(FunctionTree node, Scope scope) {
        if (node.getModifiers().getFlags().contains(Modifier.NATIVE)){
            func.getInstructions().add(new LoadNative(PoolPutter.putUtf8(context, node.getName())));
        }
        else {
            newLine(node);
            int index = FunctionGenerator.generate(context, node, ((TCTree.TCFunctionTree)node).sym.subScope);
            func.getInstructions().add(new LoadVirtual(index));
        }
        stackGrows();

        func.getInstructions().add(new StoreLocal(requireLocalAddress(node.getName())));
        stackShrinks();

        return null;
    }

    @Override
    public Void visitParameter(ParameterTree node, Scope scope) {
        func.getInstructions().add(new StoreLocal(requireLocalAddress(node.getName())));
        int defaultAddr = -1;
        if (node.getDefaultValue() != null) defaultAddr = PoolPutter.put(context, node.getDefaultValue());
        func.parameters.add(CompiledFunction.Parameter.of(node.getName(), defaultAddr));
        return null;
    }


    @Override
    public Void visitBlock(BlockTree node, Scope scope) {
        Scope child = ((TCTree.TCBlockTree)node).scope;
        for (StatementTree stmt : node.getStatements()) {
            stmt.accept(this, child);
            if (stmt instanceof Return) break;
            if (stmt instanceof ContinueTree) break;
            if (stmt instanceof BreakTree) break;
            if (stmt instanceof ThrowTree) break;
        }
        return null;
    }

    @Override
    public Void visitInteger(IntegerTree node, Scope scope) {
        int value = node.get();
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            func.getInstructions().add(new PushInt(value));
        }
        else {
            func.getInstructions().add(new LoadConst(PoolPutter.put(context, node)));
        }
        stackGrows();
        return null;
    }

    @Override
    public Void visitFloat(FloatTree node, Scope scope) {
        func.getInstructions().add(new LoadConst(PoolPutter.put(context, node)));
        stackGrows();
        return null;
    }

    @Override
    public Void visitNull(NullTree node, Scope scope) {
        func.getInstructions().add(new PushNull());
        stackGrows();
        return null;
    }

    @Override
    public Void visitString(StringTree node, Scope scope) {
        func.getInstructions().add(new LoadConst(PoolPutter.put(context, node)));
        stackGrows();
        return null;
    }

    @Override
    public Void visitBoolean(BooleanTree node, Scope scope) {
        func.getInstructions().add(new PushBool(node.get()));
        stackGrows();
        return null;
    }

    @Override
    public Void visitRange(RangeTree node, Scope scope) {
        scan(node.getFrom(), scope);
        scan(node.getTo(), scope);
        newLine(node);
        func.getInstructions().add(new MakeRange());
        stackShrinks();
        return null;
    }

    @Override
    public Void visitThis(ThisTree node, Scope scope) {
        func.getInstructions().add(new PushThis());
        stackGrows();
        return null;
    }

    @Override
    public Void visitBinaryOperation(BinaryOperationTree node, Scope scope) {
        scan(node.getLeftOperand(), scope);
        scan(node.getRightOperand(), scope);
        newLine(node);
        switch (node.getOperationType()) {
            case EQUALS -> func.getInstructions().add(new Equals());
            case NOT_EQUALS -> func.getInstructions().add(new NotEquals());
            default -> func.getInstructions().add(new BinaryOperation(node.getOperationType()));
        }
        return null;
    }

    @Override
    public Void visitNot(NotTree node, Scope scope) {
        scan(node.getOperand(), scope);
        newLine(node);
        func.getInstructions().add(new Not());
        return null;
    }

    @Override
    public Void visitSign(SignTree node, Scope scope) {
        scan(node.getOperand(), scope);
        newLine(node);
        if (node.isNegation()){
            func.getInstructions().add(new Neg());
        }
        else {
            func.getInstructions().add(new Pos());
        }
        return null;
    }

    @Override
    public Void visitReturn(ReturnTree node, Scope scope) {
        scan(node.getExpression(), scope);
        func.getInstructions().add(new Return());
        stackShrinks();
        return null;
    }

    @Override
    public Void visitArray(ArrayTree node, Scope scope) {
        ListIterator<? extends ExpressionTree> listItr = node.getContents().listIterator(node.getContents().size());
        while (listItr.hasPrevious()) {
            listItr.previous().accept(this, scope);
        }
        func.getInstructions().add(new MakeArray(node.getContents().size()));
        stackShrinks(-node.getContents().size() + 1);
        return null;
    }

    @Override
    public Void visitDictionary(DictionaryTree node, Scope scope) {
        ListIterator<? extends ExpressionTree> keyItr = node.getKeys().listIterator(node.getKeys().size());
        ListIterator<? extends ExpressionTree> valueItr = node.getValues().listIterator(node.getValues().size());

        while (keyItr.hasPrevious()) {
            keyItr.previous().accept(this, scope);
            valueItr.previous().accept(this, scope);
        }

        func.getInstructions().add(new MakeArray(node.getKeys().size()));
        stackShrinks(node.getKeys().size() * -2 + 1);

        return null;
    }

    @Override
    public Void visitLambda(LambdaTree node, Scope scope) {
        LambdaFunction bridge = new LambdaFunction(node, "Lambda@" + context.getNextLambdaIndex());
        FunctionGenerator generator = new FunctionGenerator(context, bridge);
        scope = ((TCTree.TCLambdaTree)node).scope;
        generator.handle(bridge, scope);

        int index = generator.func.getIndex();

        stackGrows(2);
        func.getInstructions().add(new LoadVirtual(index));
        func.getInstructions().add(new Dup());

        stackShrinks();
        func.getInstructions().add(new SetOwner());

        return null;
    }

    @Override
    public Void visitGetType(GetTypeTree node, Scope scope) {
        scan(node.getOperand(), scope);
        func.getInstructions().add(new GetType());
        return null;
    }

    @Override
    public Void visitCall(CallTree node, Scope scope) {

        boolean isMapped = false;
        for (ArgumentTree arg : node.getArguments()) {
            if (arg.getName() != null) {
                isMapped = true;
                break;
            }
        }

        if (isMapped) {
            for (ArgumentTree arg : node.getArguments()) {
                arg.getExpression().accept(this, scope);
                if (arg.getName() != null) {
                    func.getInstructions().add(new ToMapArg(PoolPutter.putUtf8(context, arg.getName())));
                }
                else {
                    func.getInstructions().add(new ToInplaceArg());
                }
            }
            node.getCalled().accept(this, scope);
            stackGrows();
            newLine(node);
            func.getInstructions().add(new CallMapped(node.getArguments().size()));
            stackShrinks(-node.getArguments().size() + 1);
        }
        else {
            for (ArgumentTree arg : node.getArguments()){
                scan(arg.getExpression(), scope);
            }
            node.getCalled().accept(this, scope);
            stackGrows();
            newLine(node);
            func.getInstructions().add(new CallInplace(node.getArguments().size()));
            stackShrinks(-node.getArguments().size() + 1);
        }

        return null;
    }

    @Override
    public Void visitVariable(VariableTree node, Scope scope) {
        stackGrows();

        Symbol sym = scope.accept(null);

        if (sym == null || sym.kind == Symbol.Kind.UNKNOWN){
            func.getInstructions().add(new LoadName(PoolPutter.putUtf8(context, node.getName())));
            return null;
        }

        if (sym.owner .kind == Scope.Kind.GLOBAL) {
            int addr = context.getFile().getGlobalIndex(sym.name);
            func.getInstructions().add(new LoadGlobal(addr));
        }
        else {
            func.getInstructions().add(new LoadLocal(requireLocalAddress(node.getName())));
        }

        return null;
    }

    @Override
    public Void visitArgument(ArgumentTree node, Scope scope) {
        throw new AssertionError("should be covered by visitCall");
    }

    @Override
    public Void visitContainerAccess(ContainerAccessTree node, Scope scope) {
        scan(node.getKey(), scope);
        scan(node.getContainer(), scope);
        newLine(node);
        func.getInstructions().add(new ContainerRead());
        stackShrinks(2);
        return null;
    }

    @Override
    public Void visitMemberAccess(MemberAccessTree node, Scope scope) {
        scan(node.getExpression(), scope);
        newLine(node);
        func.getInstructions().add(new LoadExternal(PoolPutter.putUtf8(context, node.getMemberName())));
        return null;
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatementTree node, Scope scope) {
        scan(node.getExpression(), scope);
        func.getInstructions().add(new Pop());
        stackShrinks();
        return null;
    }

    @Override
    public Void visitUse(UseTree node, Scope scope) {
        scan(node.getVariable(), scope);
        newLine(node);
        func.getInstructions().add(new Use());
        stackShrinks();
        return null;
    }

    @Override
    public Void visitThrow(ThrowTree node, Scope scope) {
        scan(node.getThrown(), scope);
        newLine(node);
        func.getInstructions().add(new Throw());
        return null;
    }

    @Override
    public Void visitIfElse(IfElseTree node, Scope scope) {
        scan(node.getCondition(), scope);

        BranchIfFalse branchIf = new BranchIfFalse(0);
        func.getInstructions().add(branchIf);

        stackShrinks();

        scan(node.getThenStatement(), scope);

        if (node.getElseStatement() == null) {
            branchIf.address = func.getInstructions().size();
        } else {
            Goto goto_ = new Goto(0);
            func.getInstructions().add(goto_);
            int ifEndIndex = func.getInstructions().size();

            scan(node.getElseStatement(), scope);
            int elseEndIndex = func.getInstructions().size();

            branchIf.address = ifEndIndex;
            goto_.address = elseEndIndex;
        }

        return null;
    }

    @Override
    public Void visitBreak(BreakTree node, Scope scope) {
        AddressedInstruction instruction = new Goto(0);
        func.getInstructions().add(instruction);
        loopControlFlowStack.element().add(new BreakAction(instruction));
        return null;
    }

    @Override
    public Void visitContinue(ContinueTree node, Scope scope) {
        AddressedInstruction instruction = new Goto(0);
        func.getInstructions().add(instruction);
        loopControlFlowStack.element().add(new ContinueAction(instruction));
        return null;
    }

    @Override
    public Void visitWhileDoLoop(WhileDoTree node, Scope scope) {
        TransformedWhileDoTree transform = new TransformedWhileDoTree(
                node.getCondition(),
                node.getStatement());
        scan(transform, scope);
        return null;
    }

    @Override
    public Void visitDoWhileLoop(DoWhileTree node, Scope scope) {
        int headerAddress = func.getInstructions().size();

        loopControlFlowStack.push(new LinkedList<>());

        scan(node.getStatement(), scope);

        List<LoopFlowAction> loopCFActions = loopControlFlowStack.remove();

        int conditionStartAddress = func.getInstructions().size();
        scan(node.getCondition(), scope);
        func.getInstructions().add(new BranchIfTrue(headerAddress));
        stackShrinks();

        for (LoopFlowAction loopCFAction : loopCFActions) {
            AddressedInstruction instruction = loopCFAction.getInstruction();
            if (loopCFAction.isBreak()){
                instruction.address = func.getInstructions().size();
            }
            else {
                instruction.address = conditionStartAddress;
            }
        }

        return null;
    }

    @Override
    public Void visitForLoop(ForLoopTree node, Scope scope) {
        newLine(node);
        scope = ((TCTree.TCForLoopTree)node).scope;

        scan(node.getIterable(), scope);
        func.getInstructions().add(new GetItr());
        stackGrows();

        int jumpBackAddr = func.getInstructions().size();
        AddressedInstruction branchItr = new BranchItr(0);
        func.getInstructions().add(branchItr);

        if (node.getVariable() != null) {

            func.getInstructions().add(new ItrNext());
            stackGrows();

            func.getInstructions().add(new StoreLocal(requireLocalAddress(node.getVariable().getName())));
        }
        else {
            func.getInstructions().add(new ItrNext());
            stackGrows();
            func.getInstructions().add(new Pop());
            stackShrinks();
        }

        loopControlFlowStack.push(new LinkedList<>());
        scan(node.getStatement(), scope);
        List<LoopFlowAction> loopCFActions = loopControlFlowStack.remove();

        func.getInstructions().add(new Goto(jumpBackAddr));
        branchItr.address = func.getInstructions().size();

        stackShrinks(); // pop the iterator implicitly

        for (LoopFlowAction loopCFAction : loopCFActions) {
            AddressedInstruction instruction = loopCFAction.getInstruction();
            if (loopCFAction.isBreak()){
                instruction.address = func.getInstructions().size();
            }
            else {
                instruction.address = jumpBackAddr;
            }
        }

        return null;
    }

    @Override
    public Void visitTryCatch(TryCatchTree node, Scope scope) {
        AddressedInstruction enterTry =  new EnterTry(0);
        func.getInstructions().add(enterTry);

        scan(node.getTryStatement(), scope);
        int tryEndIndex = func.getInstructions().size()+1;
        enterTry.address = tryEndIndex+1;
        func.getInstructions().add(new LeaveTry());
        AddressedInstruction goto_ = new Goto(0);
        func.getInstructions().add(goto_);

        //localScope = new LocalScope(scope);
        //localScope.putIfAbsent(SymbolKind.VARIABLE, tryCatchTree.getExceptionName(), Set.of());
        int exVarAddr = requireLocalAddress(node.getExceptionVariable().getName());

        stackGrows();
        func.getInstructions().add(new StoreLocal(exVarAddr));
        stackShrinks();

        scan(node.getCatchStatement(), ((TCTree.TCTryCatchTree)node).exceptionVar.sym.owner);
        goto_.address = func.getInstructions().size();

        return null;
    }

    @Override
    public Void visitImport(ImportTree node, Scope scope) {
        StringJoiner joiner = new StringJoiner(".");
        for (String acc : node.getAccessChain())
            joiner.add(acc);
        newLine(node);
        func.getInstructions().add(new Import(PoolPutter.putUtf8(context, joiner.toString())));
        return null;
    }

    @Override
    public Void visitFromImport(FromImportTree node, Scope scope) {
        StringJoiner joiner = new StringJoiner(".");
        for (String acc : node.getFromAccessChain())
            joiner.add(acc);
        String from = joiner.toString();

        newLine(node);
        func.getInstructions().add(new Import(PoolPutter.putUtf8(context, from)));
        for (String acc : node.getImportAccessChain()){
            func.getInstructions().add(new LoadExternal(PoolPutter.putUtf8(context, acc)));
        }

        return super.visitFromImport(node, scope);
    }

    @Override
    public Void visitVarDef(VarDefTree node, Scope scope) {
        if (node.getInitializer() != null)
            scan(node.getInitializer(), scope);
        else
            visitNull(null, scope);


        if (scope.kind == Scope.Kind.GLOBAL)
            func.getInstructions().add(new StoreGlobal(context.getFile().getGlobalIndex(node.getName())));
        else
            func.getInstructions().add(new StoreLocal(requireLocalAddress(node.getName())));

        return null;
    }

    private boolean isExplicitReturn(){
        if (func.getInstructions().isEmpty()) return false;
        return func.getInstructions().get(func.getInstructions().size()-1) instanceof Return;
    }


    public void stackGrows(){
        stackGrows(1);
    }

    private void stackGrows(int size){
        currentStackSize = currentStackSize + size;
        if(currentStackSize > maxStackSize){
            maxStackSize = currentStackSize;
        }
    }

    public void stackShrinks(){
        stackShrinks(1);
    }

    private void stackShrinks(int size){
        currentStackSize -= size;
    }

    private int requireLocalAddress(String name){
        int addr = currentLocals.indexOf(name);
        if (addr < 0){
            addr = currentLocals.size();
            currentLocals.add(name);
        }
        if(currentLocals.size() > maxLocals){
            maxLocals = currentLocals.size();
        }
        return addr;
    }


    private record TransformedWhileDoTree(ExpressionTree condition, StatementTree body) implements IfElseTree {

        @Override
        public ExpressionTree getCondition() {
            return condition;
        }

        @Override
        public StatementTree getThenStatement() {
            return new DoWhileTree() {
                @Override
                public StatementTree getStatement() {
                    return body;
                }

                @Override
                public ExpressionTree getCondition() {
                    return condition;
                }

                @Override
                public Location getLocation() {
                    return condition.getLocation();
                }
            };
        }

        @Override
        public StatementTree getElseStatement() {
            return null;
        }

        @Override
        public Location getLocation() {
            return condition.getLocation();
        }
    }


}
