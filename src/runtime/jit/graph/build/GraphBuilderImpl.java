package runtime.jit.graph.build;

import runtime.jit.graph.*;
import tscriptc.generation.Opcode;
import tscriptc.util.Conversion;

import java.util.*;

public class GraphBuilderImpl implements GraphBuilder {

    private final LinkedFlowNode enterNode;
    private LinkedFlowNode current;
    private final Map<Integer, FlowNode> nodeMap = new HashMap<>();
    private final Deque<ExpressionNode> stack = new ArrayDeque<>();

    public GraphBuilderImpl() {
        enterNode = new Flow.StartNode();
        current = enterNode;
        nodeMap.put(0, enterNode);
    }

    private void expandLinkedFlow(int index, LinkedFlowNode node){
        current.setNext(node);
        if (current == enterNode){
            nodeMap.put(0, enterNode);
        }
        else {
            nodeMap.put(index, node);
        }
        current = node;
    }

    private static int intFromSecond2Bytes(byte[] bytes){
        return jumpAddress(bytes[1], bytes[2]);
    }

    private static int jumpAddress(byte b1, byte b2){
        return ((b1 & 0xff) << 8) | (b2 & 0xff);
    }

    private int skipParameters(byte[][] code){
        int index = 0;
        Opcode opcode = Opcode.of(code[index][0]);
        while (opcode == Opcode.STORE_LOCAL){
            expandLinkedFlow(index, new Flow.LoadParameterNode(code[index][1]));
            opcode = Opcode.of(code[index][0]);
        }
        return index;
    }

