package com.tscript.runtime.jit.emit.jbc;

import com.tscript.runtime.core.Argument;
import com.tscript.runtime.core.Builtins;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.caching.TypeCache;
import com.tscript.runtime.jit.ir.*;
import com.tscript.runtime.jit.ir.nodes.*;
import com.tscript.runtime.jit.ir.nodes.Range;
import com.tscript.runtime.jit.ir.types.*;
import com.tscript.runtime.jit.lower.jbc.JBCUtils;
import com.tscript.runtime.jit.tracing.ImageFrame;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.typing.Type;
import org.objectweb.asm.Label;

import java.util.*;

public class JBCEmitterImpl extends IRScanner<Void, Void> {

    private static final int THREAD_REG = 1;
    private static final int FRAME_REG = 2;
    private static final int TEMPS_REG = 3;

    private static final int NATIVE_REG = 4;


    private final JBCImageGenerator jbc;

    private final Map<IRBlock, Label> labels;
    private final Map<IRBlock, List<Runnable>> preGens;

    public JBCEmitterImpl(String className) {
        jbc = new JBCImageGenerator(className);
        this.labels = new HashMap<>();
        this.preGens = new HashMap<>();
    }

    public byte[] emit(IRBlock block) {
        // prepare temp registers
        int tempRegs = MaxTempRegisters.compute(block);
        if (tempRegs > 0){
        jbc.newArray(TObject.class, tempRegs);
        jbc.storeLocal(TEMPS_REG);
        }

        jbc.label(getLabel(block));

        IRWalker<Void, Void> walker = new IRTreeWalker<>() {
            @Override
            public void onBlockEnter(IRBlock block) {
                jbc.label(getLabel(block));
                for (Runnable r : preGens.computeIfAbsent(block, k -> List.of())) {
                    r.run();
                }
                preGens.remove(block);
            }
        };

        walker.walk(this, block);
        return jbc.generate();
    }

    @Override
    public Void visitGoto(Goto node, Void unused) {
        jbc.gotoLabel(getLabel(node.block));
        return null;
    }

    @Override
    public Void visitBranch(Branch node, Void unused) {
        scan(node.condition, null);

        boolean requireSlowEvaluation = !(node.condition.getType().isPrimitive() && isBoolType(node.condition));

        if (requireSlowEvaluation) {
            jbc.callStatic(TNIUtils.class, "isTrue", Boolean.TYPE, TObject.class);
        }

        jbc.branch(getLabel(node.jumpBlock), node.jumpOnTrue);
        return null;
    }

    @Override
    public Void visitItrBranch(ItrBranch node, Void unused) {
        ItrHasNext hasNext = new ItrHasNext(true);
        scan(hasNext, null);

        // visitItrHasNext generates primitive Z.
        // No need for wrapping

        jbc.branch(getLabel(node.jumpBlock), false);

        preGens.put(node.jumpBlock, List.of(() -> {
            genLoadFrame();
            jbc.callStatic(TNIUtils.class, "pop", TObject.class, ImageFrame.class);
            jbc.pop();
        }));
        return null;
    }

    @Override
    public Void visitNewLine(NewLine node, Void unused) {
        genLoadFrame();
        jbc.pushInt(node.line);
        jbc.putField(ImageFrame.class, "line", Integer.TYPE);
        return null;
    }

    @Override
    public Void visitPushFrame(PushFrame node, Void unused) {
        scan(node.called, null);
        genLoadFrame();
        jbc.pushInt(node.extraLocals);
        jbc.callStatic(JBCUtils.class, "pushFrame", ImageFrame.class, TObject.class, ImageFrame.class, Integer.TYPE);
        genStoreFrame();
        return null;
    }

    @Override
    public Void visitPopFrame(PopFrame node, Void unused) {
        genLoadFrame();
        jbc.dup();
        jbc.getField(ImageFrame.class, "next", ImageFrame.class);
        genStoreFrame();
        return null;
    }

    @Override
    public Void visitNull(NullValue node, Void unused) {
        jbc.pushNull();
        return null;
    }

