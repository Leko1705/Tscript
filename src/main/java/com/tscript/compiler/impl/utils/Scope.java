package com.tscript.compiler.impl.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Scope {


    public enum Kind {
        GLOBAL,
        LOCAL,
        FUNCTION,
        LAMBDA,
        CLASS
    }

    public final Kind kind;

    public Scope enclosing;

    public ClassScope owner;

    public GlobalScope topLevel;

    public final Map<String, Symbol> symbols = new HashMap<>();


    public Scope(Kind kind, Scope enclosing, ClassScope owner, GlobalScope topLevel) {
        this.kind = kind;
        this.enclosing = enclosing;
        this.owner = owner;
        this.topLevel = topLevel;
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static final class GlobalScope extends Scope {
        public GlobalScope() {
            super(Kind.GLOBAL, null, null, null);
            topLevel = this;
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGlobal(this);
        }
    }

    public static final class LocalScope extends Scope {
        public LocalScope(Scope enclosing) {
            super(Kind.LOCAL, enclosing, enclosing.owner, enclosing.topLevel);
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLocal(this);
        }
    }

    public static final class FunctionScope extends Scope {
        public FunctionScope(Scope enclosing) {
            super(Kind.FUNCTION, enclosing, enclosing.owner, enclosing.topLevel);
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunction(this);
        }
    }

    public static final class LambdaScope extends Scope {
        public LambdaScope(Scope enclosing) {
            super(Kind.LAMBDA, enclosing, null, enclosing.topLevel);
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLambda(this);
        }
    }

    public static final class ClassScope extends Scope implements Iterable<Symbol> {
        public Symbol.ClassSymbol sym;
        public ClassScope(Scope enclosing, Symbol.ClassSymbol sym) {
            super(Kind.CLASS, enclosing, null, enclosing.topLevel);
            owner = this;
            this.sym = sym;
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitClass(this);
        }

        @Override
        public Iterator<Symbol> iterator() {
            return new Itr();
        }

        private class Itr implements Iterator<Symbol> {

            Iterator<Symbol> baseItr = symbols.values().iterator();
            Iterator<Symbol> superItr = null;

            public Itr(){
                if (sym.superClass != null)
                    superItr = sym.superClass.subScope.iterator();
            }

            @Override
            public boolean hasNext() {
                return baseItr.hasNext() || (superItr != null && superItr.hasNext());
            }

            @Override
            public Symbol next() {
                if (baseItr.hasNext())
                    return baseItr.next();
                return superItr.next();
            }
        }
    }


    public interface Visitor<R> {
        R visitGlobal(GlobalScope scope);
        R visitLocal(LocalScope scope);
        R visitFunction(FunctionScope scope);
        R visitLambda(LambdaScope scope);
        R visitClass(ClassScope scope);
    }


}
