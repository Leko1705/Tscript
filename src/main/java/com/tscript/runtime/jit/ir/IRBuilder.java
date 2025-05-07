package com.tscript.runtime.jit.ir;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.ir.nodes.*;
import com.tscript.runtime.jit.caching.TypeCache;

import java.util.*;

public class IRBuilder {


    public static abstract class LimitExceededListener {

        private final int limit;

        public LimitExceededListener(int limit) {
            this.limit = limit;
        }

        public abstract void onLimitExceeded();

    }


    private final Deque<IRValue> stack;

    private final IRBlock root;

    private IRBlock block;

    private final Map<IRBlock, ArrayDeque<SSAState>> blockSsaState;


    private final Map<Integer, IRBlock> blocks;

    private int currentIp;

    private int size;

    private int nextFreeSsa = 0;

    private final LimitExceededListener limitExceededListener;


    private class SSAState {

        private final Map<Integer, Integer> initialSsa = new HashMap<>();
        private final Map<Integer, Phi> phis;
        private final Map<Integer, Integer> realToSsa = new HashMap<>();

        public SSAState() {
            phis = new HashMap<>();
        }

        public int getCurrentSsa(int realAddress) {
            if (realToSsa.containsKey(realAddress)) {
                return realToSsa.get(realAddress);
            }
            int ssa = nextFreeSsa++;
            realToSsa.put(realAddress, ssa);
            initialSsa.put(realAddress, ssa);
            return ssa;
        }

        public int updateSsa(int address) {
            int ssa = nextFreeSsa++;
            realToSsa.put(address, ssa);
            if (!initialSsa.containsKey(address)) {
                initialSsa.put(address, ssa);
            }
            return ssa;
        }

        public void mapTransitionFrom(IRBlock sourceBlock) {
            final SSAState src = getSsaState(sourceBlock);
            final SSAState dest = this;

           for (Integer realAddress : src.realToSsa.keySet()) {
               int ssaAddress = src.realToSsa.get(realAddress);

               Phi phi = dest.getPhi(realAddress);

               LoadLocal ll = new LoadLocal(ssaAddress, realAddress);
               phi.options.put(sourceBlock, ll);
           }
        }

        public int getInitialSsa(int realAddress) {
            return initialSsa.get(realAddress);
        }

        public Phi getPhi(int address) {
            return phis.computeIfAbsent(address, k -> new Phi());
        }
    }


    public IRBuilder(int ip) {
        this(ip, null);
    }

    public IRBuilder(int ip, LimitExceededListener l) {
        this.stack = new ArrayDeque<>();
        this.blockSsaState = new HashMap<>();

        this.block = new IRBlock();
        this.root = block;
        this.blocks = new LinkedHashMap<>();
        this.currentIp = ip;
        this.size = 0;
        this.limitExceededListener = l;
    }

    boolean completed = false;

    public IRBlock getRoot() {
        if (completed) {
            return root;
        }

        for (IRBlock block : blockSsaState.keySet()) {
            SSAState ssaState = getSsaState(block);

            for (int realAddress : ssaState.realToSsa.keySet()) {
                Phi phi = ssaState.getPhi(realAddress);
                if (!phi.options.isEmpty()) {
                    int ssa = ssaState.getInitialSsa(realAddress);
                    block.getCode().add(0, new StoreLocal(ssa, realAddress, phi));
                }
            }
        }

        completed = true;
        return root;
    }

    public void push(IRValue node) {
        stack.push(node);
    }

    public IRValue pop() {
        return stack.remove();
    }

    public IRValue top() {
        return stack.element();
    }

    public void dup() {
        stack.push(stack.element());
    }

    public void nullValue() {
        this.stack.push(new NullValue());
        postHandleExpr();
    }

    public void integer(int value) {
        this.stack.push(new Int(value));
        postHandleExpr();
    }

    public void real(double value) {
        this.stack.push(new Real(value));
        postHandleExpr();
    }

    public void string(String value) {
        this.stack.push(new Str(value));
        postHandleExpr();
    }

    public void bool(boolean value) {
        this.stack.push(new Bool(value));
        postHandleExpr();
    }