    @Override
    public Void visitInteger(Int node, Void unused) {
        jbc.pushInt(node.value);
        return null;
    }

    @Override
    public Void visitReal(Real node, Void unused) {
        jbc.pushDouble(node.value);
        return null;
    }

    @Override
    public Void visitBool(Bool node, Void unused) {
        jbc.pushBoolean(node.value);
        return null;
    }

    @Override
    public Void visitString(Str node, Void unused) {
        jbc.pushString(node.value);
        return null;
    }

    @Override
    public Void visitRange(Range node, Void unused) {
        jbc.newInstance(com.tscript.runtime.typing.Range.class);
        genSequentialValueLoading(node.from, node.to);
        jbc.callConstructor(com.tscript.runtime.typing.Range.class, TInteger.class, TInteger.class);
        return null;
    }

    @Override
    public Void visitWrapTObj(WrapTObj node, Void unused) {
        final IRValue value = node.value;

        if (value instanceof NullValue) {
            jbc.getStatic(Null.class, "INSTANCE", Null.class);
            return null;
        }
        else if (value instanceof Bool bool) {
            String fieldName = bool.value ? "TRUE" : "FALSE";
            jbc.getStatic(TBoolean.class, fieldName, TBoolean.class);
            return null;
        }

        TObjectWrapGenerator wrapGenerator = new TObjectWrapGenerator(value);
        value.getType().accept(wrapGenerator, null);

        return null;
    }

    @Override
    public Void visitUnpackObj(UnpackObj node, Void unused) {

        if (!node.value.getType().isPrimitive() && node.value.getType() != IRUnknownType.INSTANCE) {
            scan(node.value, null);
            ToPrimitiveCastGenerator castGenerator = new ToPrimitiveCastGenerator();
            node.getType().accept(castGenerator, null);
        }
        else {
            scan(node.value, null);
        }

        return null;
    }

    @Override
    public Void visitReturn(Return node, Void unused) {
        scan(node.value, null);
        jbc.genReturnObject();
        return null;
    }

    @Override
    public Void visitArray(Array node, Void unused) {
        genNewTObjectArray(node.content);
        jbc.callStatic(Arrays.class, "asList", List.class, Object[].class);
        return null;
    }

    @Override
    public Void visitDictionary(Dict node, Void unused) {
        jbc.newInstance(LinkedHashMap.class);
        jbc.callConstructor(LinkedHashMap.class);

        for (int i = 0; i < node.keys.size(); i++) {
            jbc.dup();
            genSequentialValueLoading(node.keys.get(i), node.values.get(i));
            jbc.callVirtual(LinkedHashMap.class, "put", Object.class, Object.class, Object.class);
            jbc.pop();
        }

        return null;
    }

    @Override
    public Void visitThis(This node, Void unused) {
        genLoadOwner();
        return null;
    }

    @Override
    public Void visitVirtual(Virtual node, Void unused) {
        genLoadThread();
        jbc.pushInt(node.address);
        jbc.callStatic(JBCUtils.class, "loadVirtual", TObject.class, TThread.class, Integer.TYPE);
        return null;
    }

    @Override
    public Void visitNative(Native node, Void unused) {
        genLoadThread();
        jbc.pushString(node.name);
        jbc.callStatic(JBCUtils.class, "loadNative", TObject.class, TThread.class, String.class);
        return null;
    }

    @Override
    public Void visitBuiltin(Builtin node, Void unused) {
        jbc.pushInt(node.id);
        jbc.callStatic(Builtins.class, "load", TObject.class, Integer.TYPE);
        return null;
    }

    @Override
    public Void visitLoadAbstract(LoadAbstract node, Void unused) {
        genLoadOwner();
        jbc.pushString(node.name);
        jbc.callStatic(JBCUtils.class, "loadAbstract", TObject.class, TObject.class, String.class);
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt node, Void unused) {
        scan(node.expr, null);
        jbc.pop();
        return null;
    }

