package com.tscript.runtime.jit.comp.emit.jbc;


import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.caching.ObjectCache;
import com.tscript.runtime.jit.ImageFrame;
import com.tscript.runtime.typing.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import java.util.*;

public class JBC implements Opcodes {

    private static final int THREAD_REG = 1;
    private static final int FRAME_REG = 2;
    private static final int TEMPS_REG = 4;



    private final JBCImageGenerator jbc;

    private final Map<Long, Label> labels;

    private final Deque<GenNode> operandStack;

    private final Map<Integer, Class<?>> tempJvmTypes;


    public JBC(String className) {
        this.jbc = new JBCImageGenerator(className);
        this.labels = new HashMap<>();
        this.operandStack = new ArrayDeque<>();
        this.tempJvmTypes = new HashMap<>();
    }

    public byte[] build() {
        assertStackIsEmpty();
        return jbc.generate();
    }
    
    public void emitNewLine(int line) {
        genLoadFrame();
        jbc.pushInt(line);
        jbc.putField(ImageFrame.class, "line", Integer.TYPE);
    }

    public void emitLoadInt(int value) {
        pushStack("LoadInt", Integer.TYPE, () -> jbc.pushInt(value));
    }

    public void emitLoadDouble(double value) {
        pushStack("loadDouble", Double.TYPE, () -> jbc.pushDouble(value));
    }

    public void emitLoadBoolean(boolean value) {
        pushStack("loadBoolean", Boolean.TYPE, () -> jbc.pushBoolean(value));
    }

    public void emitLoadWrappedBoolean(boolean value) {
        pushStack("loadWrappedBoolean", TBoolean.class, () -> {
            if (value) {
                jbc.getStatic(TBoolean.class, "TRUE", TBoolean.class);
            }
            else {
                jbc.getStatic(TBoolean.class, "FALSE", TBoolean.class);
            }
        });
    }
    
    public void emitLoadTNull() {
        pushStack("loadTNull", Null.class, () -> jbc.getStatic(Null.class, "INSTANCE", Null.class));
    }

    public void emitLoadString(String value) {
        pushStack("loadString", String.class, () -> jbc.pushString(value));
    }
    
    public void emitLoadVirtual(int address) {
        pushStack("loadVirtual", TObject.class, () -> {
            genLoadThread();
            jbc.pushInt(address);
            jbc.callStatic(JBCUtils.class, "loadVirtual", TObject.class, TThread.class, Integer.TYPE);
        });
    }

    public void emitLoadNative(String name) {
        pushStack("loadNative", TObject.class, () -> {
            genLoadThread();
            jbc.pushString(name);
            jbc.callStatic(JBCUtils.class, "loadNative", TObject.class, TThread.class, String.class);
        });
    }

    public void emitFunctionIdentity() {
        Runnable value = popStack(TObject.class);
        pushStack("functionIdentity", Integer.TYPE, () -> {
            value.run();
            jbc.dup();

            Label falseLabel = new Label();
            Label endLabel = new Label();

            jbc.instanceOf(Callable.class);
            jbc.getMethodVisitor().visitJumpInsn(IFEQ, falseLabel);
            jbc.callInterface(Callable.class, "getMetadata", Callable.Metadata.class);
            jbc.callStatic(ObjectCache.class, "getId", Integer.TYPE, Object.class);
            jbc.gotoLabel(endLabel);

            jbc.label(falseLabel);
            jbc.pop();
            jbc.pushInt(-1);

            jbc.label(endLabel);

        });
    }
    
    public void emitLoadBuiltin(int id) {
        pushStack("loadBuiltin", TObject.class, () -> {
            jbc.pushInt(id);
            jbc.callStatic(Builtins.class, "load", TObject.class, Integer.TYPE);
        });
    }

    public void emitLoadAbstract(String name) {
        pushStack("loadAbstract", TObject.class, () -> {
            genLoadOwner();
            jbc.pushString(name);
            jbc.callStatic(JBCUtils.class, "loadAbstract", TObject.class, TObject.class, String.class);
        });
    }

    public void emitMakeArray(int size) {
        Runnable[] args = new Runnable[size];
        for (int i = size-1; i >= 0; i--) {
            args[i] = popStack(TObject.class);
        }

        pushStack("makeArray", List.class, () -> {
            genNewTObjectArray(args);
            jbc.callStatic(Arrays.class, "asList", List.class, Object[].class);
        });
    }

