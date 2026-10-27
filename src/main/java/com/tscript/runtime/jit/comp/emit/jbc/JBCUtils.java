package com.tscript.runtime.jit.comp.emit.jbc;

import com.tscript.runtime.core.*;
import com.tscript.runtime.jit.ImageFrame;
import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.jit.caching.ObjectCache;
import com.tscript.runtime.jit.tracing.DeoptInfo;
import com.tscript.runtime.jit.tracing.Deoptimization;
import com.tscript.runtime.jit.tracing.ImageAbortException;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.*;

import java.util.*;

/**
 * Runtime support routines called by the generated JVM bytecode.
 * Routines that may report a runtime error throw {@link ImageAbortException} afterwards.
 */
@SuppressWarnings("unused")
public final class JBCUtils {

    private JBCUtils() {
    }

    /* ---------------- boxing ---------------- */

    @JITSensitive
    public static TObject boxInt(int value) {
        return new TInteger(value);
    }

    @JITSensitive
    public static TObject boxReal(double value) {
        return new TReal(value);
    }

    @JITSensitive
    public static TObject boxBool(boolean value) {
        return TBoolean.of(value);
    }

    @JITSensitive
    public static TObject boxStr(String value) {
        return new TString(value);
    }

    @JITSensitive
    public static int unboxInt(TObject value) {
        return ((TInteger) value).getValue();
    }

    @JITSensitive
    public static double unboxReal(TObject value) {
        return ((TReal) value).getValue();
    }

    @JITSensitive
    public static boolean unboxBool(TObject value) {
        return ((TBoolean) value).getValue();
    }

    @JITSensitive
    public static String unboxStr(TObject value) {
        return ((TString) value).getValue();
    }

    @JITSensitive
    public static boolean isNull(TObject value) {
        return value == Null.INSTANCE;
    }

    /* ---------------- entry values ---------------- */

    @JITSensitive
    public static TObject local(ImageFrame frame, int index) {
        return frame.locals[index];
    }

    @JITSensitive
    public static TObject stackValue(ImageFrame frame, int index) {
        return frame.stack[index];
    }

    @JITSensitive
    public static TObject argument(ImageFrame frame, int index) {
        return frame.args[index];
    }

    @JITSensitive
    public static TObject constant(int id) {
        return (TObject) ObjectCache.getObject(id);
    }

    /* ---------------- operations ---------------- */

    @JITSensitive
    public static String display(TObject value) {
        return value.getDisplayName();
    }

    @JITSensitive
    public static boolean areEqual(TObject first, TObject second) {
        return TNIUtils.areEqual(first, second);
    }

    @JITSensitive
    public static boolean isTrue(TObject value) {
        return TNIUtils.isTrue(value);
    }

    @JITSensitive
    public static boolean isSelf(TObject value, ImageFrame frame) {
        return value instanceof com.tscript.runtime.typing.Function f
                && f.getMetadata() == frame.metadata
                && f.getOwner() == frame.owner;
    }

    @JITSensitive
    public static boolean isCallee(TObject value, int metadataId) {
        return value instanceof VirtualFunction f && f.getMetadata() == ObjectCache.getObject(metadataId);
    }

    @JITSensitive
    public static TObject functionOwner(TObject function) {
        return ((com.tscript.runtime.typing.Function) function).getOwner();
    }