    @Override
    public Void visitLoadLocal(LoadLocal node, Void unused) {

        if (node.getType().isPrimitive()) {
            PrimitiveLoadLocal loadLocal = new PrimitiveLoadLocal(node.address);
            node.getType().accept(loadLocal, null);
        }
        else {
            genLoadLocalsArray();
            jbc.pushInt(node.address);
            jbc.loadArray();
        }

        return null;
    }

    @Override
    public Void visitStoreLocal(StoreLocal node, Void unused) {

        if (node.value.getType().isPrimitive()) {
            scan(node.value, null);
            PrimitiveStoreLocal storeLocal = new PrimitiveStoreLocal(node.address);
            node.value.getType().accept(storeLocal, null);
        }
        else {
            genLoadLocalsArray();
            jbc.pushInt(node.address);
            scan(node.value, null);
            jbc.storeArray();
        }

        return null;
    }

    @Override
    public Void visitLoadTemp(LoadTemp node, Void unused) {
        genLoadTemps();
        jbc.pushInt(node.address);
        jbc.loadArray();
        return null;
    }

    @Override
    public Void visitStoreTemp(StoreTemp node, Void unused) {
        genLoadTemps();
        jbc.pushInt(node.address);
        scan(node.value, null);
        jbc.storeArray();
        return null;
    }

    @Override
    public Void visitLoadGlobal(LoadGlobal node, Void unused) {
        genLoadFrame();
        jbc.pushInt(node.address);
        jbc.callStatic(JBCUtils.class, "loadGlobal", TObject.class, ImageFrame.class, Integer.TYPE);
        return null;
    }

    @Override
    public Void visitStoreGlobal(StoreGlobal node, Void unused) {
        genLoadFrame();
        scan(node.value, null);
        jbc.pushInt(node.address);
        jbc.callStatic(JBCUtils.class, "StoreGlobal", Void.TYPE, ImageFrame.class, TObject.class, Integer.TYPE);
        return null;
    }

    @Override
    public Void visitLoadExternal(LoadExternal node, Void unused) {
        scan(node.value, null);
        load(node.name, -1, false);
        return null;
    }

    @Override
    public Void visitStoreExternal(StoreExternal node, Void unused) {
        store(node.name, -1, false, node.value, node.owner);
        return null;
    }

    @Override
    public Void visitLoadInternal(LoadInternal node, Void unused) {
        genLoadOwner();
        loadFast(node.address, -1, false);
        return null;
    }

    @Override
    public Void visitStoreInternal(StoreInternal node, Void unused) {
        storeFast(node.address, -1, false, node.value, this::genLoadOwner);
        return null;
    }

    @Override
    public Void visitLoadStatic(LoadStatic node, Void unused) {
        genLoadOwner();
        load(node.name, -1, true);
        return null;
    }

    @Override
    public Void visitStoreStatic(StoreStatic node, Void unused) {
        store(node.name, -1, true, node.value, this::genLoadOwner);
        return null;
    }

    @Override
    public Void visitLoadSuper(LoadSuper node, Void unused) {
        genLoadOwner();
        load(node.name, -1, false);
        return null;
    }

    @Override
    public Void visitStoreSuper(StoreSuper node, Void unused) {
        store(node.name, -1, false, node.value, this::genLoadOwner);
        return null;
    }

    @Override
    public Void visitLoadName(LoadName node, Void unused) {
        genLoadThread();
        genLoadFrame();
        jbc.pushString(node.name);
        jbc.callStatic(JBCUtils.class, "loadName", TObject.class, TThread.class, ImageFrame.class, String.class);
        return null;
    }

    @Override
    public Void visitGetType(GetType node, Void unused) {
        scan(node.value, null);
        jbc.callInterface(TObject.class, "getType", Type.class);
        return null;
    }

    @Override
    public Void visitType(com.tscript.runtime.jit.ir.nodes.Type node, Void unused) {

        if (node.fromModule) {
            genLoadThread();
            jbc.pushInt(node.address);
            jbc.callStatic(JBCUtils.class, "loadType", TObject.class, TThread.class, Integer.TYPE);
        }
        else {
            jbc.pushInt(node.address);
            jbc.callStatic(TypeCache.class, "getType", TObject.class, Integer.TYPE);
        }
        return null;
    }

