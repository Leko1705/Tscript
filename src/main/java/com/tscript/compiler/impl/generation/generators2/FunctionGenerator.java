package com.tscript.compiler.impl.generation.generators2;

import com.tscript.compiler.impl.generation.compiled.CompiledFunction;
import com.tscript.compiler.impl.generation.compiled.instruction.*;
import com.tscript.compiler.impl.generation.generators.BreakAction;
import com.tscript.compiler.impl.generation.generators.Context;
import com.tscript.compiler.impl.generation.generators.ContinueAction;
import com.tscript.compiler.impl.generation.generators.LoopFlowAction;
import com.tscript.compiler.impl.generation.generators.impls.CompFunc;
import com.tscript.compiler.impl.generation.generators.impls.PoolPutter;
import com.tscript.compiler.impl.utils.Scope;
import com.tscript.compiler.impl.utils.Symbol;
import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.impl.utils.TCTree.*;
import com.tscript.compiler.impl.utils.TCTreeScanner;
import com.tscript.compiler.source.tree.*;

import java.util.*;

public class FunctionGenerator extends TCTreeScanner<Void, Void> {

    private final Context context;

    CompFunc func;

    private int maxStackSize = 0;

    private int currentStackSize = maxStackSize;

    private int maxLocals = 0;

    private final Deque<List<LoopFlowAction>> loopControlFlowStack = new ArrayDeque<>();

    private int line;

    private final TCFunctionTree handled;


    public FunctionGenerator(Context context, TCFunctionTree generated) {
        this.context = context;
        func = new CompFunc(context.getNextFunctionIndex(), generated.name);
        this.handled = generated;
    }

    private int asLocalAddress(int addr){
        maxLocals = Math.max(maxLocals, addr + 1);
        return addr;
    }

    public int generate(List<Instruction> preInstructions) {
        func.getInstructions().addAll(preInstructions);

        newLine(handled);

        List<? extends TCParameterTree> params = handled.parameters;
        stackGrows(params.size());
        stackShrinks(params.size());
        scan(params, null);

        if (handled.name.equals("__main__")) {
            for (TCStatementTree stmt : handled.body.statements){
                scan(stmt, null);
            }
        }
        else {
            scan(handled.body, null);
        }

        if (!isExplicitReturn()){
            func.getInstructions().add(new PushNull());
            func.getInstructions().add(new Return());
        }

        func.stackSize = maxStackSize;
        func.locals = maxLocals;

        context.getFile().functions.add(func);

        return func.getIndex();
    }

    @Override
    public Void visitModifiers(TCModifiersTree node, Void unused) {
        return null;
    }

    @Override
    public Void visitFunction(TCFunctionTree node, Void unused) {
        if (node.getModifiers().getFlags().contains(Modifier.NATIVE)){
            func.getInstructions().add(new LoadNative(PoolPutter.putUtf8(context, node.getName())));
        }
        else {
            newLine(node);
            FunctionGenerator generator = new FunctionGenerator(context, node);
            int index = generator.generate(List.of());
            func.getInstructions().add(new LoadVirtual(index));
        }
        stackGrows();

        int addr = node.sym.address;
        func.getInstructions().add(new StoreLocal(asLocalAddress(addr)));
        stackShrinks();

        return null;
    }

    @Override
    public Void visitParameter(TCParameterTree node, Void unused) {
        int addr = node.sym.address;
        func.getInstructions().add(new StoreLocal(asLocalAddress(addr)));
        int defaultAddr = -1;
        if (node.getDefaultValue() != null) defaultAddr = PoolPutter.put(context, node.getDefaultValue());
        func.parameters.add(CompiledFunction.Parameter.of(node.getName(), defaultAddr));
        return null;
    }

