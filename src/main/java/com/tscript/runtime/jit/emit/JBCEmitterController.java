package com.tscript.runtime.jit.emit;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.ir.*;
import com.tscript.runtime.jit.ir.nodes.*;
import com.tscript.runtime.jit.ir.types.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JBCEmitterController extends IRScanner<Void, Void> {

    private final Map<IRBlock, Integer> labels;

    private final IREmitter emitter;

    public JBCEmitterController(IREmitter emitter) {
        this.emitter = Objects.requireNonNull(emitter);
        this.labels = new HashMap<>();
    }

    public byte[] emit(IRBlock block) {
        emitter.init(block);

        IRWalker<Void, Void> walker = new IRTreeWalker<>() {
            @Override
            public void onBlockEnter(IRBlock block) {
                emitter.emitLabel(labels.computeIfAbsent(block, k -> labels.size()));
            }
        };

        walker.walk(this, block);
        return emitter.build();
    }

    @Override
    public Void visitNewLine(NewLine node, Void unused) {
        emitter.emitNewLine(node.line);
        return null;
    }

    @Override
    public Void visitInteger(Int node, Void unused) {
        emitter.emitLoadInt(node.value);
        return null;
    }

    @Override
    public Void visitReal(Real node, Void unused) {
        emitter.emitLoadDouble(node.value);
        return null;
    }

    @Override
    public Void visitBool(Bool node, Void unused) {
        emitter.emitLoadBoolean(node.value);
        return null;
    }

    @Override
    public Void visitNull(NullValue node, Void unused) {
        emitter.emitLoadTNull();
        return null;
    }

    @Override
    public Void visitString(Str node, Void unused) {
        emitter.emitLoadString(node.value);
        return null;
    }

    @Override
    public Void visitArray(Array node, Void unused) {
        scan(node.content, unused);
        emitter.emitMakeArray(node.content.size());
        return null;
    }

    @Override
    public Void visitDictionary(Dict node, Void unused) {
        for (int i = 0; i < node.keys.size(); i++) {
            scan(node.keys.get(i), unused);
            scan(node.values.get(i), unused);
        }
        emitter.emitMakeDictionary(node.keys.size());
        return null;
    }

    @Override
    public Void visitRange(Range node, Void unused) {
        scan(node.from, unused);
        scan(node.to, unused);
        emitter.emitMakeRange(true);
        return null;
    }

    @Override
    public Void visitType(Type node, Void unused) {
        emitter.emitLoadType(node.address, node.fromModule);
        return null;
    }

    @Override
    public Void visitGetType(GetType node, Void unused) {
        scan(node.value, unused);
        emitter.emitGetType();
        return null;
    }

    @Override
    public Void visitThis(This node, Void unused) {
        emitter.emitLoadThis();
        return null;
    }

    @Override
    public Void visitVirtual(Virtual node, Void unused) {
        emitter.emitLoadVirtual(node.address);
        return null;
    }

    @Override
    public Void visitNative(Native node, Void unused) {
        emitter.emitLoadNative(node.name);
        return null;
    }

    @Override
    public Void visitBuiltin(Builtin node, Void unused) {
        emitter.emitLoadBuiltin(node.id);
        return null;
    }

    @Override
    public Void visitToInpArg(ToInpArg node, Void unused) {
        scan(node.value, unused);
        emitter.emitToInplaceArgument();
        return null;
    }

    @Override
    public Void visitToMapArg(ToMapArg node, Void unused) {
        scan(node.value, unused);
        emitter.emitToMappedArgument(node.name);
        return null;
    }

    @Override
    public Void visitCallInp(CallInp node, Void unused) {
        scan(node.args, unused);
        scan(node.called, unused);
        emitter.emitCallInplace(node.args.size());
        return null;
    }

    @Override
    public Void visitPushFrame(PushFrame node, Void unused) {
        scan(node.called, unused);
        emitter.emitPushFrame();
        return null;
    }

    @Override
    public Void visitPopFrame(PopFrame node, Void unused) {
        emitter.emitPopFrame();
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt node, Void unused) {
        scan(node.expr, unused);
        emitter.emitExprStmtCompletion();
        return null;
    }

    @Override
    public Void visitWrapTObj(WrapTObj node, Void unused) {
        scan(node.value, unused);

        if (!node.value.canBePrimitive()) {
            return null;
        }

        IRType type = node.getType();

        if (type.isType(IRIntegerType.class)) {
            emitter.emitWrapInt();
        }
        else if (type.isType(IRRealType.class)) {
            emitter.emitWrapReal();
        }
        else if (type.isType(IRBooleanType.class)) {
            emitter.emitWrapBoolean();
        }
        else if (type.isType(IRStringType.class)) {
            emitter.emitWrapString();
        }
        else if (type.isType(IRTypeType.class)) {
            emitter.emitWrapType();
        }
        else if (type.isType(IRArrayType.class)) {
            emitter.emitWrapArray();
        }
        else if (type.isType(IRDictionaryType.class)) {
            emitter.emitWrapDictionary();
        }

        // else not a wrapped value

        return null;
    }

    public Void visitUnwrapTObj(UnwrapTObj node, Void unused) {
        scan(node.value, unused);

        if (node.value.canBePrimitive()) {
            return null;
        }

        IRType type = node.getType();

        if (type.isType(IRIntegerType.class)) {
            emitter.emitUnwrapInt();
        }
        else if (type.isType(IRRealType.class)) {
            emitter.emitUnwrapReal();
        }
        else if (type.isType(IRBooleanType.class)) {
            emitter.emitUnwrapBoolean();
        }
        else if (type.isType(IRStringType.class)) {
            emitter.emitUnwrapString();
        }
        else if (type.isType(IRTypeType.class)) {
            emitter.emitUnwrapType();
        }
        else if (type.isType(IRArrayType.class)) {
            emitter.emitUnwrapArray();
        }
        else if (type.isType(IRDictionaryType.class)) {
            emitter.emitUnwrapDictionary();
        }

        // else not a primitive value

        return null;
    }

    @Override
    public Void visitEquals(Equals node, Void unused) {

        if (arePrimitiveTypes(IRIntegerType.class, node.lhs, node.rhs)) {
            scan(node.lhs, unused);
            scan(node.rhs, unused);
            emitter.emitCompareInt(node.shouldBeEqual);
        }
        else if (arePrimitiveTypes(IRRealType.class, node.lhs, node.rhs)) {
            scan(node.lhs, unused);
            scan(node.rhs, unused);
            emitter.emitCompareDouble(node.shouldBeEqual);
        }
        else if (arePrimitiveTypes(IRBooleanType.class, node.lhs, node.rhs)) {
            scan(node.lhs, unused);
            scan(node.rhs, unused);
            emitter.emitCompareBoolean(node.shouldBeEqual);
        }
        else if (arePrimitiveTypes(IRStringType.class, node.lhs, node.rhs)) {
            scan(node.lhs, unused);
            scan(node.rhs, unused);
            emitter.emitCompareString(node.shouldBeEqual);
        }

        else if (isPrimitiveType(IRIntegerType.class, node.lhs) && isPrimitiveType(IRRealType.class, node.rhs)) {
            scan(node.lhs, unused);
            emitter.emitInt2Double();
            scan(node.rhs, unused);
            emitter.emitCompareDouble(node.shouldBeEqual);
        }

        else if (isPrimitiveType(IRRealType.class, node.lhs) && isPrimitiveType(IRIntegerType.class, node.rhs)) {
            scan(node.lhs, unused);
            scan(node.rhs, unused);
            emitter.emitInt2Double();
            emitter.emitCompareDouble(node.shouldBeEqual);
        }

        else {
            emitter.emitCompareDynamic(node.shouldBeEqual);
        }

        return null;
    }

    @Override
    public Void visitBinaryOp(BinaryOp node, Void unused) {
        scan(node.lhs, unused);
        scan(node.rhs, unused);

        if (arePrimitiveTypes(IRIntegerType.class, node.rhs, node.rhs)) {
            switch (node.operation) {
                case ADD -> emitter.emitAddInt();
                case SUB -> emitter.emitSubInt();
                case MUL -> emitter.emitMulInt();
                case DIV -> emitter.emitDivInt();
                case MOD -> emitter.emitModuloInt();
                case AND -> emitter.emitAndInt();
                case OR -> emitter.emitOrInt();
                case XOR -> emitter.emitXorInt();
                default -> throw new IllegalStateException();
            }
        }
        else if (arePrimitiveTypes(IRRealType.class, node.lhs, node.rhs)) {
            switch (node.operation) {
                case ADD -> emitter.emitAddDouble();
                case SUB -> emitter.emitSubDouble();
                case MUL -> emitter.emitMulDouble();
                case DIV -> emitter.emitDivDouble();
                default -> throw new IllegalStateException();
            }
        }
        else if (arePrimitiveTypes(IRBooleanType.class, node.lhs, node.rhs)) {
            switch (node.operation) {
                case AND -> emitter.emitAndBool();
                case OR -> emitter.emitOrBool();
                case XOR -> emitter.emitXorBool();
                default -> throw new IllegalStateException();
            }
        }
        else {
            emitter.emitOperateDynamic(node.operation);
        }
        return null;
    }

    @Override
    public Void visitNeg(Neg node, Void unused) {
        scan(node.value, unused);

        if (isPrimitiveType(IRIntegerType.class, node.value)) {
            emitter.emitNegateInt();
        }
        else if (isPrimitiveType(IRRealType.class, node.value)) {
            emitter.emitNegateDouble();
        }
        else {
            emitter.emitOperateDynamic(Opcode.NEG);
        }
        return null;
    }

    @Override
    public Void visitNot(Not node, Void unused) {
        scan(node.value, unused);

        if (isPrimitiveType(IRIntegerType.class, node.value)) {
            emitter.emitNotInt();
        }
        else if (isPrimitiveType(IRBooleanType.class, node.value)) {
            emitter.emitNotBool();
        }
        else {
            emitter.emitOperateDynamic(Opcode.NOT);
        }

        return null;
    }

    @Override
    public Void visitReturn(Return node, Void unused) {
        scan(node.value, unused);
        emitter.emitReturn();
        return null;
    }

    @Override
    public Void visitGuard(Guard node, Void unused) {
        scan(node.condition, unused);

        if (isPrimitiveType(IRBooleanType.class, node.condition)) {
            emitter.emitGuardPrimitiveBoolean(node.ipRecover, node.spRecover);
        }
        else {
            emitter.emitGuardWrapped(node.ipRecover, node.spRecover);
        }

        return null;
    }

    @Override
    public Void visitGoto(Goto node, Void unused) {
        emitter.emitGoto(labels.computeIfAbsent(node.block, k -> labels.size()));
        return null;
    }

    @Override
    public Void visitBranch(Branch node, Void unused) {
        scan(node.condition, unused);

        if (isPrimitiveType(IRBooleanType.class, node.condition)) {
            emitter.emitBranchPrimitive(
                    labels.computeIfAbsent(node.jumpBlock, k -> labels.size()),
                    labels.computeIfAbsent(node.noJumpBlock, k -> labels.size())
            );
        }
        else {
            emitter.emitBranchWrapped(
                    labels.computeIfAbsent(node.jumpBlock, k -> labels.size()),
                    labels.computeIfAbsent(node.noJumpBlock, k -> labels.size())
            );
        }
        return null;
    }

    @Override
    public Void visitItrBranch(ItrBranch node, Void unused) {
        emitter.emitBranchIterator(
                labels.computeIfAbsent(node.jumpBlock, k -> labels.size()),
                labels.computeIfAbsent(node.noJumpBlock, k -> labels.size())
        );
        return null;
    }

    @Override
    public Void visitItr(Itr node, Void unused) {
        scan(node.iterable, unused);
        emitter.emitGetIterator();
        return null;
    }

    @Override
    public Void visitItrNext(ItrNext node, Void unused) {
        emitter.emitIteratorNext();
        return null;
    }

    @Override
    public Void visitItrHasNext(ItrHasNext node, Void unused) {
        emitter.emitIteratorHasNext();
        return null;
    }

    @Override
    public Void visitLoadTemp(LoadTemp node, Void unused) {
        emitter.emitLoadTemp(node.address);
        return null;
    }

    @Override
    public Void visitStoreTemp(StoreTemp node, Void unused) {
        scan(node.value, unused);
        emitter.emitStoreTemp(node.address);
        return null;
    }

    @Override
    public Void visitLoadLocal(LoadLocal node, Void unused) {
        emitter.emitLoadLocal(node.address);
        return null;
    }

    @Override
    public Void visitStoreLocal(StoreLocal node, Void unused) {
        scan(node.value, unused);
        emitter.emitStoreLocal(node.address);
        return null;
    }

    @Override
    public Void visitLoadGlobal(LoadGlobal node, Void unused) {
        emitter.emitLoadGlobal(node.address);
        return null;
    }

    @Override
    public Void visitStoreGlobal(StoreGlobal node, Void unused) {
        scan(node.value, unused);
        emitter.emitStoreGlobal(node.address);
        return null;
    }

    @Override
    public Void visitLoadInternal(LoadInternal node, Void unused) {
        emitter.emitLoadInternal(node.address);
        return null;
    }

    @Override
    public Void visitStoreInternal(StoreInternal node, Void unused) {
        scan(node.value, unused);
        emitter.emitStoreInternal(node.address);
        return null;
    }

    @Override
    public Void visitLoadExternal(LoadExternal node, Void unused) {
        scan(node.value, unused);
        emitter.emitLoadExternal(node.name);
        return null;
    }

    @Override
    public Void visitStoreExternal(StoreExternal node, Void unused) {
        scan(node.owner, unused);
        scan(node.value, unused);
        emitter.emitStoreExternal(node.name);
        return null;
    }

    @Override
    public Void visitLoadStatic(LoadStatic node, Void unused) {
        emitter.emitLoadStatic(node.name);
        return null;
    }

    @Override
    public Void visitStoreStatic(StoreStatic node, Void unused) {
        scan(node.value, unused);
        emitter.emitStoreStatic(node.name);
        return null;
    }

    @Override
    public Void visitLoadSuper(LoadSuper node, Void unused) {
        emitter.emitLoadSuper(node.name);
        return null;
    }

    @Override
    public Void visitStoreSuper(StoreSuper node, Void unused) {
        scan(node.value, unused);
        emitter.emitStoreSuper(node.name);
        return null;
    }

    @Override
    public Void visitLoadAbstract(LoadAbstract node, Void unused) {
        emitter.emitLoadAbstract(node.name);
        return null;
    }

    @Override
    public Void visitLoadName(LoadName node, Void unused) {
        emitter.emitLoadName(node.name);
        return null;
    }

    @Override
    public Void visitContainerRead(ContainerRead node, Void unused) {
        scan(node.container, unused);
        scan(node.key, unused);

        if (isPrimitiveType(IRIntegerType.class, node.container)
                && isPrimitiveType(IRIntegerType.class, node.key)) {
            emitter.emitContainerReadArray();
        }
        else {
            emitter.emitContainerReadDynamic();
        }
        return null;
    }

    @Override
    public Void visitContainerWrite(ContainerWrite node, Void unused) {
        scan(node.container, unused);
        scan(node.key, unused);
        scan(node.value, unused);

        if (isPrimitiveType(IRIntegerType.class, node.container)
                && isPrimitiveType(IRIntegerType.class, node.value)) {
            emitter.emitContainerWriteArray();
        }
        else {
            emitter.emitContainerWriteDynamic();
        }
        return null;
    }

    @Override
    public Void visitSetOwner(SetOwner node, Void unused) {
        scan(node.owner, unused);
        scan(node.member, unused);
        emitter.emitSetOwner();
        return null;
    }

    @Override
    public Void visitExtend(Extend node, Void unused) {
        scan(node.owner, unused);
        scan(node.value, unused);
        emitter.emitExtend(node.name);
        return null;
    }

    @Override
    public Void visitImportModule(ImportModule node, Void unused) {
        if (node.index != -1) {
            emitter.emitImport(node.index);
        }
        else {
            emitter.emitImport(node.name);
        }
        return null;
    }

    @Override
    public Void visitUse(Use node, Void unused) {
        scan(node.value, unused);
        emitter.emitUse(node.name);
        return null;
    }

    @Override
    public Void visitUseMembers(UseMembers node, Void unused) {
        scan(node.used, unused);
        emitter.emitUseMembers();
        return null;
    }


    private static boolean isPrimitiveType(Class<? extends IRType> type, IRValue value) {
        return !(value instanceof WrapTObj) && value.getType().isType(type);
    }

    private static boolean arePrimitiveTypes(Class<? extends IRType> type, IRValue... checked) {
        for (IRValue t : checked) {
            if (!isPrimitiveType(type, t)) {
                return false;
            }
        }
        return true;
    }

}