    @Override
    public FlowNode build(byte[][] code) {
        int index = skipParameters(code);

        for (; index < code.length; index++) {

            if (isPathClose(index) && index != 0) {
                handleSuddenPathClose(index);
            }

            byte[] inst = code[index];
            Opcode opcode = Opcode.of(inst[0]);

            switch (opcode) {

                case PUSH_NULL -> stack.push(new Expressions.NullValueNode());
                case PUSH_INT -> stack.push(new Expressions.IntegerNode(inst[1]));
                case PUSH_BOOL -> stack.push(new Expressions.BooleanNode(inst[1] == 1));
                case LOAD_CONST -> stack.push(new Expressions.ConstantNode(inst[1]));
                case PUSH_THIS -> stack.push(new Expressions.ThisNode());

                case POP -> expandLinkedFlow(index, new Flow.ExpressionStatementNode(stack.pop()));
                case NEW_LINE -> expandLinkedFlow(index, new Flow.NewLineNode(Conversion.fromBytes(inst[1], inst[2], inst[3], inst[4])));

                case LOAD_GLOBAL -> stack.push(new Expressions.LoadGlobalNode(inst[1]));
                case STORE_GLOBAL -> expandLinkedFlow(index, new Flow.StoreGlobalNode(inst[1], stack.pop()));

                case LOAD_LOCAL -> stack.push(new Expressions.LoadLocalNode(inst[1]));
                case STORE_LOCAL -> expandLinkedFlow(index, new Flow.StoreLocalNode(inst[1], stack.pop()));

                case LOAD_MEMBER_FAST -> stack.push(new Expressions.LoadMemberFastNode(inst[1]));
                case STORE_MEMBER_FAST -> expandLinkedFlow(index, new Flow.StoreMemberFastNode(inst[1], stack.pop()));

                case LOAD_STATIC -> stack.push(new Expressions.LoadStaticNode(inst[1]));
                case STORE_STATIC -> expandLinkedFlow(index, new Flow.StoreStaticNode(inst[1], stack.pop()));

                case LOAD_MEMBER -> stack.push(new Expressions.LoadMemberNode(inst[1]));
                case STORE_MEMBER -> expandLinkedFlow(index, new Flow.StoreMemberNode(inst[1], stack.pop()));

                case CONTAINER_READ -> {
                    ExpressionNode container = stack.pop();
                    ExpressionNode key = stack.pop();
                    stack.push(new Expressions.ContainerReadNode(container, key));
                }
                case CONTAINER_WRITE -> {
                    ExpressionNode container = stack.pop();
                    ExpressionNode key = stack.pop();
                    ExpressionNode value = stack.pop();
                    expandLinkedFlow(index, new Flow.ContainerWriteNode(container, key, value));
                }

                case LOAD_ABSTRACT_IMPL -> stack.push(new Expressions.LoadAbstractImplNode(inst[1]));

                case ADD, SUB, MUL, DIV, IDIV, MOD, POW, AND, OR, XOR,
                     LT, GT, LEQ, GEQ, SLA, SRA, SRL, EQUALS, NOT_EQUALS -> {
                    ExpressionNode right = stack.pop();
                    ExpressionNode left = stack.pop();
                    stack.push(new Expressions.BinaryOperationNode(opcode, left, right));
                }
                case NOT, NEG, POS -> stack.push(new Expressions.UnaryOperationNode(opcode, stack.pop()));
                case GET_TYPE -> stack.push(new Expressions.GetTypeNode(stack.pop()));
                case MAKE_ARRAY -> {
                    List<ExpressionNode> content = new ArrayList<>();
                    for (int i = 0; i < inst[1]; i++)
                        content.add(stack.pop());
                    stack.push(new Expressions.ArrayNode(content));
                }
                case MAKE_DICT -> {
                    List<ExpressionNode> keys = new ArrayList<>();
                    List<ExpressionNode> values = new ArrayList<>();
                    for (int i = 0; i < inst[1]; i++){
                        keys.add(stack.pop());
                        values.add(stack.pop());
                    }
                    stack.push(new Expressions.DictNode(keys, values));
                }
                case MAKE_RANGE -> {
                    ExpressionNode to = stack.pop();
                    ExpressionNode from = stack.pop();
                    stack.push(new Expressions.RangeNode(from, to));
                }
                case GET_ITR -> {
                }
                case BRANCH_ITR -> {
                }
                case ITR_NEXT -> {
                }
                case ENTER_TRY -> expandLinkedFlow(index, new Flow.TryStartNode());
                case LEAVE_TRY -> expandLinkedFlow(index, new Flow.TryEndNode());
                case THROW -> expandLinkedFlow(index, new Flow.ThrowNode(stack.pop()));
                case CALL -> {
                    ExpressionNode called = stack.pop();
                    List<ExpressionNode> args = new ArrayList<>();
                    for (int i = 0; i < inst[1]; i++){
                        args.add(stack.pop());
                    }
                    stack.push(new Expressions.CallNode(called, args));
                }
                case WRAP_ARGUMENT -> stack.push(new Expressions.ArgumentNode(inst[1], stack.pop()));
                case RETURN -> expandLinkedFlow(index, new Flow.ReturnNode(stack.pop()));
                case CALL_SUPER -> {
                    List<ExpressionNode> args = new ArrayList<>();
                    for (int i = 0; i < inst[1]; i++)
                        args.add(stack.pop());
                    expandLinkedFlow(index, new Flow.CallSuperNode(args));
                }
                case USE -> expandLinkedFlow(index, new Flow.UseNode());
                case LOAD_NAME -> stack.push(new Expressions.LoadNameNode(inst[1]));
                case BREAK_POINT -> expandLinkedFlow(index, new Flow.BreakPointNode());
                
                case GOTO -> handleGoto(index, inst);
                case BRANCH_IF_TRUE, BRANCH_IF_FALSE -> handleBranch(opcode, index, inst);
            }
        }

        return enterNode;
    }

    private boolean isPathClose(int index){
        return nodeMap.containsKey(index);
    }

    private void handleSuddenPathClose(int index){
        FlowNode next = nodeMap.get(index);

        if (current == null){
            current = (LinkedFlowNode) next;
        }
        else if (next instanceof Flow.PathEndNode endNode){
            current.setNext(next);
            current = endNode;
        }
        else {
            Flow.PathEndNode endNode = new Flow.PathEndNode(null);
            current.setNext(endNode);
            current = endNode;

            if (next instanceof Flow.BeginNode beginNode){
                beginNode.setNext(endNode);
            }
        }
    }