    @Override
    public Void visitItr(Itr node, Void unused) {
        genLoadThread();
        genLoadFrame();
        scan(node.iterable, null);
        jbc.callStatic(JBCUtils.class, "getIterator", TObject.class, TThread.class, ImageFrame.class, TObject.class);
        return null;
    }

    @Override
    public Void visitItrNext(ItrNext node, Void unused) {
        genStoreFrame();
        jbc.callStatic(JBCUtils.class, "iteratorNext", TObject.class, ImageFrame.class);
        return null;
    }

    @Override
    public Void visitItrHasNext(ItrHasNext node, Void unused) {
        genLoadFrame();
        jbc.callStatic(JBCUtils.class, "iteratorHasNext", Boolean.TYPE, ImageFrame.class);
        return null;
    }

    @Override
    public Void visitContainerRead(ContainerRead node, Void unused) {
        genLoadThread();
        genSequentialValueLoading(node.container, node.key);
        jbc.callStatic(JBCUtils.class, "containerRead", TObject.class, TThread.class, TObject.class, TObject.class);
        return null;
    }

    @Override
    public Void visitContainerWrite(ContainerWrite node, Void unused) {
        genLoadThread();
        genSequentialValueLoading(node.container, node.key, node.value);
        jbc.callStatic(JBCUtils.class, "containerWrite", Boolean.TYPE, TThread.class, TObject.class, TObject.class, TObject.class);
        jbc.pop();
        return null;
    }

    @Override
    public Void visitCallInp(CallInp node, Void unused) {
        genLoadThread();
        scan(node.called, null);
        genNewTObjectArray(node.args);
        jbc.callStatic(JBCUtils.class, "callInp", TObject.class, TThread.class, TObject.class, TObject[].class);
        return null;
    }

    @Override
    public Void visitToInpArg(ToInpArg node, Void unused) {
        jbc.newInstance(Argument.class);
        scan(node.value, null);
        jbc.callConstructor(Argument.class, TObject.class);
        return null;
    }

    @Override
    public Void visitToMapArg(ToMapArg node, Void unused) {
        jbc.newInstance(Argument.class);
        jbc.pushString(node.name);
        scan(node.value, null);
        jbc.callConstructor(Argument.class, String.class, TObject.class);
        return null;
    }

    @Override
    public Void visitEquals(Equals node, Void unused) {
        if (node.lhs.getType().isPrimitive() && node.rhs.getType().isPrimitive()) {
            if (isIntPrimitiveComp(node.lhs, node.rhs)) {
                genSequentialValueLoading(node.lhs, node.rhs);

                Label trueLabel = new Label();
                Label endLabel = new Label();

                jbc.branchCmp(trueLabel, true);
                jbc.pushInt(0);
                jbc.gotoLabel(endLabel);
                jbc.label(trueLabel);
                jbc.pushInt(1);
                jbc.label(endLabel);
            }
            else {
                genWrappedComparison(node.lhs, node.rhs);
            }
        }
        else {
            genWrappedComparison(node.lhs, node.rhs);
        }

        if (!node.shouldBeEqual) {
            jbc.invertBool();
        }

        return null;
    }

    private void genWrappedComparison(IRValue lhs, IRValue rhs) {
        if (lhs.getType().isPrimitive())
            lhs = new WrapTObj(lhs);
        if (rhs.getType().isPrimitive())
            rhs = new WrapTObj(rhs);
        genSequentialValueLoading(lhs, rhs);
        jbc.callInterface(TObject.class, "equals", Boolean.TYPE, Object.class);

    }

    private boolean isIntPrimitiveComp(IRValue lhs, IRValue rhs) {
        if (isTypeType(lhs) && isTypeType(rhs)) return true;
        if (isBoolType(lhs) && isBoolType(rhs)) return true;
        return isIntType(lhs) && isIntType(rhs);
    }

