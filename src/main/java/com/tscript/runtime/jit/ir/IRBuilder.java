package com.tscript.runtime.jit.ir;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.ir.nodes.*;

import java.util.*;

public class IRBuilder {


    public static abstract class LimitExceededListener {

        private final int limit;

        public LimitExceededListener(int limit) {
            this.limit = limit;
        }

        public abstract void onLimitExceeded();

    }


    private final Deque<IRNode> stack;

    private final IRBlock root;

    private IRBlock block;

    private final Map<Integer, IRBlock> blocks;

    private int currentIp;

    private int size;

    private LimitExceededListener limitExceededListener;


    public IRBuilder(int ip) {
        this.stack = new ArrayDeque<>();
        this.block = new IRBlock();
        this.root = block;
        this.blocks = new LinkedHashMap<>();
        this.currentIp = ip;
        this.size = 0;
        this.limitExceededListener = null;
    }

    public IRBuilder(int ip, LimitExceededListener l) {
        this.stack = new ArrayDeque<>();
        this.block = new IRBlock();
        this.root = block;
        this.blocks = new LinkedHashMap<>();
        this.currentIp = ip;
        this.size = 0;
        this.limitExceededListener = l;
    }


    public IRBlock getRoot() {
        return root;
    }

    public void push(IRNode node) {
        stack.push(node);
    }

    public IRNode pop() {
        return stack.remove();
    }

    public IRNode top() {
        return stack.element();
    }

    public void appendToCurrentBlock(IRNode stmt) {
        block.append(stmt);
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
        List<IRNode> exps = new ArrayList<>();
        for (int i = 0; i < argc; i++) {
            exps.add(stack.remove());
        }
        this.stack.push(new Array(exps));
        postHandleExpr();
    }

    public void dict(int argc) {
        Dict dict = new Dict();
        for (int i = 0; i < argc; i++) {
            dict.keys.add(stack.remove());
            dict.values.add(stack.remove());
        }
        this.stack.push(dict);
        postHandleExpr();
    }

    public void range() {
        IRNode to = stack.remove();
        IRNode from = stack.pop();
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
        stack.push(new Type(address));
        postHandleExpr();
    }

    public void loadType(com.tscript.runtime.typing.Type type) {
        stack.push(new Type(type));
        postHandleExpr();
    }

    public void typeof() {
        stack.push(new GetType(stack.remove()));
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
        IRNode called = stack.remove();

        if (isGuaranteedVirtual || isVirtual(called)) {
            for (int i = 0; i < argc; i++) {
                block.append(new StoreTemp(i, stack.remove()));
            }
            block.append(new PushFrame(called));
            for (int i = 0; i < argc; i++) {
                stack.push(new LoadTemp(i));
            }
        }
        else {
            List<IRNode> args = new ArrayList<>(argc);
            for (int i = 0; i < argc; i++) args.add(stack.remove());
            stack.push(new CallInp(called, args));
        }

        postHandleExpr();
    }

    public void toInpArg() {
        stack.push(new ToInpArg(stack.remove()));
        postHandleExpr();
    }

    public void toMapArg(String name) {
        stack.push(new ToMapArg(name, stack.remove()));
        postHandleExpr();
    }

    public void newLine(int line) {
        this.block.append(new NewLine(line));
        postHandleStmt();
    }

    public void loadLocal(int address) {
        stack.push(new LoadLocal(address));
        postHandleExpr();
    }

    public void storeLocal(int address) {
        IRNode value = stack.remove();
        this.block.append(new StoreLocal(address, value));
        postHandleStmt();
    }

    public void loadGlobal(int address) {
        stack.push(new LoadGlobal(address));
        postHandleExpr();
    }

    public void storeGlobal(int address) {
        IRNode value = stack.remove();
        this.block.append(new StoreGlobal(address, value));
        postHandleStmt();
    }

    public void loadInternal(int address) {
        stack.push(new LoadInternal(address));
        postHandleExpr();
    }

    public void storeInternal(int address) {
        IRNode value = stack.remove();
        this.block.append(new StoreInternal(address, value));
        postHandleStmt();
    }

