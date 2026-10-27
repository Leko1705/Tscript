package com.tscript.runtime.jit.comp.emit.jbc;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.ImageFrame;
import com.tscript.runtime.typing.*;
import org.objectweb.asm.*;
import org.objectweb.asm.Type;

public class JBCImageGenerator implements Opcodes {

    private static ClassWriter createClassWriter(String className) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(49,
                ACC_PUBLIC + ACC_SUPER,
                className,
                null,
                Type.getInternalName(Object.class),
                new String[]{Type.getInternalName(NativeImage.class)});

        cw.visitSource(null, null);

        // generate Constructor
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V"); // call super
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();


        return cw;
    }

    private static MethodVisitor createMethodVisitor(ClassWriter cw) {
        String desc = methodDescriptorOf(TObject.class, TThread.class, ImageFrame.class);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "run", desc, null, null);
        mv.visitCode();
        return mv;
    }

    private static MethodVisitor createStaticInitializerBlockVisitor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        return mv;
    }


    private final ClassWriter cw;
    private final MethodVisitor mv;
    private final MethodVisitor clinit;
    private int sp;


    public JBCImageGenerator(String className) {
        cw = createClassWriter(className);
        mv = createMethodVisitor(cw);
        clinit = createStaticInitializerBlockVisitor(cw);
        sp = 0;
    }

    private void inc(){
        sp++;
    }

    private void inc(int count) {
        sp += count;
    }

    private void dec(){
        sp--;
        //if (sp < 0) throw new IllegalStateException("sp < 0 (1 element missing)");
    }

    private void dec(int count) {
        //if (sp < count) throw new IllegalStateException("sp < 0 (" + (count - sp) + " elements missing)");
        sp -= count;
    }

    public byte[] generate() {
        //if (sp != 0) throw new IllegalStateException("sp != 0");

        clinit.visitInsn(RETURN);
        clinit.visitMaxs(0, 0);
        clinit.visitEnd();

        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    public void dup() {
        mv.visitInsn(DUP);
        inc();
    }

    public void pop() {
        mv.visitInsn(POP);
        dec();
    }

    public void label(Label label) {
        mv.visitLabel(label);
    }

    public void gotoLabel(Label label) {
        mv.visitJumpInsn(GOTO, label);
    }

    public void branch(Label label, boolean onTrue) {
        if (onTrue) {
            mv.visitJumpInsn(IFEQ, label);
        }
        else {
            mv.visitJumpInsn(IFNE, label);
        }
        dec();
    }

    public void branchCmp(Label label, boolean onTrue) {
        if (onTrue) {
            mv.visitJumpInsn(IF_ICMPEQ, label);
        }
        else {
            mv.visitJumpInsn(IF_ICMPNE, label);
        }
    }

    public void checkCast(Class<?> clazz) {
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(clazz));
    }

    public void pushNull() {
        mv.visitInsn(ACONST_NULL);
        inc();
    }

    public void pushInt(int value) {
        pushJBCInt(mv, value);
        inc();
    }

    public void pushDouble(double value) {
        mv.visitLdcInsn(value);
        inc();
    }

    public void pushBoolean(boolean value) {
        if (value) {
            mv.visitInsn(ICONST_1);
        }
        else {
            mv.visitInsn(ICONST_0);
        }
        inc();
    }

    public void pushString(String value) {
        mv.visitLdcInsn(value);
        inc();
    }

    public void newInstance(Class<?> clazz) {
        String internal = Type.getInternalName(clazz);
        mv.visitTypeInsn(NEW, internal);
        mv.visitInsn(DUP);
        inc(2);
    }

    public void instanceOf(Class<?> clazz) {
        String internal = Type.getInternalName(clazz);
        mv.visitTypeInsn(INSTANCEOF, internal);
    }

    public void newArray(Class<?> clazz, int size) {
        pushJBCInt(mv, size);
        mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(clazz));
        inc();
    }

    public void storeArray() {
        mv.visitInsn(AASTORE);
        dec(2);
    }

    public void loadArray() {
        mv.visitInsn(AALOAD);
        dec();
    }

    public void genReturnObject() {
        mv.visitInsn(ARETURN);
        dec();
    }

    public void invertBool() {
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IXOR);
    }

    public void negateInt() {
        mv.visitInsn(INEG);
    }

    public void negateDouble() {
        mv.visitInsn(DNEG);
    }

    public void getStatic(Class<?> clazz, String fieldName, Class<?> fieldType) {
        mv.visitFieldInsn(
                GETSTATIC,
                Type.getInternalName(clazz),
                fieldName,
                Type.getDescriptor(fieldType));
        inc();
    }

    public void getField(Class<?> clazz, String fieldName, Class<?> fieldType) {
        mv.visitFieldInsn(
                GETFIELD,
                Type.getInternalName(clazz),
                fieldName,
                Type.getDescriptor(fieldType));
    }

    public void putField(Class<?> clazz, String fieldName, Class<?> fieldType) {
        mv.visitFieldInsn(
                PUTFIELD,
                Type.getInternalName(clazz),
                fieldName,
                Type.getDescriptor(fieldType));
        dec(2);
    }

    public void callInterface(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
        mv.visitMethodInsn(
                INVOKEINTERFACE,
                Type.getInternalName(clazz),
                methodName,
                methodDescriptorOf(returnType, parameterTypes),
                true);
        dec(parameterTypes.length);
        if (returnType == Void.TYPE) {
            dec();
        }
    }

    public void callStatic(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(clazz),
                methodName,
                methodDescriptorOf(returnType, parameterTypes),
                false
        );
        dec(parameterTypes.length);
        if (returnType != Void.TYPE) {
            inc();
        }
    }

    public void callVirtual(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(clazz),
                methodName,
                methodDescriptorOf(returnType, parameterTypes),
                false
        );
        dec(parameterTypes.length);
        if (returnType == Void.TYPE) {
            dec();
        }
    }

    public void callConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        mv.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(clazz),
                "<init>",
                methodDescriptorOf(Void.TYPE, parameterTypes), // Constructor descriptor
                false
        );
        dec(parameterTypes.length);
    }

    public void loadLocal(int index) {
        mv.visitVarInsn(ALOAD, index);
        inc();
    }

    public void storeLocal(int index) {
        mv.visitVarInsn(ASTORE, index);
        dec();
    }

    public void storeLocal(Class<?> clazz, int index) {
        int inst = org.objectweb.asm.Type.getType(clazz).getOpcode(Opcodes.ISTORE);
        mv.visitVarInsn(inst, index);
        dec();
    }

    public void loadLocal(Class<?> clazz, int index) {
        int inst = org.objectweb.asm.Type.getType(clazz).getOpcode(Opcodes.ILOAD);
        mv.visitVarInsn(inst, index);
    }


    private void pushJBCInt(MethodVisitor mv, int value){
        if (value >= -1 && value <= 5) {
            switch (value) {
                case -1 -> mv.visitInsn(ICONST_M1);
                case 0 -> mv.visitInsn(ICONST_0);
                case 1 -> mv.visitInsn(ICONST_1);
                case 2 -> mv.visitInsn(ICONST_2);
                case 3 -> mv.visitInsn(ICONST_3);
                case 4 -> mv.visitInsn(ICONST_4);
                case 5 -> mv.visitInsn(ICONST_5);
            }
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            mv.visitIntInsn(BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, value);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    private static String methodDescriptorOf(Class<?> returnType, Class<?>... parameterTypes) {
        StringBuilder sb = new StringBuilder("(");
        for (Class<?> param : parameterTypes) {
            sb.append(Type.getDescriptor(param));
        }
        sb.append(")");
        sb.append(Type.getDescriptor(returnType));
        return sb.toString();
    }

    public MethodVisitor getMethodVisitor() {
        return mv;
    }
}
