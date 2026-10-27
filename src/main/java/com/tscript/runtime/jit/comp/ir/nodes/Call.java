package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.caching.ObjectCache;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.typing.Range;
import com.tscript.runtime.typing.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class Call extends IRValue {

    private IRValue called;

    private final List<IRValue> args;

    private final List<IRValue> all;

    public Call(IRValue called, List<IRValue> args) {
        this.called = called;
        this.args = args;
        this.all = new ArrayList<>(this.args.size() + 1);
        this.all.add(this.called);
        this.all.addAll(this.args);
        called.getUsers().add(this);
        for (IRValue arg : args) {
            arg.getUsers().add(this);
        }
    }

    public IRValue getCalled() {
        return called;
    }

    public List<IRValue> getArgs() {
        return args;
    }

    @Override
    public List<IRValue> getInputs() {
        return all;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitCall(this, p);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getCalled().toString());
        StringJoiner sj = new StringJoiner(", ", "(", ")");
        for (IRValue arg : getArgs()) {
            sj.add(arg.toString());
        }
        return sb.append(sj).toString();
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {

        IRValue called = getCalled();
        List<IRValue> args = getArgs();

        if (called instanceof Builtin builtin) {
            return canonicalizeBuiltin(tool, builtin, args);
        }

        return this;
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        if (called == oldInput) {
            called = newInput;
        }
        Collections.replaceAll(args, oldInput, newInput);
    }

    private IRValue canonicalizeBuiltin(CanonicalizerTool tool, Builtin builtin, List<IRValue> args) {

        if (builtin.getAddress() == ObjectCache.getId(Null.TYPE) && args.isEmpty()) {
            return tool.builder().nullValue();
        }

        if (builtin.getAddress() == ObjectCache.getId(Range.TYPE) && args.size() == 2) {
            IRValue from = args.get(0);
            IRValue to = args.get(1);

            if (tool.isInt(from) && tool.isInt(to)) {
                return tool.builder().range(from, to);
            }
        }

        if (builtin.getAddress() == ObjectCache.getId(Type.TYPE) && args.size() == 1) {
            IRValue arg = args.get(0);
            return tool.builder().typeof(arg);
        }

        if (builtin.getAddress() == ObjectCache.getId(TInteger.TYPE) && args.size() == 1) {
            IRValue arg = args.get(0);
            if (arg instanceof Int) return arg;
            if (arg instanceof Real r) return tool.builder().integer(r.value.intValue());
            if (arg instanceof Str s) return tool.builder().integer(Integer.parseInt(s.value));
        }

        if (builtin.getAddress() == ObjectCache.getId(TReal.TYPE) && args.size() == 1) {
            IRValue arg = args.get(0);
            if (arg instanceof Real) return arg;
            if (arg instanceof Int i) return tool.builder().real(i.value);
            if (arg instanceof Str s) return tool.builder().real(Double.parseDouble(s.value));
        }

        if (builtin.getAddress() == ObjectCache.getId(TBoolean.TYPE) && args.size() == 1) {
            IRValue arg = args.get(0);
            if (arg instanceof Bool) return arg;
            if (arg instanceof NullValue) return tool.builder().bool(false);
            if (arg instanceof Int i) return tool.builder().bool(i.value != 0);
            if (arg instanceof Str s) return tool.builder().bool(!s.value.isEmpty());
            if (arg instanceof Array a) return tool.builder().bool(!a.getValues().isEmpty());
        }

        if (builtin.getAddress() == ObjectCache.getId(TString.TYPE) && args.size() == 1) {
            IRValue arg = args.get(0);
            return tool.builder().stringify(arg);
        }

        return this;
    }
}
