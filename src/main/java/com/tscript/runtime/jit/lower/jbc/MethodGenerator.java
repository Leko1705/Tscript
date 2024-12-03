package com.tscript.runtime.jit.lower.jbc;

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
import com.tscript.runtime.jit.lower.MaxLocalAddProvider;
import com.tscript.runtime.typing.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class MethodGenerator extends TreeTraverseStrategy implements ExpressionVisitor<Void, Void> {

    private static final String TOBJECT_DESC = org.objectweb.asm.Type.getDescriptor(TObject.class);
    private static final String TOBJECT_INTERNAL = org.objectweb.asm.Type.getInternalName(TObject.class);


    private final String internalName;
    private final ClassWriter cw;
    private final MethodVisitor mv;
    private final MethodVisitor clinit;

    private final Map<Object, String> constants;
    private int sp;

    public MethodGenerator(String internalName, ClassWriter cw, MethodVisitor mv) {
        this.internalName = internalName;
        this.cw = cw;
        this.mv = mv;
        constants = new HashMap<>();
        clinit = cw.visitMethod(
                ACC_STATIC, // Access flags
                "<clinit>", // Special name for static initializer
                "()V", // Descriptor for void method with no parameters
                null, // Signature
                null  // Exceptions)
        );
    }

    public void generate(Node graph) {
        clinit.visitCode();

        int maxLocals = MaxLocalAddProvider.calculate(graph);

        mv.visitVarInsn(ALOAD, 3);

        mv.visitIntInsn(BIPUSH, maxLocals + 1);
        mv.visitTypeInsn(ANEWARRAY, TOBJECT_INTERNAL);

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "copyArray",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TObject[].class)
                        + org.objectweb.asm.Type.getDescriptor(TObject[].class)
                        + ")" + org.objectweb.asm.Type.getDescriptor(TObject[].class),
                false
        );

        mv.visitVarInsn(ASTORE, 3);


        Traverser.traverse(this, this, graph);

        // finish static initializer block
        clinit.visitInsn(RETURN);
        clinit.visitMaxs(0, 0);
        clinit.visitEnd();

        // finish actual method
        mv.visitInsn(ARETURN);
        sp--;

        if (sp != 0) throw new AssertionError("sp != 0: is " + sp);
    }

    @Override
    public boolean isThenFirst(BranchNode node, Context context) {
        return false;
    }

    @Override
    public Void visitUnknownValue(Unknown expression, Void unused) {
        // dummy expression; ignored
        return null;
    }

    @Override
    public Void visitPop(Pop expr, Void unused) {
        if (expr.expression() != null)
            expr.expression().accept(this);
        mv.visitInsn(POP);
        sp--;
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
                () -> pushJBCInt(expr.value)
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

        sp--;
        return null;
    }

    @Override
    public Void visitArray(Array expr, Void unused) {
        String tarrInternal = org.objectweb.asm.Type.getInternalName(TArray.class);

        mv.visitTypeInsn(NEW, tarrInternal);
        mv.visitInsn(DUP);

        mv.visitIntInsn(BIPUSH, expr.contents().size());
        mv.visitTypeInsn(ANEWARRAY, TOBJECT_INTERNAL);

        for (int i = 0; i < expr.contents().size(); i++) {
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, i);
            expr.contents().get(i).accept(this);
            mv.visitInsn(AASTORE);
        }

        mv.visitMethodInsn(
                INVOKESPECIAL,
                tarrInternal,
                "<init>",
                "(" + org.objectweb.asm.Type.getDescriptor(TObject[].class) + ")V",
                false
        );

        sp -= expr.contents().size()-1;
        return null;
    }

    @Override
    public Void visitDict(Dict expr, Void unused) {
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
        mv.visitIntInsn(SIPUSH, expr.index());

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


        genFailureReturnIfNull();

        sp++;
        return null;
    }

    @Override
    public Void visitNative(Native expr, Void unused) {

        mv.visitLdcInsn(expr.name());

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

        genFailureReturnIfNull();

        sp++;
        return null;
    }

    @Override
    public Void visitBuiltin(Builtin expr, Void unused) {
        mv.visitIntInsn(BIPUSH, expr.index());

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
        return null;
    }

    @Override
    public Void visitCallInp(CallInp expr, Void unused) {
        genLoadThread();
        expr.called.accept(this);

        mv.visitIntInsn(BIPUSH, expr.args.size());
        mv.visitTypeInsn(ANEWARRAY, TOBJECT_INTERNAL);

        for (int i = 0; i < expr.args.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, i);
            expr.args.get(i).accept(this);
            mv.visitInsn(AASTORE);
        }

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "call",
                "("
                        + org.objectweb.asm.Type.getDescriptor(TThread.class)
                        + TOBJECT_DESC
                        + org.objectweb.asm.Type.getDescriptor(TObject[].class)
                        + ")" + TOBJECT_DESC,
                false
        );

        genFailureReturnIfNull();

        sp -= expr.args.size();

        return null;
    }

    @Override
    public Void visitType(Type expr, Void unused) {
        genLoadThread();
        mv.visitIntInsn(SIPUSH, expr.index());

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


        genFailureReturnIfNull();
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

        expr.underlying().accept(this);

        if (expr.underlying() == expr.owner()){
            mv.visitInsn(DUP);
        }
        else {
            expr.owner().accept(this);
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

        genFailureReturnIfNull();
        sp--;

        return null;
    }

    @Override
    public Void visitBinaryOp(BinaryOp expr, Void unused) {
        genLoadThread();
        expr.lhs.accept(this);
        expr.rhs.accept(this);
        mv.visitIntInsn(BIPUSH, expr.opcode.b);

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

        genFailureReturnIfNull();

        sp--;
        return null;
    }

    @Override
    public Void visitNot(Not expr, Void unused) {
        genUnaryOp(expr.expression(), "not");
        return null;
    }

    @Override
    public Void visitNeg(Neg expr, Void unused) {
        genUnaryOp(expr.expression(), "neg");
        return null;
    }

    private void genUnaryOp(Expression exp, String jbcUtilMethodName){
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

        sp--;
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
        return null;
    }

    @Override
    public Void visitLoadGlobal(LoadGlobal expr, Void unused) {
        return null;
    }

    @Override
    public Void visitStoreGlobal(StoreGlobal expr, Void unused) {
        return null;
    }

    @Override
    public Void visitLoadLocal(LoadLocal expr, Void unused) {

        genLocalsArray();
        mv.visitIntInsn(BIPUSH, expr.index());
        mv.visitInsn(AALOAD);

        sp++;
        return null;
    }

    @Override
    public Void visitStoreLocal(StoreLocal expr, Void unused) {

        genLocalsArray();
        mv.visitIntInsn(BIPUSH, expr.index);
        expr.what.accept(this);
        mv.visitInsn(AASTORE);

        sp--;
        return null;
    }

    @Override
    public Void visitLoadExternal(LoadExternal expr, Void unused) {

        if (expr.index != -1 && expr.layer != -1){
            loadFast(expr.index, expr.layer, () -> expr.what.accept(this));
        }
        else {
            load(expr.name, expr.layer, () -> expr.what.accept(this));
        }

        return null;
    }

    @Override
    public Void visitStoreExternal(StoreExternal expr, Void unused) {
        if (expr.index != -1 && expr.layer != -1){
            storeFast(expr.what, expr.index, expr.layer, () -> expr.where.accept(this));
        }
        else {
            store(expr.what, expr.name, expr.layer, () -> expr.where.accept(this));
        }

        sp -= 2;
        return null;
    }

    @Override
    public Void visitLoadInternal(LoadInternal expr, Void unused) {
        loadFast(expr.index(), 0, this::genLoadOwner);
        sp++;
        return null;
    }

    @Override
    public Void visitStoreInternal(StoreInternal expr, Void unused) {
        storeFast(expr.what, expr.index, 0, this::genLoadOwner);
        sp--;
        return null;
    }

    @Override
    public Void visitLoadSuper(LoadSuper expr, Void unused) {
        if (expr.index != -1 && expr.layer != -1){
            loadFast(expr.index, expr.layer, this::genLoadOwner);
        }
        else {
            load(expr.name, expr.layer, this::genLoadOwner);
        }

        sp++;
        return null;
    }

    @Override
    public Void visitStoreSuper(StoreSuper expr, Void unused) {

        if (expr.index != -1 && expr.layer != -1){
            storeFast(expr.what, expr.index, expr.layer, this::genLoadOwner);
        }
        else {
            store(expr.what, expr.name, expr.layer, this::genLoadOwner);
        }

        sp--;
        return null;
    }

    private void storeFast(Expression expr, int address, int layer, Runnable accessGenerator){

        genLoadThread();
        accessGenerator.run();
        expr.accept(this);
        mv.visitIntInsn(BIPUSH, address);

        String desc = "("
                + org.objectweb.asm.Type.getDescriptor(TThread.class)
                + TOBJECT_DESC
                + TOBJECT_DESC
                + "I";


        if (layer > 0) {
            mv.visitIntInsn(BIPUSH, layer);
            desc += "I";
         }


        desc += ")Z";

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "storeFast",
                desc,
                false
        );

        genFailureReturnIfFalse();
    }

    private void loadFast(int address, int traversal, Runnable accessedGenerator){
        accessedGenerator.run();
        mv.visitIntInsn(BIPUSH, address);

        String desc = "("
                + TOBJECT_DESC
                + "I";

        if (traversal > 0) {
            mv.visitIntInsn(BIPUSH, traversal);
            desc += "I";
        }

        desc += ")" + TOBJECT_DESC;

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "loadFast",
                desc,
                false
        );
    }

    private void store(Expression expr, String name, int layer, Runnable accessedGenerator){

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
            mv.visitIntInsn(BIPUSH, layer);
            desc += "I";
        }


        desc += ")Z";

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "store",
                desc,
                false
        );

        genFailureReturnIfFalse();
    }

    private void load(String name, int traversal, Runnable accessedGenerator){
        accessedGenerator.run();

        mv.visitLdcInsn(name);

        String desc = "("
                + TOBJECT_DESC
                + org.objectweb.asm.Type.getDescriptor(String.class);

        if (traversal > 0) {
            mv.visitIntInsn(BIPUSH, traversal);
            desc += "I";
        }

        desc += ")" + TOBJECT_DESC;

        mv.visitMethodInsn(
                INVOKESTATIC,
                org.objectweb.asm.Type.getInternalName(JBCUtils.class),
                "load",
                desc,
                false
        );
    }


    @Override
    public Void visitLoadStatic(LoadStatic expr, Void unused) {
        return null;
    }

    @Override
    public Void visitStoreStatic(StoreStatic expr, Void unused) {
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

        genFailureReturnIfNull();

        sp--;

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

        sp -= 3;

        return null;
    }

    @Override
    public Void visitLoadName(LoadName expr, Void unused) {
        return null;
    }

    @Override
    public Void visitBoolGuard(BoolGuard expr, Void unused) {
        expr.expression.accept(this);
        return null;
    }

    @Override
    public Void visitTypeGuard(TypeGuard expr, Void unused) {
        expr.expression.accept(this);
        return null;
    }

    @Override
    public Void visitItrNextGuard(ItrNextGuard expr, Void unused) {
        return null;
    }

    private void addDebugPrint(MethodVisitor mv) {
        mv.visitFieldInsn(
                GETSTATIC,                       // Opcode for static field access
                "java/lang/System",                     // Class containing the field
                "out",                                  // Field name
                "Ljava/io/PrintStream;"                 // Field descriptor
        );

        // 2. Push the string "HERE" onto the stack
        mv.visitLdcInsn("HERE");

        // 3. Invoke println(String) on PrintStream
        mv.visitMethodInsn(
                INVOKEVIRTUAL,                  // Opcode for virtual method call
                "java/io/PrintStream",                 // Class containing the method
                "println",                             // Method name
                "(Ljava/lang/String;)V",               // Method descriptor
                false                                   // Not an interface
        );
    }

    private void pushJBCInt(int value){
        if (value == 0) clinit.visitInsn(ICONST_0);
        else if (value == 1) clinit.visitInsn(ICONST_1);
        else if (value == 2) clinit.visitInsn(ICONST_2);
        else if (value == 3) clinit.visitInsn(ICONST_3);
        else if (value == 4) clinit.visitInsn(ICONST_4);
        else if (value == 5) clinit.visitInsn(ICONST_5);
        else clinit.visitIntInsn(BIPUSH, value);
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
        mv.visitVarInsn(ALOAD, 2);
    }

    private void genLocalsArray(){
        mv.visitVarInsn(ALOAD, 3);
    }
}
