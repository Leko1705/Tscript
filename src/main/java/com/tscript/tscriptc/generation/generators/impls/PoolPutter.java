package com.tscript.tscriptc.generation.generators.impls;

import com.tscript.tscriptc.generation.generators.Context;
import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.SimpleTreeVisitor;

import java.util.Iterator;

public final class PoolPutter {

    private PoolPutter() {}

    public static int put(Context context, ExpressionTree expr){
        return expr.accept(new Putter(), context);
    }

    public static int putUtf8(Context context, String utf8){
        return context.getFile().pool.putUTF8(utf8);
    }


    private static class Putter extends SimpleTreeVisitor<Context, Integer> {

        @Override
        public Integer defaultAction(Tree node, Context context) {
            throw new AssertionError("expression not covered");
        }

        @Override
        public Integer visitInteger(IntegerTree node, Context context) {
            return context.getFile().pool.putInt(node.get());
        }

        @Override
        public Integer visitFloat(FloatTree node, Context context) {
            return context.getFile().pool.putFloat(node.get());
        }

        @Override
        public Integer visitString(StringTree node, Context context) {
            return context.getFile().pool.putString(node.get());
        }

        @Override
        public Integer visitBoolean(BooleanTree node, Context context) {
            return context.getFile().pool.putBoolean(node.get());
        }

        @Override
        public Integer visitNull(NullTree node, Context context) {
            return context.getFile().pool.putNull();
        }

        @Override
        public Integer visitRange(RangeTree node, Context context) {
            int from = put(context, node.getFrom());
            int to = put(context, node.getTo());
            return context.getFile().pool.putRange(from, to);
        }

        @Override
        public Integer visitArray(ArrayTree node, Context context) {
            int[] content = new int[node.getContents().size()];
            for (int i = 0; i < content.length; i++) {
                content[i] = put(context, node.getContents().get(i));
            }
            return context.getFile().pool.putArray(content);
        }

        @Override
        public Integer visitDictionary(DictionaryTree node, Context context) {
            int[] content = new int[node.getKeys().size() + node.getValues().size()];

            Iterator<? extends ExpressionTree> keyItr = node.getKeys().iterator();
            Iterator<? extends ExpressionTree> valueItr = node.getValues().iterator();

            for (int i = 0; i < content.length; i += 2) {
                ExpressionTree key = keyItr.next();
                ExpressionTree value = valueItr.next();

                content[i] = put(context, key);
                content[i + 1] = put(context, value);
            }

            return context.getFile().pool.putDictionary(content);
        }
    }


}