    public void loadExternal(String name) {
        stack.push(new LoadExternal(name));
        postHandleExpr();
    }

    public void storeExternal(String name) {
        IRNode value = stack.remove();
        this.block.append(new StoreExternal(name, value));
        postHandleStmt();
    }

    public void loadStatic(String name) {
        stack.push(new LoadStatic(name));
        postHandleExpr();
    }

    public void storeStatic(String name) {
        IRNode value = stack.remove();
        this.block.append(new StoreStatic(name, value));
        postHandleStmt();
    }

    public void loadSuper(String name) {
        stack.push(new LoadSuper(name));
        postHandleExpr();
    }

    public void storeSuper(String name) {
        IRNode value = stack.remove();
        this.block.append(new StoreSuper(name, value));
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
        this.block.append(new ExprStmt(stack.remove()));
        postHandleStmt();
    }

    public void makeReturn() {
        this.block.append(new Return(stack.remove()));
        postHandleStmt();
    }

    public void containerRead() {
        IRNode container = stack.remove();
        IRNode key = stack.remove();
        stack.push(new ContainerRead(container, key));
        postHandleExpr();
    }

    public void containerWrite() {
        IRNode container = stack.remove();
        IRNode key = stack.remove();
        IRNode value = stack.remove();
        this.block.append(new ContainerWrite(container, key, value));
        postHandleStmt();
    }

    public void equals(boolean shouldBeEqual) {
        IRNode rhs = stack.remove();
        IRNode lhs = stack.remove();
        stack.push(new Equals(lhs, rhs, shouldBeEqual));
        postHandleExpr();
    }

    public void negate() {
        IRNode value = stack.remove();
        stack.push(new Neg(value));
        postHandleExpr();
    }

    public void not() {
        IRNode value = stack.remove();
        stack.push(new Not(value));
        postHandleExpr();
    }

    public void binaryOp(Opcode opcode) {
        IRNode rhs = stack.remove();
        IRNode lhs = stack.remove();
        stack.push(new BinaryOp(rhs, lhs, opcode));
        postHandleExpr();
    }

    public void importModule(int index) {
        stack.push(new ImportModule(index));
        postHandleExpr();
    }

    public void setOwner() {
        IRNode member = stack.remove();
        IRNode owner = stack.remove();
        stack.push(new SetOwner(member, owner));
        postHandleExpr();
    }

    public void use(String name) {
        IRNode value = stack.remove();
        block.append(new Use(value, name));
        postHandleStmt();
    }

    public void useMembers() {
        IRNode value = stack.remove();
        block.append(new UseMembers(value));
        postHandleStmt();
    }

    public void extend(String name) {
        IRNode value = stack.remove();
        IRNode owner = stack.remove();
        block.append(new Extend(owner, name, value));
        postHandleStmt();
    }

    public void getItr() {
        block.append(new Itr(stack.remove()));
        blocks.put(currentIp, block);
        postHandleStmt();
    }

    public void itrHasNext(boolean shouldHaveNext) {
        stack.push(new ItrHasNext(shouldHaveNext));
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

        block.append(new ItrBranch(jumpBlock, noJumpBlock, address));

        size++;
        if (checkLimitExceeded()) return;

        currentIp++;
        block = noJumpBlock;
    }

    public void itrNext() {
        stack.push(new ItrNext());
        postHandleExpr();
    }


    public void createBranch(int address, boolean onTrue) {
        IRNode condition = stack.remove();

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

        block.append(new Branch(condition, jumpBlock, noJumpBlock, onTrue));

        currentIp++;
        block = noJumpBlock;
    }

    public void createGoto(int address) {
        if (address > currentIp) {
            IRBlock newBlock = blocks.computeIfAbsent(address, k -> new IRBlock());
            block.append(new Goto(newBlock));
        }
        else if (address < currentIp) {
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
        block.append(new Goto(block));
    }

    public void setIp(int ip) {
        currentIp = ip;
    }

    public void createGuard(int ipRecover, int spRecover) {
        IRNode condition = stack.remove();
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

}