    public void array(int argc) {
        List<IRValue> exps = new ArrayList<>();
        for (int i = 0; i < argc; i++) {
            exps.add(new WrapTObj(stack.remove()));
        }
        this.stack.push(new Array(exps));
        postHandleExpr();
    }

    public void dict(int argc) {
        Dict dict = new Dict();
        for (int i = 0; i < argc; i++) {
            IRValue value = stack.remove();
            IRValue key = stack.remove();
            dict.keys.add(new WrapTObj(key));
            dict.values.add(new WrapTObj(value));
        }
        this.stack.push(dict);
        postHandleExpr();
    }

    public void range() {
        IRValue to = stack.remove();
        IRValue from = stack.pop();
        stack.push(new Range(new WrapTObj(from), new WrapTObj(to)));
        postHandleExpr();
    }

    public void makeThis() {
        stack.push(new This());
        postHandleExpr();
    }

    public void builtin(int id) {
        stack.push(new Builtin(id));
        postHandleExpr();
    }

    public void loadType(int address) {
        stack.push(new Type(address, true));
        postHandleExpr();
    }

    public void loadType(com.tscript.runtime.typing.Type type) {
        stack.push(new Type(TypeCache.getId(type), false));
        postHandleExpr();
    }

    public void typeof() {
        stack.push(new GetType(new WrapTObj(stack.remove())));
        postHandleExpr();
    }

    public void virtual(int address) {
        stack.push(new Virtual(address));
        postHandleExpr();
    }

    public void makeNative(String name) {
        stack.push(new Native(name));
        postHandleExpr();
    }

    public void callInp(int argc) {
        callInp(argc, false);
    }

    public void callInp(int argc, boolean isGuaranteedVirtual) {
        IRValue called = stack.remove();

        if (isGuaranteedVirtual || isVirtual(called)) {
            for (int i = 0; i < argc; i++) {
                block.append(new StoreTemp(i, new WrapTObj(stack.remove())));
            }
            block.append(new PushFrame(called));
            pushSSAState(block);
            for (int i = 0; i < argc; i++) {
                stack.push(new UnpackObj(new LoadTemp(i)));
            }
        }
        else {
            List<IRValue> args = new ArrayList<>(argc);
            for (int i = 0; i < argc; i++) args.add(new WrapTObj(stack.remove()));
            stack.push(new UnpackObj(new CallInp(new WrapTObj(called), args)));
        }

        postHandleExpr();
    }

    public void toInpArg() {
        stack.push(new ToInpArg(new WrapTObj(stack.remove())));
        postHandleExpr();
    }

    public void toMapArg(String name) {
        stack.push(new ToMapArg(name, new WrapTObj(stack.remove())));
        postHandleExpr();
    }

    public void newLine(int line) {
        this.block.append(new NewLine(line));
        postHandleStmt();
    }

    public void loadTemp(int address) {
        stack.push(new UnpackObj(new LoadTemp(address)));
    }

    public void storeTemp(int address) {
        this.block.append(new StoreTemp(address, new WrapTObj(stack.remove())));
    }

    public void loadLocal(int address) {
        int ssaAddress = getSsaState(block).getCurrentSsa(address);
        stack.push(new UnpackObj(new LoadLocal(ssaAddress, address)));
        postHandleExpr();
    }

    public void storeLocal(int address) {
        IRValue value = stack.remove();
        int ssaAddress = getSsaState(block).updateSsa(address);
        this.block.append(new StoreLocal(ssaAddress, address, new WrapTObj(value)));
        postHandleStmt();
    }

    public void loadGlobal(int address) {
        stack.push(new UnpackObj(new LoadGlobal(address)));
        postHandleExpr();
    }

    public void storeGlobal(int address) {
        IRValue value = stack.remove();
        this.block.append(new StoreGlobal(address, new WrapTObj(value)));
        postHandleStmt();
    }

    public void loadInternal(int address) {
        stack.push(new UnpackObj(new LoadInternal(address)));
        postHandleExpr();
    }

    public void storeInternal(int address) {
        IRValue value = stack.remove();
        this.block.append(new StoreInternal(address, new WrapTObj(value)));
        postHandleStmt();
    }

