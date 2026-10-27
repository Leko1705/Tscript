package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.types.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.typing.Range;
import com.tscript.runtime.typing.Type;

public class Typeof extends UnaryOp {

    public Typeof(IRValue value) {
        super(value);
        value.getUsers().add(this);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitTypeof(this, p);
    }

    @Override
    public String toString() {
        return "typeof(" + getValue().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue value = getValue();
        IRType type = tool.type(value);

        if (type == IntType.INSTANCE) {
            return tool.builder().type(TInteger.TYPE);
        }
        else if (type == RealType.INSTANCE) {
            return tool.builder().type(TReal.TYPE);
        }
        else if (type == BoolType.INSTANCE) {
            return tool.builder().type(TBoolean.TYPE);
        }
        else if (type == StrType.INSTANCE) {
            return tool.builder().type(TString.TYPE);
        }
        else if (type == NullType.INSTANCE) {
            return tool.builder().type(Null.TYPE);
        }
        else if (type == RangeType.INSTANCE) {
            return tool.builder().type(Range.TYPE);
        }
        else if (type instanceof ArrayType) {
            return tool.builder().type(TArray.TYPE);
        }
        else if (type instanceof DictType) {
            return tool.builder().type(TDictionary.TYPE);
        }
        else if (type instanceof TypeMirror) {
            return tool.builder().type(Type.TYPE);
        }

        return this;
    }
}
