package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.core.Builtins;
import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRFunctionType;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRTypeType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;
import com.tscript.runtime.typing.Function;
import com.tscript.runtime.typing.TObject;

public class Builtin implements IRValue {

    public final int id;

    public Builtin(int id) {
        this.id = id;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitBuiltin(this, p);
    }

    @Override
    public IRType getType() {
        TObject builtin = Builtins.load(id);
        return (builtin.getType() == Function.TYPE) ? new IRFunctionType() : new IRTypeType(IRUnknownType.INSTANCE, true);
    }
}
