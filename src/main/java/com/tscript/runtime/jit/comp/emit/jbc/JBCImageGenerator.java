package com.tscript.runtime.jit.comp.emit.jbc;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.ImageFrame;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.typing.TObject;
import org.objectweb.asm.*;
import org.objectweb.asm.Type;

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

    private final ClassWriter cw;
    private final MethodVisitor mv;

    public JBCImageGenerator(String className) {
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

    public byte[] generate() {
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }
}
