package com.tscript.runtime.jit.caching;

import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.typing.Type;

import java.util.*;

public class LRUCache extends CacheDecorator {


    private interface Key {
        void remove();
    }

    private class FuncKey implements Key {
        private final String context;
        private final Type[] types;

        private FuncKey(String context, Type[] types) {
            this.context = context;
            this.types = types;
        }

        @Override
        public void remove() {
            removeFunctional(context, types);
        }
    }

    private class PosKey implements Key {
        private final String context;
        private final int pos;

        private PosKey(String context, int pos) {
            this.context = context;
            this.pos = pos;
        }

        @Override
        public void remove() {
            removePositional(context, pos);
        }
    }


    private final Map<NativeImage, Key> keys = new HashMap<>();
    private final Deque<Key> queue = new LinkedList<>();

    private final int capacity;

    public LRUCache(JITCache cache, int capacity) {
        super(cache);
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity <= 0");
        }
        this.capacity = capacity;
    }

    private void handlePostAddAction(Key key, NativeImage image) {
        if (super.size() > capacity) {
            key = queue.removeLast();
            key.remove();
        }
        else {
            queue.addFirst(key);
            keys.put(image, key);
        }
    }

    @Override
    public void putPositional(String context, int ip, NativeImage image) {
        super.putPositional(context, ip, image);
        handlePostAddAction(new PosKey(context, ip), image);
    }

    @Override
    public void putFunctional(String context, Type[] argumentTypes, NativeImage image) {
        super.putFunctional(context, argumentTypes, image);
        handlePostAddAction(new FuncKey(context, argumentTypes), image);
    }

    @Override
    public boolean removeFunctional(String context, Type[] argumentTypes) {
        NativeImage img = super.getFunctional(context, argumentTypes);
        if (img != null) {
            Key key = keys.remove(img);
            queue.remove(key);
            return false;
        }
        return super.removeFunctional(context, argumentTypes);
    }

    @Override
    public boolean removePositional(String context, int ip) {
        NativeImage img = super.getPositional(context, ip);
        if (img != null) {
            Key key = keys.remove(img);
            queue.remove(key);
            return false;
        }
        return super.removePositional(context, ip);
    }

    @Override
    public NativeImage getFunctional(String context, Type[] argumentTypes) {
        NativeImage image = super.getFunctional(context, argumentTypes);
        if (image != null) {
            Key key = keys.get(image);
            queue.remove(key);
            queue.addFirst(key);
        }
        return image;
    }

    @Override
    public NativeImage getPositional(String context, int ip) {
        NativeImage image = super.getPositional(context, ip);
        if (image != null) {
            Key key = keys.get(image);
            queue.remove(key);
            queue.addFirst(key);
        }
        return image;
    }
}
