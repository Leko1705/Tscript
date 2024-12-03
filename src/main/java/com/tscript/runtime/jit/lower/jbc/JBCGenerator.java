package com.tscript.runtime.jit.lower.jbc;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.lower.Generator;
import com.tscript.runtime.typing.TObject;
import org.objectweb.asm.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

import static org.objectweb.asm.Opcodes.*;

public class JBCGenerator implements Generator {

    private static long imageId = 0;

    @Override
    @SuppressWarnings("deprecation")
    public NativeImage generate(Node graph) {
        String name = "JBCImage" + (imageId++);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(49,
                ACC_PUBLIC + ACC_SUPER,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[]{Type.getInternalName(NativeImage.class)});

        cw.visitSource(null, null);


        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V"); // call super
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        String desc = "("
                + Type.getDescriptor(TThread.class)
                + Type.getDescriptor(TObject.class)
                + Type.getDescriptor(TObject[].class)
                + ")"
                + Type.getDescriptor(TObject.class);

        mv = cw.visitMethod(ACC_PUBLIC, "run", desc, null, null);
        MethodGenerator generator = new MethodGenerator(name, cw, mv);
        generator.generate(graph);

        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        byte[] b = cw.toByteArray();

        // new ClassReader(b).accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(System.out)), 0);

        Class<?> clazz = JBCGenClassLoader.getInstance()
                .defineClass(b);

        NativeImage image;

        try {
            image = (NativeImage) clazz.getConstructor().newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return image;
    }

}
