package com.tscript.runtime.tni.natfuncs.api4j;

import com.tscript.runtime.typing.TObject;

import java.lang.reflect.Method;
import java.util.*;

class JavaMethodTree {


    private static class Node {

        private Method method;
        private final Map<Class<?>, Node> children = new HashMap<>();

        public void put(Iterator<Class<?>> itr, Method method) {
            if (!itr.hasNext()) {
                this.method = method;
                return;
            }
            Node node = children.computeIfAbsent(itr.next(), k -> new Node());
            node.put(itr, method);
        }

        public Method getMethod(Iterator<TObject> itr) {
            if (!itr.hasNext()) return method;
            TObject next = itr.next();
            Class<?> clazz = APIUtils.getClassOf(next);
            Node child = children.get(clazz);
            if (child == null) return null;
            return child.getMethod(itr);
        }


    }


    private final Node root = new Node();

    void add(Method method) {
        Iterator<Class<?>> itr = List.of(method.getParameterTypes()).iterator();
        itr = new ClassPutterItr(itr);
        root.put(itr, method);
    }

    Method getMethod(Iterator<TObject> params) {
        return root.getMethod(params);
    }


    private static class ClassPutterItr implements Iterator<Class<?>> {

        private final Iterator<Class<?>> itr;

        private ClassPutterItr(Iterator<Class<?>> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public Class<?> next() {
            Class<?> clazz = itr.next();

            if (!clazz.isPrimitive() && clazz != Void.class)
                return clazz;

            if (clazz == Integer.TYPE) return Integer.class;
            if (clazz == Long.TYPE) return Long.class;
            if (clazz == Float.TYPE) return Float.class;
            if (clazz == Double.TYPE) return Double.class;
            if (clazz == Boolean.TYPE) return Boolean.class;
            if (clazz == Character.TYPE) return Character.class;
            if (clazz == Byte.TYPE) return Byte.class;
            if (clazz == Short.TYPE) return Short.class;
            if (clazz == Void.TYPE) return Void.class;
            return null;
        }
    }

}
