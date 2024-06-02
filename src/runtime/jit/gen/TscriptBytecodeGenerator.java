package runtime.jit.gen;

import runtime.core.Data;
import runtime.core.Pool;
import runtime.core.VirtualFunction;
import runtime.jit.graph.*;
import runtime.type.Callable;
import tscriptc.generation.Opcode;
import tscriptc.util.Conversion;

import java.util.*;

public class TscriptBytecodeGenerator implements Generator, FlowWalker<Void, Void>, ExpressionVisitor<Void, Void> {

    private final VirtualFunction old;

    private final List<Instruction> instructions = new ArrayList<>();

    private final Map<FlowNode, Integer> addresses = new HashMap<>();

    private final Deque<Flow.PathEndNode> pathEndStack = new ArrayDeque<>();


    public TscriptBytecodeGenerator(VirtualFunction old) {
        this.old = old;
    }

    @Override
    public Callable generate(FlowNode graph) {
        graph.accept(this);
        printAll();
        return new OptimizedVirtualFunction(old.getName(), convert(), old.getStackSize(), old.getLocals(), old.getParameters(), old.getPool());
    }

    private byte[][] convert(){
        byte[][] as2DBytecode = new byte[instructions.size()][];

        for (int i = 0; i < as2DBytecode.length; i++) {
            Instruction instruction = instructions.get(i);

            byte[] raw = new byte[instruction.extras.length + 1];
            System.arraycopy(instruction.extras, 0, raw, 1, instruction.extras.length);
            raw[0] = instruction.opcode.b;

            as2DBytecode[i] = raw;
        }

        return as2DBytecode;
    }

    private void printAll(){
        for (Instruction inst : instructions){
            System.out.print(inst.opcode);
            for (int i = 1; i < inst.extras.length; i++){
                System.out.print(" " + inst.extras[i]);
            }
            System.out.println();
        }
    }

    private Void addInstruction(Opcode opcode, byte... bytes){
        instructions.add(new Instruction(opcode, bytes));
        return null;
    }

    private Void addInstruction(int index, Opcode opcode, byte... bytes){
        instructions.add(index, new Instruction(opcode, bytes));
        return null;
    }

    private Void addInstruction(Opcode opcode, int... extras){
        return addInstruction(instructions.size(), opcode, extras);
    }

    private Void addInstruction(int index, Opcode opcode, int... extras){
        byte[] extended = new byte[extras.length];
        for (int i = 0; i < extras.length; i++) {
            extended[i] = (byte) extras[i];
        }
        return addInstruction(index, opcode, extended);
    }


    private void setAddress(FlowNode node) {
        addresses.put(node, instructions.size());
    }




    @Override
    public Void visitStartNode(Flow.StartNode startNode, Void unused) {
        setAddress(startNode);
        return startNode.getNext().accept(this);
    }

    @Override
    public Void visitBranchNode(Flow.BranchNode branchNode, Void unused) {
        if (addresses.containsKey(branchNode)) return null;

        setAddress(branchNode);
        branchNode.condition.accept(this);

        int branchPos = instructions.size();

        branchNode.T.accept(this);

        Instruction branchInst = new Instruction(Opcode.BRANCH_IF_FALSE, Conversion.toJumpAddress(instructions.size() + 1));

        int gotoPos = instructions.size();
        branchNode.F.accept(this);

        if (gotoPos != instructions.size()) {
            instructions.add(gotoPos, new Instruction(Opcode.GOTO, Conversion.toJumpAddress(instructions.size() + 2)));
            branchInst.extras = Conversion.toJumpAddress(gotoPos + 2);
        }

        instructions.add(branchPos, branchInst);

        pathEndStack.pop().getNext().accept(this);
        return null;
    }

    @Override
    public Void visitBeginNode(Flow.BeginNode beginNode, Void unused) {
        return beginNode.getNext().accept(this);
    }

    @Override
    public Void visitGotoNode(Flow.GotoNode gotoNode, Void unused) {
        if (addresses.containsKey(gotoNode)) return null;
        setAddress(gotoNode);

        FlowNode next = gotoNode.getNext();

        if (next == gotoNode){
            addInstruction(Opcode.GOTO, Conversion.toJumpAddress(instructions.size()));
            return null;
        }

        return next.accept(this);
    }

    private final Set<Flow.PathEndNode> visitedEnds = new HashSet<>();

    @Override
    public Void visitPathEndNode(Flow.PathEndNode pathEndNode, Void unused) {

        if (visitedEnds.contains(pathEndNode)){
            // all branch paths visited
            visitedEnds.remove(pathEndNode);
            pathEndStack.push(pathEndNode);
            return null;
        }

        // Track back
        // only first branch pah visited
        visitedEnds.add(pathEndNode);
        return null;
    }