    @JITSensitive
    public static TObject binary(TThread thread, TObject left, TObject right, int opcode) {
        Opcode op = Opcode.values()[opcode];
        TObject result = ALU.performBinaryOperation(left, right, op, thread);
        if (result == null) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.invalidBinaryOperation(left, right, op));
            throw ImageAbortException.INSTANCE;
        }
        return result;
    }

    @JITSensitive
    public static TObject unary(TThread thread, TObject value, int opcode) {
        Opcode op = Opcode.values()[opcode];
        TObject result = ALU.performUnaryOperation(value, op);
        if (result == null) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.invalidUnaryOperation(value, op));
            throw ImageAbortException.INSTANCE;
        }
        return result;
    }

    @JITSensitive
    public static TObject range(int from, int to) {
        return new Range(new TInteger(from), new TInteger(to));
    }

    @JITSensitive
    public static TObject newArray(TObject[] values) {
        return new TArray(new ArrayList<>(Arrays.asList(values)));
    }

    @JITSensitive
    public static TObject newDict(TObject[] keysAndValues) {
        Map<TObject, TObject> content = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            content.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return new TDictionary(content);
    }

    /* ---------------- state access ---------------- */

    @JITSensitive
    public static TObject loadGlobal(int moduleId, int index) {
        return ((Module) ObjectCache.getObject(moduleId)).loadMember(index).get();
    }

    @JITSensitive
    public static void storeGlobal(TThread thread, ImageFrame frame, int site, int moduleId, int index, TObject value) {
        int depth = thread.frameStack.size();
        int mark = frame.enter(thread, site);
        try {
            ((Module) ObjectCache.getObject(moduleId)).loadMember(index).set(value, thread);
        }
        finally {
            frame.leave(thread, mark);
        }
        checkNoError(thread, depth);
    }

    /**
     * Writes back a global that was kept in a register by compiled code (see global promotion).
     */
    @JITSensitive
    public static void writeGlobal(TThread thread, int moduleId, int index, TObject value) {
        ((Module) ObjectCache.getObject(moduleId)).loadMember(index).set(value, thread);
    }

    @JITSensitive
    public static TObject getIterator(TThread thread, ImageFrame frame, int site, TObject candidate) {
        if (!(candidate instanceof IterableObject iterable)) {
            int mark = frame.enter(thread, site);
            try {
                thread.reportRuntimeError(InternalRuntimeErrorMessages.notIterable(candidate));
            }
            finally {
                frame.leave(thread, mark);
            }
            throw ImageAbortException.INSTANCE;
        }
        return iterable.iterator();
    }

    @JITSensitive
    public static boolean hasNext(TObject iterator) {
        return ((IteratorObject) iterator).hasNext();
    }

    @JITSensitive
    public static TObject next(TObject iterator) {
        return ((IteratorObject) iterator).next();
    }

    /* ---------------- objects ---------------- */

    /**
     * Allocates an instance of a class without running its constructor.
     */
    @JITSensitive
    public static TObject newInstance(int typeId) {
        return ((VirtualType) ObjectCache.getObject(typeId)).newInstance();
    }

    /**
     * Creates a function object like LOAD_VIRTUAL, optionally followed by SET_OWNER.
     * @param owner the owner of the function or null if the function owns itself
     */
    @JITSensitive
    public static TObject newFunction(int moduleId, int index, TObject owner) {
        Module module = (Module) ObjectCache.getObject(moduleId);
        VirtualFunction function = module.getFunctionArea().loadFunction(index);
        function.setOwner(owner == null ? function : owner);
        return function;
    }

    @JITSensitive
    public static boolean isIdentical(TObject value, int id) {
        return value == ObjectCache.getObject(id);
    }

    /* ---------------- members ---------------- */

    /**
     * Reads a member of a class instance whose type was guarded before.
     */
    @JITSensitive
    public static TObject loadField(TObject object, int index) {
        return ((VirtualObject) object).member(index).get();
    }

    @JITSensitive
    public static void storeField(TThread thread, TObject object, int index, TObject value) {
        ((VirtualObject) object).member(index).set(value, thread);
    }

    /**
     * Reads a member of the owner of the running function by index (like LOAD_INTERNAL).
     */
    @JITSensitive
    public static TObject loadInternal(TObject owner, int index) {
        if (owner instanceof VirtualObject object) return object.member(index).get();
        return owner.loadMember(index).get();
    }

    @JITSensitive
    public static void storeInternal(TThread thread, TObject owner, int index, TObject value) {
        Member member = owner instanceof VirtualObject object ? object.member(index) : owner.loadMember(index);
        member.set(value, thread);
    }

    /**
     * Reads a member by name like LOAD_EXTERNAL (public members) or LOAD_SUPER (protected members,
     * the object is the super object), reporting the same errors as the interpreter.
     */
    @JITSensitive
    public static TObject loadExternal(TThread thread, ImageFrame frame, int site, TObject object, String name, boolean protectedAccess) {
        Member member = accessibleMember(thread, frame, site, object, name, protectedAccess);
        return member.get();
    }

    /**
     * Writes a member by name like STORE_EXTERNAL or STORE_SUPER, reporting the same errors as the interpreter.
     */
    @JITSensitive
    public static void storeExternal(TThread thread, ImageFrame frame, int site, TObject object, String name, boolean protectedAccess, TObject value) {
        Member member = accessibleMember(thread, frame, site, object, name, protectedAccess);
        if (!member.isMutable()) {
            reportError(thread, frame, site, InternalRuntimeErrorMessages.invalidMutability(member.getName()));
        }
        member.set(value, thread);
    }

    private static Member accessibleMember(TThread thread, ImageFrame frame, int site, TObject object, String name, boolean protectedAccess) {
        Member member = TNIUtils.searchMember(object, name);
        if (member == null) {
            reportError(thread, frame, site, InternalRuntimeErrorMessages.noSuchMember(object, name));
        }
        Visibility minimum = protectedAccess ? Visibility.PROTECTED : Visibility.PUBLIC;
        if (member.getVisibility().ordinal() > minimum.ordinal()) {
            reportError(thread, frame, site, InternalRuntimeErrorMessages.invalidAccessVisibility(name, member.getVisibility()));
        }
        return member;
    }

    /**
     * Reports a runtime error at the given site and leaves compiled code.
     */
    private static void reportError(TThread thread, ImageFrame frame, int site, String message) {
        int mark = frame.enter(thread, site);
        try {
            thread.reportRuntimeError(message);
        }
        finally {
            frame.leave(thread, mark);
        }
        throw ImageAbortException.INSTANCE;
    }

    @JITSensitive
    public static TObject containerRead(TThread thread, ImageFrame frame, int site, TObject container, TObject key) {
        int depth = thread.frameStack.size();
        int mark = frame.enter(thread, site);
        TObject result;
        try {
            if (!(container instanceof ContainerAccessibleObject accessible)) {
                thread.reportRuntimeError(InternalRuntimeErrorMessages.notAccessible(container));
                throw ImageAbortException.INSTANCE;
            }
            result = accessible.readFromContainer(thread, key);
        }
        finally {
            frame.leave(thread, mark);
        }
        if (result == null) throw ImageAbortException.INSTANCE;
        checkNoError(thread, depth);
        return result;
    }

    @JITSensitive
    public static void containerWrite(TThread thread, ImageFrame frame, int site, TObject container, TObject key, TObject value) {
        int depth = thread.frameStack.size();
        int mark = frame.enter(thread, site);
        try {
            if (!(container instanceof ContainerWriteableObject writeable)) {
                thread.reportRuntimeError(InternalRuntimeErrorMessages.notWriteable(container));
                throw ImageAbortException.INSTANCE;
            }
            writeable.writeToContainer(thread, key, value);
        }
        finally {
            frame.leave(thread, mark);
        }
        checkNoError(thread, depth);
    }

    private static void checkNoError(TThread thread, int depth) {
        if (!thread.isRunning() || thread.frameStack.size() != depth) {
            throw ImageAbortException.INSTANCE;
        }
    }

    /* ---------------- deoptimization ---------------- */

    @JITSensitive
    public static TObject deopt(int infoId, TObject[] values) {
        return new Deoptimization((DeoptInfo) ObjectCache.getObject(infoId), values);
    }

}