    public void emitMakeDictionary(int size) {
        pushStack("makeDictionary", LinkedHashMap.class, () -> {
            jbc.newInstance(LinkedHashMap.class);
            jbc.callConstructor(LinkedHashMap.class);

            for (int i = 0; i < size; i++) {
                jbc.dup();

                Runnable value = popStack(TObject.class);
                Runnable key = popStack(TObject.class);

                key.run();
                value.run();

                jbc.callVirtual(LinkedHashMap.class, "put", Object.class, Object.class, Object.class);
                jbc.pop();
            }
        });
    }
    
    public void emitMakeRange(boolean fromWrapped) {
        Runnable to = popStack(fromWrapped ? TObject.class : Integer.TYPE);
        Runnable from = popStack(fromWrapped ? TObject.class : Integer.TYPE);

        pushStack("makeRange", Range.class, () -> {
            jbc.newInstance(com.tscript.runtime.typing.Range.class);

            if (fromWrapped) {
                from.run();
                jbc.checkCast(TInteger.class);
                to.run();
                jbc.checkCast(TInteger.class);
                jbc.callConstructor(com.tscript.runtime.typing.Range.class, TInteger.class, TInteger.class);
            }
            else {
                from.run();
                to.run();
                jbc.callConstructor(com.tscript.runtime.typing.Range.class, Integer.TYPE, Integer.TYPE);
            }
        });
    }
    
    public void emitLoadType(int address) {
        pushStack("loadType", Integer.TYPE, () -> jbc.pushInt(address));
    }
    
    public void emitGetType() {
        Runnable value = popStack(TObject.class);

        pushStack("getType", Type.class, () -> {
            value.run();
            jbc.callInterface(TObject.class, "getType", Type.class);
        });
    }

    public void emitLoadThis() {
        pushStack("loadThis", TObject.class, this::genLoadOwner);
    }

    public void emitGetIterator() {
        Runnable value = popStack(TObject.class);
        genLoadThread();
        genLoadFrame();
        value.run();
        jbc.callStatic(JBCUtils.class, "getIterator", TObject.class, TThread.class, ImageFrame.class, TObject.class);
        jbc.pop();
        assertStackIsEmpty();
    }

    public void emitIteratorHasNext() {
        pushStack("ItrHasNext", Boolean.TYPE, () -> {
            genLoadFrame();
            jbc.callStatic(JBCUtils.class, "iteratorHasNext", Boolean.TYPE, ImageFrame.class);
        });
    }
    
    public void emitIteratorNext() {
        pushStack("ItrNext", TObject.class, () -> {
            genLoadFrame();
            jbc.callStatic(JBCUtils.class, "iteratorNext", TObject.class, ImageFrame.class);
        });
    }

    public void emitToInplaceArgument() {
        Runnable value = popStack(TObject.class);

        pushStack("toInp", Argument.class, () -> {
            jbc.newInstance(Argument.class);
            value.run();
            jbc.callConstructor(Argument.class, TObject.class);
        });
    }

    public void emitToMappedArgument(String name) {
        Runnable value = popStack(TObject.class);

        pushStack("toMap", Argument.class, () -> {
            jbc.newInstance(Argument.class);
            jbc.pushString(name);
            value.run();
            jbc.callConstructor(Argument.class, String.class, TObject.class);
        });
    }
    
    public void emitCallInplace(int argCount) {
        Runnable value = popStack(TObject.class);

        Runnable[] args = new Runnable[argCount];
        for (int i = argCount-1; i >= 0; i--) {
            args[0] = popStack(TObject.class);
        }

        pushStack("callInplace", TObject.class, () -> {
            genLoadThread();
            value.run();
            genNewTObjectArray(args);
            jbc.callStatic(JBCUtils.class, "callInp", TObject.class, TThread.class, TObject.class, TObject[].class);
        });
    }

    public void emitPushFrame() {
        Runnable value = popStack(TObject.class);

        value.run();
        genLoadFrame();
        jbc.callStatic(JBCUtils.class, "pushFrame", ImageFrame.class, TObject.class, ImageFrame.class);
        genStoreFrame();

        assertStackIsEmpty();
    }

    public void emitPopFrame() {
        genLoadFrame();
        jbc.getField(ImageFrame.class, "next", ImageFrame.class);
        genStoreFrame();
    }
    