    @Override
    public Void visitLoadParameterNode(Flow.LoadParameterNode loadParameterNode, Void unused) {
        setAddress(loadParameterNode);
        return addInstruction(Opcode.STORE_LOCAL, loadParameterNode.address);
    }

    @Override
    public Void visitExpressionStatementNode(Flow.ExpressionStatementNode expressionStatementNode, Void unused) {
        setAddress(expressionStatementNode);
        expressionStatementNode.expression.accept(this);
        addInstruction(Opcode.POP);
        return expressionStatementNode.getNext().accept(this);
    }

    @Override
    public Void visitThrowNode(Flow.ThrowNode throwNode, Void unused) {
        setAddress(throwNode);
        throwNode.accept(this);
        return addInstruction(Opcode.POP);
    }

    @Override
    public Void visitTryStartNode(Flow.TryStartNode tryStartNode, Void unused) {
        return null;
    }

    @Override
    public Void visitTryEndNode(Flow.TryEndNode tryEndNode, Void unused) {
        return null;
    }

    @Override
    public Void visitCatchStartNode(Flow.CatchStartNode catchStartNode, Void unused) {
        return null;
    }

    @Override
    public Void visitCatchEndNode(Flow.CatchEndNode catchEndNode, Void unused) {
        return null;
    }

    @Override
    public Void visitReturnNode(Flow.ReturnNode returnNode, Void unused) {
        setAddress(returnNode);
        returnNode.expression.accept(this);
        return addInstruction(Opcode.RETURN);
    }

    @Override
    public Void visitNewLineNode(Flow.NewLineNode newLineNode, Void unused) {
        setAddress(newLineNode);
        addInstruction(Opcode.NEW_LINE, Conversion.getBytes(newLineNode.line));
        return newLineNode.getNext().accept(this);
    }

    @Override
    public Void visitStoreLocalNode(Flow.StoreLocalNode storeLocalNode, Void unused) {
        setAddress(storeLocalNode);
        storeLocalNode.expression.accept(this);
        return addInstruction(Opcode.STORE_LOCAL, storeLocalNode.address);
    }

    @Override
    public Void visitStoreGlobalNode(Flow.StoreGlobalNode storeGlobalNode, Void unused) {
        setAddress(storeGlobalNode);
        storeGlobalNode.expression.accept(this);
        return addInstruction(Opcode.STORE_GLOBAL, storeGlobalNode.address);
    }

    @Override
    public Void visitStoreStaticNode(Flow.StoreStaticNode storeStaticNode, Void unused) {
        setAddress(storeStaticNode);
        storeStaticNode.expression.accept(this);
        return addInstruction(Opcode.STORE_STATIC, storeStaticNode.address);
    }

    @Override
    public Void visitStoreMemberNode(Flow.StoreMemberNode storeMemberNode, Void unused) {
        setAddress(storeMemberNode);
        storeMemberNode.expression.accept(this);
        return addInstruction(Opcode.STORE_MEMBER, storeMemberNode.address);
    }

    @Override
    public Void visitStoreMemberFastNode(Flow.StoreMemberFastNode storeMemberFastNode, Void unused) {
        setAddress(storeMemberFastNode);
        storeMemberFastNode.expression.accept(this);
        return addInstruction(Opcode.STORE_MEMBER_FAST, storeMemberFastNode.address);
    }

    @Override
    public Void visitBreakPointNode(Flow.BreakPointNode breakPointNode, Void unused) {
        setAddress(breakPointNode);
        return addInstruction(Opcode.BREAK_POINT);
    }

    @Override
    public Void visitContainerWriteNode(Flow.ContainerWriteNode containerWriteNode, Void unused) {
        setAddress(containerWriteNode);
        containerWriteNode.value.accept(this);
        containerWriteNode.key.accept(this);
        containerWriteNode.container.accept(this);
        return addInstruction(Opcode.CONTAINER_WRITE);
    }

    @Override
    public Void visitUseNode(Flow.UseNode useNode, Void unused) {
        setAddress(useNode);
        return addInstruction(Opcode.USE);
    }

    @Override
    public Void visitCallSuperNode(Flow.CallSuperNode callSuperNode, Void unused) {
        return null;
    }




    /* ------------------------- Expressions ------------------------- */




    @Override
    public Void visitNullValueNode(Expressions.NullValueNode nullValueNode, Void unused) {
        return addInstruction(Opcode.PUSH_NULL);
    }

    @Override
    public Void visitIntegerValueNode(Expressions.IntegerNode integerNode, Void unused) {
        return addInstruction(Opcode.PUSH_INT, integerNode.get());
    }

    @Override
    public Void visitBooleanValueNode(Expressions.BooleanNode booleanNode, Void unused) {
        return addInstruction(Opcode.PUSH_BOOL, booleanNode.get() ? 1 : 0);
    }

    @Override
    public Void visitThisNode(Expressions.ThisNode thisNode, Void unused) {
        return addInstruction(Opcode.PUSH_THIS);
    }

