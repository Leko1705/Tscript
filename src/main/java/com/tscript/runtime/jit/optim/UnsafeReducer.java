package com.tscript.runtime.jit.optim;

import com.tscript.runtime.core.Builtins;
import com.tscript.runtime.jit.graph.BranchNode;
import com.tscript.runtime.jit.graph.Context;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.graph.expr.*;
import com.tscript.runtime.jit.graph.traversal.TreeTraverseStrategy;
import com.tscript.runtime.jit.optim.post.PostOptimizer;
import com.tscript.runtime.tni.natfuncs.std.NativePrint;
import com.tscript.runtime.typing.TObject;

/**
 * Removes NEW_LINE instructions and ENTER_TRY as well as LEAVE_TRY instructions
 * if possible.
 */
public class UnsafeReducer implements Optimizer, PostOptimizer {

    private boolean optimized;

    private Node prev;


    private static class UnsafeState {
        Node prevFramePushNode = null;
        Node prevNewLineNode = null;
        boolean unsafeDetected = false;
    }

    private final State<UnsafeState> state = new State<>(new UnsafeState());

    @Override
    public Node optimize(Node graph, com.tscript.runtime.typing.Type[] inputTypes) {
        return optimize(graph);
    }

    @Override
    public Node optimize(Node graph) {
        optimized = false;

        Trav trav = new Trav();
        Handler handler = new Handler();

        Context context = new Context(graph);

        while (context.getNext() != null){
            Node current = context.getNext();
            Expression expr = current.getExpr();
            expr.accept(handler);
            prev = current;
            current.step(trav, context);
        }

        handler.clearLine();
        return graph;
    }

    @Override
    public boolean isOptimized() {
        return optimized;
    }

    private static class Trav extends TreeTraverseStrategy {
        @Override
        public boolean isThenFirst(BranchNode node, Context context) {
            return false;
        }
    }

    private class Handler extends ExpressionScanner<Void, Void> {

        private void handleUnsafe(){
            UnsafeState unsafeState = state.get();
            unsafeState.prevNewLineNode = null;
            unsafeState.unsafeDetected = true;
            unsafeState.prevFramePushNode = null;
        }

        private void clearLine(){
            UnsafeState unsafeState = state.get();
            if (unsafeState.prevNewLineNode != null) {
                optimized = true;
                unsafeState.prevNewLineNode.setNext(unsafeState.prevNewLineNode.getNext().getNext());
            }
        }

        @Override
        public Void visitNewLine(NewLine expr, Void unused) {
            clearLine();
            state.get().prevNewLineNode = prev;
            return null;
        }

        @Override
        public Void visitCallInp(CallInp expr, Void unused) {
            super.visitCallInp(expr, null);
            if (expr.called instanceof Builtin builtin) {
                TObject obj = Builtins.load(builtin.index());

                if (obj instanceof NativePrint && expr.args.size() == 1){
                    return null;
                }
            }
            handleUnsafe();
            return null;
        }

        @Override
        public Void visitCallMap(CallMap expr, Void unused) {
            super.visitCallMap(expr, unused);
            handleUnsafe();
            return null;
        }

        @Override
        public Void visitPushFrame(PushFrame expr, Void unused) {
            super.visitPushFrame(expr, unused);
            UnsafeState newState = new UnsafeState();
            newState.prevFramePushNode = prev;
            state.storeAndSet(newState);
            return null;
        }

        @Override
        public Void visitPushSuperFrame(PushSuperFrame expr, Void unused) {
            return super.visitPushSuperFrame(expr, unused);
        }

        @Override
        public Void visitPopFrame(PopFrame expr, Void unused) {
            super.visitPopFrame(expr, unused);

            UnsafeState prevState = state.get();
            clearLine();
            if (prevState.prevFramePushNode != null){
                prevState.prevFramePushNode.setNext(prevState.prevFramePushNode.getNext().getNext());
                prev.setNext(prev.getNext().getNext());
            }

            state.restore();
            return null;
        }

        @Override
        public Void visitBinaryOp(BinaryOp expr, Void unused) {
            super.visitBinaryOp(expr, unused);
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitAbstract(Abstract expr, Void unused) {
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitBoolGuard(BoolGuard expr, Void unused) {
            super.visitBoolGuard(expr, unused);
            handleUnsafe();
            return null;
        }

        @Override
        public Void visitContainerRead(ContainerRead expr, Void unused) {
            super.visitContainerRead(expr, unused);
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitContainerWrite(ContainerWrite expr, Void unused) {
            super.visitContainerWrite(expr, unused);
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitImportModule(ImportModule expr, Void unused) {
            if (expr.index > -1)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitItr(Itr expr, Void unused) {
            super.visitItr(expr, unused);
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitLoadName(LoadName expr, Void unused) {
            super.visitLoadName(expr, unused);
            if (expr.canFail())
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitLoadExternal(LoadExternal expr, Void unused) {
            if (expr.index != -1)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitStoreExternal(StoreExternal expr, Void unused) {
            super.visitStoreExternal(expr, unused);
            if (expr.index != -1)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitItrNext(ItrNext expr, Void unused) {
            handleUnsafe();
            return null;
        }

        @Override
        public Void visitLoadStatic(LoadStatic expr, Void unused) {
            super.visitLoadStatic(expr, unused);
            if (expr.index != -1)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitStoreStatic(StoreStatic expr, Void unused) {
            super.visitStoreStatic(expr, unused);
            if (expr.index != -1)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitLoadSuper(LoadSuper expr, Void unused) {
            super.visitLoadSuper(expr, unused);
            if (expr.index != -1)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitStoreSuper(StoreSuper expr, Void unused) {
            super.visitStoreSuper(expr, unused);
            if (expr.index != -1)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitUseMembers(UseMembers expr, Void unused) {
            super.visitUseMembers(expr, unused);
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitUse(Use expr, Void unused) {
            super.visitUse(expr, unused);
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitMethod(Method expr, Void unused) {
            super.visitMethod(expr, unused);
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitNeg(Neg expr, Void unused) {
            super.visitNeg(expr, unused);
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitNot(Not expr, Void unused) {
            super.visitNot(expr, unused);
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitNative(Native expr, Void unused) {
            super.visitNative(expr, unused);
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitRange(Range expr, Void unused) {
            super.visitRange(expr, unused);
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitExtended(Extended expr, Void unused) {
            super.visitExtended(expr, unused);
            if (expr.canFail)
                handleUnsafe();
            return null;
        }

        @Override
        public Void visitTypeGuard(TypeGuard expr, Void unused) {
            handleUnsafe();
            return null;
        }

        @Override
        public Void visitItrNextGuard(ItrNextGuard expr, Void unused) {
            handleUnsafe();
            return null;
        }

    }

}
