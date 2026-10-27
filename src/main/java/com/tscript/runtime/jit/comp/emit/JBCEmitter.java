package com.tscript.runtime.jit.comp.emit;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.comp.emit.jbc.EmitterImpl;
import com.tscript.runtime.jit.comp.ir.Graph;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Emits graphs as JVM bytecode and loads them as {@link NativeImage}s.
 */
public class JBCEmitter implements Emitter {

    private static final AtomicLong imageId = new AtomicLong();

    private static String nextImageClassName() {
        return "NativeImage$" + imageId.getAndIncrement();
    }


    @Override
    public NativeImage emit(JITCompiler compiler, Graph graph) {
        String className = nextImageClassName();
        byte[] bytecode;
        try {
            bytecode = new EmitterImpl(className, graph, true).emit();
        }
        catch (RuntimeException e) {
            // e.g. the method became too large: try again without stitched side traces
            compiler.debug(() -> "emitting with stitched side traces failed: " + e);
            bytecode = new EmitterImpl(className, graph, false).emit();
        }
        if (compiler.getConfiguration().debug()) {
            printJBC(bytecode);
        }
        return loadImageInstance(bytecode);
    }

    private NativeImage loadImageInstance(byte[] bytecode) {
        OneWayClassLoader classLoader = new OneWayClassLoader();
        try {
            Class<?> clazz = classLoader.defineClass(bytecode);
            return (NativeImage) clazz.getConstructor().newInstance();
        }
        catch (Error | Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void printJBC(byte[] bytecode) {
        PrintWriter writer = new PrintWriter(System.err);
        new ClassReader(bytecode).accept(new TraceClassVisitor(null, new Textifier(), writer), 0);
        writer.flush();
    }


    // Don't make this class a singleton, to avoid holding generated class reference.
    // If the class reference is not held by a class loader it is garbage collected if
    // is not needed anymore.
    private static class OneWayClassLoader extends ClassLoader {

        OneWayClassLoader() {
            super(JBCEmitter.class.getClassLoader());
        }

        public Class<?> defineClass(byte[] b) {
            return defineClass(null, b, 0, b.length);
        }
    }

}
