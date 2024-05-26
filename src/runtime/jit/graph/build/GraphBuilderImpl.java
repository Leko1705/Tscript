package runtime.jit.graph.build;

import runtime.jit.graph.ExpressionNode;
import runtime.jit.graph.FlowNode;
import tscriptc.generation.Opcode;
import tscriptc.util.Conversion;

import java.util.*;

import static runtime.jit.graph.Flow.*;
import static runtime.jit.graph.Expressions.*;

public class GraphBuilderImpl implements GraphBuilder {

    private final FlowNode enterNode;
    private FlowNode current;
    private final Deque<ExpressionNode> stack = new ArrayDeque<>();
    private final NodeMap nodeMap = new NodeMap();


    public GraphBuilderImpl() {
        enterNode = new StartNode();
        current = enterNode;
    }

    private int skipParameters(byte[][] code){
        int index = 0;
        Opcode opcode = Opcode.of(code[index][0]);
        while (opcode == Opcode.STORE_LOCAL){
            expandFlow(index, new LoadParameterNode(code[index][1]));
            index++;
            opcode = Opcode.of(code[index][0]);
        }
        return index;
    }


    @Override
    public FlowNode build(byte[][] code) {
        int index = skipParameters(code);

        nodeMap.put(index, current);
        for (; index < code.length; index++) {
            byte[] inst = code[index];
            Opcode opcode = Opcode.of(inst[0]);


            if (nodeMap.get(index) != null){
                FlowNode next = nodeMap.get(index);
                current.setNext(next);
                current = next;
            }

            switch (opcode){
                case PUSH_NULL -> stack.push(new NullValueNode());
                case PUSH_INT -> stack.push(new IntegerNode(inst[1]));
                case PUSH_BOOL -> stack.push(new BooleanNode(inst[1] == 1));
                case LOAD_CONST -> stack.push(new ConstantNode(inst[1]));
                case PUSH_THIS -> stack.push(new ThisNode());

                case POP -> expandFlow(index, new ExpressionStatementNode(stack.pop()));
                case NEW_LINE -> expandFlow(index, new NewLineNode(Conversion.fromBytes(inst[1], inst[2], inst[3], inst[4])));

                case LOAD_GLOBAL -> stack.push(new LoadGlobalNode(inst[1]));
                case STORE_GLOBAL -> expandFlow(index, new StoreGlobalNode(inst[1], stack.pop()));

                case LOAD_LOCAL -> stack.push(new LoadLocalNode(inst[1]));
                case STORE_LOCAL -> expandFlow(index, new StoreLocalNode(inst[1], stack.pop()));

                case LOAD_MEMBER_FAST -> stack.push(new LoadMemberFastNode(inst[1]));
                case STORE_MEMBER_FAST -> expandFlow(index, new StoreMemberFastNode(inst[1], stack.pop()));

                case LOAD_STATIC -> stack.push(new LoadStaticNode(inst[1]));
                case STORE_STATIC -> expandFlow(index, new StoreStaticNode(inst[1], stack.pop()));

                case LOAD_MEMBER -> stack.push(new LoadMemberNode(inst[1]));
                case STORE_MEMBER -> expandFlow(index, new StoreMemberNode(inst[1], stack.pop()));

                case CONTAINER_READ -> {
                    ExpressionNode container = stack.pop();
                    ExpressionNode key = stack.pop();
                    stack.push(new ContainerReadNode(container, key));
                }
                case CONTAINER_WRITE -> {
                    ExpressionNode container = stack.pop();
                    ExpressionNode key = stack.pop();
                    ExpressionNode value = stack.pop();
                    expandFlow(index, new ContainerWriteNode(container, key, value));
                }

                case LOAD_ABSTRACT_IMPL -> stack.push(new LoadAbstractImplNode(inst[1]));

                case ADD, SUB, MUL, DIV, IDIV, MOD, POW, AND, OR, XOR,
                     LT, GT, LEQ, GEQ, SLA, SRA, SRL, EQUALS, NOT_EQUALS -> {
                    ExpressionNode right = stack.pop();
                    ExpressionNode left = stack.pop();
                    stack.push(new BinaryOperationNode(opcode, left, right));
                }
                case NOT, NEG, POS -> stack.push(new UnaryOperationNode(opcode, stack.pop()));
                case GET_TYPE -> stack.push(new GetTypeNode(stack.pop()));
                case MAKE_ARRAY -> {
                    List<ExpressionNode> content = new ArrayList<>();
                    for (int i = 0; i < inst[1]; i++)
                        content.add(stack.pop());
                    stack.push(new ArrayNode(content));
                }
                case MAKE_DICT -> {
                    List<ExpressionNode> keys = new ArrayList<>();
                    List<ExpressionNode> values = new ArrayList<>();
                    for (int i = 0; i < inst[1]; i++){
                        keys.add(stack.pop());
                        values.add(stack.pop());
                    }
                    stack.push(new DictNode(keys, values));
                }
                case MAKE_RANGE -> {
                    ExpressionNode to = stack.pop();
                    ExpressionNode from = stack.pop();
                    stack.push(new RangeNode(from, to));
                }
                case GET_ITR -> {
                }
                case BRANCH_ITR -> {
                }
                case ITR_NEXT -> {
                }
                case ENTER_TRY -> expandFlow(index, new TryStartNode());
                case LEAVE_TRY -> expandFlow(index, new TryEndNode());
                case THROW -> expandFlow(index, new ThrowNode(stack.pop()));
                case CALL -> {
                    ExpressionNode called = stack.pop();
                    List<ExpressionNode> args = new ArrayList<>();
                    for (int i = 0; i < inst[1]; i++){
                        args.add(stack.pop());
                    }
                    stack.push(new CallNode(called, args));
                }
                case WRAP_ARGUMENT -> stack.push(new ArgumentNode(inst[1], stack.pop()));
                case RETURN -> expandFlow(index, new ReturnNode(stack.pop()));
                case CALL_SUPER -> {
                    List<ExpressionNode> args = new ArrayList<>();
                    for (int i = 0; i < inst[1]; i++)
                        args.add(stack.pop());
                    expandFlow(index, new CallSuperNode(args));
                }
                case USE -> expandFlow(index, new UseNode());
                case LOAD_NAME -> stack.push(new LoadNameNode(inst[1]));
                case BREAK_POINT -> expandFlow(index, new BreakPointNode());

                case GOTO -> {
                    int address = intFrom2Bytes(inst);

                    if (address < index){
                        // TODO later -> never generated by the tscript compiler
                    }
                    else if (address == index){
                        // build the infinite loop
                        LoopBeginNode infLoopStart = new LoopBeginNode();
                        current.setNext(infLoopStart);
                        LoopEndNode infLoopEnd = new LoopEndNode();
                        infLoopStart.setNext(infLoopEnd);
                        current = infLoopEnd;
                    }
                    else { // address >= index
                        MergeNode mergeNode = new MergeNode();
                        current.setNext(mergeNode);
                        nodeMap.put(address, mergeNode);
                        while (nodeMap.get(index) != null){
                            current = nodeMap.get(++index);
                        }
                    }
                }

                case BRANCH_IF_TRUE, BRANCH_IF_FALSE -> handleBranchInstruction(index, opcode, inst);
            }
        }

        return enterNode;
    }