    @Override
    public Void visitInteger(TCIntegerTree node, Void unused) {
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
    public Void visitFloat(TCFloatTree node, Void unused) {
        func.getInstructions().add(new LoadConst(PoolPutter.put(context, node)));
        stackGrows();
        return null;
    }

    @Override
    public Void visitNull(TCNullTree node, Void unused) {
        func.getInstructions().add(new PushNull());
        stackGrows();
        return null;
    }

    @Override
    public Void visitString(TCStringTree node, Void unused) {
        func.getInstructions().add(new LoadConst(PoolPutter.put(context, node)));
        stackGrows();
        return null;
    }

    @Override
    public Void visitBoolean(TCBooleanTree node, Void unused) {
        func.getInstructions().add(new PushBool(node.get()));
        stackGrows();
        return null;
    }

    @Override
    public Void visitRange(TCRangeTree node, Void unused) {
        scan(node.from, null);
        scan(node.to, null);
        newLine(node);
        func.getInstructions().add(new MakeRange());
        stackShrinks();
        return null;
    }

    @Override
    public Void visitThis(TCThisTree node, Void unused) {
        func.getInstructions().add(new PushThis());
        stackGrows();
        return null;
    }

    @Override
    public Void visitBinaryOperation(TCBinaryOperationTree node, Void unused) {
        scan(node.left, null);
        scan(node.right, null);
        newLine(node);
        switch (node.getOperationType()) {
            case EQUALS -> func.getInstructions().add(new Equals());
            case NOT_EQUALS -> func.getInstructions().add(new NotEquals());
            default -> func.getInstructions().add(new BinaryOperation(node.getOperationType()));
        }
        return null;
    }

    @Override
    public Void visitNot(TCNotTree node, Void unused) {
        scan(node.operand, null);
        newLine(node);
        func.getInstructions().add(new Not());
        return null;
    }

    @Override
    public Void visitSign(TCSignTree node, Void unused) {
        scan(node.operand, null);
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
    public Void visitReturn(TCReturnTree node, Void unused) {
        scan(node.returned, null);
        func.getInstructions().add(new Return());
        stackShrinks();
        return null;
    }

    @Override
    public Void visitArray(TCArrayTree node, Void unused) {
        ListIterator<? extends TCExpressionTree> listItr = node.content.listIterator(node.getContents().size());
        while (listItr.hasPrevious()) {
            listItr.previous().accept(this, null);
        }
        func.getInstructions().add(new MakeArray(node.getContents().size()));
        stackShrinks(-node.getContents().size() + 1);
        return null;
    }

    @Override
    public Void visitDictionary(TCDictionaryTree node, Void unused) {
        ListIterator<? extends TCExpressionTree> keyItr = node.keys.listIterator(node.getKeys().size());
        ListIterator<? extends TCExpressionTree> valueItr = node.values.listIterator(node.getValues().size());

        while (keyItr.hasPrevious()) {
            keyItr.previous().accept(this, null);
            valueItr.previous().accept(this, null);
        }

        func.getInstructions().add(new MakeArray(node.getKeys().size()));
        stackShrinks(node.getKeys().size() * -2 + 1);

        return null;
    }

    @Override
    public Void visitLambda(TCLambdaTree node, Void unused) {
        LambdaFunction bridge = new LambdaFunction(node, "Lambda@" + context.getNextLambdaIndex());
        FunctionGenerator generator = new FunctionGenerator(context, bridge);
        generator.generate(List.of());

        int index = generator.func.getIndex();

        stackGrows(2);
        func.getInstructions().add(new LoadVirtual(index));
        func.getInstructions().add(new Dup());

        stackShrinks();
        func.getInstructions().add(new SetOwner());

        return null;
    }

    @Override
    public Void visitGetType(TCGetTypeTree node, Void unused) {
        scan(node.operand, null);
        func.getInstructions().add(new GetType());
        return null;
    }

    @Override
    public Void visitCall(TCCallTree node, Void unused) {

        boolean isMapped = false;
        for (ArgumentTree arg : node.getArguments()) {
            if (arg.getName() != null) {
                isMapped = true;
                break;
            }
        }

        if (isMapped) {
            for (TCArgumentTree arg : node.arguments) {
                scan(arg.expression, null);
                if (arg.getName() != null) {
                    func.getInstructions().add(new ToMapArg(PoolPutter.putUtf8(context, arg.getName())));
                }
                else {
                    func.getInstructions().add(new ToInplaceArg());
                }
            }
            node.called.accept(this, null);
            stackGrows();
            newLine(node);
            func.getInstructions().add(new CallMapped(node.getArguments().size()));
            stackShrinks(-node.getArguments().size() + 1);
        }
        else {
            for (TCArgumentTree arg : node.arguments){
                scan(arg.expression, null);
            }
            node.called.accept(this, null);
            stackGrows();
            newLine(node);
            func.getInstructions().add(new CallInplace(node.getArguments().size()));
            stackShrinks(-node.getArguments().size() + 1);
        }

        return null;
    }

    @Override
    public Void visitArgument(TCArgumentTree node, Void unused) {
        throw new AssertionError("should be covered by visitCall");
    }

    @Override
    public Void visitBlock(TCBlockTree node, Void unused) {
        for (TCStatementTree stmt : node.statements) {
            scan(stmt, null);
            if (stmt instanceof ReturnTree) break;
            if (stmt instanceof ContinueTree) break;
            if (stmt instanceof BreakTree) break;
            if (stmt instanceof ThrowTree) break;
        }
        return null;
    }

    @Override
    public Void visitContainerAccess(TCContainerAccessTree node, Void unused) {
        scan(node.key, null);
        scan(node.container, null);
        newLine(node);
        func.getInstructions().add(new ContainerRead());
        stackShrinks(2);
        return null;
    }

    @Override
    public Void visitMemberAccess(TCMemberAccessTree node, Void unused) {
        scan(node.expression, null);
        newLine(node);
        func.getInstructions().add(new LoadExternal(PoolPutter.putUtf8(context, node.getMemberName())));
        return null;
    }

    @Override
    public Void visitExpressionStatement(TCExpressionStatementTree node, Void unused) {
        scan(node.expression, null);
        func.getInstructions().add(new Pop());
        stackShrinks();
        return null;
    }

    @Override
    public Void visitUse(TCUseTree node, Void unused) {
        scan(node.varTree, null);
        newLine(node);
        func.getInstructions().add(new Use());
        stackShrinks();
        return null;
    }

    @Override
    public Void visitThrow(TCThrowTree node, Void unused) {
        scan(node.thrown, null);
        newLine(node);
        func.getInstructions().add(new Throw());
        return null;
    }

    @Override
    public Void visitIfElse(TCIfElseTree node, Void unused) {
        scan(node.condition, null);

        BranchIfFalse branchIf = new BranchIfFalse(0);
        func.getInstructions().add(branchIf);

        stackShrinks();

        scan(node.thenStatement, null);

        if (node.getElseStatement() == null) {
            branchIf.address = func.getInstructions().size();
        } else {
            Goto goto_ = new Goto(0);
            func.getInstructions().add(goto_);
            int ifEndIndex = func.getInstructions().size();

            scan(node.elseStatement, null);
            int elseEndIndex = func.getInstructions().size();

            branchIf.address = ifEndIndex;
            goto_.address = elseEndIndex;
        }

        return null;
    }

    @Override
    public Void visitBreak(TCBreakTree node, Void unused) {
        AddressedInstruction instruction = new Goto(0);
        func.getInstructions().add(instruction);
        loopControlFlowStack.element().add(new BreakAction(instruction));
        return null;
    }

    @Override
    public Void visitContinue(TCContinueTree node, Void unused) {
        AddressedInstruction instruction = new Goto(0);
        func.getInstructions().add(instruction);
        loopControlFlowStack.element().add(new ContinueAction(instruction));
        return null;
    }

    @Override
    public Void visitDoWhileLoop(TCDoWhileTree node, Void unused) {
        int headerAddress = func.getInstructions().size();

        loopControlFlowStack.push(new LinkedList<>());

        scan(node.statement, null);

        List<LoopFlowAction> loopCFActions = loopControlFlowStack.remove();

        int conditionStartAddress = func.getInstructions().size();
        scan(node.condition, null);
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
    public Void visitForLoop(TCForLoopTree node, Void unused) {
        newLine(node);

        scan(node.iterable, null);
        func.getInstructions().add(new GetItr());
        stackGrows();

        int jumpBackAddr = func.getInstructions().size();
        AddressedInstruction branchItr = new BranchItr(0);
        func.getInstructions().add(branchItr);

        if (node.getVariable() != null) {

            func.getInstructions().add(new ItrNext());
            stackGrows();
            int addr = ((TCTree.TCVarDefTree)node.getVariable()).sym.address;
            func.getInstructions().add(new StoreLocal(asLocalAddress(addr)));
        }
        else {
            func.getInstructions().add(new ItrNext());
            stackGrows();
            func.getInstructions().add(new Pop());
            stackShrinks();
        }

        loopControlFlowStack.push(new LinkedList<>());
        scan(node.statement, null);
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
    public Void visitTryCatch(TCTryCatchTree node, Void unused) {
        AddressedInstruction enterTry =  new EnterTry(0);
        func.getInstructions().add(enterTry);

        scan(node.tryStatement, null);
        int tryEndIndex = func.getInstructions().size()+1;
        enterTry.address = tryEndIndex+1;
        func.getInstructions().add(new LeaveTry());
        AddressedInstruction goto_ = new Goto(0);
        func.getInstructions().add(goto_);

        int exVarAddr = node.exceptionVar.sym.address;

        stackGrows();
        func.getInstructions().add(new StoreLocal(asLocalAddress(exVarAddr)));
        stackShrinks();

        scan(node.catchStatement, null);
        goto_.address = func.getInstructions().size();

        return null;
    }

    @Override
    public Void visitImport(TCImportTree node, Void unused) {
        StringJoiner joiner = new StringJoiner(".");
        for (String acc : node.getAccessChain())
            joiner.add(acc);
        newLine(node);
        func.getInstructions().add(new Import(PoolPutter.putUtf8(context, joiner.toString())));
        return null;
    }

    @Override
    public Void visitFromImport(TCFromImportTree node, Void unused) {
        StringJoiner joiner = new StringJoiner(".");
        for (String acc : node.getFromAccessChain())
            joiner.add(acc);
        String from = joiner.toString();

        newLine(node);
        func.getInstructions().add(new Import(PoolPutter.putUtf8(context, from)));
        for (String acc : node.getImportAccessChain()){
            func.getInstructions().add(new LoadExternal(PoolPutter.putUtf8(context, acc)));
        }

        return super.visitFromImport(node, null);
    }

    @Override
    public Void visitVarDefs(TCVarDefsTree node, Void unused) {
        return super.visitVarDefs(node, unused);
    }

    @Override
    public Void visitVarDef(TCVarDefTree node, Void unused) {
        scan(node.initializer, null);
        if (node.initializer == null){
            visitNull(null, null);
        }
        int addr = node.sym.address;
        if (node.sym.owner.kind == Scope.Kind.GLOBAL){
            func.getInstructions().add(new StoreGlobal(addr));
        }
        else {
            func.getInstructions().add(new StoreLocal(asLocalAddress(addr)));
        }
        stackShrinks();
        return null;
    }

    @Override
    public Void visitVariable(TCVariableTree node, Void unused) {
        stackGrows();

        int addr = node.sym.address;

        if (node.sym.owner.kind == Scope.Kind.GLOBAL){
            func.getInstructions().add(new LoadGlobal(addr));
            return null;
        }

        if (node.sym.owner.kind.isContainer()){
            if (node.sym.isStatic()){
                func.getInstructions().add(new LoadStatic(PoolPutter.putUtf8(context, node.name)));
            }
            else {
                func.getInstructions().add(new LoadInternal(PoolPutter.putUtf8(context, node.name)));
            }
            return null;
        }

        if (node.sym.kind == Symbol.Kind.UNKNOWN){
            func.getInstructions().add(new LoadName(PoolPutter.putUtf8(context, node.name)));
            return null;
        }

        func.getInstructions().add(new LoadLocal(addr));
        return null;
    }

    @Override
    public Void visitSuper(TCSuperTree node, Void unused) {
        func.getInstructions().add(new LoadInternal(PoolPutter.putUtf8(context, node.name)));
        return null;
    }

    private void newLine(Tree tree){
        int newLine = tree.getLocation().line();
        if (newLine > line){
            line = newLine;
            func.getInstructions().add(new NewLine(line));
        }
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


    private boolean isExplicitReturn(){
        if (func.getInstructions().isEmpty()) return false;
        return func.getInstructions().get(func.getInstructions().size()-1) instanceof Return;
    }

    private boolean inGlobalScope(Scope scope){
        while (scope != null && scope.kind == Scope.Kind.LOCAL)
            scope = scope.enclosing;
        return scope != null && scope.kind == Scope.Kind.GLOBAL;
    }
}
