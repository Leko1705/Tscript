package com.tscript.runtime.jit.lower.jbc;

import com.tscript.projectfile.ProjectFile;
import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.tracing.FallbackException;
import com.tscript.runtime.jit.tracing.ImageFrame;
import com.tscript.runtime.stroage.FunctionArea;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.stroage.TypeArea;
import com.tscript.runtime.stroage.loading.ModuleLoader;
import com.tscript.runtime.stroage.loading.ModuleLoadingException;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.*;

import java.io.File;
import java.util.*;

@SuppressWarnings("unused")
public class JBCUtils {

    @JITSensitive
    public static TObject[] copyArray(TObject[] src, TObject[] dst) {
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    @JITSensitive
    public static TObject makeDict(TObject[] keys, TObject[] values) {
        TDictionary dict = new TDictionary();
        Map<TObject, TObject> map = dict.getValue();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return dict;
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
    public static TObject getIterator(TThread thread, ImageFrame frame, TObject candidate) {
        if (!(candidate instanceof IterableObject iterable)){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.notIterable(candidate));
            return null;
        }
        TObject itr = iterable.iterator();
        frame.stack[frame.sp++] = itr;
        return itr;
    }

    @JITSensitive
    public static TObject iteratorNext(ImageFrame frame) {
        IteratorObject itr = (IteratorObject) frame.stack[frame.sp-1];
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
    public static TObject callInp(TThread thread, TObject caller, TObject[] args) {

        if (isStackOverflowError(thread))
            return null;

        if (!(caller instanceof Callable c)) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.notCallable(caller));
            return null;
        }

        return thread.call(c, Arrays.asList(args));
    }