    public void emitReturn() {
        popStack().run();
        jbc.genReturnObject();

        assertStackIsEmpty();
    }
    
    public void emitWrapInt() {
        Runnable value = popStack(Integer.TYPE);

        pushStack("wrapInt", TInteger.class, () -> {
            jbc.newInstance(TInteger.class);
            value.run();
            jbc.callConstructor(TInteger.class, Integer.TYPE);
        });
    }

    public void emitUnwrapInt() {
        GenNode value = popStack(TObject.class);

        pushStack("unwrapInt", Integer.TYPE, () -> {
            value.run();
            if (value.jvmType != TInteger.class) {
                jbc.checkCast(TInteger.class);
            }
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(Integer.class);
            jbc.callVirtual(Integer.class, "intValue", Integer.TYPE);
        });
    }

    public void emitWrapReal() {
        Runnable value = popStack(Double.TYPE);

        pushStack("wrapReal", TReal.class, () -> {
            jbc.newInstance(TReal.class);
            value.run();
            jbc.callConstructor(TReal.class, Double.TYPE);
        });
    }
    
    public void emitUnwrapReal() {
        GenNode value = popStack(TObject.class);

        pushStack("unwrapReal", Double.TYPE, () -> {
            value.run();
            if (value.jvmType != TReal.class) {
                jbc.checkCast(TReal.class);
            }
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(Double.class);
            jbc.callVirtual(Double.class, "doubleValue", Double.TYPE);
        });
    }

    public void emitWrapBoolean() {
        Runnable value = popStack(Boolean.TYPE);

        pushStack("wrapBoolean", TBoolean.class, () -> {
            value.run();
            jbc.callStatic(TBoolean.class, "of", TBoolean.class, Boolean.TYPE);
        });
    }

    public void emitUnwrapBoolean() {
        GenNode value = popStack(TObject.class);

        pushStack("unwrapBoolean", Boolean.TYPE, () -> {
            value.run();
            if (value.jvmType != TBoolean.class) {
                jbc.checkCast(TBoolean.class);
            }
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(Boolean.class);
            jbc.callVirtual(Boolean.class, "booleanValue", Boolean.TYPE);
        });
    }
    
    public void emitWrapString() {
        Runnable value = popStack(String.class);

        pushStack("wrapString", TString.class, () -> {
            jbc.newInstance(TString.class);
            value.run();
            jbc.callConstructor(TString.class, String.class);
        });
    }
    
    public void emitUnwrapString() {
        GenNode value = popStack(TObject.class);

        pushStack("unwrapString", String.class, () -> {
            value.run();
            if (value.jvmType != TString.class) {
                jbc.checkCast(TString.class);
            }
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(String.class);
        });
    }
    
    public void emitWrapType() {
        Runnable value = popStack(Integer.TYPE);

        pushStack("wrapType", TObject.class, () -> {
            value.run();
            jbc.callStatic(ObjectCache.class, "getObject", Object.class, Integer.TYPE);
            jbc.checkCast(Type.class);
        });
    }
    
    public void emitUnwrapType() {
        Runnable value = popStack(TObject.class);

        pushStack("unwrapType", Integer.TYPE, () -> {
            value.run();
            jbc.callStatic(ObjectCache.class, "getId", Integer.TYPE, Object.class);
        });
    }

    public void emitWrapArray() {
        Runnable value = popStack(List.class);

        pushStack("wrapArray", TArray.class, () -> {
            jbc.newInstance(TArray.class);
            value.run();
            jbc.callConstructor(TArray.class, List.class);
        });
    }

    public void emitUnwrapArray() {
        GenNode value = popStack(TObject.class);

        pushStack("unwrapArray", List.class, () -> {
            value.run();
            if (value.jvmType != TArray.class) {
                jbc.checkCast(TArray.class);
            }
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(List.class);
        });
    }
    
    public void emitWrapDictionary() {
        Runnable value = popStack(Map.class);

        pushStack("wrapDictionary", TDictionary.class, () -> {
            jbc.newInstance(TDictionary.class);
            value.run();
            jbc.callConstructor(TDictionary.class, Map.class);
        });
    }

    public void emitUnwrapDictionary() {
        GenNode value = popStack(TObject.class);

        pushStack("unwrapDictionary", Map.class, () -> {
            value.run();
            if (value.jvmType != TDictionary.class) {
                jbc.checkCast(TDictionary.class);
            }
            jbc.callVirtual(PrimitiveObject.class, "getValue", Object.class);
            jbc.checkCast(Map.class);
        });
    }
    
