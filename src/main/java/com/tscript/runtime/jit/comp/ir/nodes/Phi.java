package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

import java.util.*;

public class Phi extends IRValue {

    private final List<IRValue> args;

    public Phi(List<IRValue> args) {
        this.args = args;
        for (IRValue arg : args) {
            arg.getUsers().add(this);
        }
    }

    public Phi(IRValue... args) {
        this(Arrays.asList(args));
    }

    public List<IRValue> getArgs() {
        return args;
    }

    @Override
    public List<IRValue> getInputs() {
        return getArgs();
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        Collections.replaceAll(args, oldInput, newInput);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitPhi(this, p);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "(", ")");
        for (IRValue arg : getArgs()) {
            try {
                sj.add(arg.toString());
            }
            catch (StackOverflowError e) {
                sj.add("...");
            }
        }
        return "phi" + sj;
    }
}
