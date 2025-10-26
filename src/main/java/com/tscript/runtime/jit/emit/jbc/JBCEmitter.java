package com.tscript.runtime.jit.emit.jbc;

import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.emit.Emitter;
import com.tscript.runtime.jit.emit.IREmitterJBC;
import com.tscript.runtime.jit.emit.JBCEmitterController;
import com.tscript.runtime.jit.ir.IRBlock;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

public class JBCEmitter implements Emitter {

    private static long imageId = 0;

    private static String nextImageClassName() {
        return "NativeImage" + imageId++;
    }



    @Override
    public NativeImage emit(IRBlock block) {
        String className = nextImageClassName();
        byte[] bytecode = generateImageJBC(className, block);
        return loadImageInstance(bytecode);
    }

    private byte[] generateImageJBC(String className, IRBlock block) {
        //JBCEmitterImpl emitter = new JBCEmitterImpl(className);
        JBCEmitterController controller = new JBCEmitterController(new IREmitterJBC(className));

        return controller.emit(block);
    }


    private NativeImage loadImageInstance(byte[] bytecode) {
        OneWayClassLoader classLoader = new OneWayClassLoader();

        new ClassReader(bytecode).accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(System.err)), 0);

        try {
            Class<?> clazz = classLoader.defineClass(bytecode);
            return (NativeImage) clazz.getConstructor().newInstance();
        }
        catch (Error | Exception e) {
            throw new RuntimeException(e);
        }
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