    private void handleGoto(int index, byte[] inst){
        int targetAddress = intFromSecond2Bytes(inst);

        if (index == targetAddress){
            // endless loop
            Flow.GotoNode gotoNode = new Flow.GotoNode(null);
            gotoNode.setNext(gotoNode);
            expandLinkedFlow(index, gotoNode);
            return;
        }

        if (index + 1 == targetAddress){
            // one jump forward has no effect and can be ignored
            return;
        }

        FlowNode next = nodeMap.get(targetAddress);

        if (next != null){
            Flow.GotoNode gotoNode = new Flow.GotoNode(null);
            gotoNode.setNext(next);
            expandLinkedFlow(index, gotoNode);
        }
        else {
            Flow.PathEndNode pathEndNode = new Flow.PathEndNode(null);
            nodeMap.put(targetAddress, pathEndNode);

            Flow.GotoNode gotoNode = new Flow.GotoNode(null);
            gotoNode.setNext(pathEndNode);
            expandLinkedFlow(index, gotoNode);

            current = null;
        }
    }


    private void handleBranch(Opcode opcode, int index, byte[] inst){
        int targetAddress = intFromSecond2Bytes(inst);
        int offs = stack.element().accept(new InstructionSizeCalculator());

        if (index == targetAddress + offs){
            handleSelfBranch(opcode, index);
            return;
        }

        if (index + 1 == targetAddress){
            // one jump forward has no effect and can be ignored
            return;
        }

        FlowNode jumpTargetNode = nodeMap.get(targetAddress);

        if (jumpTargetNode != null){
            handleKnownTargetBranch(opcode, index, jumpTargetNode);
        }
        else {
            handleUnknownTargetBranch(opcode, index, targetAddress);
        }

    }

    private void handleSelfBranch(Opcode opcode, int index){
        Flow.BranchNode branchNode = new Flow.BranchNode(stack.pop());

        Flow.GotoNode gotoNode = new Flow.GotoNode(null);
        gotoNode.setNext(branchNode);

        Flow.BeginNode beginNode = new Flow.BeginNode();


        if (opcode == Opcode.BRANCH_IF_TRUE){
            // while true
            branchNode.T = gotoNode;
            branchNode.F = beginNode;
        }
        else {
            // while false
            branchNode.T = beginNode;
            branchNode.F = gotoNode;
        }

        nodeMap.put(index, branchNode);
        current.setNext(branchNode);
        current = beginNode;

    }

    private void handleKnownTargetBranch(Opcode opcode, int index, FlowNode jumpTargetNode){
        Flow.GotoNode gotoNode = new Flow.GotoNode(null);
        gotoNode.setNext(jumpTargetNode);

        Flow.BeginNode beginNode = new Flow.BeginNode();

        Flow.BranchNode branchNode = new Flow.BranchNode(stack.pop());

        if (opcode == Opcode.BRANCH_IF_TRUE){
            // if true
            branchNode.T = gotoNode;
            branchNode.F = beginNode;
        }
        else {
            // if false
            branchNode.T = beginNode;
            branchNode.F = gotoNode;
        }

        nodeMap.put(index, branchNode);
        current.setNext(branchNode);
        current = beginNode;
    }


    private void handleUnknownTargetBranch(Opcode opcode, int index, int targetAddress){
        Flow.BranchNode branchNode = new Flow.BranchNode(stack.pop());

        Flow.GotoNode gotoNode = new Flow.GotoNode(null);
        Flow.BeginNode gotoBeginNode = new Flow.BeginNode();
        gotoNode.setNext(gotoBeginNode);

        Flow.BeginNode beginNode = new Flow.BeginNode();

        if (opcode == Opcode.BRANCH_IF_FALSE){
            // if true
            branchNode.T = beginNode;
            branchNode.F = gotoNode;
        }
        else {
            // if false
            branchNode.F = beginNode;
            branchNode.T = gotoNode;
        }

        nodeMap.put(index, branchNode);
        nodeMap.put(targetAddress, gotoBeginNode);
        current.setNext(branchNode);
        current = beginNode;
    }

}