    private void handleBranchInstruction(int index, Opcode opcode, byte[] inst){
        boolean branchOnTrue = opcode == Opcode.BRANCH_IF_TRUE;

        IfNode ifNode = new IfNode(stack.pop(), opcode);
        current.setNext(ifNode);
        int address = intFrom2Bytes(inst);

        if (address < index){

            LoopEndNode loopEndNode = new LoopEndNode();
            if (branchOnTrue)
                ifNode.T = loopEndNode;
            else
                ifNode.F = loopEndNode;

            FlowNode target = nodeMap.get(address);
            for (int i = 0; target == null; i++) target = nodeMap.get(address - i);
            LoopBeginNode loopBeginNode = new LoopBeginNode();
            FlowNode next = target.getNext();
            target.setNext(loopBeginNode);
            loopBeginNode.setNext(next);

            loopEndNode.setNext(loopBeginNode);

            LoopExitNode exitNode = new LoopExitNode();

            if (branchOnTrue)
                ifNode.F = exitNode;
            else
                ifNode.T = exitNode;
            current = exitNode;

            updateLoopContent(new Range(address, index));

            ifNode.branchOpcode = ifNode.branchOpcode == Opcode.BRANCH_IF_TRUE
                    ? Opcode.BRANCH_IF_FALSE
                    : Opcode.BRANCH_IF_TRUE;
        }

        else if (address > index){
            if (!branchOnTrue){
                ifNode.F = new MergeNode();
                nodeMap.put(address, ifNode.F);
                ifNode.T = new StartNode();
                current = ifNode.T;
                nodeMap.put(index, ifNode.T);
            }
            else {
                ifNode.T = new MergeNode();
                nodeMap.put(address, ifNode.T);
                ifNode.F = new StartNode();
                current = ifNode.F;
                nodeMap.put(index, ifNode.F);
            }
        }

        else {
            throw new IllegalStateException("requires a value to check for isTrue");
        }
    }

    private void updateLoopContent(Range range){
        for (int i = range.from; i <= range.to; i++){
            FlowNode node = nodeMap.get(i);
            if (node == null) return;
        }
    }

    private void expandFlow(int index, FlowNode node){
        nodeMap.put(index, node);
        current.setNext(node);
        current = node;
    }

    private int intFrom2Bytes(byte[] bytes){
        return jumpAddress(bytes[1], bytes[2]);
    }

    private int jumpAddress(byte b1, byte b2){
        return ((b1 & 0xff) << 8) | (b2 & 0xff);
    }



    private record Range(int from, int to){
        public boolean contains(int i){
            return i >= from && i <= to;
        }
    }


    private static class NodeMap {
        private final Map<Integer, FlowNode> int2Node = new HashMap<>();
        private final Map<FlowNode, Integer> node2Int = new HashMap<>();


        public void put(int index, FlowNode node) {
            int2Node.put(index, node);
            node2Int.put(node, index);
        }

        public FlowNode get(int index) {
            return int2Node.get(index);
        }

        public int get(FlowNode node) {
            return node2Int.get(node);
        }
    }

}