    @Override
    public Void visitBinaryOp(BinaryOp node, Void unused) {

        if (node.lhs.getType().isPrimitive() && node.rhs.getType().isPrimitive()) {
            throw new UnsupportedOperationException("primitive operation not supported");
        }

        genLoadThread();
        IRValue value = node.lhs;

        if (node.lhs.getType().isPrimitive()) {
            value = new WrapTObj(value);
        }
        scan(value, null);

        if (node.lhs == node.rhs) {
            jbc.dup();
        }
        else {
            value = node.rhs;
            if (node.rhs.getType().isPrimitive()) {
                value = new WrapTObj(value);
            }
            scan(value, null);
        }

        jbc.pushInt(node.operation.b);
        jbc.callStatic(JBCUtils.class, "binary", TObject.class, TThread.class, TObject.class, TObject.class, Byte.TYPE);
        return null;
    }

    @Override
    public Void visitNot(Not node, Void unused) {
        if (node.value.getType().isPrimitive()){
            scan(node.value, null);
            jbc.invertBool();
        }
        else {
            genLoadThread();
            scan(node.value, null);
            jbc.callStatic(JBCUtils.class, "not", TObject.class, TThread.class, TObject.class);
        }
        return null;
    }

    @Override
    public Void visitNeg(Neg node, Void unused) {
        if (node.value.getType().isPrimitive()){
            scan(node.value, null);
            NegationGenerator negationGenerator = new NegationGenerator();
            node.value.getType().accept(negationGenerator, null);
        }
        else {
            genLoadThread();
            scan(node.value, null);
            jbc.callStatic(JBCUtils.class, "neg", TObject.class, TThread.class, TObject.class);
        }
        return null;
    }

    @Override
    public Void visitUse(Use node, Void unused) {
        genLoadThread();
        genLoadFrame();
        scan(node.value, null);
        jbc.pushString(node.name);
        jbc.callStatic(JBCUtils.class, "use", Boolean.TYPE, TThread.class, ImageFrame.class, TObject.class, String.class);
        jbc.pop();
        return null;
    }

    @Override
    public Void visitUseMembers(UseMembers node, Void unused) {
        genLoadThread();
        genLoadFrame();
        scan(node.used, null);
        jbc.callStatic(JBCUtils.class, "use", Boolean.TYPE, TThread.class, ImageFrame.class, TObject.class);
        jbc.pop();
        return null;
    }

    @Override
    public Void visitImportModule(ImportModule node, Void unused) {
        genLoadThread();

        if (node.index == -1) {
            jbc.pushInt(node.index);
            jbc.callStatic(JBCUtils.class, "importModule", TObject.class, TThread.class, Integer.TYPE);
        }
        else {
            jbc.pushString(node.name);
            jbc.callStatic(JBCUtils.class, "importModule", TObject.class, TThread.class, String.class);
        }

        return null;
    }

    @Override
    public Void visitExtend(Extend node, Void unused) {
        genLoadThread();
        genSequentialValueLoading(node.owner, node.value);
        jbc.pushString(node.name);
        jbc.callStatic(JBCUtils.class, "extend", TObject.class, TThread.class, TObject.class, TObject.class, String.class);
        return null;
    }

    @Override
    public Void visitSetOwner(SetOwner node, Void unused) {
        genLoadThread();
        genSequentialValueLoading(node.member, node.owner);
        jbc.callStatic(JBCUtils.class, "setOwner", TObject.class, TThread.class, TObject.class, TObject.class);
        return null;
    }

    @Override
    public Void visitGuard(Guard node, Void unused) {
        if (node.condition.getType().isPrimitive()) {
            scan(new WrapTObj(node.condition), null);
        }
        else {
            scan(node.condition, null);
        }

        genLoadThread();
        genLoadFrame();
        jbc.pushInt(node.ipRecover);
        jbc.pushInt(node.spRecover);
        jbc.pushBoolean(true);
        jbc.callStatic(JBCUtils.class, "guardBool", Void.TYPE, TObject.class, TThread.class, ImageFrame.class, Integer.TYPE, Integer.TYPE, Boolean.TYPE);
        return null;
    }

