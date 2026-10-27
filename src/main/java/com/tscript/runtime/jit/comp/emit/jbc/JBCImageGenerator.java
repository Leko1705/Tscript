package com.tscript.runtime.jit.comp.emit.jbc;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.ImageFrame;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.typing.TObject;
import org.objectweb.asm.*;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates the skeleton of a native image class:
 * <pre>
 * public class NativeImage$N implements NativeImage {
 *     public TObject run(TThread thread, ImageFrame frame) { ... }
 * }
 * </pre>
 * The class file version is 49, so the JVM uses the type inferencing verifier and no stack map
 * frames have to be computed.
 */
public class JBCImageGenerator implements Opcodes {

    public static final int THIS_SLOT = 0;
    public static final int THREAD_SLOT = 1;
    public static final int FRAME_SLOT = 2;
    public static final int FIRST_FREE_SLOT = 3;

    private static final String UTILS = "com/tscript/runtime/jit/comp/emit/jbc/JBCUtils";
    private static final String TOBJECT_DESC = Type.getDescriptor(TObject.class);

    private final ClassWriter cw;
    private final MethodVisitor mv;
    private final String className;
    /** deoptimization stubs by the descriptors of their arguments */
    private final Map<String, String> deoptStubs = new HashMap<>();

    public JBCImageGenerator(String className) {
        this.className = className;
        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_5,
                ACC_PUBLIC + ACC_SUPER,
                className,
                null,
                Type.getInternalName(Object.class),
                new String[]{Type.getInternalName(NativeImage.class)});

        MethodVisitor init = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        init.visitCode();
        init.visitVarInsn(ALOAD, 0);
        init.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        init.visitInsn(RETURN);
        init.visitMaxs(0, 0);
        init.visitEnd();

        String desc = Type.getMethodDescriptor(Type.getType(TObject.class), Type.getType(TThread.class), Type.getType(ImageFrame.class));
        mv = cw.visitMethod(ACC_PUBLIC, "run", desc, null, null);
        mv.visitCode();
    }

    public MethodVisitor getMethodVisitor() {
        return mv;
    }

    public String getClassName() {
        return className;
    }

    /**
     * Returns (and creates on first use) a static method {@code (I<values>)TObject} that boxes the
     * values, puts them into an array and creates the deoptimization of the given DeoptInfo id.
     * Leaving compiled code then only needs the values on the operand stack and a call, which keeps
     * the (cold) failure paths of guards small. Stubs that are never called are never compiled by
     * the JVM.
     *
     * @param valueDescs the descriptors of the values (I, D, Z, Ljava/lang/String; or TObject)
     * @return the descriptor of the stub; its name is {@link #deoptStubName(String)}
     */
    public String deoptStub(List<String> valueDescs) {
        StringBuilder sb = new StringBuilder("(I");
        for (String desc : valueDescs) sb.append(desc);
        String desc = sb.append(")").append(TOBJECT_DESC).toString();
        if (deoptStubs.containsKey(desc)) return desc;
        String name = "deopt$" + deoptStubs.size();
        deoptStubs.put(desc, name);

        MethodVisitor stub = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, name, desc, null, null);
        stub.visitCode();
        stub.visitVarInsn(ILOAD, 0);
        pushInt(stub, valueDescs.size());
        stub.visitTypeInsn(ANEWARRAY, Type.getInternalName(TObject.class));
        int slot = 1;
        for (int i = 0; i < valueDescs.size(); i++) {
            String valueDesc = valueDescs.get(i);
            stub.visitInsn(DUP);
            pushInt(stub, i);
            switch (valueDesc) {
                case "I" -> {
                    stub.visitVarInsn(ILOAD, slot);
                    stub.visitMethodInsn(INVOKESTATIC, UTILS, "boxInt", "(I)" + TOBJECT_DESC, false);
                }
                case "D" -> {
                    stub.visitVarInsn(DLOAD, slot);
                    stub.visitMethodInsn(INVOKESTATIC, UTILS, "boxReal", "(D)" + TOBJECT_DESC, false);
                    slot++;
                }
                case "Z" -> {
                    stub.visitVarInsn(ILOAD, slot);
                    stub.visitMethodInsn(INVOKESTATIC, UTILS, "boxBool", "(Z)" + TOBJECT_DESC, false);
                }
                case "Ljava/lang/String;" -> {
                    stub.visitVarInsn(ALOAD, slot);
                    stub.visitMethodInsn(INVOKESTATIC, UTILS, "boxStr", "(Ljava/lang/String;)" + TOBJECT_DESC, false);
                }
                default -> stub.visitVarInsn(ALOAD, slot);
            }
            slot++;
            stub.visitInsn(AASTORE);
        }
        stub.visitMethodInsn(INVOKESTATIC, UTILS, "deopt", "(I[" + TOBJECT_DESC + ")" + TOBJECT_DESC, false);
        stub.visitInsn(ARETURN);
        stub.visitMaxs(0, 0);
        stub.visitEnd();
        return desc;
    }

    public String deoptStubName(String desc) {
        return deoptStubs.get(desc);
    }

    private static void pushInt(MethodVisitor mv, int value) {
        if (value >= -1 && value <= 5) mv.visitInsn(ICONST_0 + value);
        else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) mv.visitIntInsn(BIPUSH, value);
        else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) mv.visitIntInsn(SIPUSH, value);
        else mv.visitLdcInsn(value);
    }

    public byte[] generate() {
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }
}
