package com.tscript.runtime.jit.emit;

import com.tscript.runtime.core.Argument;
import com.tscript.runtime.core.Builtins;
import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.caching.TypeCache;
import com.tscript.runtime.jit.emit.jbc.JBCImageGenerator;
import com.tscript.runtime.jit.emit.jbc.MaxTempRegisters;
import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.jit.lower.jbc.JBCUtils;
import com.tscript.runtime.jit.tracing.ImageFrame;
import com.tscript.runtime.typing.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import java.util.*;

public class IREmitterJBC implements IREmitter, Opcodes {

    private static final int THREAD_REG = 1;
    private static final int FRAME_REG = 2;
    private static final int TEMPS_REG = 3;

    private static final int NATIVE_REG = 4;



    private final JBCImageGenerator jbc;

    private final Map<Integer, Label> labels;
    
    private final Deque<Runnable> operandStack;


    public IREmitterJBC(String className) {
        this.jbc = new JBCImageGenerator(className);
        this.labels = new HashMap<>();
        this.operandStack = new ArrayDeque<>();
    }


    @Override
    public void init(IRBlock block) {
        int tempRegs = MaxTempRegisters.compute(block);
        if (tempRegs > 0){
            jbc.newArray(TObject.class, tempRegs);
            jbc.storeLocal(TEMPS_REG);
        }
    }

    @Override
    public byte[] build() {
        assertStackIsEmpty();
        return jbc.generate();
    }


    @Override
    public void emitNewLine(int line) {
        genLoadFrame();
        jbc.pushInt(line);
        jbc.putField(ImageFrame.class, "line", Integer.TYPE);
    }

    @Override
    public void emitLoadInt(int value) {
        pushStack("LoadInt", () -> jbc.pushInt(value));
    }

    @Override
    public void emitLoadDouble(double value) {
        pushStack("loadDouble", () -> jbc.pushDouble(value));
    }

    @Override
    public void emitLoadBoolean(boolean value) {
        pushStack("loadBoolean", () -> jbc.pushBoolean(value));
    }

    @Override
    public void emitLoadTNull() {
        pushStack("loadTNull", () -> jbc.getStatic(Null.class, "INSTANCE", Null.class));
    }

    @Override
    public void emitLoadString(String value) {
        pushStack("loadString", () -> jbc.pushString(value));
    }

    @Override
    public void emitLoadVirtual(int address) {
        pushStack("loadVirtual", () -> {
            genLoadThread();
            jbc.pushInt(address);
            jbc.callStatic(JBCUtils.class, "loadVirtual", TObject.class, TThread.class, Integer.TYPE);
        });
    }

    @Override
    public void emitLoadNative(String name) {
        pushStack("loadNative", () -> {
            genLoadThread();
            jbc.pushString(name);
            jbc.callStatic(JBCUtils.class, "loadNative", TObject.class, TThread.class, String.class);
        });
    }

    @Override
    public void emitLoadBuiltin(int id) {
        pushStack("loadBuiltin", () -> {
            jbc.pushInt(id);
            jbc.callStatic(Builtins.class, "load", TObject.class, Integer.TYPE);
        });
    }

    @Override
    public void emitLoadAbstract(String name) {
        pushStack("loadAbstract", () -> {
            genLoadOwner();
            jbc.pushString(name);
            jbc.callStatic(JBCUtils.class, "loadAbstract", TObject.class, TObject.class, String.class);
        });
    }

    @Override
    public void emitMakeArray(int size) {
        pushStack("makeArray", () -> {
            for (int i = 0; i < size; i++) {
                genNewTObjectArray(size);
                jbc.callStatic(Arrays.class, "asList", List.class, Object[].class);
            }
        });
    }

    @Override
    public void emitMakeDictionary(int size) {
        pushStack("makeDictionary", () -> {
            jbc.newInstance(LinkedHashMap.class);
            jbc.callConstructor(LinkedHashMap.class);

            for (int i = 0; i < size; i++) {
                jbc.dup();

                Runnable value = popStack();
                Runnable key = popStack();

                key.run();
                value.run();

                jbc.callVirtual(LinkedHashMap.class, "put", Object.class, Object.class, Object.class);
                jbc.pop();
            }
        });
    }