    private void genNewTObjectArray(List<IRValue> content) {
        jbc.newArray(TObject.class, content.size());

        for (int i = 0; i < content.size(); i++) {
            jbc.dup();
            jbc.pushInt(i);
            scan(content.get(i), null);
            jbc.storeArray();
        }
    }

    private Label getLabel(IRBlock block) {
        return labels.computeIfAbsent(block, k -> new Label());
    }

    private boolean isBoolType(IRValue value) {
        return value.getType().accept(new IsBoolType(), null);
    }

    private boolean isIntType(IRValue value) {
        return value.getType().accept(new IsIntType(), null);
    }

    private boolean isRealType(IRValue value) {
        return value.getType().accept(new IsRealType(), null);
    }

    private boolean isTypeType(IRValue value) {
        return value.getType().accept(new IsTypeType(), null);
    }

    private void load(String name, int layer, boolean staticAccess) {
        jbc.pushString(name);

        String methodName = staticAccess ? "loadStatic" : "load";

        if (layer > 0) {
            jbc.pushInt(layer);
            jbc.callStatic(JBCUtils.class, methodName, TObject.class, TObject.class, String.class, Integer.TYPE);
        }
        else {
            jbc.callStatic(JBCUtils.class, methodName, TObject.class, TObject.class, String.class);
        }
    }

    private void loadFast(int address, int layer, boolean staticAccess) {
        jbc.pushInt(address);

        String methodName = staticAccess ? "loadStaticFast" : "loadFast";

        if (layer > 0) {
            jbc.pushInt(layer);
            jbc.callStatic(JBCUtils.class, methodName, TObject.class, TObject.class, Integer.TYPE, Integer.TYPE);
        }
        else {
            jbc.callStatic(JBCUtils.class, methodName, TObject.class, TObject.class, Integer.TYPE);
        }
    }

    private void store(String name, int layer, boolean staticAccess, IRValue storedValue, IRValue accessedValue) {
        store(name, layer, staticAccess, storedValue, () -> scan(accessedValue, null));
    }

    private void store(String name, int layer, boolean staticAccess, IRValue storedValue, Runnable accessedValueGenerator) {
        genLoadThread();
        accessedValueGenerator.run();
        scan(storedValue, null);
        jbc.pushString(name);

        String methodName = staticAccess ? "storeStatic" : "store";

        if (layer > 0) {
            jbc.pushInt(layer);
            jbc.callStatic(JBCUtils.class, methodName, Boolean.TYPE, TThread.class, TObject.class, TObject.class, String.class, Integer.TYPE);
        }
        else {
            jbc.callStatic(JBCUtils.class, methodName, Boolean.TYPE, TThread.class, TObject.class, TObject.class, String.class);
        }
    }

    private void storeFast(int address, int layer, boolean staticAccess, IRValue storedValue, IRValue accessedValue) {
        storeFast(address, layer, staticAccess, storedValue, () -> scan(accessedValue, null));
    }

    private void storeFast(int address, int layer, boolean staticAccess, IRValue storedValue, Runnable accessedValueGenerator) {
        genLoadThread();
        accessedValueGenerator.run();
        scan(storedValue, null);
        jbc.pushInt(address);

        String methodName = staticAccess ? "storeStatic" : "store";

        if (layer > 0) {
            jbc.pushInt(layer);
            jbc.callStatic(JBCUtils.class, methodName, Boolean.TYPE, TThread.class, TObject.class, TObject.class, Integer.TYPE, Integer.TYPE);
        }
        else {
            jbc.callStatic(JBCUtils.class, methodName, Boolean.TYPE, TThread.class, TObject.class, TObject.class, Integer.TYPE);
        }
    }

    private void genSequentialValueLoading(IRValue... values) {
        IRValue current = values[0];
        scan(current, null);

        for (int i = 1; i < values.length; i++) {
            IRValue next = values[i];
            if (current == next) {
                jbc.dup();
            }
            else {
                scan(next, null);
            }
            current = next;
        }
    }

