package com.tscript.runtime.jit.graph.parse;

import com.tscript.runtime.jit.graph.EndNode;
import com.tscript.runtime.jit.graph.FlowNode;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.graph.expr.Expression;
import com.tscript.runtime.jit.graph.expr.Int;
import com.tscript.runtime.jit.graph.expr.Null;
import com.tscript.runtime.jit.instructions.Instruction;
import com.tscript.runtime.jit.instructions.InstructionHandler;
import com.tscript.runtime.jit.instructions.PushInt;
import com.tscript.runtime.jit.instructions.PushNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class GraphParserImpl implements GraphParser, InstructionHandler {

    private final Deque<Expression> stack;
    private Node root;
    private Node current;

    public GraphParserImpl() {
        root = new FlowNode(null, null);
        current = root;
        stack = new ArrayDeque<>();
    }

    @Override
    public Node parse(List<Instruction> instructions) {
        for (Instruction instruction : instructions) {
            instruction.handle(this);
        }
        if (requireToEndNodeConversion()) {
            root = new EndNode(root.getExpr());
        }
        return root;
    }

    private boolean requireToEndNodeConversion(){
        return root == current
                && current instanceof FlowNode f
                && f.expression == null;
    }

    @Override
    public void handlePushNull(PushNull pushNull) {
        stack.push(Null.INSTANCE);
    }

    @Override
    public void handlePushInt(PushInt pushInt) {
        stack.push(new Int(pushInt.value()));
    }

}
