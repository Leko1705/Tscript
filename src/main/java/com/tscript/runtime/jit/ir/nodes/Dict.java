package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRDictionaryType;
import com.tscript.runtime.jit.ir.types.IRType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dict implements IRValue {

    public final List<IRValue> keys = new ArrayList<>();
    public final List<IRValue> values = new ArrayList<>();

    public final IRDictionaryType type = new IRDictionaryType();

    public Dict() {}

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitDictionary(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }

    @Override
    public boolean canBePrimitive() {
        return true;
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {

        AtomicBoolean b = new AtomicBoolean(false);
        keys.replaceAll(candidate -> {
            if (candidate == oldNode) {
                b.set(true);
                return newNode;
            }
            else {
                return candidate;
            }
        });

        if (b.get()) return b.get();

        values.replaceAll(candidate -> {
            if (candidate == oldNode) {
                b.set(true);
                return newNode;
            }
            else {
                return candidate;
            }
        });
        return b.get();
    }
}