    public void emitInt2Double() {
        Runnable value = popStack(Integer.TYPE);

        pushStack("int2Double", Double.TYPE, () -> {
            value.run();
            jbc.getMethodVisitor().visitInsn(I2D);
        });
    }
    
    public void emitDouble2Int() {
        Runnable value = popStack(Double.TYPE);

        pushStack("double2Int", Integer.TYPE, () -> {
            value.run();
            jbc.getMethodVisitor().visitInsn(D2I);
        });
    }

    public void emitLabel(long label) {
        assertStackIsEmpty();
        jbc.label(getASMLabel(label));
    }

    public void emitGoto(long label) {
        assertStackIsEmpty();
        jbc.gotoLabel(getASMLabel(label));
    }
    
    public void emitBranchWrapped(long jumpLabel) {
        // TODO
    }

    public void emitBranchPrimitive(long jumpLabel) {
        popStack().run();
        Label falseLabel = getASMLabel(jumpLabel);
        jbc.getMethodVisitor().visitJumpInsn(IFNE, falseLabel);
    }
    
    public void emitBranchIterator(long jumpLabel) {
        // TODO
    }

    public void emitGuardItrHasNext(int guardId, int ipRecover, int spRecover, boolean shouldHaveNext) {
        genLoadThread();
        genLoadFrame();
        jbc.pushInt(guardId);
        jbc.pushInt(ipRecover);
        jbc.pushInt(spRecover);
        jbc.pushBoolean(shouldHaveNext);
        jbc.callStatic(JBCUtils.class, "guardItrHasNext", Void.TYPE, TThread.class, ImageFrame.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE);
    }

    public void emitCreateSnapshotThenFallback(int guardId, int snapshotShapeId) {
        Runnable stack =  popStack(List.class);
        Runnable locals = popStack(List.class);

        genLoadThread();
        genLoadFrame();
        jbc.pushInt(snapshotShapeId);
        jbc.pushInt(guardId);
        locals.run();
        stack.run();

        jbc.callStatic(JBCUtils.class, "snapshotThenFallback", Void.TYPE, TThread.class, ImageFrame.class, Integer.TYPE, Integer.TYPE, List.class, List.class);
        assertStackIsEmpty();
    }

    public void emitCreateSnapshot(int snapshotShapeId) {
        Runnable stack =  popStack(List.class);
        Runnable locals = popStack(List.class);

        genLoadThread();
        genLoadFrame();
        jbc.pushInt(snapshotShapeId);
        locals.run();
        stack.run();

        jbc.callStatic(JBCUtils.class, "createSnapshot", Void.TYPE, TThread.class, ImageFrame.class, Integer.TYPE, List.class, List.class);
        assertStackIsEmpty();
    }

    public void emitFallback(int guardId) {
        genLoadThread();
        genLoadFrame();
        jbc.pushInt(guardId);
        jbc.callStatic(JBCUtils.class, "fallback", Void.TYPE, TThread.class, ImageFrame.class, Integer.TYPE);
        assertStackIsEmpty();
    }

    public void emitStoreLocal(int address) {
        genLoadLocalsArray();
        jbc.pushInt(address);
        popStack(TObject.class).run();
        jbc.storeArray();

        assertStackIsEmpty();
    }

    public void emitStoreTemp(int address, boolean requireStackEmpty) {
        GenNode value = popStack();
        value.run();  // any type

        jbc.storeLocal(value.jvmType, 4 + address);
        tempJvmTypes.put(address, value.jvmType);

        if (requireStackEmpty) {
            assertStackIsEmpty();
        }
    }

    public void emitLoadTemp(Class<?> clazz, int address) {
        pushStack("loadTemp", Objects.requireNonNull(tempJvmTypes.get(address)), () -> jbc.loadLocal(clazz, 4 + address));
    }

    public void emitLoadLocal(int address) {
        pushStack("loadLocal", TObject.class, () -> {
            genLoadLocalsArray();
            jbc.pushInt(address);
            jbc.loadArray();
        });
    }

    public void emitLoadGlobal(int address) {
        pushStack("loadGlobal", TObject.class, () -> {
            genLoadFrame();
            jbc.pushInt(address);
            jbc.callStatic(JBCUtils.class, "loadGlobal", TObject.class, ImageFrame.class, Integer.TYPE);
        });
    }
    
