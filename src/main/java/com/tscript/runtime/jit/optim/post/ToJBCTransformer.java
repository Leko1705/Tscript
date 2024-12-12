package com.tscript.runtime.jit.optim.post;

import com.tscript.runtime.core.Builtins;
import com.tscript.runtime.jit.graph.BranchNode;
import com.tscript.runtime.jit.graph.Context;
import com.tscript.runtime.jit.graph.FlowNode;
import com.tscript.runtime.jit.graph.expr.*;
import com.tscript.runtime.jit.graph.expr.Null;
import com.tscript.runtime.jit.graph.traversal.Traverser;
import com.tscript.runtime.jit.graph.traversal.TreeTraverseStrategy;
import com.tscript.runtime.jit.optim.State;
import com.tscript.runtime.tni.natfuncs.std.NativePrint;
import com.tscript.runtime.tni.natfuncs.std.NativeTime;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.typing.Type;

import java.util.*;

public class ToJBCTransformer implements PostOptimizer {


    private static final int THRESHOLD = 2;


    private final List<Node> accepted = new ArrayList<>();
    private final State<Map<Integer, ListNode>> localCandidates = new State<>(new HashMap<>());
    private final Map<Integer, ListNode> temps = new HashMap<>();


    private static Class<?> fromType(Type type) {
        if (type == TInteger.TYPE) return int.class;
        if (type == TString.TYPE) return String.class;
        if (type == TBoolean.TYPE) return boolean.class;
        if (type == TReal.TYPE) return double.class;
        return null;
    }


    @Override
    public com.tscript.runtime.jit.graph.Node optimize(com.tscript.runtime.jit.graph.Node graph, Type[] inputTypes) {
        com.tscript.runtime.jit.graph.Node[] ref = new com.tscript.runtime.jit.graph.Node[]{graph};

        for (int i = 0; i < inputTypes.length; i++) {
            Type type = inputTypes[i];

            if (type != null) {
                Class<?> targetType = fromType(type);

                if (targetType != null) {
                    Node dependenceTree = getLLParamConversionNode(i, targetType, ref);
                    localCandidates.get().put(i, new ListNode(targetType, dependenceTree));
                }
            }
        }

        Traverser.traverse(new Trav(), new Handler(), graph);

        acceptLocals();

        for (Node candidate : accepted) {
            candidate.run();
        }

        return ref[0];
    }

    private static Node getLLParamConversionNode(final int index, Class<?> targetType, com.tscript.runtime.jit.graph.Node[] ref) {
        return new Leaf(
                targetType,
                () -> {
                    Expression exp = new LoadLocal(index);
                    exp = new Lower(new Lower.Unpack(targetType, exp));
                    exp = new StoreLocal(index, exp, targetType);
                    FlowNode beginning =
                            new FlowNode(exp);
                    beginning.setNext(ref[0]);
                    ref[0] = beginning;
                }
        );
    }

    private void acceptLocals(){
        for (ListNode node : localCandidates.get().values()){
            if (node.childCount() >= THRESHOLD){
                node.run();
            }
        }
    }


    private static class Trav extends TreeTraverseStrategy {
        @Override
        public boolean isThenFirst(BranchNode node, Context context) {
            return false;
        }

    }



    private interface Node extends Runnable {
        Node EMPTY = new Leaf(null, () -> {});

        Class<?> type();
        void run();
        int depth();
        int childCount();
    }

    private record Member(Runnable action) implements Node {
        @Override
        public Class<?> type() {
            return null;
        }

        @Override
        public void run() {
            action.run();
        }

        @Override
        public int depth() {
            return 0;
        }

        @Override
        public int childCount() {
            return 0;
        }
    }

    private record Leaf(Class<?> type, Runnable action) implements Node {

        @Override
        public void run() {
            action.run();
        }

        @Override
        public int depth() {
            return 1;
        }

        @Override
        public int childCount() {
            return 0;
        }
    }

    private static class ListNode implements Node {
        public Class<?> clazz;
        private final List<Node> actions;
        public int depth = 0;

        public ListNode(Class<?> clazz, Node... actions) {
            this.actions = new ArrayList<>(List.of(actions));
            this.clazz = clazz;
            for (Node action : actions) {
                depth = Math.max(depth, action.depth());
            }
        }

        @Override
        public Class<?> type() {
            return clazz;
        }

        @Override
        public void run() {
            for (Runnable action : actions) {
                action.run();
            }
        }

        public void add(Node action) {
            actions.add(action);
            depth = Math.max(depth, action.depth());
        }

        @Override
        public int depth() {
            return depth;
        }

        @Override
        public int childCount() {
            int count = 0;
            for (Node action : actions) {
                if (!(action instanceof Member)){
                    count++;
                }
            }
            return count;
        }
    }


