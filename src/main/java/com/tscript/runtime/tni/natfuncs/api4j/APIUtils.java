package com.tscript.runtime.tni.natfuncs.api4j;

import com.tscript.runtime.core.TscriptVM;
import com.tscript.runtime.typing.*;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

class APIUtils {

    protected static Visibility visibility(int modifiers) {
        if (Modifier.isPublic(modifiers))
            return Visibility.PUBLIC;
        if (Modifier.isProtected(modifiers))
            return Visibility.PROTECTED;
        return Visibility.PRIVATE;
    }


    protected static Object validate(Class<?> expected, TObject got) {
        if (expected == Integer.TYPE || expected == Integer.class) {
            return got.getType() == TInteger.TYPE
                    ? ((TInteger)got).getValue()
                    : null;
        }
        if (expected == Long.TYPE || expected == Long.class) {
            return got.getType() == TInteger.TYPE
                    ? ((TInteger)got).getValue().longValue()
                    : null;
        }
        if (expected == Double.TYPE || expected == Double.class) {
            return got.getType() == TReal.TYPE
                    ? ((TReal) got).getValue()
                    : null;
        }
        if (expected == Float.TYPE || expected == Float.class) {
            return got.getType() == TReal.TYPE
                    ? ((TReal)got).getValue().floatValue()
                    : null;
        }
        if (expected == Boolean.TYPE || expected == Boolean.class) {
            return got.getType() == TBoolean.TYPE
                    ? ((TBoolean)got).getValue()
                    : null;
        }
        if (expected == Byte.TYPE || expected == Byte.class) {
            return got.getType() == TInteger.TYPE
                    ? ((TInteger)got).getValue().byteValue()
                    : null;
        }
        if (expected == Short.TYPE || expected == Short.class) {
            return got.getType() == TInteger.TYPE
                    ? ((TInteger)got).getValue().shortValue()
                    : null;
        }
        if (expected == Character.TYPE || expected == Character.class) {
            return got.getType() == TString.TYPE
                    ? ((TString)got).getValue().charAt(0)
                    : null;
        }
        if (expected == String.class) {
            return got.getType() == TString.TYPE
                    ? ((TString)got).getValue()
                    : null;
        }

        if (got instanceof JavaInstanceObject) {
            Object candidate = ((JavaInstanceObject)got).instance;
            return expected == candidate.getClass()
                    ? candidate
                    : null;
        }

        return null;
    }

    protected static Class<?> getClassOf(TObject obj){
        if (obj.getType() == Null.INSTANCE) return Void.class;
        if (obj.getType() == TInteger.TYPE) return Integer.class;
        if (obj.getType() == TReal.TYPE) return Double.class;
        if (obj.getType() == TBoolean.TYPE) return Boolean.class;
        if (obj.getType() == TString.TYPE) return String.class;
        if (obj.getType() instanceof JavaInstanceObject o)
            return o.instance.getClass();
        return null;
    }

    protected static TObject toTObject(Object obj, TscriptVM vm) {

        if (obj == null){
            return Null.INSTANCE;
        }

        if (obj instanceof Integer i) {
            return new TInteger(i);
        }

        if (obj instanceof Long l) {
            return new TReal(l.doubleValue());
        }

        if (obj instanceof Double d) {
            return new TReal(d);
        }

        if (obj instanceof Float f) {
            return new TReal(f.doubleValue());
        }

        if (obj instanceof Short s) {
            return new TInteger(s.intValue());
        }

        if (obj instanceof Byte b) {
            return new TInteger(b.intValue());
        }

        if (obj instanceof Boolean b) {
            return TBoolean.of(b);
        }

        if (obj instanceof Character c) {
            return new TString(c.toString());
        }

        if (obj instanceof String s) {
            return new TString(s);
        }

        JavaType type = JavaAPIStateManager.getInstance(vm)
                .getType(obj.getClass());
        return new JavaInstanceObject(type, obj, vm);
    }

    protected static String invalidType(Class<?> expected, TObject got){
        String expectedName = expected.getName();

        if (expected == Integer.TYPE) expectedName = "Integer";
        if (expected == Long.TYPE) expectedName = "Long";
        if (expected == Double.TYPE) expectedName = "Double";
        if (expected == Float.TYPE) expectedName = "Float";
        if (expected == Short.TYPE) expectedName = "Short";
        if (expected == Byte.TYPE) expectedName = "Byte";
        if (expected == Boolean.TYPE) expectedName = "Boolean";
        if (expected == Character.TYPE) expectedName = "Character";
        if (expected == String.class) expectedName = "String";

        return "invalid parameter type: expected Type<" + expectedName + "> but got: " + got.getType().getDisplayName();
    }

    protected static String primitiveNotNullableError(Class<?> clazz){
        return "primitive type '" + clazz.getName() + "' is not nullable";
    }

}