    @Override
    public void emitMakeRange(boolean fromWrapped) {
        Runnable to = popStack();
        Runnable from = popStack();

        pushStack("makeRange", () -> {
            jbc.newInstance(com.tscript.runtime.typing.Range.class);

            from.run();
            to.run();

            if (fromWrapped) {
                jbc.callConstructor(com.tscript.runtime.typing.Range.class, TInteger.class, TInteger.class);
            }
            else {
                jbc.callConstructor(com.tscript.runtime.typing.Range.class, Integer.TYPE, Integer.TYPE);
            }
        });
    }

    @Override
    public void emitLoadType(int address, boolean fromModule) {
        pushStack("loadType", () -> {
            if (fromModule) {
                genLoadThread();
                jbc.pushInt(address);
                jbc.callStatic(JBCUtils.class, "loadType", TObject.class, TThread.class, Integer.TYPE);
            }
            else {
                jbc.pushInt(address);
                jbc.callStatic(TypeCache.class, "getType", TObject.class, Integer.TYPE);
            }
        });
    }

    @Override
    public void emitGetType() {
        pushStack("getType", () -> jbc.callInterface(TObject.class, "getType", Type.class));
    }

    @Override
    public void emitLoadThis() {
        pushStack("loadThis", this::genLoadOwner);
    }

    @Override
    public void emitGetIterator() {
        Runnable value = popStack();
        genLoadThread();
        genLoadFrame();
        value.run();
        jbc.callStatic(JBCUtils.class, "getIterator", TObject.class, TThread.class, ImageFrame.class, TObject.class);
        assertStackIsEmpty();
    }

    @Override
    public void emitIteratorHasNext() {
        pushStack("ItrHasNext", () -> {
            genLoadFrame();
            jbc.callStatic(JBCUtils.class, "iteratorHasNext", Boolean.TYPE, ImageFrame.class);
        });
    }

    @Override
    public void emitIteratorNext() {
        pushStack("ItrNext", () -> {
            genLoadFrame();
            jbc.callStatic(JBCUtils.class, "iteratorNext", TObject.class, ImageFrame.class);
        });
    }

    @Override
    public void emitToInplaceArgument() {
        Runnable value = popStack();

        pushStack("toInp",() -> {
            jbc.newInstance(Argument.class);
            value.run();
            jbc.callConstructor(Argument.class, TObject.class);
        });
    }

    @Override
    public void emitToMappedArgument(String name) {
        Runnable value = popStack();

        pushStack("toMap", () -> {
            jbc.newInstance(Argument.class);
            jbc.pushString(name);
            value.run();
            jbc.callConstructor(Argument.class, String.class, TObject.class);
        });
    }

    @Override
    public void emitCallInplace(int argCount) {
        Runnable value = popStack();

        pushStack("callInplace", () -> {
            genLoadThread();
            value.run();
            genNewTObjectArray(argCount);
            jbc.callStatic(JBCUtils.class, "callInp", TObject.class, TThread.class, TObject.class, TObject[].class);
        });
    }

    @Override
    public void emitPushFrame() {
        Runnable value = popStack();

        pushStack("pushFrame", () -> {
            value.run();
            genLoadFrame();
            jbc.pushInt(0);
            jbc.callStatic(JBCUtils.class, "pushFrame", ImageFrame.class, TObject.class, ImageFrame.class, Integer.TYPE);
            genStoreFrame();
        });
    }

    @Override
    public void emitPopFrame() {
        pushStack("popFrame", () -> {
            genLoadFrame();
            jbc.dup();
            jbc.getField(ImageFrame.class, "next", ImageFrame.class);
            genStoreFrame();
        });
    }

    @Override
    public void emitReturn() {
        popStack().run();
        jbc.genReturnObject();

        assertStackIsEmpty();
    }

    @Override
    public void emitWrapInt() {
        Runnable value = popStack();

        pushStack("wrapInt", () -> {
            jbc.newInstance(TInteger.class);
            value.run();
            jbc.callConstructor(TInteger.class, Integer.TYPE);
        });
    }

