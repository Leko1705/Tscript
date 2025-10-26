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

    private final BlockManager blockManager;

    private final Deque<SsaState> callStack;

    private int currentIp;

    private int size;

    private final LimitExceededListener limitExceededListener;

    private int nextFreeGuardId;


    public IRBuilder(int ip) {
        this(ip, null);
    }

    public IRBuilder(int ip, LimitExceededListener l) {
        nextFreeGuardId = 0;

        this.blockManager = new BlockManager();
        this.root = blockManager.currentBlock();
        blockManager.associateAddress(ip, root);

        this.stack = new ArrayDeque<>();

        this.callStack = new ArrayDeque<>(List.of(root.ssa));

        this.currentIp = ip;
        this.size = 0;
        this.limitExceededListener = l;
    }

    boolean completed = false;

    public IRBlock getRoot() {
        if (completed) {
            return root;
        }

        for (IRBlock block : blockManager.getAllBlocks()) {

            for (int realAddress : block.getRealAddresses()) {
                Phi phi = block.getPhi(realAddress);
                if (!phi.options.isEmpty()) {
                    int ssa = block.getInitialSsa(realAddress);
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
        this.stack.push(new WrapTObj(new Int(value)));
        postHandleExpr();
    }

    public void real(double value) {
        this.stack.push(new WrapTObj(new Real(value)));
        postHandleExpr();
    }

    public void string(String value) {
        this.stack.push(new WrapTObj(new Str(value)));
        postHandleExpr();
    }

    public void bool(boolean value) {
        this.stack.push(new WrapTObj(new Bool(value)));
        postHandleExpr();
    }

    public void array(int argc) {
        List<IRValue> exps = new ArrayList<>();
        for (int i = 0; i < argc; i++) {
            exps.add(new WrapTObj(stack.remove()));
        }
        this.stack.push(new WrapTObj(new Array(exps)));
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
        this.stack.push(new WrapTObj(dict));
        postHandleExpr();
    }

    public void range() {
        IRValue to = stack.remove();
        IRValue from = stack.pop();
        stack.push(new Range(from, to));
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
        stack.push(new WrapTObj(new Type(address)));
        postHandleExpr();
    }

    public void loadType(com.tscript.runtime.typing.Type type) {
        stack.push(new Type(TypeCache.getId(type)));
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
                blockManager.currentBlock().append(new StoreTemp(i, new WrapTObj(stack.remove())));
            }
            blockManager.currentBlock().append(new PushFrame(called));
            callStack.push(new SsaState(blockManager.getNextFreeSsa()));
            for (int i = 0; i < argc; i++) {
                stack.push(new LoadTemp(i));
            }
        }
        else {
            List<IRValue> args = new ArrayList<>(argc);
            for (int i = 0; i < argc; i++) args.add(new WrapTObj(stack.remove()));
            stack.push(new CallInp(new WrapTObj(called), args));
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
        blockManager.currentBlock().append(new NewLine(line));
        postHandleStmt();
    }

    public void loadTemp(int address) {
        stack.push(new WrapTObj(new LoadTemp(address)));
    }

    public void storeTemp(int address) {
        blockManager.currentBlock().append(new StoreTemp(address, new WrapTObj(stack.remove())));
    }

    public void loadLocal(int address) {
        int ssaAddress = getSsaState().getSsa(address);
        stack.push(new WrapTObj(new LoadLocal(ssaAddress, address)));
        postHandleExpr();
    }

    public void storeLocal(int address) {
        IRValue value = stack.remove();
        int ssaAddress = getSsaState().updateSsa(address);
        blockManager.currentBlock().append(new StoreLocal(ssaAddress, address, new WrapTObj(value)));
        postHandleStmt();
    }

    public void loadGlobal(int address) {
        stack.push(new LoadGlobal(address));
        postHandleExpr();
    }

    public void storeGlobal(int address) {
        IRValue value = stack.remove();
        blockManager.currentBlock().append(new StoreGlobal(address, new WrapTObj(value)));
        postHandleStmt();
    }

    public void loadInternal(int address) {
        stack.push(new LoadInternal(address));
        postHandleExpr();
    }

    public void storeInternal(int address) {
        IRValue value = stack.remove();
        blockManager.currentBlock().append(new StoreInternal(address, new WrapTObj(value)));
        postHandleStmt();
    }

    public void loadExternal(String name) {
        stack.push(new LoadExternal(new WrapTObj(stack.remove()), name));
        postHandleExpr();
    }

    public void storeExternal(String name) {
        IRValue accessed = stack.remove();
        IRValue value = stack.remove();
        blockManager.currentBlock().append(new StoreExternal(name, new WrapTObj(accessed), new WrapTObj(value)));
        postHandleStmt();
    }

    public void loadStatic(String name) {
        stack.push(new LoadStatic(name));
        postHandleExpr();
    }

    public void storeStatic(String name) {
        IRValue value = stack.remove();
        blockManager.currentBlock().append(new StoreStatic(name, new WrapTObj(value)));
        postHandleStmt();
    }

    public void loadSuper(String name) {
        stack.push(new LoadSuper(name));
        postHandleExpr();
    }

    public void storeSuper(String name) {
        IRValue value = stack.remove();
        blockManager.currentBlock().append(new StoreSuper(name, new WrapTObj(value)));
        postHandleStmt();
    }

    public void loadName(String name) {
        stack.push(new LoadName(name));
        postHandleExpr();
    }

    public void loadAbstract(String name) {
        stack.push(new LoadAbstract(name));
        postHandleExpr();
    }

    public void makeExprStmt() {
        blockManager.currentBlock().append(new ExprStmt(stack.remove()));
        postHandleStmt();
    }

    public void makeReturn() {
        blockManager.currentBlock().append(new Return(new WrapTObj(stack.remove())));
        postHandleStmt();
    }

    public void makePopFrameReturn() {
        blockManager.currentBlock().append(new PopFrame());
        callStack.remove();
    }

    public void containerRead() {
        IRValue container = stack.remove();
        IRValue key = stack.remove();
        stack.push(new ContainerRead(new WrapTObj(container), new WrapTObj(key)));
        postHandleExpr();
    }

    public void containerWrite() {
        IRValue container = stack.remove();
        IRValue key = stack.remove();
        IRValue value = stack.remove();
        blockManager.currentBlock().append(new ContainerWrite(new WrapTObj(container), new WrapTObj(key), new WrapTObj(value)));
        postHandleStmt();
    }

    public void equals(boolean shouldBeEqual) {
        IRValue rhs = stack.remove();
        IRValue lhs = stack.remove();
        stack.push(new Equals(new WrapTObj(lhs), new WrapTObj(rhs), shouldBeEqual));
        postHandleExpr();
    }

    public void negate() {
        IRValue value = stack.remove();
        stack.push(new Neg(new WrapTObj(value)));
        postHandleExpr();
    }

    public void not() {
        IRValue value = stack.remove();
        stack.push(new Not(new WrapTObj(value)));
        postHandleExpr();
    }

    public void binaryOp(Opcode opcode) {
        IRValue rhs = stack.remove();
        IRValue lhs = stack.remove();
        stack.push(new BinaryOp(new WrapTObj(lhs), new WrapTObj(rhs), opcode));
        postHandleExpr();
    }

    public void importModule(int index) {
        stack.push(new ImportModule(index));
        postHandleExpr();
    }

    public void setOwner() {
        IRValue member = stack.remove();
        IRValue owner = stack.remove();
        stack.push(new SetOwner(new WrapTObj(owner), new WrapTObj(member)));
        postHandleExpr();
    }

    public void use(String name) {
        IRValue value = stack.remove();
        blockManager.currentBlock().append(new Use(new WrapTObj(value), name));
        postHandleStmt();
    }

    public void useMembers() {
        IRValue value = stack.remove();
        blockManager.currentBlock().append(new UseMembers(new WrapTObj(value)));
        postHandleStmt();
    }

    public void extend(String name) {
        IRValue value = stack.remove();
        IRValue owner = stack.remove();
        blockManager.currentBlock().append(new Extend(new WrapTObj(owner), name, new WrapTObj(value)));
        postHandleStmt();
    }

    public void getItr() {
        blockManager.currentBlock().append(new Itr(new WrapTObj(stack.remove())));
        blockManager.associateAddress(currentIp, blockManager.currentBlock());
        postHandleStmt();
    }

    public void itrHasNext(boolean shouldHaveNext) {
        stack.push(new ItrHasNext(shouldHaveNext));
        postHandleExpr();
    }

    public void itrBranch(int address) {

        IRBlock noJumpBlock = blockManager.computeBlockForAddress(currentIp+1);
        IRBlock jumpBlock;

        if (address > currentIp) {
            jumpBlock = blockManager.computeBlockForAddress(address);
        }
        else if (address < currentIp) {
            jumpBlock = blockManager.requireBlockForAddress(address);
        }
        else {
            throw new IllegalStateException("Endless loop does not work on branching directly");
        }

        blockManager.currentBlock().append(new ItrBranch(jumpBlock, noJumpBlock));

        size++;
        if (checkLimitExceeded()) return;

        currentIp++;
        blockManager.setCurrentBlock(noJumpBlock);
    }

    public void itrNext() {
        stack.push(new ItrNext());
        storeTemp(0);
        loadTemp(0);
        postHandleExpr();
    }


    public void createBranch(int address, boolean onTrue) {
        IRValue condition = stack.remove();

        IRBlock noJumpBlock = blockManager.computeBlockForAddress(currentIp+1);
        IRBlock jumpBlock;

        if (address > currentIp) {
            jumpBlock = blockManager.computeBlockForAddress(address);
        }
        else if (address < currentIp) {
            jumpBlock = blockManager.requireBlockForAddress(address);
        }
        else {
            throw new IllegalStateException("Endless loop does not work on branching directly");
        }

        blockManager.currentBlock().append(new Branch(new WrapTObj(condition), jumpBlock, noJumpBlock, onTrue));
        getSsaState().mapTransitionFrom(blockManager.currentBlock());
        getSsaState().mapTransitionFrom(blockManager.currentBlock());

        currentIp++;
        blockManager.setCurrentBlock(noJumpBlock);
    }

    public void createGoto(int address) {
        if (address > currentIp) {
            IRBlock destBlock = blockManager.computeBlockForAddress(address);
            blockManager.currentBlock().append(new Goto(destBlock));
            getSsaState().mapTransitionFrom(blockManager.currentBlock());
        }
        else if (address < currentIp) {
            IRBlock destBlock = blockManager.requireBlockForAddress(address);
            Objects.requireNonNull(destBlock);
            getSsaState().mapTransitionFrom(blockManager.currentBlock());
            blockManager.currentBlock().append(new Goto(destBlock));
        }
        else {
            blockManager.currentBlock().append(new Goto(IRBlock.ENDLESS_LOOP));
        }

        size++;
        if (checkLimitExceeded()) return;

        currentIp++;
        blockManager.setCurrentBlock(blockManager.computeBlockForAddress(currentIp));
    }

    public void createGoto(IRBlock block) {
        blockManager.currentBlock().append(new Goto(block));
        getSsaState().mapTransitionFrom(blockManager.currentBlock());
    }

    public void setIp(int ip) {
        currentIp = ip;
    }

    public void createGuard(int ipRecover, int spRecover) {
        createGuard(ipRecover, spRecover, true);
    }

    public void createGuard(int ipRecover, int spRecover, boolean shouldHold) {
        IRValue condition = stack.remove();
        blockManager.currentBlock().append(new Guard(nextFreeGuardId++, condition, ipRecover, spRecover, shouldHold));
        postHandleStmt();
    }


    private void postHandleExpr() {
        blockManager.associateAddress(currentIp, blockManager.currentBlock());
        currentIp++;
    }

    private void postHandleStmt() {
        currentIp++;
        size++;
        if (checkLimitExceeded()) return;

        if (blockManager.hasBlockForAddress(currentIp)) {
            IRBlock nextBlock = blockManager.requireBlockForAddress(currentIp);
            blockManager.currentBlock().append(new Goto(nextBlock));
            blockManager.setCurrentBlock(nextBlock);
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

    private SsaState getSsaState() {
        return callStack.element();
    }

}
