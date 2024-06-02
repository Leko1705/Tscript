package runtime.jit.graph.build;

import runtime.jit.graph.ExpressionNode;
import runtime.jit.graph.Expressions;
import runtime.jit.graph.Flow.*;
import runtime.jit.graph.FlowNode;
import tscriptc.generation.Opcode;
import tscriptc.util.Conversion;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class GraphBuilder2 implements GraphBuilder {

    private final FlowNode enterNode;
    private FlowNode current;

    private final Deque<ExpressionNode> stack = new ArrayDeque<>();
    private final NodeMap nodeMap = new NodeMapImpl();


    public GraphBuilder2() {
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


    private void expandFlow(int index, FlowNode node){
        current.setNext(node);
        current = node;
        nodeMap.add(node, index);
    }

    private int intFrom2Bytes(byte[] bytes){
        return jumpAddress(bytes[1], bytes[2]);
    }

    private int jumpAddress(byte b1, byte b2){
        return ((b1 & 0xff) << 8) | (b2 & 0xff);
    }


    @Override
    public FlowNode build(byte[][] code) {
        int index = skipParameters(code);

        for (; index < code.length; index++) {
            byte[] inst = code[index];
            Opcode opcode = Opcode.of(inst[0]);

            switch (opcode){
                case PUSH_NULL -> stack.push(new Expressions.NullValueNode());
                case PUSH_INT -> stack.push(new Expressions.IntegerNode(inst[1]));
                case PUSH_BOOL -> stack.push(new Expressions.BooleanNode(inst[1] == 1));
                case LOAD_CONST -> stack.push(new Expressions.ConstantNode(inst[1]));
                case PUSH_THIS -> stack.push(new Expressions.ThisNode());

                case POP -> expandFlow(index, new ExpressionStatementNode(stack.pop()));
                case NEW_LINE -> expandFlow(index, new NewLineNode(Conversion.fromBytes(inst[1], inst[2], inst[3], inst[4])));

                case LOAD_GLOBAL -> stack.push(new Expressions.LoadGlobalNode(inst[1]));
                case STORE_GLOBAL -> expandFlow(index, new StoreGlobalNode(inst[1], stack.pop()));

                case LOAD_LOCAL -> stack.push(new Expressions.LoadLocalNode(inst[1]));
                case STORE_LOCAL -> expandFlow(index, new StoreLocalNode(inst[1], stack.pop()));

                case LOAD_MEMBER_FAST -> stack.push(new Expressions.LoadMemberFastNode(inst[1]));
                case STORE_MEMBER_FAST -> expandFlow(index, new StoreMemberFastNode(inst[1], stack.pop()));

                case LOAD_STATIC -> stack.push(new Expressions.LoadStaticNode(inst[1]));
                case STORE_STATIC -> expandFlow(index, new StoreStaticNode(inst[1], stack.pop()));

                case LOAD_MEMBER -> stack.push(new Expressions.LoadMemberNode(inst[1]));
                case STORE_MEMBER -> expandFlow(index, new StoreMemberNode(inst[1], stack.pop()));

                case CONTAINER_READ -> {
                    ExpressionNode container = stack.pop();
                    ExpressionNode key = stack.pop();
                    stack.push(new Expressions.ContainerReadNode(container, key));
                }
                case CONTAINER_WRITE -> {
                    ExpressionNode container = stack.pop();
                    ExpressionNode key = stack.pop();
                    ExpressionNode value = stack.pop();
                    expandFlow(index, new ContainerWriteNode(container, key, value));
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
                case ENTER_TRY -> expandFlow(index, new TryStartNode());
                case LEAVE_TRY -> expandFlow(index, new TryEndNode());
                case THROW -> expandFlow(index, new ThrowNode(stack.pop()));
                case CALL -> {
                    ExpressionNode called = stack.pop();
                    List<ExpressionNode> args = new ArrayList<>();
                    for (int i = 0; i < inst[1]; i++){
                        args.add(stack.pop());
                    }
                    stack.push(new Expressions.CallNode(called, args));
                }
                case WRAP_ARGUMENT -> stack.push(new Expressions.ArgumentNode(inst[1], stack.pop()));
                case RETURN -> expandFlow(index, new ReturnNode(stack.pop()));
                case CALL_SUPER -> {
                    List<ExpressionNode> args = new ArrayList<>();
                    for (int i = 0; i < inst[1]; i++)
                        args.add(stack.pop());
                    expandFlow(index, new CallSuperNode(args));
                }
                case USE -> expandFlow(index, new UseNode());
                case LOAD_NAME -> stack.push(new Expressions.LoadNameNode(inst[1]));
                case BREAK_POINT -> expandFlow(index, new BreakPointNode());

                case GOTO -> handleGoto(index, inst);

                case BRANCH_IF_TRUE, BRANCH_IF_FALSE -> handleBranch(opcode, index, inst);
            }
        }

        return enterNode;
    }


    private void handleGoto(int index, byte[] inst){
        
    }

    private void handleBranch(Opcode opcode, int index, byte[] inst){

    }


}