    @Override
    public void emitUnwrapInt() {
        Runnable value = popStack();

        pushStack("unwrapInt", () -> {
            value.run();
            jbc.checkCast(TInteger.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(Integer.class);
            jbc.callVirtual(Integer.class, "intValue", Integer.TYPE);
        });
    }

    @Override
    public void emitWrapReal() {
        Runnable value = popStack();

        pushStack("wrapReal", () -> {
            jbc.newInstance(TReal.class);
            value.run();
            jbc.callConstructor(TReal.class, Double.TYPE);
        });
    }

    @Override
    public void emitUnwrapReal() {
        Runnable value = popStack();

        pushStack("unwrapReal", () -> {
            value.run();
            jbc.checkCast(TReal.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(Double.class);
            jbc.callVirtual(Double.class, "doubleValue", Double.TYPE);
        });
    }

    @Override
    public void emitWrapBoolean() {
        Runnable value = popStack();

        pushStack("wrapBoolean", () -> {
            value.run();
            jbc.callStatic(TBoolean.class, "of", TBoolean.class, Boolean.TYPE);
        });
    }

    @Override
    public void emitUnwrapBoolean() {
        Runnable value = popStack();

        pushStack("unwrapBoolean", () -> {
            value.run();
            jbc.checkCast(TBoolean.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(Boolean.class);
            jbc.callVirtual(Boolean.class, "booleanValue", Boolean.TYPE);
        });
    }

    @Override
    public void emitWrapString() {
        Runnable value = popStack();

        pushStack("wrapString", () -> {
            jbc.newInstance(TString.class);
            value.run();
            jbc.callConstructor(TString.class, String.class);
        });
    }

    @Override
    public void emitUnwrapString() {
        Runnable value = popStack();

        pushStack("unwrapString", () -> {
            value.run();
            jbc.checkCast(TString.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(String.class);
        });
    }

    @Override
    public void emitWrapType() {
        Runnable value = popStack();

        pushStack("wrapType", () -> {
            value.run();
            jbc.callStatic(TypeCache.class, "getType", TObject.class, Integer.TYPE);
        });
    }

    @Override
    public void emitUnwrapType() {
        Runnable value = popStack();

        pushStack("unwrapType", () -> {
            value.run();
            jbc.checkCast(Type.class);
            jbc.callStatic(TypeCache.class, "getId", Integer.TYPE, TObject.class);
        });
    }

    @Override
    public void emitWrapArray() {
        Runnable value = popStack();

        pushStack("wrapArray", () -> {
            jbc.newInstance(TArray.class);
            value.run();
            jbc.callConstructor(TArray.class, List.class);
        });
    }

    @Override
    public void emitUnwrapArray() {
        Runnable value = popStack();

        pushStack("unwrapArray", () -> {
            value.run();
            jbc.checkCast(TArray.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(List.class);
        });
    }

    @Override
    public void emitWrapDictionary() {
        Runnable value = popStack();

        pushStack("wrapDictionary", () -> {
            jbc.newInstance(TDictionary.class);
            value.run();
            jbc.callConstructor(TDictionary.class, Map.class);
        });
    }

    @Override
    public void emitUnwrapDictionary() {
        Runnable value = popStack();

        pushStack("unwrapDictionary", () -> {
            value.run();
            jbc.checkCast(TDictionary.class);
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(Map.class);
        });
    }

    @Override
    public void emitInt2Double() {
        Runnable value = popStack();

        pushStack("int2Double", () -> {
            value.run();
            jbc.getMethodVisitor().visitInsn(I2D);
        });
    }

    @Override
    public void emitDouble2Int() {
        Runnable value = popStack();

        pushStack("double2Int", () -> {
            value.run();
            jbc.getMethodVisitor().visitInsn(D2I);
        });
    }

    @Override
    public void emitLabel(int label) {
        assertStackIsEmpty();
        jbc.label(getASMLabel(label));
    }

    @Override
    public void emitGoto(int label) {
        assertStackIsEmpty();
        jbc.gotoLabel(getASMLabel(label));
    }

    @Override
    public void emitBranchWrapped(int jumpLabel, int nonJumpLabel) {
        // TODO
    }

    @Override
    public void emitBranchPrimitive(int jumpLabel, int nonJumpLabel) {
        // TODO
    }

    @Override
    public void emitBranchIterator(int jumpLabel, int nonJumpLabel) {
        // TODO
    }

    @Override
    public void emitGuardWrapped(int ipRecover, int spRecover) {
        Runnable value = popStack();
        value.run();
        genLoadThread();
        genLoadFrame();
        jbc.pushInt(ipRecover);
        jbc.pushInt(spRecover);
        jbc.pushBoolean(true);
        jbc.callStatic(JBCUtils.class, "guardBool", Void.TYPE, TObject.class, TThread.class, ImageFrame.class, Integer.TYPE, Integer.TYPE, Boolean.TYPE);
        assertStackIsEmpty();
    }

    @Override
    public void emitGuardPrimitiveBoolean(int ipRecover, int spRecover) {
        emitWrapBoolean();
        emitGuardWrapped(ipRecover, spRecover);
    }

    @Override
    public void emitStoreLocal(int address) {
        genLoadLocalsArray();
        jbc.pushInt(address);
        popStack().run();
        jbc.storeArray();
        
        assertStackIsEmpty();
    }

    @Override
    public void emitStoreLocalInt(int address) {
    }

    @Override
    public void emitStoreLocalDouble(double address) {
    }

    @Override
    public void emitStoreLocalBoolean(boolean address) {
    }

    @Override
    public void emitStoreLocalString(String address) {
    }

    @Override
    public void emitLoadLocal(int address) {
        pushStack("loadLocal", () -> {
            genLoadLocalsArray();
            jbc.pushInt(address);
            jbc.loadArray();
        });
    }

    @Override
    public void emitLoadLocalInt(int address) {
    }

    @Override
    public void emitLoadLocalDouble(int address) {
    }

    @Override
    public void emitLoadLocalBoolean(int address) {
    }

    @Override
    public void emitLoadLocalString(int address) {
    }

    @Override
    public void emitLoadTemp(int address) {
        pushStack("loadTemp", () -> {
            genLoadTemps();
            jbc.pushInt(address);
            jbc.loadArray();
        });
    }

    @Override
    public void emitStoreTemp(int address) {
        genLoadTemps();
        jbc.pushInt(address);
        popStack().run();
        jbc.storeArray();
        assertStackIsEmpty();
    }

    @Override
    public void emitLoadGlobal(int address) {
        pushStack("loadGlobal", () -> {
            genLoadFrame();
            jbc.pushInt(address);
            jbc.callStatic(JBCUtils.class, "loadGlobal", TObject.class, ImageFrame.class, Integer.TYPE);
        });
    }

    @Override
    public void emitStoreGlobal(int address) {
        genLoadFrame();
        popStack().run();
        jbc.pushInt(address);
        jbc.callStatic(JBCUtils.class, "StoreGlobal", Void.TYPE, ImageFrame.class, TObject.class, Integer.TYPE);
        assertStackIsEmpty();
    }

    @Override
    public void emitLoadInternal(int address) {
    }

    @Override
    public void emitStoreInternal(int address) {
    }

    @Override
    public void emitLoadExternal(String name) {
    }

    @Override
    public void emitStoreExternal(String name) {
    }

    @Override
    public void emitLoadStatic(String name) {
    }

    @Override
    public void emitStoreStatic(String name) {
    }

    @Override
    public void emitLoadSuper(String name) {
    }

    @Override
    public void emitStoreSuper(String name) {
    }

    @Override
    public void emitLoadName(String name) {
        pushStack("loadName", () -> {
            genLoadThread();
            genLoadFrame();
            jbc.pushString(name);
            jbc.callStatic(JBCUtils.class, "loadName", TObject.class, TThread.class, ImageFrame.class, String.class);
        });
    }

    @Override
    public void emitCompareDynamic(boolean shouldBeEqual) {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("compareDynamic", () -> {

            lhs.run();
            rhs.run();

            jbc.callInterface(TObject.class, "equals", Boolean.TYPE, Object.class);
        });
    }

    @Override
    public void emitCompareInt(boolean shouldBeEqual) {
        pushStack("compareInt", () -> { /* TODO impl me */ });
    }

    @Override
    public void emitCompareDouble(boolean shouldBeEqual) {
        pushStack("compareDouble", () -> { /* TODO impl me */ });
    }

    @Override
    public void emitCompareBoolean(boolean shouldBeEqual) {
        pushStack("compareBoolean", () -> { /* TODO impl me */ });
    }

    @Override
    public void emitCompareString(boolean shouldBeEqual) {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("compareString", () -> {
            lhs.run();
            rhs.run();
            jbc.callVirtual(String.class, "equals", Boolean.TYPE, Object.class);
        });
    }

    @Override
    public void emitOperateDynamic(Opcode operation) {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("operateDynamic", () -> {

            genLoadThread();
            lhs.run();
            rhs.run();

            jbc.pushInt(operation.b);
            jbc.callStatic(JBCUtils.class, "binary", TObject.class, TThread.class, TObject.class, TObject.class, Byte.TYPE);
        });
    }

    @Override
    public void emitAddInt() {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("addInt", () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IADD);
        });
    }

    @Override
    public void emitAddDouble() {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("addDouble", () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(DADD);
        });
    }

    @Override
    public void emitSubInt() {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("subInt", () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(ISUB);
        });
    }

    @Override
    public void emitSubDouble() {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("subDouble", () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(DSUB);
        });
    }

