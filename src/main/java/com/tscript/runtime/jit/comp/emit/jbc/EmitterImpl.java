package com.tscript.runtime.jit.comp.emit.jbc;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.tracing.SnapshotShape;
import com.tscript.runtime.jit.tracing.Snapshots;
import com.tscript.runtime.jit.comp.analysis.Boxing;
import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.types.*;


import java.util.*;

public class EmitterImpl
        implements IRControlVisitor<Void, Void>,
        IRValueVisitor<Void, Void>, IREffectVisitor<Void, Void> {

    public int nextGuardId = 0;

    private final JBC jbc;
    private final Graph graph;
    private final JITCompiler compiler;
    private final Set<IRNode> visited;
    private long nextJumpLabel = 0;

    private final Deque<Integer> freeRegisterAddresses = new ArrayDeque<>();
    private final Map<IRValue, Integer> registers;

    private final Graph.TypeMap typeMap;

    public EmitterImpl(String className, JITCompiler compiler, Graph graph) {
        this.jbc = new JBC(className);
        this.graph = graph;
        this.compiler = compiler;
        this.visited = new HashSet<>();
        this.registers = new HashMap<>();
        this.typeMap = graph.typeMap();
    }

    public byte[] emit() {
        if (graph.exitControls().isEmpty()) {
            throw new IllegalStateException("IRGraph must have at least one exit control");
        }
        for (IRControl ctrl : graph.exitControls()) {
            ctrl.accept(this);
        }

        return jbc.build();
    }

    private boolean visited(IRNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean preferLoadRegister(IRValue value) {
        if (registers.containsKey(value)) {

            int addr = registers.get(value);
            jbc.emitLoadTemp(typeMap.getType(value).getJvmType(), addr);

            if (value.getUsers().isEmpty()) {
                freeRegisterAddresses.addLast(addr);
                registers.remove(value);
            }
            return true;
        }
        return value.getUsers().isEmpty();
    }

    private Void storeRegisterIfRequired(IRValue value) {
        if (value.getUsers().size() > 1){
            int addr = registers.computeIfAbsent(value, k -> freeRegisterAddresses.isEmpty() ? registers.size() : freeRegisterAddresses.removeFirst());
            jbc.emitStoreTemp(addr, false);
            jbc.emitLoadTemp(typeMap.getType(value).getJvmType(), addr);
        }
        return null;
    }

    @Override
    public Void visitStart(StartControl node, Void unused) {
        if (visited(node)) return null;
        for (IREffect effect : graph.getEffectsOf(node)) {
            effect.accept(this);
        }
        return null;
    }

    @Override
    public Void visitReturn(Return node, Void unused) {
        if (visited(node)) return null;
        for (IRControl pred : node.getPredecessors()) {
            pred.accept(this);
        }
        for (IREffect effect : graph.getEffectsOf(node)) {
            effect.accept(this);
        }
        box(node.getValue());
        jbc.emitReturn();
        return null;
    }

    @Override
    public Void visitMerge(MergeControl node, Void unused) {
        if (visited(node)) return null;

        long label = nextJumpLabel++;

        for (IRControl pred : node.getPredecessors()) {
            pred.accept(this);
            jbc.emitGoto(label);
        }

        jbc.emitLabel(label);

        for (IREffect effect : graph.getEffectsOf(node)) {
            effect.accept(this);
        }

        return null;
    }

    @Override
    public Void visitIfElse(IfElse node, Void unused) {
        if (visited(node)) return null;

        for (IRControl pred : node.getPredecessors()) {
            pred.accept(this);
        }

        for (IREffect effect : graph.getEffectsOf(node)) {
            effect.accept(this);
        }

        // 2. Emit condition
        node.getCondition().accept(this);

        // 3. Branching: emit jumps for bytecode
        long elseLabel = nextJumpLabel++;
        long endLabel = nextJumpLabel++;

        if (Boxing.isBoxed(node.getCondition(), graph)) {
            jbc.emitBranchWrapped(elseLabel);
        }
        else {
            jbc.emitBranchPrimitive(elseLabel);
        }

        // 4. Emit 'if' branch
        node.getIfBranch().accept(this);
        jbc.emitGoto(endLabel);

        // 5. Emit 'else' branch
        jbc.emitLabel(elseLabel);
        node.getElseBranch().accept(this);

        // 6. Mark end
        jbc.emitLabel(endLabel);

        return null;
    }

    @Override
    public Void visitInteger(Int node, Void unused) {
        jbc.emitLoadInt(node.getValue());
        return null;
    }

    @Override
    public Void visitReal(Real node, Void unused) {
        jbc.emitLoadDouble(node.getValue());
        return null;
    }

    @Override
    public Void visitString(Str node, Void unused) {
        jbc.emitLoadString(node.getValue());
        return null;
    }

    @Override
    public Void visitBool(Bool node, Void unused) {
        jbc.emitLoadBoolean(node.getValue());
        return null;
    }

    @Override
    public Void visitNull(NullValue node, Void unused) {
        jbc.emitLoadTNull();
        return null;
    }

    @Override
    public Void visitArray(Array node, Void unused) {
        ListIterator<IRValue> itr = node.getValues().listIterator(node.getValues().size());
        while (itr.hasPrevious()) {
            IRValue value = itr.previous();
            value.accept(this);
        }
        jbc.emitMakeArray(node.getValues().size());
        return null;
    }

    @Override
    public Void visitDict(Dict node, Void unused) {
        for (int i = node.getKeys().size()-1; i >= 0; i--) {
            node.getKeys().get(i).accept(this);
            node.getValues().get(i).accept(this);
        }
        jbc.emitMakeDictionary(node.getKeys().size());
        return null;
    }

    @Override
    public Void visitRange(Range node, Void unused) {
        node.getFrom().accept(this);
        node.getTo().accept(this);
        jbc.emitMakeRange(true);  // TODO fix this
        return null;
    }

    @Override
    public Void visitThis(This node, Void unused) {
        jbc.emitLoadThis();
        return null;
    }

    @Override
    public Void visitAdd(Add node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitAddInt();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitAddDouble();
        }
        else if (lhsTy == IntType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            jbc.emitInt2Double();
            unbox(rhs);
            jbc.emitAddDouble();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitInt2Double();
            jbc.emitAddDouble();
        }
        else if (lhsTy == StrType.INSTANCE || rhsTy == StrType.INSTANCE) {

            new ToString(lhs).accept(this);

            if (rhsTy == IntType.INSTANCE) {
                unbox(rhs);
                jbc.emitAddStringWith(Integer.TYPE);
            }
            else if (rhsTy == RealType.INSTANCE) {
                unbox(rhs);
                jbc.emitAddStringWith(Double.TYPE);
            }
            else if (rhsTy == BoolType.INSTANCE) {
                unbox(rhs);
                jbc.emitAddStringWith(Boolean.TYPE);
            }
            else {
                box(rhs);
                jbc.emitAddStringWith(Object.class);
            }
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.ADD);
        }
        
        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitSub(Sub node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitSubInt();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitSubDouble();
        }
        else if (lhsTy == IntType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            jbc.emitInt2Double();
            unbox(rhs);
            jbc.emitSubDouble();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitInt2Double();
            jbc.emitSubDouble();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.SUB);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitMul(Mul node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitMulInt();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitMulDouble();
        }
        else if (lhsTy == IntType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            jbc.emitInt2Double();
            unbox(rhs);
            jbc.emitMulDouble();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitInt2Double();
            jbc.emitMulDouble();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.MUL);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitDiv(Div node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitDivInt();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitDivDouble();
        }
        else if (lhsTy == IntType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            jbc.emitInt2Double();
            unbox(rhs);
            jbc.emitDivDouble();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitInt2Double();
            jbc.emitDivDouble();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.DIV);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitIDiv(IDiv node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitDivInt();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.IDIV);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitMod(Mod node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitModuloInt();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.MOD);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitPow(Pow node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            jbc.emitInt2Double();
            unbox(rhs);
            jbc.emitInt2Double();
            jbc.emitPowDouble();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitPowDouble();
        }
        else if (lhsTy == IntType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            jbc.emitInt2Double();
            unbox(rhs);
            jbc.emitPowDouble();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitInt2Double();
            jbc.emitPowDouble();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.POW);
        }
        
        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitShl(Shl node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitShlInt();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.SHL);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitShr(Shr node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitShrInt();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.SHR);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitUshr(Ushr node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitUshrInt();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.USHR);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitAnd(And node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitAndInt();
        }
        else if (lhsTy == BoolType.INSTANCE && rhsTy == BoolType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitAndBool();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.AND);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitOr(Or node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitOrInt();
        }
        else if (lhsTy == BoolType.INSTANCE && rhsTy == BoolType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitOrBool();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.OR);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitXor(Xor node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitXorInt();
        }
        else if (lhsTy == BoolType.INSTANCE && rhsTy == BoolType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitXorBool();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.XOR);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitNot(Not node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue value = node.getValue();

        IRType type = typeMap.getType(value);
        if (type == IntType.INSTANCE) {
            unbox(value);
            jbc.emitNotInt();
        }
        else if (type == BoolType.INSTANCE) {
            unbox(value);
            jbc.emitNotBool();
        }
        else {
            box(value);
            jbc.emitOperateDynamicUnary(Opcode.NOT);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitNeg(Neg node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue value = node.getValue();

        IRType type = typeMap.getType(value);
        if (type == IntType.INSTANCE) {
            unbox(value);
            jbc.emitNegateInt();
        }
        else if (type == RealType.INSTANCE) {
            unbox(value);
            jbc.emitNegateDouble();
        }
        else {
            box(value);
            jbc.emitOperateDynamicUnary(Opcode.NOT);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitPos(Pos node, Void unused) {
        IRValue value = node.getValue();

        IRType type = typeMap.getType(value);
        if (type == IntType.INSTANCE ||  type == RealType.INSTANCE) {
            unbox(value);
            return null;
        }

        if (preferLoadRegister(node)) return null;
        box(value);
        jbc.emitOperateDynamicUnary(Opcode.POS);
        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitType(Type node, Void unused) {
        jbc.emitLoadType(node.getAddress());
        return null;
    }

    @Override
    public Void visitTypeof(Typeof node, Void unused) {
        if (preferLoadRegister(node)) return null;
        box(node.getValue());
        jbc.emitGetType();
        jbc.emitUnwrapType();
        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitGt(Gt node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitCompareIntGt();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitCompareDoubleGt();
        }
        else if (lhsTy == IntType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            jbc.emitInt2Double();
            unbox(rhs);
            jbc.emitCompareDoubleGt();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitInt2Double();
            jbc.emitCompareDoubleGt();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.GT);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitLt(Lt node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType lhsTy = typeMap.getType(lhs);
        IRType rhsTy = typeMap.getType(rhs);

        if (lhsTy == IntType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitCompareIntLt();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitCompareDoubleLt();
        }
        else if (lhsTy == IntType.INSTANCE && rhsTy == RealType.INSTANCE) {
            unbox(lhs);
            jbc.emitInt2Double();
            unbox(rhs);
            jbc.emitCompareDoubleLt();
        }
        else if (lhsTy == RealType.INSTANCE && rhsTy == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitInt2Double();
            jbc.emitCompareDoubleLt();
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitOperateDynamicBinary(Opcode.LT);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitEquals(Equals node, Void unused) {
        if (preferLoadRegister(node)) return null;

        IRValue lhs = node.getLeft();
        IRValue rhs = node.getRight();

        IRType leftType = typeMap.getType(lhs);
        IRType rightType = typeMap.getType(rhs);

        if (leftType == IntType.INSTANCE && rightType == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitCompareInt(true);
        }
        else if (leftType == RealType.INSTANCE && rightType == RealType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitCompareDouble(true);
        }
        else if (leftType == IntType.INSTANCE && rightType == RealType.INSTANCE) {
            unbox(lhs);
            jbc.emitInt2Double();
            unbox(rhs);
            jbc.emitCompareDouble(true);
        }
        else if (leftType == RealType.INSTANCE && rightType == IntType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitInt2Double();
            jbc.emitCompareDouble(true);
        }
        else if (leftType == BoolType.INSTANCE && rightType == BoolType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitCompareBoolean(true);
        }
        else if (leftType == StrType.INSTANCE && rightType == StrType.INSTANCE) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitCompareString(true);
        }
        else if (leftType instanceof TypeMirror && rightType instanceof TypeMirror) {
            unbox(lhs);
            unbox(rhs);
            jbc.emitCompareInt(true);
        }
        else {
            box(lhs);
            box(rhs);
            jbc.emitCompareDynamic(true);
        }

        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitParameter(Parameter node, Void unused) {
        if (preferLoadRegister(node)) return null;
        jbc.emitLoadLocal(node.index);
        IRType ty = typeMap.getType(node);
        if (ty != UnknownType.INSTANCE) {
            unbox(ty);
        }
        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitBuiltin(Builtin node, Void unused) {
        jbc.emitLoadBuiltin(node.getAddress());
        return null;
    }

    @Override
    public Void visitToInpArg(ToInpArg node, Void unused) {
        box(node.getValue());
        jbc.emitToInplaceArgument();
        return null;
    }

    @Override
    public Void visitToMapArg(ToMapArg node, Void unused) {
        box(node.getValue());
        jbc.emitToMappedArgument(node.getName());
        return null;
    }

    @Override
    public Void visitCall(Call node, Void unused) {
        ListIterator<IRValue> itr = node.getArgs().listIterator(node.getArgs().size());
        while (itr.hasPrevious()) {
            IRValue value = itr.previous();
            box(value);
        }
        node.getCalled().accept(this);
        jbc.emitCallInplace(node.getArgs().size());
        return null;
    }

    public void box(IRValue value) {

        if (value instanceof Bool b){
            jbc.emitLoadWrappedBoolean(b.getValue());
            return;
        }

        if (value instanceof Parameter p) {
            jbc.emitLoadLocal(p.index);
            return;
        }

        value.accept(this);

        if (Boxing.isBoxed(value, graph)) return;

        IRType type = typeMap.getType(value);

        if (type == IntType.INSTANCE) {
            jbc.emitWrapInt();
        }
        else if (type == RealType.INSTANCE) {
            jbc.emitWrapReal();
        }
        else if (type == BoolType.INSTANCE) {
            jbc.emitWrapBoolean();
        }
        else if (type == StrType.INSTANCE) {
            jbc.emitWrapString();
        }
        else if (type instanceof ArrayType) {
            jbc.emitWrapArray();
        }
        else if (type instanceof DictType) {
            jbc.emitWrapDictionary();
        }
        else if (type instanceof TypeMirror) {
            jbc.emitWrapType();
        }
        else {
            throw new AssertionError();
        }
    }

    public void unbox(IRValue value) {
        value.accept(this);
        if (!Boxing.isBoxed(value, graph)) return;
        IRType type = typeMap.getType(value);
        unbox(type);
    }

    @Override
    public Void visitToString(ToString node, Void unused) {
        IRValue value = node.getValue();
        IRType type = typeMap.getType(value);

        if (type == IntType.INSTANCE) {
            unbox(value);
            jbc.emit2Str(Integer.class, Integer.TYPE);
        }
        else if (type ==  RealType.INSTANCE) {
            unbox(value);
            jbc.emit2Str(Double.class, Double.TYPE);
        }
        else if (type == BoolType.INSTANCE) {
            unbox(value);
            jbc.emit2Str(Boolean.class, Boolean.TYPE);
        }
        else if (type == StrType.INSTANCE) {
            // already a primitive String
            unbox(value);
            return null;
        }
        else {
            box(value);
            jbc.emit2Str(Objects.class, Object.class);
        }

        return null;
    }

    @Override
    public Void visitPhi(Phi node, Void unused) {
        return null;
    }

    @Override
    public Void visitLocal(Local node, Void unused) {
        jbc.emitLoadLocal(node.index);
        return null;
    }

    @Override
    public Void visitGlobal(Global node, Void unused) {
        if (preferLoadRegister(node)) return null;
        jbc.emitLoadGlobal(node.index);
        return storeRegisterIfRequired(node);
    }

    @Override
    public Void visitStart(StartEffect node, Void unused) {
        // nothing to emit here
        return null;
    }

    @Override
    public Void visitNewLine(NewLine node, Void unused) {
        if (visited(node)) return null;
        jbc.emitNewLine(node.getLine());
        return null;
    }

    @Override
    public Void visitGuardBool(GuardBool node, Void unused) {
        if (visited(node)) return null;
        
        IRValue value = node.getValue();
        IRType type = typeMap.getType(value);

        preEmitGuardStateValues(node);

        if (!Boxing.isBoxed(value, graph) && type == BoolType.INSTANCE) {
            value.accept(this);
            emitGuard(node);
        }
        else {
            unbox(value);
            emitGuard(node);
        }
        return null;
    }

    @Override
    public Void visitGuardFunctionIdentity(GuardFunctionIdentity node, Void unused) {
        if (visited(node)) return null;

        preEmitGuardStateValues(node);

        IRValue value = node.getValue();
        box(value);

        jbc.emitFunctionIdentity();
        jbc.emitLoadInt(node.identity);
        jbc.emitCompareInt(true);
        emitGuard(node);

        return null;
    }

    private void preEmitGuardStateValues(Guard guard) {
        for (IRValue local : guard.getLocals()) {
            if (registers.containsKey(local)) continue;
            local.accept(this);
            jbc.emitPop();
        }
        for (IRValue value : guard.getStack()) {
            if (registers.containsKey(value)) continue;
            value.accept(this);
            jbc.emitPop();
        }
    }

    private void emitGuard(Guard guard) {

        long successContinueLabel = nextJumpLabel++;
        jbc.emitBranchPrimitive(successContinueLabel);

        for (IRValue local : guard.getLocals()) {
            box(local);
        }
        jbc.emitMakeArray(guard.getLocals().size());

        for (IRValue stackValue : guard.getStack()) {
            box(stackValue);
        }
        jbc.emitMakeArray(guard.getStack().size());

        SnapshotShape snapshotShape = guard.getSnapshotShape();
        Snapshots snapshots = compiler.get(Snapshots.KEY);
        int snapshotShapeId = snapshots.put(snapshotShape);

        jbc.emitCreateSnapshotThenFallback(nextGuardId++, snapshotShapeId);

        jbc.emitLabel(successContinueLabel);
    }

    @Override
    public Void visitValueEffect(ValueEffect node, Void unused) {
        node.getValue().accept(this);
        jbc.emitPop();
        return null;
    }

    @Override
    public Void visitMergeEffect(MergeEffect node, Void unused) {
        return null;
    }

    @Override
    public Void visitPushFrame(PushFrame node, Void unused) {
        box(node.getCalled());
        jbc.emitPushFrame();
        return null;
    }

    @Override
    public Void visitPopFrame(PopFrame node, Void unused) {
        jbc.emitPopFrame();
        return null;
    }


    private void unbox(IRType type) {

        if (type == IntType.INSTANCE) {
            jbc.emitUnwrapInt();
        }
        else if (type == RealType.INSTANCE) {
            jbc.emitUnwrapReal();
        }
        else if (type == BoolType.INSTANCE) {
            jbc.emitUnwrapBoolean();
        }
        else if (type == StrType.INSTANCE) {
            jbc.emitUnwrapString();
        }
        else if (type instanceof ArrayType) {
            jbc.emitUnwrapArray();
        }
        else if (type instanceof DictType) {
            jbc.emitUnwrapDictionary();
        }
        else if (type  instanceof TypeMirror) {
            jbc.emitUnwrapType();
        }
        else {
            throw new AssertionError();
        }
    }

}
