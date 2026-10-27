package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.analysis.Boxing;
import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.utils.State;

import java.util.Deque;
import java.util.List;

public class EscapeAnalysis implements IROptimizer,
        IRControlVisitor<Void, Void>, IREffectVisitor<Void, Void>,
        IRValueVisitor<Void, Void> {

    private Graph graph;
    private boolean optimize;

    private State<Boolean> escaped;

    private final State<Runnable> recentPop = new State<>(() -> {});

    @Override
    public boolean optimize(Graph graph) {
        optimize = false;
        escaped = new State<>(false);
        this.graph = graph;
        for (IRControl exit : graph.exitControls()) {
            exit.accept(this);
        }
        return optimize;
    }


    @Override
    public Void visitStart(StartControl node, Void unused) {
        traverseEffectsInReverse(node);
        for (IRControl pred : node.getPredecessors()) pred.accept(this);
        return null;
    }

    @Override
    public Void visitReturn(Return node, Void unused) {
        node.getValue().accept(this);
        traverseEffectsInReverse(node);
        for (IRControl pred : node.getPredecessors()) pred.accept(this);
        return null;
    }

    @Override
    public Void visitMerge(MergeControl node, Void unused) {
        traverseEffectsInReverse(node);
        return null;
    }

    @Override
    public Void visitIfElse(IfElse node, Void unused) {
        node.getCondition().accept(this);
        traverseEffectsInReverse(node);
        for (IRControl pred : node.getPredecessors()) pred.accept(this);
        return null;
    }

    @Override
    public Void visitStart(StartEffect node, Void unused) {
        return null;
    }

    @Override
    public Void visitNewLine(NewLine node, Void unused) {
        if (!escaped.get()) {
            node.remove();
            optimize = true;
        }
        else if (node.next instanceof NewLine) {
            node.remove();
            optimize = true;
        }
        return null;
    }

    @Override
    public Void visitGuardBool(GuardBool node, Void unused) {
        node.getValue().accept(this);
        escape();
        return null;
    }

    @Override
    public Void visitGuardFunctionIdentity(GuardFunctionIdentity node, Void unused) {
        node.getValue().accept(this);
        escape();
        return null;
    }

    @Override
    public Void visitValueEffect(ValueEffect node, Void unused) {
        node.getValue().accept(this);
        return null;
    }

    @Override
    public Void visitMergeEffect(MergeEffect node, Void unused) {
        return null;
    }

    @Override
    public Void visitPushFrame(PushFrame node, Void unused) {
        Runnable recent = recentPop.getAndRestore();
        if (!escaped.get()) {
            recent.run();
            node.remove();
        }
        if (!escaped.isEmpty()) {
            escaped.restore();
        }
        return null;
    }

    @Override
    public Void visitPopFrame(PopFrame node, Void unused) {
        recentPop.storeAndSet(node::remove);
        escaped.storeAndSet(false);
        return null;
    }

    @Override
    public Void visitInteger(Int node, Void unused) {
        return null;
    }

    @Override
    public Void visitReal(Real node, Void unused) {
        return null;
    }

    @Override
    public Void visitString(Str node, Void unused) {
        return null;
    }

    @Override
    public Void visitBool(Bool node, Void unused) {
        return null;
    }

    @Override
    public Void visitNull(NullValue node, Void unused) {
        return null;
    }

    @Override
    public Void visitArray(Array node, Void unused) {
        for (IRValue value : node.getValues()) value.accept(this);
        return null;
    }

    @Override
    public Void visitDict(Dict node, Void unused) {
        for (IRValue value : node.getAll()) value.accept(this);
        return null;
    }

    @Override
    public Void visitRange(Range node, Void unused) {
        node.getFrom().accept(this);
        node.getTo().accept(this);
        return null;
    }

    @Override
    public Void visitThis(This node, Void unused) {
        return null;
    }

    @Override
    public Void visitAdd(Add node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitSub(Sub node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitMul(Mul node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitDiv(Div node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitIDiv(IDiv node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitMod(Mod node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitPow(Pow node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitShl(Shl node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitShr(Shr node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitUshr(Ushr node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitAnd(And node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitOr(Or node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitXor(Xor node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitNot(Not node, Void unused) {
        node.getValue().accept(this);
        if (Boxing.isBoxed(node.getValue(), graph)) {
            escape();
        }
        return null;
    }

    @Override
    public Void visitNeg(Neg node, Void unused) {
        node.getValue().accept(this);
        if (Boxing.isBoxed(node.getValue(), graph)) {
            escape();
        }
        return null;
    }

    @Override
    public Void visitPos(Pos node, Void unused) {
        node.getValue().accept(this);
        if (Boxing.isBoxed(node.getValue(), graph)) {
            escape();
        }
        return null;
    }

    @Override
    public Void visitType(Type node, Void unused) {
        return null;
    }

    @Override
    public Void visitTypeof(Typeof node, Void unused) {
        node.getValue().accept(this);
        return null;
    }

    @Override
    public Void visitGt(Gt node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitLt(Lt node, Void unused) {
        analyseBinary(node);
        return null;
    }

    @Override
    public Void visitEquals(Equals node, Void unused) {
        node.getLeft().accept(this);
        node.getRight().accept(this);
        return null;
    }

    @Override
    public Void visitParameter(Parameter node, Void unused) {
        return null;
    }

    @Override
    public Void visitBuiltin(Builtin node, Void unused) {
        return null;
    }

    @Override
    public Void visitToInpArg(ToInpArg node, Void unused) {
        node.getValue().accept(this);
        return null;
    }

    @Override
    public Void visitToMapArg(ToMapArg node, Void unused) {
        node.getValue().accept(this);
        return null;
    }

    @Override
    public Void visitCall(Call node, Void unused) {
        node.getCalled().accept(this);
        for (IRValue arg : node.getArgs()) arg.accept(this);
        escape();
        return null;
    }

    @Override
    public Void visitToString(ToString node, Void unused) {
        node.getValue().accept(this);
        return null;
    }

    @Override
    public Void visitPhi(Phi node, Void unused) {
        for (IRValue arg : node.getArgs()) arg.accept(this);
        return null;
    }

    @Override
    public Void visitLocal(Local node, Void unused) {
        return null;
    }

    @Override
    public Void visitGlobal(Global node, Void unused) {
        return null;
    }

    private void traverseEffectsInReverse(IRControl node) {
        List<IREffect> effects = graph.getEffectsOf(node);

        for (int i = effects.size() - 1; i >= 0; i--) {
            IREffect effect = effects.get(i);
            effect.accept(this);
        }
    }

    private void analyseBinary(BinaryOp node) {
        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        lhs.accept(this);
        rhs.accept(this);

        if (Boxing.isBoxed(lhs, graph) || Boxing.isBoxed(rhs, graph)) {
            escape();
        }
    }

    private void escape() {
        escaped.set(true);
    }

}