    @JITSensitive
    public static TObject callMap(TThread thread, TObject caller, TObject[] args) {

        if (isStackOverflowError(thread))
            return null;

        if (!(caller instanceof Callable c)) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.notCallable(caller));
            return null;
        }

        Argument[] arguments = (Argument[]) args;

        List<String> names = new ArrayList<>(args.length);
        List<TObject> values = new ArrayList<>(args.length);

        for (Argument arg : arguments) {
            names.add(arg.getValue().getFirst());
            values.add(arg.getValue().getSecond());
        }

        return c.call(thread, names, values);
    }

    private static boolean isStackOverflowError(TThread thread){
        if (thread.frameStack.size() == 30_000){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.stackOverflowError());
            return true;
        }
        return false;
    }

    @JITSensitive
    public static TObject callImage(TThread thread, TObject called, ImageFrame prevFrame, TObject[] args) {
        Type[] types = new Type[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i].getType();
        }

        ImageFrame frame = pushFrame(called, prevFrame, 0);
        System.arraycopy(args, 0, frame.locals, 0, args.length);

        VirtualFunction vfunc = (VirtualFunction) called;
        NativeImage image = thread.getVM().getJitCompiler().getCache().getFunctional(vfunc, types);

        return image.run(thread, frame);
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


    /* ------------------------ load/store fast ------------------------ */


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
    public static TObject loadFast(TObject obj, int address){
        return obj.loadMember(address).get();
    }



    /* ------------------------ load/store ------------------------ */



    @JITSensitive
    public static boolean store(TThread thread, TObject obj, TObject what, String name, int layer){
        return getMember(obj, name, layer).set(what, thread);
    }

    @JITSensitive
    public static boolean store(TThread thread, TObject obj, TObject what, String name){
        return obj.loadMember(name).set(what, thread);
    }

    @JITSensitive
    public static TObject load(TObject obj, String name, int layer){
        return getMember(obj, name, layer).get();
    }

    @JITSensitive
    public static TObject load(TObject obj, String name){
        return obj.loadMember(name).get();
    }


    /* ------------------------ load/store static ------------------------ */


    @JITSensitive
    public static TObject loadStatic(TObject obj, String name, int layer){
        return getStaticMember(obj, name, layer).get();
    }

    @JITSensitive
    public static TObject loadStatic(TObject obj, String name){
        return obj.getType().loadMember(name).get();
    }

    @JITSensitive
    public static boolean storeStatic(TThread thread, TObject obj, TObject what, String name, int layer){
        return getStaticMember(obj, name, layer).set(what, thread);
    }

    @JITSensitive
    public static boolean storeStatic(TThread thread, TObject obj, TObject what, String name){
        return obj.getType().loadMember(name).set(what, thread);
    }


    /* ------------------------ load/store static fast ------------------------ */



    @JITSensitive
    public static TObject loadStaticFast(TObject obj, int address, int layer){
        return getStaticMember(obj, address, layer).get();
    }

    @JITSensitive
    public static TObject loadStaticFast(TObject obj, int address){
        return obj.getType().loadMember(address).get();
    }

    @JITSensitive
    public static boolean storeStaticFast(TThread thread, TObject obj, TObject what, int address, int layer){
        return getStaticMember(obj, address, layer).set(what, thread);
    }

    @JITSensitive
    public static boolean storeStaticFast(TThread thread, TObject obj, TObject what, int address){
        return obj.getType().loadMember(address).set(what, thread);
    }

    @JITSensitive
    public static TObject importModule(TThread thread, String name){
        try {
            File[] rootPaths = thread.getVM().getRootPaths();
            ModuleLoader loader = thread.getVM().getSharedModuleLoader();
            return loader.loadModule(rootPaths, name.split("[.]"));
        }
        catch (ModuleLoadingException ex) {
            thread.reportRuntimeError(ex.getMessage());
            return null;
        }
    }

    @JITSensitive
    public static TObject importModule(TThread thread, int index){
        try {
            return thread.getVM().getSharedModuleLoader().loadModule(index);
        }
        catch (ModuleLoadingException e) {
            throw new AssertionError(e);
        }
    }

    @JITSensitive
    public static TObject extend(TThread thread, TObject obj, TObject what, String name) {
        Member member = Member.of(Visibility.PRIVATE, true, name, what);

        try {
            obj.addMember(member);
        }
        catch (UnsupportedOperationException ex){
            thread.reportRuntimeError("can not extend " + obj.getType().getDisplayName());
            return null;
        }
        return obj;
    }

    @JITSensitive
    public static boolean use(TThread thread, ImageFrame frame, TObject obj, String name){
        if (frame.names == null)
            frame.names = new HashMap<>();
        if (frame.names.containsKey(name)) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.nameAlreadyExists(name));
            return false;
        }
        frame.names.put(name, obj);
        return true;
    }

    @JITSensitive
    public static boolean useMembers(TThread thread, ImageFrame frame, TObject obj){
        if (frame.names == null)
            frame.names = new HashMap<>();

        Set<String> added = new HashSet<>();
        for (Member member : obj.getMembers()){
            boolean success = use(thread, frame, member.get(), member.getName());
            if (!success){
                for (String name : added) frame.names.remove(name);
                return false;
            }
            added.add(member.getName());
        }
        return true;
    }

    @JITSensitive
    public static TObject loadName(TThread thread, ImageFrame frame, String name){
        if (frame.names != null){
            TObject obj = frame.names.get(name);
            if (obj != null)
                return obj;
        }

        if (frame.owner != null){
            Member found = frame.owner.loadMember(name);
            if (found != null) return found.get();

            TObject curr = frame.owner;
            while (curr != null){
                TObject superObj = curr.getSuper();
                Member member = TNIUtils.searchMember(superObj, name);
                if (member != null && member.getVisibility().ordinal() <= Visibility.PROTECTED.ordinal()){
                    return member.get();
                }
                curr = curr.getSuper();
            }
        }

        Member member = frame.module.loadMember(name);
        if (member != null)
            return member.get();

        int builtinIndex = Builtins.indexOf(name);
        if (builtinIndex != -1)
            return Builtins.load(builtinIndex);

        thread.reportRuntimeError(InternalRuntimeErrorMessages.canNotFind(name));
        return null;
    }

    private static Member getStaticMember(TObject obj, String name, int layer){
        return traverseType(obj.getType(), layer).loadMember(name);
    }

    private static Member getStaticMember(TObject obj,int index, int layer){
        return traverseType(obj.getType(), layer).loadMember(index);
    }



    private static Member getMember(TObject obj, String name, int layer){
        return traverseObject(obj, layer).loadMember(name);
    }

    private static Member getMember(TObject obj, int address, int layer){
        return traverseObject(obj, layer).loadMember(address);
    }



    private static TObject traverseObject(TObject obj, int layer){
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
        return obj;
    }

    @JITSensitive
    public static TObject searchMemberDownward(TObject obj, String name){
        VirtualObject vobj = (VirtualObject) obj;
        Member member = null;
        while (vobj != null && (member = vobj.subObject.loadMember(name)) == null){
            vobj = vobj.subObject;
        }
        if (vobj == null) return null;
        return member.get();
    }

    private static Type traverseType(Type type, int layer){
        if (layer > 0){
            while (layer != 0){
                type = type.getSuperType();
                layer--;
            }
        }
        else if (layer < 0){
            throw new ExecutionException(
                    "JIT execution error --> layer for static access is < 0 ");
        }
        return type;
    }


    @JITSensitive
    public static ImageFrame pushFrame(TObject called, ImageFrame prevFrame, int extraLocals){

        if (called instanceof VirtualFunction vfunc) {
            return new ImageFrame(vfunc, prevFrame, extraLocals);
        }
        else if (called instanceof VirtualType type) {
            ImageFrame frame = pushFrame(type.getConstructor(), prevFrame, extraLocals);
            frame.owner = type.newInstance(null);
            return frame;
        }
        else {
            return ImageFrame.fakeFrame((Callable) called, prevFrame);
        }
    }

    @JITSensitive
    public static ImageFrame pushSuperFrame(ImageFrame prevFrame, int extraLocals){
        TObject curr = prevFrame.owner;
        Type superType = curr.getType().getSuperType();
        Function constructor = superType.getConstructor().dup();
        constructor.setOwner(curr.getSuper());
        return pushFrame(constructor, prevFrame, extraLocals);
    }

    // popFrame is generated by the code generator


    @JITSensitive
    public static void storeGlobal(TThread thread, ImageFrame frame, TObject what, int index){
        frame.module.loadMember(index).set(what, thread);
    }

    @JITSensitive
    public static TObject loadGlobal(ImageFrame frame, int index){
        return frame.module.loadMember(index).get();
    }

    @JITSensitive
    public static void guardItrHasNext(TThread thread,
                                       ImageFrame frame, int recoverAddress,
                                       int spRecover, boolean shouldHaveNext){
        IteratorObject itr = (IteratorObject) frame.stack[frame.sp-1];
        boolean hasNext = itr.hasNext();
        if (hasNext != shouldHaveNext){
            frame.sp = spRecover;
            frame.ip = recoverAddress;
            fallback(thread, frame);
        }
    }

    @JITSensitive
    public static void guardBool(TObject obj, TThread thread,
                                 ImageFrame frame, int recoverAddress,
                                 int spRecover, boolean expected){
        boolean given = TNIUtils.isTrue(obj);
        if (given != expected){
            frame.sp = spRecover;
            frame.stack[spRecover-1] = obj;
            frame.ip = recoverAddress;
            fallback(thread, frame);
        }
    }

    @JITSensitive
    public static void guardTypes(TObject obj, TThread thread,
                                  ImageFrame frame, int recoverAddress,
                                  int spRecover, String[] expected){
        for (String s : expected){
            String given = obj.getType().getId();
            if (given.equals(s)) return;
        }

        frame.sp = spRecover;
        frame.stack[frame.sp-1] = obj;
        frame.ip = recoverAddress;
        fallback(thread, frame);
    }

    @JITSensitive
    public static void guardType(TObject obj, TThread thread,
                                  ImageFrame frame, int recoverAddress,
                                  int spRecover, String expected){
        String given = obj.getType().getId();
        if (!given.equals(expected)){
            frame.sp = spRecover;
            frame.stack[frame.sp-1] = obj;
            frame.ip = recoverAddress;
            fallback(thread, frame);
        }
    }

    private static void fallback(TThread thread, ImageFrame frame){
        recover(thread, frame);
        throw new FallbackException();
    }

    private static void recover(TThread thread, ImageFrame frame){
        List<ImageFrame> frames = new ArrayList<>();
        while (frame != null){
            frames.add(frame);
            frame = frame.next;
        }

        ImageFrame currImgFrame = frames.get(frames.size()-1);
        Frame currRealFrame = thread.frameStack.element();

        currRealFrame.ip = currImgFrame.ip;
        currRealFrame.sp = currImgFrame.sp;
        currRealFrame.setLine(currImgFrame.line);
        System.arraycopy(currImgFrame.locals, 0, currRealFrame.locals, 0, currImgFrame.locals.length);
        System.arraycopy(currImgFrame.stack, 0, currRealFrame.stack, 0, currImgFrame.stack.length);


        for (int i = frames.size() - 2; i >= 0; i--){  // ignore the current frame; already updated before
            ImageFrame imgFrame = frames.get(i);

            Frame trueFrame = new Frame(
                    imgFrame.owner,
                    imgFrame.name,
                    imgFrame.instructions,
                    imgFrame.stack,
                    imgFrame.locals,
                    imgFrame.module);
            trueFrame.ip = imgFrame.ip;
            trueFrame.sp = imgFrame.sp;

            thread.frameStack.push(trueFrame);
        }
    }

}
