package com.tscript.tscriptc.generation.generators;

import com.tscript.tscriptc.analyze.scoping.GlobalScope;
import com.tscript.tscriptc.analyze.scoping.Scope;
import com.tscript.tscriptc.analyze.search.SymbolSearcher;
import com.tscript.tscriptc.analyze.structures.Symbol;
import com.tscript.tscriptc.generation.compiled.CompiledFunction;
import com.tscript.tscriptc.generation.compiled.instruction.*;
import com.tscript.tscriptc.generation.generators.impls.CompFunc;
import com.tscript.tscriptc.generation.generators.impls.LambdaFunction;
import com.tscript.tscriptc.generation.generators.impls.PoolPutter;
import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.TreeScanner;

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

        node.getBody().accept(this, scope);

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
        if (node.getModifiers().getModifiers().contains(Modifier.NATIVE)){
            func.getInstructions().add(new LoadNative(PoolPutter.putUtf8(context, node.getName())));
        }
        else {
            newLine(node);
            int index = FunctionGenerator.generate(context, node, scope.getChildScope(node));
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
        Scope child = scope;
        if (!func.getName().equals("__main__"))
            child = scope.getChildScope(node);
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
        scope = scope.getChildScope(node);
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
        Symbol sym = scope.accept(new SymbolSearcher(), node.getName());

        if (sym != null){
            if (sym.kind == Symbol.Kind.UNKNOWN){
                func.getInstructions().add(new LoadName(PoolPutter.putUtf8(context, node.getName())));
            }
            else if (sym.scope instanceof GlobalScope) {
                int addr = context.getFile().getGlobalIndex(sym.name);
                func.getInstructions().add(new LoadGlobal(addr));
            }
            else {
                func.getInstructions().add(new LoadLocal(requireLocalAddress(node.getName())));
            }
        }
        stackGrows();

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


        if (scope instanceof GlobalScope)
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

}