    private void genLoadThread() {
        jbc.loadLocal(THREAD_REG);
    }

    private void genLoadFrame(){
        jbc.loadLocal(FRAME_REG);
    }

    private void genStoreFrame() {
        jbc.storeLocal(FRAME_REG);
    }

    private void genLoadTemps() {
        jbc.loadLocal(TEMPS_REG);
    }

    private void genLoadLocalsArray(){
        genLoadFrame();
        jbc.getField(ImageFrame.class, "locals", TObject[].class);
    }

    private void genLoadOwner() {
        genLoadFrame();
        jbc.getField(ImageFrame.class, "owner", TObject.class);
    }


    private class TObjectWrapGenerator implements IRTypeVisitor<Void, Void> {

        private final IRValue value;

        private TObjectWrapGenerator(IRValue value) {
            this.value = value;
        }

        @Override
        public Void visitIntegerType(IRIntegerType integerType, Void unused) {
            jbc.newInstance(TInteger.class);
            scan(value, null);
            jbc.callConstructor(TInteger.class, Integer.TYPE);
            return null;
        }

        @Override
        public Void visitRealType(IRRealType realType, Void unused) {
            jbc.newInstance(TReal.class);
            scan(value, null);
            jbc.callConstructor(TReal.class, Double.TYPE);
            return null;
        }

        @Override
        public Void visitBooleanType(IRBooleanType booleanType, Void unused) {
            scan(value, null);
            jbc.callStatic(TBoolean.class, "of", TBoolean.class, Boolean.TYPE);
            return null;
        }

        @Override
        public Void visitStringType(IRStringType stringType, Void unused) {
            jbc.newInstance(TString.class);
            scan(value, null);
            jbc.callConstructor(TString.class, String.class);
            return null;
        }

        @Override
        public Void visitNullType(IRNullType nullType, Void unused) {
            jbc.getStatic(Null.class, "INSTANCE", Null.class);
            return null;
        }

        @Override
        public Void visitArrayType(IRArrayType arrayType, Void unused) {
            jbc.newInstance(TArray.class);
            scan(value, null);
            jbc.callConstructor(TArray.class, List.class);
            return null;
        }

        @Override
        public Void visitDictionaryType(IRDictionaryType dictionaryType, Void unused) {
            jbc.newInstance(TDictionary.class);
            scan(value, null);
            jbc.callConstructor(TDictionary.class, Map.class);
            return null;
        }

        @Override
        public Void visitRangeType(IRRangeType rangeType, Void unused) {
            return scan(value, null);
        }

        @Override
        public Void visitFunctionType(IRFunctionType functionType, Void unused) {
            return scan(value, null);
        }

        @Override
        public Void visitTypeType(IRTypeType typeType, Void unused) {
            return scan(value, null);
        }

        @Override
        public Void visitUnknownType(IRUnknownType unknownType, Void unused) {
            return scan(value, null);
        }
    }


    private class ToPrimitiveCastGenerator extends IRTypeVisitorImpl<Void, Void> {

