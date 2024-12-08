package com.tscript.runtime.jit.optim;

import com.tscript.runtime.jit.graph.BranchNode;
import com.tscript.runtime.jit.graph.Context;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.graph.expr.*;
import com.tscript.runtime.jit.graph.traversal.TreeTraverseStrategy;

/**
 * Removes NEW_LINE instructions and ENTER_TRY as well as LEAVE_TRY instructions
 * if possible.
 */
public class UnsafeReducer implements Optimizer {

    private boolean optimized;

    private Node prev;


    private Node prevNewLineNode = null;
    private boolean unsafeDetected = false;

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
            prevNewLineNode = null;
            unsafeDetected = true;
        }

        @Override
        public Void visitNewLine(NewLine expr, Void unused) {
            if (prevNewLineNode != null) {
                optimized = true;
                prevNewLineNode.setNext(prevNewLineNode.getNext().getNext());
            }
            prevNewLineNode = prev;
            return null;
        }

        @Override
        public Void visitCallInp(CallInp expr, Void unused) {
            super.visitCallInp(expr, null);
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
