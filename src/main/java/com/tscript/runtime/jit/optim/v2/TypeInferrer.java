package com.tscript.runtime.jit.optim.v2;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.ir.*;
import com.tscript.runtime.jit.ir.nodes.BinaryOp;
import com.tscript.runtime.jit.ir.nodes.LoadLocal;
import com.tscript.runtime.jit.ir.nodes.StoreLocal;
import com.tscript.runtime.jit.ir.types.*;
import com.tscript.runtime.typing.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class TypeInferrer extends IRScanner<Void, Void> implements IROptimizer {

    private Deque<Map<Integer, IRType>> localsStack;

    @Override
    public IRBlock optimize(IRBlock block, Type[] inputTypes) {

        IRWalker<Void, Void> walker = new IRTreeWalker<>() {
            @Override
            public void onBlockEnter(IRBlock pBlock) {
                localsStack = new ArrayDeque<>();

                Map<Integer, IRType> locals = new HashMap<>();
                localsStack.push(locals);

                if (pBlock == block) {
                    for (int i = 0; i < inputTypes.length; i++) {
                        IRType type = fromTType(inputTypes[i]);
                        locals.put(i, type);
                    }
                }
            }
        };
        walker.walk(this, block);

        return block;
    }

    @Override
    public boolean isOptimized() {
        return false;
    }

    @Override
    public Void visitStoreLocal(StoreLocal node, Void unused) {
        super.visitStoreLocal(node, unused);
        getLocals().put(node.address, node.value.getType());
        return null;
    }

    @Override
    public Void visitLoadLocal(LoadLocal node, Void unused) {
        node.type = getLocals().getOrDefault(node.address, IRUnknownType.INSTANCE);
        return null;
    }

    @Override
    public Void visitBinaryOp(BinaryOp node, Void unused) {
        super.visitBinaryOp(node, unused);

        IRType lhsType = node.lhs.getType();
        IRType rhsType = node.rhs.getType();

        if (node.operation == Opcode.ADD) {
            if (lhsType.isType(IRIntegerType.class) && rhsType.isType(IRIntegerType.class)) {
                node.type = new IRIntegerType();
            }

        }

        return null;
    }

    private Map<Integer, IRType> getLocals() {
        return localsStack.element();
    }


    private static IRType fromTType(Type type) {
        if (type == Null.TYPE) return new IRNullType();
        if (type == TInteger.TYPE) return new IRIntegerType();
        if (type == TReal.TYPE) return new IRRealType();
        if (type == TBoolean.TYPE) return new IRBooleanType();
        if (type == TString.TYPE) return new IRStringType();
        if (type == TArray.TYPE) return new IRArrayType();
        if (type == TDictionary.TYPE) return new IRDictionaryType();
        if (type == Range.TYPE) return new IRRangeType();
        return IRUnknownType.INSTANCE;
    }
}