        @Override
        public Void visitIntegerType(IRIntegerType integerType, Void unused) {
            jbc.checkCast(TInteger.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(Integer.class);
            jbc.callVirtual(Integer.class, "intValue", Integer.TYPE);
            return null;
        }

        @Override
        public Void visitRealType(IRRealType realType, Void unused) {
            jbc.checkCast(TReal.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(Double.class);
            jbc.callVirtual(Double.class, "doubleValue", Double.TYPE);
            return null;
        }

        @Override
        public Void visitBooleanType(IRBooleanType booleanType, Void unused) {
            jbc.checkCast(TBoolean.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(Boolean.class);
            jbc.callVirtual(Boolean.class, "booleanValue", Boolean.TYPE);
            return null;
        }

        @Override
        public Void visitStringType(IRStringType stringType, Void unused) {
            jbc.checkCast(TString.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(String.class);
            return null;
        }

        @Override
        public Void visitNullType(IRNullType nullType, Void unused) {
            jbc.checkCast(Null.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            return null;
        }

        @Override
        public Void visitArrayType(IRArrayType arrayType, Void unused) {
            jbc.checkCast(TArray.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(List.class);
            return null;
        }

        @Override
        public Void visitDictionaryType(IRDictionaryType dictionaryType, Void unused) {
            jbc.checkCast(TDictionary.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(Map.class);
            return null;
        }

    }


    private class PrimitiveLoadLocal extends IRTypeVisitorImpl<Void, Void> {

        private final int index;

        private PrimitiveLoadLocal(int index) {
            this.index = index;
        }

        @Override
        public Void visitIntegerType(IRIntegerType integerType, Void unused) {
            jbc.loadLocalInt(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitRealType(IRRealType realType, Void unused) {
            jbc.loadLocalDouble(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitBooleanType(IRBooleanType booleanType, Void unused) {
            jbc.loadLocalInt(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitStringType(IRStringType stringType, Void unused) {
            jbc.loadLocal(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitNullType(IRNullType nullType, Void unused) {
            jbc.loadLocal(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitArrayType(IRArrayType arrayType, Void unused) {
            jbc.loadLocal(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitDictionaryType(IRDictionaryType dictionaryType, Void unused) {
            jbc.loadLocal(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitUnknownType(IRUnknownType unknownType, Void unused) {
            jbc.loadLocal(NATIVE_REG + index);
            return null;
        }
    }


    private class PrimitiveStoreLocal extends IRTypeVisitorImpl<Void, Void> {

        private final int index;

        private PrimitiveStoreLocal(int index) {
            this.index = index;
        }

        @Override
        public Void visitIntegerType(IRIntegerType integerType, Void unused) {
            jbc.storeLocalInt(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitRealType(IRRealType realType, Void unused) {
            jbc.storeLocalDouble(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitBooleanType(IRBooleanType booleanType, Void unused) {
            jbc.storeLocalInt(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitStringType(IRStringType stringType, Void unused) {
            jbc.storeLocal(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitNullType(IRNullType nullType, Void unused) {
            jbc.storeLocal(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitArrayType(IRArrayType arrayType, Void unused) {
            jbc.storeLocal(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitDictionaryType(IRDictionaryType dictionaryType, Void unused) {
            jbc.storeLocal(NATIVE_REG + index);
            return null;
        }

        @Override
        public Void visitUnknownType(IRUnknownType unknownType, Void unused) {
            jbc.storeLocal(NATIVE_REG + index);
            return null;
        }
    }


    private class NegationGenerator extends IRTypeVisitorImpl<Void, Void> {

        @Override
        public Void visitIntegerType(IRIntegerType integerType, Void unused) {
            jbc.negateInt();
            return null;
        }

        @Override
        public Void visitRealType(IRRealType realType, Void unused) {
            jbc.negateDouble();
            return null;
        }

    }

    private static class IsBoolType extends IRTypeVisitorImpl<Void, Boolean> {
        @Override
        public Boolean visitBooleanType(IRBooleanType booleanType, Void unused) {
            return true;
        }

        @Override
        public Boolean defaultOperation(IRType type, Void unused) {
            return false;
        }
    }

    private static class IsIntType extends IRTypeVisitorImpl<Void, Boolean> {

        @Override
        public Boolean visitIntegerType(IRIntegerType integerType, Void unused) {
            return super.visitIntegerType(integerType, unused);
        }

        @Override
        public Boolean defaultOperation(IRType type, Void unused) {
            return false;
        }
    }

    private static class IsRealType extends IRTypeVisitorImpl<Void, Boolean> {

        @Override
        public Boolean visitRealType(IRRealType realType, Void unused) {
            return true;
        }

        @Override
        public Boolean defaultOperation(IRType type, Void unused) {
            return false;
        }
    }

    private static class IsTypeType extends IRTypeVisitorImpl<Void, Boolean> {

        @Override
        public Boolean visitTypeType(IRTypeType typeType, Void unused) {
            return true;
        }

        @Override
        public Boolean defaultOperation(IRType type, Void unused) {
            return false;
        }
    }

}