    @Override
    public Void visitConstantNode(Expressions.ConstantNode constantNode, Void unused) {
        return addInstruction(Opcode.LOAD_CONST, constantNode.address);
    }

    @Override
    public Void visitLoadLocalNode(Expressions.LoadLocalNode loadLocalNode, Void unused) {
        return addInstruction(Opcode.LOAD_LOCAL, loadLocalNode.address);
    }

    @Override
    public Void visitLoadGlobalNode(Expressions.LoadGlobalNode loadGlobalNode, Void unused) {
        return addInstruction(Opcode.LOAD_GLOBAL, loadGlobalNode.address);
    }

    @Override
    public Void visitLoadMemberNode(Expressions.LoadMemberNode loadMemberNode, Void unused) {
        return addInstruction(Opcode.LOAD_MEMBER, loadMemberNode.address);
    }

    @Override
    public Void visitLoadMemberFastNode(Expressions.LoadMemberFastNode loadMemberFastNode, Void unused) {
        return addInstruction(Opcode.LOAD_MEMBER_FAST, loadMemberFastNode.address);
    }

    @Override
    public Void visitLoadStaticNode(Expressions.LoadStaticNode loadStaticNode, Void unused) {
        return addInstruction(Opcode.LOAD_STATIC, loadStaticNode.address);
    }

    @Override
    public Void visitLoadAbstractImplNode(Expressions.LoadAbstractImplNode loadAbstractImplNode, Void unused) {
        return addInstruction(Opcode.LOAD_ABSTRACT_IMPL, loadAbstractImplNode.address);
    }

    @Override
    public Void visitLoadNameNode(Expressions.LoadNameNode loadNameNode, Void unused) {
        return addInstruction(Opcode.LOAD_NAME, loadNameNode.address);
    }

    @Override
    public Void visitContainerReadNode(Expressions.ContainerReadNode containerReadNode, Void unused) {
        containerReadNode.container.accept(this);
        containerReadNode.key.accept(this);
        return addInstruction(Opcode.CONTAINER_READ);
    }

    @Override
    public Void visitArgumentNode(Expressions.ArgumentNode argumentNode, Void unused) {
        argumentNode.value.accept(this);
        return addInstruction(Opcode.WRAP_ARGUMENT, argumentNode.address);
    }

    @Override
    public Void visitBinaryOperationNode(Expressions.BinaryOperationNode binaryOperationNode, Void unused) {
        binaryOperationNode.left.accept(this);
        binaryOperationNode.right.accept(this);
        return addInstruction(binaryOperationNode.operation);
    }

    @Override
    public Void visitUnaryOperationNode(Expressions.UnaryOperationNode unaryOperationNode, Void unused) {
        unaryOperationNode.expression.accept(this);
        return addInstruction(unaryOperationNode.operation);
    }

    @Override
    public Void visitGetTypeNode(Expressions.GetTypeNode getTypeNode, Void unused) {
        getTypeNode.expression.accept(this);
        return addInstruction(Opcode.GET_TYPE);
    }

    @Override
    public Void visitRangeNode(Expressions.RangeNode rangeNode, Void unused) {
        rangeNode.from.accept(this);
        rangeNode.to.accept(this);
        return addInstruction(Opcode.MAKE_RANGE);
    }

    @Override
    public Void visitArrayNode(Expressions.ArrayNode arrayNode, Void unused) {
        for (ExpressionNode exp : arrayNode.content)
            exp.accept(this);
        return addInstruction(Opcode.MAKE_ARRAY, arrayNode.content.size());
    }

    @Override
    public Void visitDictNode(Expressions.DictNode dictNode, Void unused) {
        for (int i = 0; i < dictNode.keys.size(); i++){
            dictNode.values.get(i).accept(this);
            dictNode.keys.get(i).accept(this);
        }
        return addInstruction(Opcode.MAKE_DICT, dictNode.keys.size());
    }

    @Override
    public Void visitCallNode(Expressions.CallNode callNode, Void unused) {
        for (int i = callNode.arguments.size()-1; i >= 0; i--)
            callNode.arguments.get(i).accept(this);
        callNode.called.accept(this);
        return addInstruction(Opcode.CALL, callNode.arguments.size());
    }


    private static class OptimizedVirtualFunction extends VirtualFunction {

        public OptimizedVirtualFunction(String name, byte[][] instructions, int stackSize, int locals, LinkedHashMap<String, Data> params, Pool pool) {
            super(name, instructions, stackSize, locals, params, pool);
        }

        @Override
        public boolean isHot() {
            // avoid recursive optimizations
            return false;
        }
    }

    private static class Instruction {
        public Opcode opcode;
        public byte[] extras;
        public Instruction(Opcode opcode, byte[] extras) {
            this.opcode = opcode;
            this.extras = extras;
        }
    }
}
