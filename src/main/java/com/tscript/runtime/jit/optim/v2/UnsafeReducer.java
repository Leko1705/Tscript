package com.tscript.runtime.jit.optim.v2;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.ir.*;
import com.tscript.runtime.jit.ir.nodes.*;
import com.tscript.runtime.jit.ir.types.*;
import com.tscript.runtime.jit.optim.State;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class UnsafeReducer extends IRScanner<Void, Void> implements IROptimizer {

    private static class UnsafeState {
        int lastNewLineNumber = -1;
        Runnable removeLastNewLine = null;
        Runnable removeLastPushFrame = null;
        boolean unsafeDetected = false;
    }

    private boolean optimized;
    private final Queue<Runnable> optimizations = new LinkedList<>();

    private IRBlock currentBlock;
    private State<UnsafeState> state;


    @Override
    public IRBlock optimize(IRBlock block, com.tscript.runtime.typing.Type[] inputTypes) {
        optimized = false;
        state = new State<>(new UnsafeState());

        IRWalker<Void, Void> walker = new IRTreeWalker<>() {
            @Override
            public void onBlockEnter(IRBlock block) {
                currentBlock = block;
            }
            @Override
            public void onBlockExit(IRBlock block) {
                clearLine();
                state.get().lastNewLineNumber = -1;
            }
        };

        walker.walk(this, block);

        while (!optimizations.isEmpty()) {
            Runnable optimization = optimizations.poll();
            optimization.run();
            optimized = true;
        }

        return block;
    }

    @Override
    public boolean isOptimized() {
        return optimized;
    }


    private void handleUnsafe(){
        UnsafeReducer.UnsafeState unsafeState = state.get();
        unsafeState.removeLastNewLine = null;
        unsafeState.unsafeDetected = true;
        unsafeState.removeLastPushFrame = null;
    }

    private void clearLine(){
        UnsafeReducer.UnsafeState unsafeState = state.get();
        if (unsafeState.removeLastNewLine != null) {
            optimizations.add(unsafeState.removeLastNewLine);
        }
    }

    private boolean clearFramePush() {
        UnsafeState prevState = state.get();
        if (prevState.removeLastPushFrame != null){
            optimizations.add(prevState.removeLastPushFrame);
            return true;
        }
        return false;
    }


    @Override
    public Void visitNewLine(NewLine node, Void unused) {
        UnsafeState currentState = state.get();

        if (currentState.lastNewLineNumber == node.line) {
            optimizations.add(removeNodeOperation(currentBlock, node));
            return null;
        }

        if (currentState.removeLastNewLine != null) {
            optimizations.add(currentState.removeLastNewLine);
        }

        currentState.removeLastNewLine = removeNodeOperation(currentBlock, node);
        currentState.lastNewLineNumber = node.line;
        return null;
    }

    @Override
    public Void visitPushFrame(PushFrame node, Void unused) {
        super.visitPushFrame(node, unused);
        UnsafeState newState = new UnsafeState();
        newState.removeLastPushFrame = removeNodeOperation(currentBlock, node);
        state.storeAndSet(newState);
        return null;
    }

    @Override
    public Void visitPopFrame(PopFrame node, Void unused) {
        clearLine();
        boolean optimized = clearFramePush();
        if (optimized) {
            optimizations.add(removeNodeOperation(currentBlock, node));
        }
        state.restore();
        return null;
    }


    @Override
    public Void visitLoadAbstract(LoadAbstract node, Void unused) {
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitGuard(Guard node, Void unused) {
        super.visitGuard(node, unused);
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitContainerRead(ContainerRead node, Void unused) {
        super.visitContainerRead(node, unused);
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitContainerWrite(ContainerWrite node, Void unused) {
        super.visitContainerWrite(node, unused);
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitImportModule(ImportModule node, Void unused) {
        if (node.index > -1) {
            handleUnsafe();
        }
        return null;
    }

    @Override
    public Void visitItr(Itr node, Void unused) {
        super.visitItr(node, unused);
        if (!node.iterable.getType().isIterable()) {
            handleUnsafe();
        }
        return null;
    }

    @Override
    public Void visitItrNext(ItrNext node, Void unused) {
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitLoadName(LoadName node, Void unused) {
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitLoadExternal(LoadExternal node, Void unused) {
        super.visitLoadExternal(node, unused);
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitStoreExternal(StoreExternal node, Void unused) {
        super.visitStoreExternal(node, unused);
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitLoadStatic(LoadStatic node, Void unused) {
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitStoreStatic(StoreStatic node, Void unused) {
        super.visitStoreStatic(node, unused);
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitLoadSuper(LoadSuper node, Void unused) {
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitStoreSuper(StoreSuper node, Void unused) {
        super.visitStoreSuper(node, unused);
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitUseMembers(UseMembers node, Void unused) {
        super.visitUseMembers(node, unused);
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitUse(Use node, Void unused) {
        super.visitUse(node, unused);
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitSetOwner(SetOwner node, Void unused) {
        super.visitSetOwner(node, unused);
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitBinaryOp(BinaryOp node, Void unused) {
        super.visitBinaryOp(node, unused);

        IRType leftType = node.lhs.getType();
        IRType rightType = node.rhs.getType();

        if (leftType.isType(IRStringType.class) || rightType.isType(IRStringType.class)) {
            return null;
        }

        if (leftType.isNumeric() && rightType.isNumeric()) {
            if (Set.of(Opcode.ADD, Opcode.SUB, Opcode.MUL, Opcode.DIV, Opcode.POW,
                    Opcode.LT, Opcode.GT, Opcode.LEQ, Opcode.GEQ).contains(node.operation)) {
                return null;
            }
        }

        if (leftType.isType(IRIntegerType.class) && rightType.isType(IRIntegerType.class)) {
            if (Set.of(Opcode.IDIV, Opcode.MOD, Opcode.SLA, Opcode.SRL,
                    Opcode.SRA, Opcode.AND, Opcode.OR, Opcode.XOR).contains(node.operation)) {
                return null;
            }
        }

        if (leftType.isType(IRBooleanType.class) && rightType.isType(IRBooleanType.class)) {
            if (Set.of(Opcode.AND, Opcode.OR, Opcode.XOR).contains(node.operation)) {
                return null;
            }
        }

        handleUnsafe();
        return null;
    }

    @Override
    public Void visitNeg(Neg node, Void unused) {
        super.visitNeg(node, unused);

        boolean isInt = node.value.getType().isType(IRIntegerType.class);
        boolean isReal = node.value.getType().isType(IRRealType.class);

        if (!isInt && !isReal) {
            handleUnsafe();
        }
        return null;
    }

    @Override
    public Void visitNot(Not node, Void unused) {
        super.visitNot(node, unused);

        boolean isBool = node.value.getType().isType(IRBooleanType.class);
        boolean isInt = node.value.getType().isType(IRIntegerType.class);

        if (!isBool && !isInt) {
            handleUnsafe();
        }
        return null;
    }

    @Override
    public Void visitNative(Native node, Void unused) {
        handleUnsafe();
        return null;
    }

    @Override
    public Void visitRange(Range node, Void unused) {
        super.visitRange(node, unused);

        boolean leftIsInt = node.from.getType().isType(IRIntegerType.class);
        boolean rightIsInt = node.to.getType().isType(IRIntegerType.class);

        if (!(leftIsInt && rightIsInt)) {
            handleUnsafe();
        }
        return null;
    }

    @Override
    public Void visitExtend(Extend node, Void unused) {
        super.visitExtend(node, unused);
        handleUnsafe();
        return null;
    }

    private static Runnable removeNodeOperation(IRBlock owner, IRNode node) {
        return () -> {
            List<IRNode> code = owner.getCode();
            code.remove(node);
        };
    }

}
