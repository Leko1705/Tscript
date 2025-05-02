package com.tscript.runtime.jit.ir;

import com.tscript.runtime.jit.ir.nodes.Goto;

import java.util.LinkedList;
import java.util.List;

public class IRBlock {

    public static final IRBlock ENDLESS_LOOP = new IRBlock();

    static {
        ENDLESS_LOOP.append(new Goto(ENDLESS_LOOP));
    }


    private final List<IRNode> code = new LinkedList<>();

    public List<IRNode> getCode() {
        return code;
    }

    public void append(IRNode node) {
        code.add(node);
    }

}