    private class Handler extends ParentDelegationScanner<Node> {

        @Override
        public Node visitExpression(Expression expr, Expression expression) {
            return Node.EMPTY;
        }

        @Override
        public Node visitPushFrame(PushFrame expr, Expression expression) {
            super.visitPushFrame(expr, expression);
            localCandidates.storeAndSet(new HashMap<>());
            return null;
        }

        @Override
        public Node visitPushSuperFrame(PushSuperFrame expr, Expression expression) {
            super.visitPushSuperFrame(expr, expression);
            localCandidates.storeAndSet(new HashMap<>());
            return null;
        }

        @Override
        public Node visitPopFrame(PopFrame expr, Expression expression) {
            acceptLocals();
            localCandidates.restore();
            return null;
        }

        @Override
        public Node visitNull(Null expr, Expression expression) {
            return new Leaf(Void.TYPE, () -> expr.asPrimitive = true);
        }

        @Override
        public Node visitInt(Int expr, Expression parent) {
            return new Leaf(int.class, () -> expr.asPrimitive = true);
        }

        @Override
        public Node visitReal(Real expr, Expression expression) {
            return new Leaf(double.class, () -> expr.asPrimitive = true);
        }

        @Override
        public Node visitBool(Bool expr, Expression expression) {
            return new Leaf(int.class, () -> expr.asPrimitive = true);
        }

        @Override
        public Node visitString(Str expr, Expression expression) {
            return new Leaf(String.class, () -> expr.asPrimitive = true);
        }

        @Override
        public Node visitNot(Not expr, Expression expression) {
            Node sub = scan(expr.expression, expr);

            if (sub.type() == int.class || sub.type() == boolean.class) {
                return new ListNode(
                        sub.type(),
                        sub,
                        new Member(() -> expr.primitiveTarget = sub.getClass()));
            }

            return Node.EMPTY;
        }

        @Override
        public Node visitNeg(Neg expr, Expression expression) {
            Node sub = scan(expr.expression, expr);
            if (sub.type() == int.class || sub.type() == double.class) {
                return new ListNode(
                        sub.type(),
                        sub,
                        new Member(() -> expr.primitiveTarget = sub.getClass()));
            }
            return Node.EMPTY;
        }

        @Override
        public Node visitBinaryOp(BinaryOp expr, Expression expression) {
            Node lhs = scan(expr.lhs, expr);
            Node rhs = scan(expr.rhs, expr);

            if (lhs != Node.EMPTY && rhs != Node.EMPTY) {
                Class<?> resultType = getBinaryResult(expr, lhs.type(), rhs.type());

                return new ListNode(resultType,
                        lhs,
                        rhs,
                        new Member(() -> expr.lhsTarget = lhs.type()),
                        new Member(() -> expr.rhsTarget = rhs.type()));
            }

            return Node.EMPTY;
        }


        @Override
        public Node visitStoreLocal(StoreLocal expr, Expression expression) {
            Node sub = scan(expr.what, expr);

            if (sub != Node.EMPTY && sub.type() != null) {
                ListNode state = localCandidates.get().get(expr.index);
                if (state == null) {
                    ListNode newNode = new ListNode(expr.primitiveType, sub);
                    localCandidates.get().put(expr.index, newNode);
                } else {
                    if (state.type() == sub.type()) {
                        state.add(sub);
                    } else {
                        if (state.childCount() >= THRESHOLD) {
                            accepted.add(state);
                        }
                        localCandidates.get().put(expr.index, new ListNode(expr.primitiveType, sub));
                    }
                }
            }

            return Node.EMPTY;
        }

        @Override
        public Node visitLoadLocal(LoadLocal expr, Expression expression) {
            ListNode state = localCandidates.get().get(expr.index);
            if (state != null) {
                state.add(new Member(() -> expr.primitiveType = state.type()));
                return state;
            }
            return Node.EMPTY;
        }

        @Override
        public Node visitStoreTemp(StoreTemp expr, Expression expression) {
            Node sub = scan(expr.what, expr);

            if (sub != Node.EMPTY && sub.type() != null) {
                ListNode state = temps.get(expr.index);
                if (state == null) {
                    ListNode newNode = new ListNode(expr.primitiveType, sub);
                    temps.put(expr.index, newNode);
                } else {
                    if (state.type() == sub.type()) {
                        state.add(sub);
                    } else {
                        if (state.childCount() >= THRESHOLD) {
                            accepted.add(state);
                        }
                        temps.put(expr.index, new ListNode(expr.primitiveType, sub));
                    }
                }
            }

            return Node.EMPTY;
        }

        @Override
        public Node visitLoadTemp(LoadTemp expr, Expression expression) {
            ListNode state = temps.get(expr.index);
            if (state != null) {
                state.add(new Member(() -> expr.primitiveType = state.type()));
                return state;
            }
            return Node.EMPTY;
        }