    @Override
    public void emitMulInt() {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("mulInt", () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IMUL);
        });
    }

    @Override
    public void emitMulDouble() {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("mulDouble", () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(DMUL);
        });
    }

    @Override
    public void emitDivInt() {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("divInt", () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IDIV);
        });
    }

    @Override
    public void emitDivDouble() {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("divDouble", () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(DDIV);
        });
    }

    @Override
    public void emitModuloInt() {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("moduloInt", () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IREM);
        });
    }

    @Override
    public void emitAndInt() {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("andInt", () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IAND);
        });
    }

    @Override
    public void emitOrInt() {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("orInt", () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IOR);
        });
    }

    @Override
    public void emitXorInt() {
        Runnable rhs = popStack();
        Runnable lhs = popStack();

        pushStack("xorInt", () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IXOR);
        });
    }

    @Override
    public void emitAndBool() {
        emitAndInt();
    }

    @Override
    public void emitOrBool() {
        emitOrInt();
    }

    @Override
    public void emitXorBool() {
        emitXorInt();
    }

    @Override
    public void emitNotInt() {
        Runnable value = popStack();
        pushStack("notInt", () -> {
            value.run();
            jbc.getMethodVisitor().visitInsn(ICONST_M1);
            jbc.getMethodVisitor().visitInsn(IXOR);
        });
    }

    @Override
    public void emitNotBool() {
        Runnable value = popStack();

        pushStack("notBool", () -> {
            value.run();

            Label falseLabel = new Label();
            Label endLabel = new Label();

            jbc.getMethodVisitor().visitJumpInsn(IFEQ, falseLabel);
            jbc.pushInt(0);
            jbc.gotoLabel(endLabel);

            jbc.label(falseLabel);
            jbc.pushInt(1);

            jbc.gotoLabel(endLabel);
        });
    }

    @Override
    public void emitNegateInt() {
        Runnable value = popStack();

        pushStack("negateInt", () -> {
            value.run();
            jbc.getMethodVisitor().visitInsn(INEG);
        });
    }

    @Override
    public void emitNegateDouble() {
        Runnable value = popStack();

        pushStack("negateDouble", () -> {
            value.run();
            jbc.getMethodVisitor().visitInsn(DNEG);
        });
    }

    @Override
    public void emitContainerWriteDynamic() {
        genLoadThread();

        Runnable value = popStack();
        Runnable key = popStack();
        Runnable container = popStack();

        container.run();
        key.run();
        value.run();

        jbc.callStatic(JBCUtils.class, "containerWrite", Boolean.TYPE, TThread.class, TObject.class, TObject.class, TObject.class);
        jbc.pop();

        assertStackIsEmpty();
    }

    @Override
    public void emitContainerWriteArray() {
        Runnable value = popStack();
        Runnable key = popStack();
        Runnable container = popStack();

        container.run();
        key.run();
        value.run();

        jbc.callInterface(List.class, "set", Object.class, Integer.TYPE, Object.class);
        jbc.pop();

        assertStackIsEmpty();
    }

    @Override
    public void emitContainerReadDynamic() {
        Runnable key = popStack();
        Runnable container = popStack();

        pushStack("containerReadDynamic", () -> {
            genLoadThread();

            container.run();
            key.run();

            jbc.callStatic(JBCUtils.class, "containerRead", TObject.class, TThread.class, TObject.class, TObject.class);
        });
    }

    @Override
    public void emitContainerReadArray() {
        Runnable key = popStack();
        Runnable container = popStack();

        pushStack("containerReadArray", () -> {

            container.run();
            key.run();

            jbc.callInterface(List.class, "get", Object.class, Integer.TYPE);
        });
    }

    @Override
    public void emitExprStmtCompletion() {
        popStack().run();
        assertStackIsEmpty();
        jbc.pop();
        assertStackIsEmpty();
    }

    @Override
    public void emitSetOwner() {
        Runnable value = popStack();
        Runnable owner = popStack();

        pushStack("setOwner", () -> {
            genLoadThread();
            value.run();
            owner.run();
            jbc.callStatic(JBCUtils.class, "setOwner", TObject.class, TThread.class, TObject.class, TObject.class);
        });
    }

    @Override
    public void emitExtend(String name) {
        Runnable value = popStack();
        Runnable owner = popStack();

        pushStack("extend", () -> {
            genLoadThread();

            owner.run();
            value.run();

            jbc.pushString(name);
            jbc.callStatic(JBCUtils.class, "extend", TObject.class, TThread.class, TObject.class, TObject.class, String.class);
        });
    }

    @Override
    public void emitImport(int address) {
        pushStack("import", () -> {
            jbc.pushInt(address);
            jbc.callStatic(JBCUtils.class, "importModule", TObject.class, TThread.class, Integer.TYPE);
        });
    }

    @Override
    public void emitImport(String name) {
        pushStack("import", () -> {
            jbc.pushString(name);
            jbc.callStatic(JBCUtils.class, "importModule", TObject.class, TThread.class, String.class);
        });
    }

    @Override
    public void emitUse(String name) {
        genLoadThread();
        genLoadFrame();
        popStack().run();
        jbc.pushString(name);
        jbc.callStatic(JBCUtils.class, "use", Boolean.TYPE, TThread.class, ImageFrame.class, TObject.class, String.class);
        jbc.pop();
        assertStackIsEmpty();
    }

    @Override
    public void emitUseMembers() {
        genLoadThread();
        genLoadFrame();
        popStack().run();
        jbc.callStatic(JBCUtils.class, "use", Boolean.TYPE, TThread.class, ImageFrame.class, TObject.class);
        jbc.pop();
        assertStackIsEmpty();
    }


    private Label getASMLabel(int address) {
        return labels.computeIfAbsent(address, k -> new Label());
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

    private void genNewTObjectArray(int size) {
        jbc.newArray(TObject.class, size);

        for (int i = 0; i < size; i++) {
            jbc.dup();
            jbc.pushInt(i);
            popStack().run();
            jbc.storeArray();
        }
    }
    
    private void pushStack(String name, Runnable runnable) {
        operandStack.push(new Runnable() {
            @Override
            public void run() {
                runnable.run();
            }

            @Override
            public String toString() {
                return name;
            }
        });
    }
    
    private Runnable popStack() {
        return operandStack.remove();
    }
    
    private void assertStackIsEmpty() {
        if (!operandStack.isEmpty()) {
            throw new AssertionError("stack is not empty (size is " + operandStack.size() + ")");
        }
    }
}
