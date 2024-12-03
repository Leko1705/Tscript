package com.tscript.runtime.jit.lower.jbc;

import com.tscript.projectfile.ProjectFile;
import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.stroage.FunctionArea;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.stroage.TypeArea;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.*;

import java.util.Arrays;

public class JBCUtils {

    @JITSensitive
    public static TObject[] copyArray(TObject[] src, TObject[] dst) {
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    @JITSensitive
    public static TObject loadNative(TThread thread, String name) {
        ProjectFile projectFile = thread.getVM().projectFile;

        if (projectFile == null){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.noSuchNativeFunctionFound(name));
            return null;
        }
        NativeFunction func = projectFile.getNative(name);

        if (func == null){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.noSuchNativeFunctionFound(name));
            return null;
        }

        return func;
    }

    @JITSensitive
    public static TObject loadVirtual(TThread thread, int index){
        Module module = thread.frameStack.element().getModule();
        FunctionArea functionArea = module.getFunctionArea();
        return functionArea.loadFunction(index, module);
    }

    @JITSensitive
    public static TObject loadType(TThread thread, int index){
        Module module = thread.frameStack.element().getModule();
        TypeArea typeArea = module.getTypeArea();
        return typeArea.loadType(thread, index);
    }

    @JITSensitive
    public static TObject setOwner(TThread thread, TObject candidate, TObject owner) {
        if (!(candidate instanceof Function func)){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.typeExpected("Function", candidate.getType().getName()));
            return null;
        }
        func.setOwner(owner);
        return func;
    }

    @JITSensitive
    public static TObject getIterator(TThread thread, TObject candidate) {
        if (!(candidate instanceof IterableObject iterable)){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.notIterable(candidate));
            return null;
        }
        return iterable.iterator();
    }

    @JITSensitive
    public static TObject iteratorNext(TObject candidate) {
        IteratorObject itr = (IteratorObject) candidate;
        if (!itr.hasNext()){
            return null;
        }
        return itr.next();
    }

    @JITSensitive
    public static TObject not(TThread thread, TObject candidate) {
        return performUnaryOperation(Opcode.NOT, thread, candidate);
    }

    @JITSensitive
    public static TObject pos(TThread thread, TObject candidate) {
        return performUnaryOperation(Opcode.POS, thread, candidate);
    }

    @JITSensitive
    public static TObject neg(TThread thread, TObject candidate) {
        return performUnaryOperation(Opcode.NEG, thread, candidate);
    }

    @JITSensitive
    public static TObject binary(TThread thread, TObject left, TObject right, byte opc) {
        Opcode opcode = Opcode.of(opc);
        TObject result = ALU.performBinaryOperation(left, right, opcode, thread);
        if (result == null){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.invalidBinaryOperation(left, right, opcode));
            return null;
        }
        return result;
    }

    private static TObject performUnaryOperation(Opcode opcode, TThread thread, TObject value) {
        TObject result = ALU.performUnaryOperation(value, opcode);
        if (result == null){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.invalidUnaryOperation(value, opcode));
            return null;
        }
        return result;
    }

    @JITSensitive
    public static TObject call(TThread thread, TObject caller, TObject[] args) {

        if (isStackOverflowError(thread))
            return null;

        if (!(caller instanceof Callable c)) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.notCallable(caller));
            return null;
        }

        return thread.call(c, Arrays.asList(args));
    }

    private static boolean isStackOverflowError(TThread thread){
        if (thread.frameStack.size() == 30_000){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.stackOverflowError());
            return true;
        }
        return false;
    }

    @JITSensitive
    public static TObject readContainer(TThread thread, TObject candidate, TObject key) {
        if (!(candidate instanceof ContainerAccessibleObject accessible)){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.notAccessible(candidate));
            return null;
        }
        return accessible.readFromContainer(thread, key);
    }

    @JITSensitive
    public static boolean writeContainer(TThread thread, TObject candidate, TObject key, TObject value) {
        if (!(candidate instanceof ContainerWriteableObject writeable)){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.notWriteable(candidate));
            return false;
        }
        return writeable.writeToContainer(thread, key, value);
    }

    @JITSensitive
    public static boolean storeFast(TThread thread, TObject obj, TObject what, int address, int layer){
        return getMember(obj, address, layer).set(what, thread);
    }

    @JITSensitive
    public static boolean storeFast(TThread thread, TObject obj, TObject what, int address){
        return obj.loadMember(address).set(what, thread);
    }

    @JITSensitive
    public static TObject loadFast(TObject obj, int address, int layer){
        return getMember(obj, address, layer).get();
    }

    @JITSensitive
    public static TObject loadFast(TObject obj, int layer){
        return obj.loadMember(layer).get();
    }

    private static Member getMember(TObject obj, int address, int layer){
        if (layer > 0){
            while (layer != 0){
                obj = obj.getSuper();
                layer--;
            }
        }
        else if (layer < 0){
            VirtualObject vobj = (VirtualObject) obj;
            while (layer != 0){
                vobj = vobj.subObject;
                layer--;
            }
            obj = vobj;
        }
        return obj.loadMember(address);
    }

}
