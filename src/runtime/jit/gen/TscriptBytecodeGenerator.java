package runtime.jit.gen;

import runtime.core.Data;
import runtime.core.Pool;
import runtime.core.VirtualFunction;
import runtime.jit.graph.*;
import runtime.type.Callable;
import tscriptc.generation.Opcode;
import tscriptc.util.Conversion;

import java.util.*;

import static runtime.jit.graph.Flow.*;
import static runtime.jit.graph.Expressions.*;

public class TscriptBytecodeGenerator
        implements Generator, FlowWalker<Integer, Void>, ExpressionVisitor<Void, Void> {

    private final VirtualFunction fun;

    private final List<byte[]> code;

    private final Map<FlowNode, Integer> loopScopes = new HashMap<>();

    private final Map<FlowNode, Integer> visited = new HashMap<>();


    public TscriptBytecodeGenerator(VirtualFunction fun) {
        this.fun = fun;
        code = new ArrayList<>();
    }

    @Override
    public Callable generate(FlowNode graph) {
        scan(graph);
        byte[][] code = this.code.toArray(new byte[0][]);
        printCode(code);
        return new OptimizedVirtualFunction(
                fun.getName(),
                code,
                fun.getStackSize(),
                fun.getLocals(),
                fun.getParameters(),
                fun.getPool());
    }

    private void printCode(byte[][] code) {
        for (byte[] instr : code){
            byte[] args = new byte[instr.length-1];
            System.arraycopy(instr, 1, args, 0, args.length);
            printInstruction(Opcode.of(instr[0]), args);
        }
    }

    private void printInstruction(Opcode opcode, byte... args){
        System.out.print(opcode);
        for (byte b : args)
            System.out.print(" " + b);
        System.out.println();
    }

    private byte[] addInstruction(Opcode opcode, byte... args){
        return addInstruction(code.size(), opcode, args);
    }

    private byte[] addInstruction(int index, Opcode opcode, byte... args){
        byte[] instr = new byte[args.length + 1];
        System.arraycopy(args, 0, instr, 1, args.length);
        instr[0] = opcode.b;
        code.add(index, instr);
        return instr;
    }

    private byte[] getLastInstruction(){
        if (code.isEmpty()) return null;
        return code.get(code.size()-1);
    }

    private void putJumpAddress(byte[] inst, int addr){
        byte[] addrInBytes = Conversion.toJumpAddress(addr);
        System.arraycopy(addrInBytes, 0, inst, 1, addrInBytes.length);
    }

    private Void scan(ExpressionNode node){
        if (node != null) return node.accept(this, null);
        return null;
    }


    private Integer scan(FlowNode node){
        if (node != null) return node.accept(this, null);
        return null;
    }

    @Override
    public Void visitNullValueNode(NullValueNode nullValueNode, Void unused) {
        addInstruction(Opcode.PUSH_NULL);
        return null;
    }

    @Override
    public Void visitIntegerValueNode(IntegerNode integerNode, Void unused) {
        addInstruction(Opcode.PUSH_INT, integerNode.get().byteValue());
        return null;
    }

    @Override
    public Void visitBooleanValueNode(BooleanNode booleanNode, Void unused) {
        addInstruction(Opcode.PUSH_BOOL, (byte) (booleanNode.get() ? 1 : 0));
        return null;
    }

    @Override
    public Void visitThisNode(ThisNode thisNode, Void unused) {
        addInstruction(Opcode.PUSH_THIS);
        return null;
    }

    @Override
    public Void visitConstantNode(ConstantNode constantNode, Void unused) {
        addInstruction(Opcode.LOAD_CONST, (byte) constantNode.address);
        return null;
    }

    @Override
    public Void visitLoadLocalNode(LoadLocalNode loadLocalNode, Void unused) {
        addInstruction(Opcode.LOAD_LOCAL, (byte) loadLocalNode.address);
        return null;
    }

    @Override
    public Void visitLoadGlobalNode(LoadGlobalNode loadGlobalNode, Void unused) {
        addInstruction(Opcode.LOAD_GLOBAL, (byte) loadGlobalNode.address);
        return null;
    }

    @Override
    public Void visitLoadMemberNode(LoadMemberNode loadMemberNode, Void unused) {
        addInstruction(Opcode.LOAD_MEMBER, (byte) loadMemberNode.address);
        return null;
    }

    @Override
    public Void visitLoadMemberFastNode(LoadMemberFastNode loadMemberFastNode, Void unused) {
        addInstruction(Opcode.LOAD_MEMBER_FAST, (byte) loadMemberFastNode.address);
        return null;
    }

    @Override
    public Void visitLoadStaticNode(LoadStaticNode loadStaticNode, Void unused) {
        addInstruction(Opcode.LOAD_STATIC, (byte) loadStaticNode.address);
        return null;
    }

    @Override
    public Void visitLoadAbstractImplNode(LoadAbstractImplNode loadAbstractImplNode, Void unused) {
        addInstruction(Opcode.LOAD_ABSTRACT_IMPL, (byte) loadAbstractImplNode.address);
        return null;
    }

    @Override
    public Void visitLoadNameNode(LoadNameNode loadNameNode, Void unused) {
        addInstruction(Opcode.LOAD_NAME, (byte) loadNameNode.address);
        return null;
    }

    @Override
    public Void visitContainerReadNode(ContainerReadNode containerReadNode, Void unused) {
        scan(containerReadNode.key);
        scan(containerReadNode.container);
        addInstruction(Opcode.CONTAINER_READ);
        return null;
    }

    @Override
    public Void visitArgumentNode(ArgumentNode argumentNode, Void unused) {
        scan(argumentNode.value);
        addInstruction(Opcode.WRAP_ARGUMENT, (byte) argumentNode.address);
        return null;
    }

    @Override
    public Void visitBinaryOperationNode(BinaryOperationNode binaryOperationNode, Void unused) {
        scan(binaryOperationNode.left);
        scan(binaryOperationNode.right);
        addInstruction(binaryOperationNode.operation);
        return null;
    }

    @Override
    public Void visitUnaryOperationNode(UnaryOperationNode unaryOperationNode, Void unused) {
        scan(unaryOperationNode.expression);
        addInstruction(unaryOperationNode.operation);
        return null;
    }

    @Override
    public Void visitGetTypeNode(GetTypeNode getTypeNode, Void unused) {
        scan(getTypeNode.expression);
        addInstruction(Opcode.GET_TYPE);
        return null;
    }

    @Override
    public Void visitRangeNode(RangeNode rangeNode, Void unused) {
        scan(rangeNode.from);
        scan(rangeNode.to);
        addInstruction(Opcode.MAKE_RANGE);
        return null;
    }

    @Override
    public Void visitArrayNode(ArrayNode arrayNode, Void unused) {
        List<ExpressionNode> content = arrayNode.content;
        for (int i = content.size()-1; i >= 0; i --)
            scan(content.get(i));
        addInstruction(Opcode.MAKE_ARRAY, (byte) arrayNode.content.size());
        return null;
    }

    @Override
    public Void visitDictNode(DictNode dictNode, Void unused) {
        for (int i = dictNode.keys.size()-1; i >= 0; i--){
            scan(dictNode.values.get(i));
            scan(dictNode.keys.get(i));
        }
        addInstruction(Opcode.MAKE_DICT, (byte) dictNode.keys.size());
        return null;
    }

    @Override
    public Void visitCallNode(CallNode callNode, Void unused) {
        List<ExpressionNode> args = callNode.arguments;
        for (int i = args.size()-1; i >= 0; i--)
            scan(args.get(i));
        scan(callNode.called);
        addInstruction(Opcode.CALL, (byte) callNode.arguments.size());
        return null;
    }



    @Override
    public Integer visitStartNode(StartNode startNode, Void unused) {
        return scan(startNode.next);
    }

    @Override
    public Integer visitBeginNode(BeginNode endNode, Void unused) {
        return 0;
    }

    @Override
    public Integer visitEndNode(EndNode endNode, Void unused) {
        return 0;
    }

    @Override
    public Integer visitLoadParameterNode(LoadParameterNode loadParameterNode, Void unused) {
        addInstruction(Opcode.STORE_LOCAL, (byte) loadParameterNode.address);
        return scan(loadParameterNode.next);
    }

    @Override
    public Integer visitExpressionStatementNode(ExpressionStatementNode statementNode, Void unused) {
        scan(statementNode.expression);
        addInstruction(Opcode.POP);
        scan(statementNode.next);
        return null;
    }

    @Override
    public Integer visitIfNode(IfNode ifNode, Void unused) {
        scan(ifNode.condition);

        Opcode opcode = ifNode.branchOpcode;
        byte[] instr = addInstruction(opcode, (byte) 0, (byte) 0);

        int address;
        if (opcode == Opcode.BRANCH_IF_FALSE){
            address = scan(ifNode.T);
        }
        else {
            address = scan(ifNode.F);
        }

        putJumpAddress(instr, address);

        if (opcode == Opcode.BRANCH_IF_FALSE){
            scan(ifNode.F);
        }
        else {
            scan(ifNode.T);
        }

        return null;
    }

    @Override
    public Integer visitMergeNode(MergeNode mergeNode, Void unused) {
        // TODO merge correctly (see visited- map field)
        return null;
    }

    @Override
    public Integer visitLoopBeginNode(LoopBeginNode loopBeginNode, Void unused) {
        loopScopes.put(loopBeginNode, code.size());
        scan(loopBeginNode.next);
        loopScopes.remove(loopBeginNode);
        return null;
    }

    @Override
    public Integer visitLoopEndNode(LoopEndNode loopEndNode, Void unused) {
        int address = loopScopes.get(loopEndNode.next);
        byte[] last = getLastInstruction();
        if (last != null && isBranchedJump(Opcode.of(last[0]))){
            int jumpAddr = Conversion.getJumpAddress(last[1], last[2]);
            code.remove(code.size()-1);
            addInstruction(getInvertedBranchedJump(Opcode.of(last[0])), Conversion.toJumpAddress(jumpAddr));
        }
        else {
            addInstruction(Opcode.GOTO, Conversion.toJumpAddress(address));
        }
        return code.size();
    }

    private static boolean isBranchedJump(Opcode opcode){
        return opcode == Opcode.BRANCH_IF_FALSE || opcode == Opcode.BRANCH_IF_TRUE;
    }

    private static Opcode getInvertedBranchedJump(Opcode opcode){
        return switch (opcode){
            case BRANCH_IF_FALSE -> Opcode.BRANCH_IF_TRUE;
            case BRANCH_IF_TRUE -> Opcode.BRANCH_IF_FALSE;
            default -> throw new IllegalStateException();
        };
    }

    @Override
    public Integer visitLoopExitNode(LoopExitNode loopExitNode, Void unused) {
        scan(loopExitNode.next);
        return code.size();
    }

    @Override
    public Integer visitTryStartNode(TryStartNode tryStartNode, Void unused) {
        return null;
    }

    @Override
    public Integer visitTryEndNode(TryEndNode tryEndNode, Void unused) {
        return null;
    }

    @Override
    public Integer visitCatchStartNode(CatchStartNode catchStartNode, Void unused) {
        return null;
    }

    @Override
    public Integer visitCatchEndNode(CatchEndNode catchEndNode, Void unused) {
        return null;
    }

    @Override
    public Integer visitThrowNode(ThrowNode throwNode, Void unused) {
        scan(throwNode.expression);
        addInstruction(Opcode.THROW);
        return scan(throwNode.next);
    }

    @Override
    public Integer visitReturnNode(ReturnNode returnNode, Void unused) {
        scan(returnNode.expression);
        addInstruction(Opcode.RETURN);
        return code.size();
    }

    @Override
    public Integer visitNewLineNode(NewLineNode newLineNode, Void unused) {
        final int value = newLineNode.line;
        addInstruction(Opcode.NEW_LINE,
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value);
        return scan(newLineNode.next);
    }

    @Override
    public Integer visitStoreLocalNode(StoreLocalNode storeLocalNode, Void unused) {
        scan(storeLocalNode.expression);
        addInstruction(Opcode.STORE_LOCAL, (byte) storeLocalNode.address);
        return scan(storeLocalNode.next);
    }

    @Override
    public Integer visitStoreStaticNode(StoreStaticNode storeStaticNode, Void unused) {
        scan(storeStaticNode.expression);
        addInstruction(Opcode.STORE_STATIC, (byte) storeStaticNode.address);
        return scan(storeStaticNode.next);
    }

    @Override
    public Integer visitStoreMemberNode(StoreMemberNode storeMemberNode, Void unused) {
        scan(storeMemberNode.expression);
        addInstruction(Opcode.STORE_MEMBER, (byte) storeMemberNode.address);
        return scan(storeMemberNode.next);
    }

    @Override
    public Integer visitStoreMemberFastNode(StoreMemberFastNode storeMemberFastNode, Void unused) {
        scan(storeMemberFastNode.expression);
        addInstruction(Opcode.STORE_MEMBER_FAST, (byte) storeMemberFastNode.address);
        return scan(storeMemberFastNode.next);
    }

    @Override
    public Integer visitContainerWriteNode(ContainerWriteNode containerWriteNode, Void unused) {
        scan(containerWriteNode.value);
        scan(containerWriteNode.key);
        scan(containerWriteNode.container);
        addInstruction(Opcode.CONTAINER_WRITE);
        return scan(containerWriteNode.next);
    }

    @Override
    public Integer visitBreakPointNode(BreakPointNode breakPointNode, Void unused) {
        addInstruction(Opcode.BREAK_POINT);
        return scan(breakPointNode.next);
    }

    @Override
    public Integer visitUseNode(UseNode useNode, Void unused) {
        addInstruction(Opcode.USE);
        return scan(useNode.next);
    }

    @Override
    public Integer visitCallSuperNode(CallSuperNode callSuperNode, Void unused) {
        List<ExpressionNode> args = callSuperNode.arguments;
        for (int i = args.size()-1; i >= 0; i--)
            scan(args.get(i));
        return scan(callSuperNode.next);
    }


    private static class OptimizedVirtualFunction extends VirtualFunction {

        public OptimizedVirtualFunction(String name, byte[][] instructions, int stackSize, int locals, LinkedHashMap<String, Data> params, Pool pool) {
            super(name, instructions, stackSize, locals, params, pool);
        }

        @Override
        public boolean isHot() {
            return false;
        }
    }

    private interface GenerateTask {
        void generate();
    }
}
