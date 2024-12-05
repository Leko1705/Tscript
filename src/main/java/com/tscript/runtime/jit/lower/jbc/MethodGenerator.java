package com.tscript.runtime.jit.lower.jbc;

import com.tscript.runtime.core.Argument;
import com.tscript.runtime.core.Builtins;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.graph.BranchNode;
import com.tscript.runtime.jit.graph.Context;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.graph.expr.*;
import com.tscript.runtime.jit.graph.expr.Null;
import com.tscript.runtime.jit.graph.expr.Range;
import com.tscript.runtime.jit.graph.expr.Type;
import com.tscript.runtime.jit.graph.traversal.Traverser;
import com.tscript.runtime.jit.graph.traversal.TreeTraverseStrategy;
import com.tscript.runtime.jit.tracing.ImageFrame;
import com.tscript.runtime.typing.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodGenerator extends TreeTraverseStrategy implements ExpressionVisitor<Void, Void>, Opcodes {

    private static final String TOBJECT_DESC = org.objectweb.asm.Type.getDescriptor(TObject.class);
    private static final String TOBJECT_INTERNAL = org.objectweb.asm.Type.getInternalName(TObject.class);


    private final String internalName;
    private final ClassWriter cw;
    private final MethodVisitor mv;
    private final MethodVisitor clinit;

    private final Map<Object, String> constants;
    private int sp; // used for debugging

    public MethodGenerator(String internalName, ClassWriter cw, MethodVisitor mv) {
        this.internalName = internalName;
        this.cw = cw;
        this.mv = mv;
        sp = 0;
        constants = new HashMap<>();
        clinit = cw.visitMethod(
                ACC_STATIC, // Access flags
                "<clinit>", // Special name for static initializer
                "()V", // Descriptor for void method with no parameters
                null, // Signature
                null  // Exceptions)
        );
    }
    
    private void decSp(){
        decSp(1);
    }
    
    private void decSp(int delta){
        sp -= delta;
        if (sp < 0){
            throw new IllegalStateException("sp < 0");
        }
    }

    public void generate(Node graph) {
        clinit.visitCode();

        Traverser.traverse(this, this, graph);

        // finish static initializer block
        clinit.visitInsn(RETURN);
        clinit.visitMaxs(0, 0);
        clinit.visitEnd();

        // finish actual method
        mv.visitInsn(ARETURN);
        decSp();

        if (sp != 0) throw new AssertionError("sp != 0: is " + sp);
    }

    @Override
    public boolean isThenFirst(BranchNode node, Context context) {
        return false;
    }

    @Override
    public Void visitPushFrame(PushFrame expr, Void unused) {

        expr.called.accept(this);
        genLoadFrame();
        pushJBCInt(mv, expr.extraLocals);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "pushFrame",
                "("
                        + TOBJECT_DESC
                        + org.objectweb.asm.Type.getDescriptor(ImageFrame.class)
                        + "I)"
                        + org.objectweb.asm.Type.getDescriptor(ImageFrame.class),
                false
        );
        genStoreFrame();

        decSp();
        return null;
    }

    @Override
    public Void visitPopFrame(PopFrame expr, Void unused) {
        genLoadFrame();

        mv.visitFieldInsn(
                GETFIELD,
                org.objectweb.asm.Type.getInternalName(ImageFrame.class),
                "next",
                org.objectweb.asm.Type.getDescriptor(ImageFrame.class)
        );

        genStoreFrame();
        return null;
    }

    @Override
    public Void visitPop(Pop expr, Void unused) {
        if (expr.expression() != null)
            expr.expression().accept(this);
        mv.visitInsn(POP);
        decSp();
        return null;
    }

    @Override
    public Void visitNull(Null expr, Void unused) {
        mv.visitFieldInsn(GETSTATIC,
                org.objectweb.asm.Type.getInternalName(com.tscript.runtime.typing.Null.class),
                "INSTANCE",
                org.objectweb.asm.Type.getDescriptor(com.tscript.runtime.typing.Null.class));
        sp++;
        return null;
    }

    @Override
    public Void visitInt(Int expr, Void unused) {

        String name = loadConstant(
                expr.value,
                TInteger.class,
                "(I)V",
                () -> pushJBCInt(clinit, expr.value)
        );

        mv.visitFieldInsn(
                GETSTATIC,
                internalName,
                name,
                TOBJECT_DESC
        );

        sp++;

        return null;
    }

    @Override
    public Void visitString(Str expr, Void unused) {

        String name = loadConstant(
                expr.value,
                TString.class,
                "(" + org.objectweb.asm.Type.getDescriptor(String.class) + ")V",
                () -> clinit.visitLdcInsn(expr.value)
        );

        mv.visitFieldInsn(
                GETSTATIC,
                internalName,
                name,
                TOBJECT_DESC
        );

        sp++;
        return null;
    }

    @Override
    public Void visitBool(Bool expr, Void unused) {

        String fieldName = expr.value ? "TRUE" : "FALSE";

        mv.visitFieldInsn(
                GETSTATIC,
                org.objectweb.asm.Type.getInternalName(TBoolean.class),
                fieldName,
                org.objectweb.asm.Type.getDescriptor(TBoolean.class));

        sp++;
        return null;
    }

    @Override
    public Void visitReal(Real expr, Void unused) {
        String name = loadConstant(
                expr.value,
                TReal.class,
                "(D)V",
                () -> clinit.visitLdcInsn(expr.value)
        );
        mv.visitFieldInsn(
                GETSTATIC,
                internalName,
                name,
                TOBJECT_DESC
        );

        sp++;
        return null;
    }

    @Override
    public Void visitRange(Range expr, Void unused) {
        String tIntegerDesc = org.objectweb.asm.Type.getDescriptor(TInteger.class);
        String tIntegerInternal = org.objectweb.asm.Type.getInternalName(TInteger.class);

        String rangeInternal = org.objectweb.asm.Type.getInternalName(com.tscript.runtime.typing.Range.class);

        mv.visitTypeInsn(NEW, rangeInternal);
        mv.visitInsn(DUP);

        expr.to.accept(this);
        mv.visitTypeInsn(CHECKCAST, tIntegerInternal);

        expr.from.accept(this);
        mv.visitTypeInsn(CHECKCAST, tIntegerInternal);

        mv.visitMethodInsn(
                INVOKESPECIAL,
                rangeInternal,
                "<init>",
                "("
                        + tIntegerDesc
                        + tIntegerDesc
                    + ")V",
                false
        );

        decSp();
        return null;
    }

    @Override
    public Void visitArray(Array expr, Void unused) {
        String tarrInternal = org.objectweb.asm.Type.getInternalName(TArray.class);

        mv.visitTypeInsn(NEW, tarrInternal);
        mv.visitInsn(DUP);

        genNewTObjectArray(expr.contents());

        mv.visitMethodInsn(
                INVOKESPECIAL,
                tarrInternal,
                "<init>",
                "(" + org.objectweb.asm.Type.getDescriptor(TObject[].class) + ")V",
                false
        );

        decSp(expr.contents().size()-1);
        return null;
    }

    @Override
    public Void visitDict(Dict expr, Void unused) {

        genNewTObjectArray(expr.keys);
        genNewTObjectArray(expr.values);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "makeDict",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TObject[].class)
                        + org.objectweb.asm.Type.getDescriptor(TObject[].class)
                        + ")"
                        + TOBJECT_DESC,
                false
        );

        decSp(expr.values.size() + expr.keys.size()-1);
        return null;
    }

    @Override
    public Void visitThis(This expr, Void unused) {
        genLoadOwner();
        sp++;
        return null;
    }

    @Override
    public Void visitVirtual(Virtual expr, Void unused) {

        genLoadThread();
        pushJBCInt(mv, expr.index());

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "loadVirtual",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + "I)"
                        + TOBJECT_DESC,
                false
        );

        sp++;
        return null;
    }

    @Override
    public Void visitNative(Native expr, Void unused) {

        mv.visitLdcInsn(expr.name);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "loadNative",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + org.objectweb.asm.Type.getDescriptor(String.class)
                        + ")"
                        + TOBJECT_DESC,
                false
        );

        if (expr.canFail)
            genFailureReturnIfNull();

        sp++;
        return null;
    }

    @Override
    public Void visitBuiltin(Builtin expr, Void unused) {
        pushJBCInt(mv, expr.index());

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(Builtins.class),
                "load",
                "(I)" + TOBJECT_DESC,
                false
        );

        sp++;
        return null;
    }

    @Override
    public Void visitAbstract(Abstract expr, Void unused) {

        if (expr.index != -1 && expr.layer < 0){
            loadFast(expr.index, expr.layer, false, this::genLoadOwner);
        }
        else {
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                    "searchMemberDownward",
                    "("
                            + TOBJECT_DESC
                            + org.objectweb.asm.Type.getDescriptor(String.class)
                            + ")" + TOBJECT_DESC,
                    false
            );

            if (expr.calFail)
                genFailureReturnIfNull();
        }

        sp++;
        return null;
    }

    @Override
    public Void visitCallInp(CallInp expr, Void unused) {
        genLoadThread();
        expr.called.accept(this);

        genNewTObjectArray(expr.args);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "callInp",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + TOBJECT_DESC
                        + org.objectweb.asm.Type.getDescriptor(TObject[].class)
                        + ")" + TOBJECT_DESC,
                false
        );

        if (expr.canFail)
            genFailureReturnIfNull();

        decSp(expr.args.size());

        return null;
    }

    @Override
    public Void visitCallMap(CallMap expr, Void unused) {
        genLoadThread();
        expr.called.accept(this);

        genNewTObjectArray(expr.args);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "callMap",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + TOBJECT_DESC
                        + org.objectweb.asm.Type.getDescriptor(TObject[].class)
                        + ")" + TOBJECT_DESC,
                false
        );

        if (expr.canFail)
            genFailureReturnIfNull();

        decSp(expr.args.size());

        return null;
    }

    @Override
    public Void visitToInpArg(ToInpArg expr, Void unused) {
        mv.visitTypeInsn(NEW, org.objectweb.asm.Type.getInternalName(Argument.class));
        mv.visitInsn(DUP);

        expr.arg.accept(this);

        mv.visitMethodInsn(
                INVOKESPECIAL,
                org.objectweb.asm.Type.getInternalName(Argument.class),
                "<init>",
                "("
                        + TOBJECT_DESC
                        + ")V",
                false
        );

        return null;
    }

    @Override
    public Void visitToMapArg(ToMapArg expr, Void unused) {
        mv.visitTypeInsn(NEW, org.objectweb.asm.Type.getInternalName(Argument.class));
        mv.visitInsn(DUP);

        mv.visitLdcInsn(expr.name);
        expr.arg.accept(this);

        mv.visitMethodInsn(
                INVOKESPECIAL,
                org.objectweb.asm.Type.getInternalName(Argument.class),
                "<init>",
                "("
                        + org.objectweb.asm.Type.getDescriptor(String.class)
                        + TOBJECT_DESC
                        + ")V",
                false
        );

        return null;
    }

    @Override
    public Void visitType(Type expr, Void unused) {
        genLoadThread();
        pushJBCInt(mv, expr.index());

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "loadType",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + "I)"
                        + TOBJECT_DESC,
                false
        );


        if (expr.canFail())
            genFailureReturnIfNull();
        sp++;
        return null;
    }

    @Override
    public Void visitGetType(GetType expr, Void unused) {
        expr.expression.accept(this);

        mv.visitMethodInsn(
                INVOKEINTERFACE,                  // Opcode for virtual method call
                TOBJECT_INTERNAL,                       // Class containing the method
                "getType",                             // Method name
                "()" + org.objectweb.asm.Type.getDescriptor(com.tscript.runtime.typing.Type.class),               // Method descriptor
                true                                   // Not an interface
        );
        return null;
    }

    @Override
    public Void visitMethod(Method expr, Void unused) {

        genLoadThread();

        expr.underlying.accept(this);

        if (expr.underlying == expr.owner){
            mv.visitInsn(DUP);
            sp++;
        }
        else {
            expr.owner.accept(this);
        }

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "setOwner",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + TOBJECT_DESC
                        + TOBJECT_DESC
                        + ")" + TOBJECT_DESC,
                false
        );

        if (expr.canFail)
            genFailureReturnIfNull();
        decSp();

        return null;
    }

    @Override
    public Void visitBinaryOp(BinaryOp expr, Void unused) {
        genLoadThread();
        expr.lhs.accept(this);
        expr.rhs.accept(this);
        pushJBCInt(mv, expr.opcode.b);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "binary",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + TOBJECT_DESC
                        + TOBJECT_DESC
                        + "B"
                        + ")" + TOBJECT_DESC,
                false
        );

        if (expr.canFail)
            genFailureReturnIfNull();

        decSp();
        return null;
    }

    @Override
    public Void visitNot(Not expr, Void unused) {
        genUnaryOp(expr.expression, "not", expr.canFail);
        return null;
    }

    @Override
    public Void visitNeg(Neg expr, Void unused) {
        genUnaryOp(expr.expression, "neg", expr.canFail);
        return null;
    }

    private void genUnaryOp(Expression exp, String jbcUtilMethodName, boolean calFail){
        genLoadThread();
        exp.accept(this);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                jbcUtilMethodName,
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + TOBJECT_DESC
                        + ")" + TOBJECT_DESC,
                false
        );

        if (calFail)
            genFailureReturnIfNull();

    }

    @Override
    public Void visitEquality(Equality expr, Void unused) {

        expr.lhs.accept(this);
        expr.rhs.accept(this);

        mv.visitMethodInsn(
                INVOKEINTERFACE,
                TOBJECT_INTERNAL,
                "equals",
                "(" + org.objectweb.asm.Type.getDescriptor(Object.class) + ")Z",
                true);

        if (!expr.shouldBeEqual) {
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IXOR);
        }

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(TBoolean.class),
                "of",
                "(Z)" + org.objectweb.asm.Type.getDescriptor(TBoolean.class),
                false
        );

        decSp();
        return null;
    }

    @Override
    public Void visitItr(Itr expr, Void unused) {
        genLoadThread();
        expr.iterable.accept(this);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "getIterator",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + TOBJECT_DESC
                        + ")" + TOBJECT_DESC,
                false
        );

        if (expr.canFail)
            genFailureReturnIfNull();

        return null;
    }

    @Override
    public Void visitItrNext(ItrNext expr, Void unused) {
        mv.visitInsn(DUP);
        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "iteratorNext",
                "(" + TOBJECT_DESC + ")" + TOBJECT_DESC,
                false
        );
        sp++;
        return null;
    }

    @Override
    public Void visitNewLine(NewLine expr, Void unused) {
        genLoadFrame();
        pushJBCInt(mv, expr.line());

        mv.visitFieldInsn(
                PUTFIELD,
                org.objectweb.asm.Type.getInternalName(ImageFrame.class),
                "line",
                "I"
        );

        return null;
    }

    @Override
    public Void visitLoadGlobal(LoadGlobal expr, Void unused) {
        genLoadFrame();
        pushJBCInt(mv, expr.index());

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "loadGlobal",
                "("
                        + org.objectweb.asm.Type.getDescriptor(ImageFrame.class)
                        + "I)" + TOBJECT_DESC,
                false
        );

        sp++;
        return null;
    }

    @Override
    public Void visitStoreGlobal(StoreGlobal expr, Void unused) {
        genLoadThread();
        genLoadFrame();
        expr.what.accept(this);
        pushJBCInt(mv, expr.index);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "storeGlobal",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + org.objectweb.asm.Type.getDescriptor(ImageFrame.class)
                        + TOBJECT_DESC
                        + "I)V",
                false
        );

        decSp();
        return null;
    }

    @Override
    public Void visitLoadLocal(LoadLocal expr, Void unused) {

        genLocalsArray();
        pushJBCInt(mv, expr.index());
        mv.visitInsn(AALOAD);

        sp++;
        return null;
    }

    @Override
    public Void visitStoreLocal(StoreLocal expr, Void unused) {

        genLocalsArray();
        pushJBCInt(mv, expr.index);
        expr.what.accept(this);
        mv.visitInsn(AASTORE);

        decSp();
        return null;
    }

    @Override
    public Void visitLoadExternal(LoadExternal expr, Void unused) {

        if (expr.index != -1 && expr.layer != -1){
            loadFast(expr.index, expr.layer, expr.isStaticAccess, () -> expr.what.accept(this));
        }
        else {
            load(expr.name, expr.layer, expr.isStaticAccess, () -> expr.what.accept(this));
        }

        return null;
    }

    @Override
    public Void visitStoreExternal(StoreExternal expr, Void unused) {
        if (expr.index != -1 && expr.layer != -1){
            storeFast(expr.what, expr.index, expr.layer, expr.isStaticAccess, () -> expr.where.accept(this));
        }
        else {
            store(expr.what, expr.name, expr.layer, expr.isStaticAccess, () -> expr.where.accept(this));
        }

        decSp(2);
        return null;
    }

    @Override
    public Void visitLoadInternal(LoadInternal expr, Void unused) {
        loadFast(expr.index(), 0, false, this::genLoadOwner);
        sp++;
        return null;
    }

    @Override
    public Void visitStoreInternal(StoreInternal expr, Void unused) {
        storeFast(expr.what, expr.index, 0, false, this::genLoadOwner);
        decSp();
        return null;
    }

    @Override
    public Void visitLoadSuper(LoadSuper expr, Void unused) {
        if (expr.index != -1 && expr.layer != -1){
            loadFast(expr.index, expr.layer, true, this::genLoadOwner);
        }
        else {
            load(expr.name, expr.layer, true, this::genLoadOwner);
        }

        sp++;
        return null;
    }

    @Override
    public Void visitStoreSuper(StoreSuper expr, Void unused) {

        if (expr.index != -1 && expr.layer != -1){
            storeFast(expr.what, expr.index, expr.layer, false, this::genLoadOwner);
        }
        else {
            store(expr.what, expr.name, expr.layer, false, this::genLoadOwner);
        }

        decSp();
        return null;
    }


    @Override
    public Void visitLoadStatic(LoadStatic expr, Void unused) {
        if (expr.index != -1 && expr.layer != -1){
            loadFast(expr.index, expr.layer, false, this::genLoadOwner);
        }
        else {
            load(expr.name, expr.layer, false, this::genLoadOwner);
        }

        sp++;
        return null;
    }

    @Override
    public Void visitStoreStatic(StoreStatic expr, Void unused) {
        if (expr.index != -1 && expr.layer != -1){
            storeFast(expr.what, expr.index, expr.layer, true, this::genLoadOwner);
        }
        else {
            store(expr.what, expr.name, expr.layer, true, this::genLoadOwner);
        }

        decSp();
        return null;
    }

    @Override
    public Void visitContainerRead(ContainerRead expr, Void unused) {

        genLoadThread();
        expr.container.accept(this);
        expr.key.accept(this);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "readContainer",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + TOBJECT_DESC
                        + TOBJECT_DESC
                        + ")" + TOBJECT_DESC,
                false
        );

        if (expr.canFail)
            genFailureReturnIfNull();

        decSp();

        return null;
    }

    @Override
    public Void visitContainerWrite(ContainerWrite expr, Void unused) {

        genLoadThread();
        expr.container.accept(this);
        expr.key.accept(this);
        expr.value.accept(this);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "writeContainer",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + TOBJECT_DESC
                        + TOBJECT_DESC
                        + TOBJECT_DESC
                        + ")Z",
                false
        );

        genFailureReturnIfFalse();

        decSp(3);

        return null;
    }

    @Override
    public Void visitImportModule(ImportModule expr, Void unused) {
        genLoadThread();

        if (expr.index != -1){
            pushJBCInt(mv, expr.index);

            mv.visitMethodInsn(
                    INVOKESTATIC,
                    org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                    "importModule",
                    "("
                            + org.objectweb.asm.Type.getDescriptor(TThread.class)
                            + "I)" + TOBJECT_DESC,
                    false
            );
        }
        else {
            mv.visitLdcInsn(expr.name);

            mv.visitMethodInsn(
                    INVOKESTATIC,
                    org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                    "importModule",
                    "("
                            + org.objectweb.asm.Type.getDescriptor(TThread.class)
                            + org.objectweb.asm.Type.getDescriptor(String.class)
                            + ")" + TOBJECT_DESC,
                    false
            );

            genFailureReturnIfNull();
        }

        sp++;
        return null;
    }

    @Override
    public Void visitUse(Use expr, Void unused) {
        return expr.used.accept(this);
    }

    @Override
    public Void visitUseMembers(UseMembers expr, Void unused) {
        return null;
    }

    @Override
    public Void visitLoadName(LoadName expr, Void unused) {
        return null;
    }


    @Override
    public Void visitExtended(Extended expr, Void unused) {
        genLoadThread();
        expr.owner.accept(this);
        expr.content.accept(this);
        mv.visitLdcInsn(expr.name);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "extend",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + TOBJECT_DESC
                        + TOBJECT_DESC
                        + org.objectweb.asm.Type.getDescriptor(String.class)
                        + ")" + TOBJECT_DESC,
                false
        );

        if (expr.canFail) {
            genFailureReturnIfNull();
        }

        decSp(1);

        return null;
    }

    @Override
    public Void visitBoolGuard(BoolGuard expr, Void unused) {
        expr.expression.accept(this);

        mv.visitInsn(DUP);
        genLoadThread();
        genLoadFrame();
        if (expr.required){
            mv.visitInsn(ICONST_1);
        }
        else {
            mv.visitInsn(ICONST_0);
        }

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "guardBool",
                "("
                        + TOBJECT_DESC
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + org.objectweb.asm.Type.getDescriptor(ImageFrame.class)
                        + "Z)V",
                false
        );

        return null;
    }

    @Override
    public Void visitTypeGuard(TypeGuard expr, Void unused) {
        expr.expression.accept(this);
        return null;
    }

    @Override
    public Void visitItrNextGuard(ItrNextGuard expr, Void unused) {
        mv.visitInsn(DUP);
        genLoadThread();
        genLoadFrame();
        if (expr.shouldHaveNext){
            mv.visitInsn(ICONST_1);
        }
        else {
            mv.visitInsn(ICONST_0);
        }

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "guardItrHasNext",
                "("
                        + TOBJECT_DESC
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + org.objectweb.asm.Type.getDescriptor(ImageFrame.class)
                        + "Z)V",
                false
        );

        return null;
    }



    private void pushJBCInt(MethodVisitor mv, int value){
        if (value == 0) mv.visitInsn(ICONST_0);
        else if (value == 1) mv.visitInsn(ICONST_1);
        else if (value == 2) mv.visitInsn(ICONST_2);
        else if (value == 3) mv.visitInsn(ICONST_3);
        else if (value == 4) mv.visitInsn(ICONST_4);
        else if (value == 5) mv.visitInsn(ICONST_5);
        else if (value == -1) mv.visitInsn(ICONST_M1);
        else if (value >= -128 && value <= 127) mv.visitIntInsn(BIPUSH, value);
        else if (value >= -32768 && value <= 32767) mv.visitIntInsn(SIPUSH, value);
        else mv.visitIntInsn(SIPUSH, 30000);
    }

    private String loadConstant(Object value, Class<?> type, String constructorDesc, Runnable loader){
        if (constants.containsKey(value))
            return constants.get(value);

        String name = constants.computeIfAbsent(value, k -> "const" + constants.size());

        cw.visitField(
                ACC_PRIVATE + ACC_STATIC + ACC_FINAL,
                name,
                TOBJECT_DESC,
                null,
                null
        ).visitEnd();

        String internal = org.objectweb.asm.Type.getInternalName(type);
        clinit.visitTypeInsn(NEW, internal);
        clinit.visitInsn(DUP);

        loader.run();

        clinit.visitMethodInsn(
                INVOKESPECIAL,
                internal,
                "<init>",
                constructorDesc, // Constructor descriptor
                false
        );

        // Store the value in the static field "message"
        clinit.visitFieldInsn(
                PUTSTATIC, // Opcode for static field assignment
                internalName,    // Class containing the field
                name,         // Field name
                TOBJECT_DESC // Field descriptor
        );

        return name;
    }

    private void genFailureReturnIfNull(){
        Label label = new Label();
        mv.visitInsn(DUP);
        mv.visitJumpInsn(IFNONNULL, label);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitLabel(label);
    }

    private void genFailureReturnIfFalse(){
        Label label = new Label();
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(IFNE, label);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitLabel(label);
    }

    private void genLoadThread(){
        mv.visitVarInsn(ALOAD, 1);
    }

    private void genLoadOwner(){
        genLoadFrame();
        mv.visitFieldInsn(
                GETFIELD,
                org.objectweb.asm.Type.getInternalName(ImageFrame.class),
                "owner",
                TOBJECT_DESC
        );
    }

    private void genLocalsArray(){
        genLoadFrame();
        mv.visitFieldInsn(
                GETFIELD,
                org.objectweb.asm.Type.getInternalName(ImageFrame.class),
                "locals",
                org.objectweb.asm.Type.getDescriptor(TObject[].class)
        );
    }

    private void storeFast(Expression expr, int address, int layer, boolean staticAccess, Runnable accessGenerator){

        genLoadThread();
        accessGenerator.run();
        expr.accept(this);
        pushJBCInt(mv, address);

        String desc = "("
                + org.objectweb.asm.Type.getDescriptor(TThread.class)
                + TOBJECT_DESC
                + TOBJECT_DESC
                + "I";


        if (layer > 0) {
            pushJBCInt(mv, layer);
            desc += "I";
        }

        desc += ")Z";

        String methodName = "store";
        if (staticAccess) methodName += "Static";
        methodName += "Fast";

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                methodName,
                desc,
                false
        );

        genFailureReturnIfFalse();
    }

    private void loadFast(int address, int layer, boolean staticAccess, Runnable accessedGenerator){
        accessedGenerator.run();
        pushJBCInt(mv, address);

        String desc = "("
                + TOBJECT_DESC
                + "I";

        if (layer > 0) {
            pushJBCInt(mv, layer);
            desc += "I";
        }

        desc += ")" + TOBJECT_DESC;

        String methodName = "load";
        if (staticAccess) methodName += "Static";
        methodName += "Fast";

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                methodName,
                desc,
                false
        );
    }

    private void store(Expression expr, String name, int layer, boolean staticAccess, Runnable accessedGenerator){

        genLoadThread();
        accessedGenerator.run();
        expr.accept(this);

        mv.visitLdcInsn(name);

        String desc = "("
                + org.objectweb.asm.Type.getDescriptor(TThread.class)
                + TOBJECT_DESC
                + TOBJECT_DESC
                + org.objectweb.asm.Type.getDescriptor(String.class);


        if (layer > 0) {
            pushJBCInt(mv, layer);
            desc += "I";
        }

        String methodName = "store";
        if (staticAccess) methodName += "Static";

        desc += ")Z";

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                methodName,
                desc,
                false
        );

        genFailureReturnIfFalse();
    }

    private void load(String name, int layer, boolean staticAccess, Runnable accessedGenerator){
        accessedGenerator.run();

        mv.visitLdcInsn(name);

        String desc = "("
                + TOBJECT_DESC
                + org.objectweb.asm.Type.getDescriptor(String.class);

        if (layer > 0) {
            pushJBCInt(mv, layer);
            desc += "I";
        }

        desc += ")" + TOBJECT_DESC;

        String methodName = "load";
        if (staticAccess) methodName += "Static";


        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                methodName,
                desc,
                false
        );
    }

    private void genNewTObjectArray(List<Expression> values){
        pushJBCInt(mv, values.size());
        mv.visitTypeInsn(ANEWARRAY, TOBJECT_INTERNAL);

        for (int i = 0; i < values.size(); i++) {
            mv.visitInsn(DUP);
            pushJBCInt(mv, i);
            values.get(i).accept(this);
            mv.visitInsn(AASTORE);
        }
    }

    private void genLoadFrame(){
        mv.visitVarInsn(ALOAD, 2);
    }

    private void genStoreFrame(){
        mv.visitVarInsn(ASTORE, 2);
    }
}
