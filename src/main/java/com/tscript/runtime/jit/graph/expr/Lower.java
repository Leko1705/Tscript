package com.tscript.runtime.jit.graph.expr;


public class Lower implements Expression {

    public LowerBuiltin builtin;

    public Lower(LowerBuiltin builtin) {
        this.builtin = builtin;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitBuiltinLower(this, p);
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        builtin.replace(toReplace, tree);
    }

    public interface Generator {
        void generate(Cast builtin);
        void generate(Unpack builtin);
        void generate(Print builtin);
        void generate(Time builtin);
    }

    public interface LowerBuiltin {
        void lower(Generator g);
        default void replace(Expression toReplace, Expression with){ }
    }

    public static class Unpack implements LowerBuiltin {
        public Class<?> unpackedType;
        public Expression wrapper;
        public Unpack(Class<?> unpackedType, Expression wrapper) {
            this.unpackedType = unpackedType;
            this.wrapper = wrapper;
        }
        @Override
        public void lower(Generator g) {
            g.generate(this);
        }
    }

    public static class Cast implements LowerBuiltin {
        public boolean asPrimitive = false;
        public Expression cast;
        public Class<?> from;
        public Class<?> to;
        public Cast(Expression sub, Class<?> from, Class<?> to){
            if (from == null || to == null) throw new AssertionError(new NullPointerException());
            this.cast = sub;
            this.from = from;
            this.to = to;
        }
        public void lower(Generator g) {
            g.generate(this);
        }
    }


    public static class Print implements LowerBuiltin {
        public Class<?> clazz;
        public Expression sub;

        public Print(Class<?> clazz, Expression sub) {
            this.clazz = clazz;
            this.sub = sub;
        }
        @Override public void lower(Generator g) { g.generate(this); }
@       Override public void replace(Expression toReplace, Expression with) {
            if (toReplace == sub) {
                sub = with;
            }
}
    }

    public static class Time implements LowerBuiltin {
        public boolean asPrimitive = false;
        @Override public void lower(Generator g) { g.generate(this); }
    }

}