        @Override
        public Node visitReturn(Return expr, Expression parent) {
            Node node = scan(expr.expression, expr);

            if (node == Node.EMPTY || node.type() == null) return Node.EMPTY;

            int childCount = node.childCount();
            int depth = node.depth();
            if (childCount >= THRESHOLD || depth >= THRESHOLD) {
                expr.replace(expr.expression, new Lower(new Lower.Cast(expr.expression, node.type(), node.type())));
                node.run();
            }

            return Node.EMPTY;
        }


        private static Class<?> getBinaryResult(BinaryOp expr, Class<?> lhs, Class<?> rhs) {

            return switch (expr.opcode) {
                case ADD, SUB, MUL, DIV:
                    if (lhs == String.class || rhs == String.class) yield (String.class);
                    else if (lhs == double.class || rhs == double.class) yield (double.class);
                    else if (lhs == int.class && rhs == int.class) yield (int.class);

                case XOR, AND, OR:
                    if (lhs == int.class || rhs == int.class) yield (int.class);
                    else if (lhs == boolean.class || rhs == boolean.class) yield (boolean.class);

                case MOD, SLA, SRA, SRL:
                    if (lhs == int.class && rhs == int.class) yield (int.class);

                default:
                    throw new AssertionError(expr.opcode + " (" + lhs + ", " + rhs + ")");
            };
        }


        @Override
        public Node visitCallInp(CallInp expr, Expression parent) {
            if (!(expr.called instanceof Builtin)) {
                scan(expr.called, expr);
                return null;
            }

            boolean canTransform = true;
            Node[] nodes = new Node[expr.args.size()];
            for (int i = 0; i < expr.args.size(); i++) {
                nodes[i] = expr.args.get(i).accept(this, expr);
                if (nodes[i] == null) canTransform = false;
            }
            if (!canTransform) return null;

            if (expr.called instanceof Builtin b) {
                TObject builtin = Builtins.load(b.index());

                if (builtin instanceof NativePrint
                        && nodes.length == 1
                        && (nodes[0].type().isPrimitive() || nodes[0].type() == String.class)) {

                    Expression arg = expr.args.get(0);
                    parent.replace(expr, new Lower(new Lower.Print(nodes[0].type(), arg)));

                    // This subexpression definitely can be printed.
                    // Since it is always more performant to unpack + call than
                    // the interpreted way, we enforce the lowering version.
                    nodes[0].run();

                    return new Leaf(Void.TYPE, () -> {});
                }
                else if (builtin instanceof NativeTime && nodes.length == 0) {
                    Lower.Time time = new Lower.Time();
                    parent.replace(expr, new Lower(time));
                    return new Leaf(double.class, () -> time.asPrimitive = true);
                }
                else if (builtin == com.tscript.runtime.typing.Null.TYPE && nodes.length == 0) {
                    Null null0 = new Null();
                    parent.replace(expr, null0);
                    return scan(null0, expr);
                }
                else if (builtin == TInteger.TYPE) {
                    if (nodes.length == 0) {
                        return handleDefaultInitialValue(expr, parent, new Int(0));
                    }
                    else if (nodes.length == 1 && Set.of(int.class, double.class).contains(nodes[0].type())) {
                        return handleCast(expr, parent, expr.args.get(0), nodes[0], int.class);
                    }
                }
                else if (builtin == TReal.TYPE) {
                    if (nodes.length == 0) {
                        return handleDefaultInitialValue(expr, parent, new Real(0d));
                    }
                    else if (nodes.length == 1 && Set.of(int.class, double.class).contains(nodes[0].type())) {
                        return handleCast(expr, parent, expr.args.get(0), nodes[0], double.class);
                    }
                }
                else if (builtin == TString.TYPE) {
                    if (nodes.length == 0) {
                        return handleDefaultInitialValue(expr, parent, new Str(""));
                    }
                    else if (nodes.length == 1) {
                        // works, since node[0] is primitive or at
                        // least a String
                        return handleCast(expr, parent, expr.args.get(0), nodes[0], String.class);
                    }
                }
            }

            return null;
        }


        private Node handleDefaultInitialValue(Expression what, Expression parent, Expression value) {
            parent.replace(what, value);
            return scan(value, what);
        }


        private Node handleCast(Expression what, Expression parent, Expression value, Node from, Class<?> to) {
            // At this point it is guaranteed that
            // the builtin-types constructor
            // is always primitive. Therefore,
            // we enforce lowering.
            from.run();

            Lower.Cast cast = new Lower.Cast(value, from.type(), to);
            parent.replace(what, new Lower(cast));

            return new Leaf(to, new Member(() -> cast.asPrimitive = true));
        }

    }


}