    public void loadExternal(String name) {
        stack.push(new UnpackObj(new LoadExternal(new WrapTObj(stack.remove()), name)));
        postHandleExpr();
    }

    public void storeExternal(String name) {
        IRValue accessed = stack.remove();
        IRValue value = stack.remove();
        this.block.append(new StoreExternal(name, new WrapTObj(accessed), new WrapTObj(value)));
        postHandleStmt();
    }

    public void loadStatic(String name) {
        stack.push(new UnpackObj(new LoadStatic(name)));
        postHandleExpr();
    }

    public void storeStatic(String name) {
        IRValue value = stack.remove();
        this.block.append(new StoreStatic(name, new WrapTObj(value)));
        postHandleStmt();
    }

    public void loadSuper(String name) {
        stack.push(new UnpackObj(new LoadSuper(name)));
        postHandleExpr();
    }

    public void storeSuper(String name) {
        IRValue value = stack.remove();
        this.block.append(new StoreSuper(name, new WrapTObj(value)));
        postHandleStmt();
    }

    public void loadName(String name) {
        stack.push(new UnpackObj(new LoadName(name)));
        postHandleExpr();
    }

    public void loadAbstract(String name) {
        stack.push(new LoadAbstract(name));
        postHandleExpr();
    }

    public void makeExprStmt() {
        this.block.append(new ExprStmt(stack.remove()));
        postHandleStmt();
    }

    public void makeReturn() {
        this.block.append(new Return(new WrapTObj(stack.remove())));
        postHandleStmt();
    }

    public void makePopFrameReturn() {
        this.block.append(new PopFrame());
        popSSAState(block);
    }

    public void containerRead() {
        IRValue container = stack.remove();
        IRValue key = stack.remove();
        stack.push(new UnpackObj(new ContainerRead(new WrapTObj(container), new WrapTObj(key))));
        postHandleExpr();
    }

    public void containerWrite() {
        IRValue container = stack.remove();
        IRValue key = stack.remove();
        IRValue value = stack.remove();
        this.block.append(new ContainerWrite(new WrapTObj(container), new WrapTObj(key), new WrapTObj(value)));
        postHandleStmt();
    }

    public void equals(boolean shouldBeEqual) {
        IRValue rhs = stack.remove();
        IRValue lhs = stack.remove();
        stack.push(new UnpackObj(new Equals(lhs, rhs, shouldBeEqual)));
        postHandleExpr();
    }

    public void negate() {
        IRValue value = stack.remove();
        stack.push(new UnpackObj(new Neg(value)));
        postHandleExpr();
    }

    public void not() {
        IRValue value = stack.remove();
        stack.push(new UnpackObj(new Not(value)));
        postHandleExpr();
    }

    public void binaryOp(Opcode opcode) {
        IRValue rhs = stack.remove();
        IRValue lhs = stack.remove();
        stack.push(new UnpackObj(new BinaryOp(new WrapTObj(lhs), new WrapTObj(rhs), opcode)));
        postHandleExpr();
    }

    public void importModule(int index) {
        stack.push(new ImportModule(index));
        postHandleExpr();
    }

    public void setOwner() {
        IRValue member = stack.remove();
        IRValue owner = stack.remove();
        stack.push(new UnpackObj(new SetOwner(new WrapTObj(owner), new WrapTObj(member))));
        postHandleExpr();
    }

    public void use(String name) {
        IRValue value = stack.remove();
        block.append(new Use(new WrapTObj(value), name));
        postHandleStmt();
    }

    public void useMembers() {
        IRValue value = stack.remove();
        block.append(new UseMembers(new WrapTObj(value)));
        postHandleStmt();
    }

    public void extend(String name) {
        IRValue value = stack.remove();
        IRValue owner = stack.remove();
        block.append(new Extend(new WrapTObj(owner), name, new WrapTObj(value)));
        postHandleStmt();
    }

    public void getItr() {
        block.append(new Itr(new WrapTObj(stack.remove())));
        blocks.put(currentIp, block);
        postHandleStmt();
    }

