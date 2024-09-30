package com.tscript.compiler.impl.utils;

import java.util.HashMap;
import java.util.Map;

public abstract class Scope {


    public enum Kind {
        GLOBAL,
        LOCAL,
        FUNCTION,
        LAMBDA,
        CLASS,
        NAMESPACE;

        public boolean isContainer(){
            return this == CLASS || this == NAMESPACE;
        }
    }

    public final Kind kind;

    public Scope enclosing;

    public Scope owner;

    public GlobalScope topLevel;

    public final Map<String, Symbol> symbols = new HashMap<>();


    public Scope(Kind kind, Scope enclosing, Scope owner, GlobalScope topLevel) {
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

    public static final class ClassScope extends Scope {
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
    }

    public static final class NamespaceScope extends Scope {
        public NamespaceScope(Scope enclosing) {
            super(Kind.NAMESPACE, enclosing, null, enclosing.topLevel);
            owner = this;
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitNamespace(this);
        }
    }


    public interface Visitor<R> {
        R visitGlobal(GlobalScope scope);
        R visitLocal(LocalScope scope);
        R visitFunction(FunctionScope scope);
        R visitLambda(LambdaScope scope);
        R visitClass(ClassScope scope);
        R visitNamespace(NamespaceScope scope);
    }


}