    public void emitStoreGlobal(int address) {
        genLoadFrame();
        popStack(TObject.class).run();
        jbc.pushInt(address);
        jbc.callStatic(JBCUtils.class, "storeGlobal", Void.TYPE, ImageFrame.class, TObject.class, Integer.TYPE);
        assertStackIsEmpty();
    }
    
    public void emitLoadInternal(int address) {
    }

    public void emitStoreInternal(int address) {
    }
    
    public void emitLoadExternal(String name) {
    }

    public void emitStoreExternal(String name) {
    }
    
    public void emitLoadStatic(String name) {
    }
    
    public void emitStoreStatic(String name) {
    }
    
    public void emitLoadSuper(String name) {
    }
    
    public void emitStoreSuper(String name) {
    }
    
    public void emitLoadName(String name) {
        pushStack("loadName", TObject.class, () -> {
            genLoadThread();
            genLoadFrame();
            jbc.pushString(name);
            jbc.callStatic(JBCUtils.class, "loadName", TObject.class, TThread.class, ImageFrame.class, String.class);
        });
    }
    
    public void emitCompareDynamic(boolean shouldBeEqual) {
        Runnable rhs = popStack(TObject.class);
        Runnable lhs = popStack(TObject.class);

        pushStack("compareDynamic", Boolean.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.callInterface(TObject.class, "equals", Boolean.TYPE, Object.class);
            if (!shouldBeEqual) emitNotBool();
        });
    }
    
    public void emitCompareInt(boolean shouldBeEqual) {
        if (shouldBeEqual) {
            emitCmp("compareInt", Integer.TYPE, IF_ICMPNE);
        } else {
            emitCmp("compareInt", Integer.TYPE, IF_ICMPEQ);
        }
    }
    
    public void emitCompareDouble(boolean shouldBeEqual) {
        pushStack("compareDouble", Boolean.TYPE, () -> {
            jbc.getMethodVisitor().visitInsn(DCMPL); // compare top two doubles on stack

            // 2. Branch based on result
            if (shouldBeEqual) {
                emitCmp("compareDouble", Double.TYPE, IFEQ);  // == case
            } else {
                emitCmp("compareDouble", Double.TYPE, IFNE);  // != case
            }
        });
    }

    public void emitCompareBoolean(boolean shouldBeEqual) {
        if (shouldBeEqual) {
            emitCmp("compareBool", Integer.TYPE, IF_ICMPNE);
        } else {
            emitCmp("compareBool", Integer.TYPE, IF_ICMPEQ);
        }
    }

    public void emitCompareIntLt() {
        emitCmp("compareIntLt", Integer.TYPE, IF_ICMPGE);
    }

    public void emitCompareDoubleLt() {
        pushStack("compareDoubleLt", Boolean.TYPE, () -> { /* TODO impl me */ });
    }

    public void emitCompareIntGt() {
        emitCmp("compareIntGt", Integer.TYPE, IF_ICMPLE);
    }

    public void emitCompareDoubleGt() {
        pushStack("compareDoubleGt", Boolean.TYPE, () -> { /* TODO impl me */ });
    }

    private void emitCmp(String desc, Class<?> comparableType, int cmp) {
        Runnable rhs = popStack(comparableType);
        Runnable lhs = popStack(comparableType);

        pushStack(desc, Boolean.TYPE, () -> {
            lhs.run();
            rhs.run();

            Label falseLabel = new Label();
            Label endLabel = new Label();

            jbc.getMethodVisitor().visitJumpInsn(cmp, falseLabel);


            jbc.pushInt(1);
            jbc.gotoLabel(endLabel);

            jbc.label(falseLabel);
            jbc.pushInt(0);

            jbc.label(endLabel);
        });
    }

    public void emitCompareString(boolean shouldBeEqual) {
        Runnable rhs = popStack(String.class);
        Runnable lhs = popStack(String.class);

        pushStack("compareString", Boolean.TYPE, () -> {
            lhs.run();
            rhs.run();
            jbc.callVirtual(String.class, "equals", Boolean.TYPE, Object.class);
            if (!shouldBeEqual) emitNotBool();
        });
    }
    
    public void emitOperateDynamicBinary(Opcode operation) {
        Runnable rhs = popStack(TObject.class);
        Runnable lhs = popStack(TObject.class);

        pushStack("operateDynamicBinary", TObject.class, () -> {

            genLoadThread();
            lhs.run();
            rhs.run();

            jbc.pushInt(operation.b);
            jbc.callStatic(JBCUtils.class, "binary", TObject.class, TThread.class, TObject.class, TObject.class, Byte.TYPE);
        });
    }

    public void emitOperateDynamicUnary(Opcode operation) {
        Runnable value = popStack(TObject.class);

        pushStack("operateDynamicUnary", TObject.class, () -> {

            genLoadThread();
            value.run();

            String name = switch (operation) {
                case NOT -> "not";
                case NEG -> "neg";
                case POS -> "pos";
                default -> throw new IllegalStateException();
            };
            jbc.callStatic(JBCUtils.class, name, TObject.class, TThread.class, TObject.class);
        });
    }

    public void emitAddInt() {
        Runnable rhs = popStack(Integer.TYPE);
        Runnable lhs = popStack(Integer.TYPE);

        pushStack("addInt", Integer.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IADD);
        });
    }

    public void emitAddDouble() {
        Runnable rhs = popStack(Double.TYPE);
        Runnable lhs = popStack(Double.TYPE);

        pushStack("addDouble", Double.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(DADD);
        });
    }

    public void emitAddStringWith(Class<?> rhsType) {
        Runnable rhs = popStack(rhsType);
        Runnable lhs = popStack(String.class);

        pushStack("addStringWith" + rhsType.getName(), String.class, () -> {
            jbc.newInstance(StringBuilder.class);
            lhs.run();
            jbc.callConstructor(StringBuilder.class, String.class);
            rhs.run();
            jbc.callVirtual(StringBuilder.class, "append", StringBuilder.class, rhsType);
            jbc.callVirtual(StringBuilder.class, "toString", String.class);
        });
    }

    public void emitSubInt() {
        Runnable rhs = popStack(Integer.TYPE);
        Runnable lhs = popStack(Integer.TYPE);

        pushStack("subInt", Integer.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(ISUB);
        });
    }
    
    public void emitSubDouble() {
        Runnable rhs = popStack(Double.TYPE);
        Runnable lhs = popStack(Double.TYPE);

        pushStack("subDouble", Double.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(DSUB);
        });
    }

    public void emitMulInt() {
        Runnable rhs = popStack(Integer.TYPE);
        Runnable lhs = popStack(Integer.TYPE);

        pushStack("mulInt", Integer.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IMUL);
        });
    }
    
    public void emitMulDouble() {
        Runnable rhs = popStack(Double.TYPE);
        Runnable lhs = popStack(Double.TYPE);

        pushStack("mulDouble", Double.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(DMUL);
        });
    }
    
    public void emitDivInt() {
        Runnable rhs = popStack(Integer.TYPE);
        Runnable lhs = popStack(Integer.TYPE);

        pushStack("divInt", Integer.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IDIV);
        });
    }
    
    public void emitDivDouble() {
        Runnable rhs = popStack(Double.TYPE);
        Runnable lhs = popStack(Double.TYPE);

        pushStack("divDouble", Double.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(DDIV);
        });
    }

    public void emitPowDouble() {
        Runnable rhs = popStack(Double.TYPE);
        Runnable lhs = popStack(Double.TYPE);

        pushStack("divDouble", Double.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.callStatic(Math.class, "pow", Double.TYPE, Double.TYPE, Double.TYPE);
        });
    }
    
    public void emitModuloInt() {
        Runnable rhs = popStack(Integer.TYPE);
        Runnable lhs = popStack(Integer.TYPE);

        pushStack("moduloInt", Integer.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IREM);
        });
    }

    public void emitAndInt() {
        Runnable rhs = popStack(Integer.TYPE);
        Runnable lhs = popStack(Integer.TYPE);

        pushStack("andInt", Integer.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IAND);
        });
    }
    
    public void emitOrInt() {
        Runnable rhs = popStack(Integer.TYPE);
        Runnable lhs = popStack(Integer.TYPE);

        pushStack("orInt", Integer.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IOR);
        });
    }
    
    public void emitXorInt() {
        Runnable rhs = popStack(Integer.TYPE);
        Runnable lhs = popStack(Integer.TYPE);

        pushStack("xorInt", Integer.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IXOR);
        });
    }
    
    public void emitAndBool() {
        emitAndInt();
    }
    
    public void emitOrBool() {
        emitOrInt();
    }
    
    public void emitXorBool() {
        emitXorInt();
    }

    public void emitShlInt() {
        Runnable rhs = popStack(Integer.TYPE);
        Runnable lhs = popStack(Integer.TYPE);

        pushStack("slaInt", Integer.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(ISHL);
        });
    }

    public void emitShrInt() {
        Runnable rhs = popStack(Integer.TYPE);
        Runnable lhs = popStack(Integer.TYPE);

        pushStack("slaInt", Integer.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(ISHR);
        });
    }

    public void emitUshrInt() {
        Runnable rhs = popStack(Integer.TYPE);
        Runnable lhs = popStack(Integer.TYPE);

        pushStack("slaInt", Integer.TYPE, () -> {

            lhs.run();
            rhs.run();

            jbc.getMethodVisitor().visitInsn(IUSHR);
        });
    }
    
    public void emitNotInt() {
        Runnable value = popStack(Integer.TYPE);
        pushStack("notInt", Integer.TYPE, () -> {
            value.run();
            jbc.getMethodVisitor().visitInsn(ICONST_M1);
            jbc.getMethodVisitor().visitInsn(IXOR);
        });
    }
    
    public void emitNotBool() {
        Runnable value = popStack(Boolean.TYPE);

        pushStack("notBool", Boolean.TYPE, () -> {
            value.run();

            Label falseLabel = new Label();
            Label endLabel = new Label();

            jbc.getMethodVisitor().visitJumpInsn(IFEQ, falseLabel);
            jbc.pushInt(0);
            jbc.gotoLabel(endLabel);

            jbc.label(falseLabel);
            jbc.pushInt(1);

            jbc.label(endLabel);
        });
    }

    public void emitNegateInt() {
        Runnable value = popStack(Integer.TYPE);

        pushStack("negateInt", Integer.TYPE, () -> {
            value.run();
            jbc.getMethodVisitor().visitInsn(INEG);
        });
    }
    
    public void emitNegateDouble() {
        Runnable value = popStack(Double.TYPE);

        pushStack("negateDouble", Double.TYPE, () -> {
            value.run();
            jbc.getMethodVisitor().visitInsn(DNEG);
        });
    }

    public void emitContainerWriteDynamic() {
        genLoadThread();

        Runnable value = popStack(TObject.class);
        Runnable key = popStack(TObject.class);
        Runnable container = popStack(TObject.class);

        container.run();
        key.run();
        value.run();

        jbc.callStatic(JBCUtils.class, "containerWrite", Boolean.TYPE, TThread.class, TObject.class, TObject.class, TObject.class);
        jbc.pop();

        assertStackIsEmpty();
    }
    
    public void emitContainerWriteArray() {
        Runnable value = popStack(TObject.class);
        Runnable key = popStack(Integer.TYPE);
        Runnable container = popStack(List.class);

        container.run();
        key.run();
        value.run();

        jbc.callInterface(List.class, "set", Object.class, Integer.TYPE, Object.class);
        jbc.pop();

        assertStackIsEmpty();
    }
    
    public void emitContainerReadDynamic() {
        Runnable key = popStack(TObject.class);
        Runnable container = popStack(TObject.class);

        pushStack("containerReadDynamic", TObject.class, () -> {
            genLoadThread();

            container.run();
            key.run();

            jbc.callStatic(JBCUtils.class, "containerRead", TObject.class, TThread.class, TObject.class, TObject.class);
        });
    }
    
    public void emitContainerReadArray() {
        Runnable key = popStack(TObject.class);
        Runnable container = popStack(List.class);

        pushStack("containerReadArray", Object.class, () -> {

            container.run();
            key.run();

            jbc.callInterface(List.class, "get", Object.class, Integer.TYPE);
        });
    }
    
    public void emitExprStmtCompletion() {
        popStack(TObject.class).run();
        assertStackIsEmpty();
        jbc.pop();
        assertStackIsEmpty();
    }
    
    public void emitSetOwner() {
        Runnable value = popStack(TObject.class);
        Runnable owner = popStack(TObject.class);

        pushStack("setOwner", TObject.class, () -> {
            genLoadThread();
            value.run();
            owner.run();
            jbc.callStatic(JBCUtils.class, "setOwner", TObject.class, TThread.class, TObject.class, TObject.class);
        });
    }

    public void emitExtend(String name) {
        GenNode value = popStack(TObject.class);
        GenNode owner = popStack(TObject.class);

        pushStack("extend", TObject.class, () -> {
            genLoadThread();

            owner.run();
            value.run();

            jbc.pushString(name);
            jbc.callStatic(JBCUtils.class, "extend", TObject.class, TThread.class, TObject.class, TObject.class, String.class);
        });
    }
    
    public void emitImport(int address) {
        pushStack("import", TObject.class, () -> {
            jbc.pushInt(address);
            jbc.callStatic(JBCUtils.class, "importModule", TObject.class, TThread.class, Integer.TYPE);
        });
    }
    
    public void emitImport(String name) {
        pushStack("import", TObject.class, () -> {
            jbc.pushString(name);
            jbc.callStatic(JBCUtils.class, "importModule", TObject.class, TThread.class, String.class);
        });
    }

    public void emitUse(String name) {
        genLoadThread();
        genLoadFrame();
        popStack(TObject.class).run();
        jbc.pushString(name);
        jbc.callStatic(JBCUtils.class, "use", Boolean.TYPE, TThread.class, ImageFrame.class, TObject.class, String.class);
        jbc.pop();
        assertStackIsEmpty();
    }

    public void emitUseMembers() {
        genLoadThread();
        genLoadFrame();
        popStack(TObject.class).run();
        jbc.callStatic(JBCUtils.class, "use", Boolean.TYPE, TThread.class, ImageFrame.class, TObject.class);
        jbc.pop();
        assertStackIsEmpty();
    }

    public void emit2Str(Class<?> receiver, Class<?> arg) {
        GenNode value = popStack(TObject.class);

        pushStack("int2Str", String.class, () -> {
            value.run();
            jbc.callStatic(receiver, "toString", String.class, arg);
        });
    }

    public void emitPop() {
        operandStack.pop().run();
        assertStackIsEmpty();
        jbc.pop();
    }

    private Label getASMLabel(long address) {
        return labels.computeIfAbsent(address, k -> new Label());
    }

    private void genLoadThread() {
        jbc.loadLocal(TThread.class, THREAD_REG);
    }

    private void genLoadFrame(){
        jbc.loadLocal(VirtualFrame.class, FRAME_REG);
    }

    private void genStoreFrame() {
        jbc.storeLocal(VirtualFrame.class, FRAME_REG);
    }

    private void genLoadLocalsArray(){
        genLoadFrame();
        jbc.getField(ImageFrame.class, "locals", TObject[].class);
    }

    private void genLoadOwner() {
        genLoadFrame();
        jbc.getField(ImageFrame.class, "owner", TObject.class);
    }

    private void genNewTObjectArray(Runnable[] args) {
        jbc.newArray(TObject.class, args.length);

        for (int i = 0; i < args.length; i++) {
            jbc.dup();
            jbc.pushInt(i);
            args[i].run();
            jbc.storeArray();
        }
    }

    private void pushStack(String name, Class<?> jvmType, Runnable runnable) {
        operandStack.push(new GenNode(name, jvmType, runnable));
    }

    private GenNode popStack() {
        return operandStack.remove();
    }

    private GenNode popStack(Class<?> jvmType) {
        GenNode node = operandStack.remove();
        expectJvmType(node, jvmType);
        return node;
    }

    private void assertStackIsEmpty() {
        if (!operandStack.isEmpty()) {
            throw new AssertionError("stack is not empty (size is " + operandStack.size() + ")");
        }
    }

    private void expectJvmType(GenNode node, Class<?> expectedJvm) {
        if (node.jvmType == null) return;
        if (!expectedJvm.isAssignableFrom(node.jvmType)) {
            operandStack.push(node);
            throw new AssertionError("Expected " + expectedJvm.getName() + " but got " + node.jvmType.getName() + " with stack " + operandStack);
        }
    }

    private static class GenNode implements Runnable {
        public final Runnable runnable;
        public final Class<?> jvmType;
        private final String name;

        public GenNode(String name, Class<?> jvmType, Runnable runnable) {
            this.runnable = runnable;
            this.jvmType = jvmType;
            this.name = name;
        }

        @Override
        public void run() {
            runnable.run();
        }

        @Override
        public String toString() {
            return name + "(" + jvmType.getSimpleName() + ")";
        }
    }
}