    public void itrHasNext(boolean shouldHaveNext) {
        stack.push(new UnpackObj(new ItrHasNext(shouldHaveNext)));
        postHandleExpr();
    }

    public void itrBranch(int address) {

        IRBlock noJumpBlock = blocks.computeIfAbsent(currentIp+1, k -> new IRBlock());
        IRBlock jumpBlock;

        if (address > currentIp) {
            jumpBlock = blocks.computeIfAbsent(address, k -> new IRBlock());
        }
        else if (address < currentIp) {
            jumpBlock = blocks.get(address);
        }
        else {
            throw new IllegalStateException("Endless loop does not work on branching directly");
        }

        block.append(new ItrBranch(jumpBlock, noJumpBlock));

        size++;
        if (checkLimitExceeded()) return;

        currentIp++;
        block = noJumpBlock;
    }

    public void itrNext() {
        stack.push(new ItrNext());
        storeTemp(0);
        loadTemp(0);
        postHandleExpr();
    }


    public void createBranch(int address, boolean onTrue) {
        IRValue condition = stack.remove();

        IRBlock noJumpBlock = blocks.computeIfAbsent(currentIp+1, k -> new IRBlock());
        IRBlock jumpBlock;

        if (address > currentIp) {
            jumpBlock = blocks.computeIfAbsent(address, k -> new IRBlock());
        }
        else if (address < currentIp) {
            jumpBlock = blocks.get(address);
        }
        else {
            throw new IllegalStateException("Endless loop does not work on branching directly");
        }

        block.append(new Branch(new UnpackObj(condition), jumpBlock, noJumpBlock, onTrue));
        getSsaState(jumpBlock).mapTransitionFrom(block);
        getSsaState(noJumpBlock).mapTransitionFrom(block);

        currentIp++;
        block = noJumpBlock;
    }

    public void createGoto(int address) {
        if (address > currentIp) {
            IRBlock destBlock = blocks.computeIfAbsent(address, k -> new IRBlock());
            block.append(new Goto(destBlock));
            getSsaState(destBlock).mapTransitionFrom(block);
        }
        else if (address < currentIp) {
            IRBlock destBlock = blocks.get(address);
            getSsaState(destBlock).mapTransitionFrom(block);
            block.append(new Goto(blocks.get(address)));
        }
        else {
            block.append(new Goto(IRBlock.ENDLESS_LOOP));
        }

        size++;
        if (checkLimitExceeded()) return;

        currentIp++;
        block = blocks.computeIfAbsent(currentIp, k -> new IRBlock());
    }

    public void createGoto(IRBlock block) {
        this.block.append(new Goto(block));
        getSsaState(block).mapTransitionFrom(this.block);
    }

    public void setIp(int ip) {
        currentIp = ip;
    }

    public void createGuard(int ipRecover, int spRecover) {
        IRValue condition = stack.remove();
        block.append(new Guard(condition, ipRecover, spRecover));
        postHandleStmt();
    }


    private void postHandleExpr() {
        blocks.put(currentIp, block);
        currentIp++;
    }

    private void postHandleStmt() {
        currentIp++;
        size++;
        if (checkLimitExceeded()) return;

        if (blocks.containsKey(currentIp)) {
            IRBlock nextBlock = blocks.get(currentIp);
            block.append(new Goto(nextBlock));
            block = nextBlock;
        }
    }

    private boolean checkLimitExceeded() {
        if (limitExceededListener != null && size > limitExceededListener.limit) {
            limitExceededListener.onLimitExceeded();
            return true;
        }
        return false;
    }

    private boolean isVirtual(IRNode node) {
        if (node instanceof Virtual) return true;

        return false;
    }

    private SSAState getSsaState(IRBlock block) {
        return blockSsaState.computeIfAbsent(block, k -> new ArrayDeque<>(List.of(new SSAState()))).element();
    }

    private void pushSSAState(IRBlock block) {
        blockSsaState.computeIfAbsent(block, k -> new ArrayDeque<>(List.of(new SSAState()))).push(new SSAState());
    }

    private void popSSAState(IRBlock block) {
        blockSsaState.computeIfAbsent(block, k -> new ArrayDeque<>(List.of(new SSAState()))).remove();
    }

}
