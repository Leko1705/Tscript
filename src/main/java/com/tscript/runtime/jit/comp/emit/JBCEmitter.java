package com.tscript.runtime.jit.comp.emit;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.comp.emit.jbc.EmitterImpl;
import com.tscript.runtime.jit.comp.ir.Graph;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

public class JBCEmitter implements Emitter {

    private static long imageId = 0;

    private static String nextImageClassName() {
        return "NativeImage$" + imageId++;
    }


    @Override
    public NativeImage emit(JITCompiler compiler, Graph graph) {
        String className = nextImageClassName();
        byte[] bytecode = generateImageJBC(className, compiler, graph);
        return loadImageInstance(bytecode);
    }

    private byte[] generateImageJBC(String className, JITCompiler compiler, Graph graph) {
        EmitterImpl impl = new EmitterImpl(className, compiler, graph);
        return impl.emit();
    }

    private NativeImage loadImageInstance(byte[] bytecode) {
        OneWayClassLoader classLoader = new OneWayClassLoader();

        printJBC(bytecode);

        try {
            Class<?> clazz = classLoader.defineClass(bytecode);
            return (NativeImage) clazz.getConstructor().newInstance();
        }
        catch (Error | Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void printJBC(byte[] bytecode) {
        new ClassReader(bytecode).accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(System.err)), 0);
    }


    // Don't make this class a singleton, to avoid holding generated class reference.
    // If the class reference is not held by a class loader it is garbage collected if
    // is not needed anymore.
    private static class OneWayClassLoader extends ClassLoader {
        public Class<?> defineClass(byte[] b) {
            return defineClass(null, b, 0, b.length);
        }
    }

}
