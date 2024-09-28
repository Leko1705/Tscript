package com.tscript.compiler.impl.utils;

import com.tscript.compiler.impl.analyze.structures.typing.Type;
import com.tscript.compiler.source.tree.Modifier;

import java.util.Set;

public abstract class Symbol {

    public enum Kind {
        VARIABLE,
        FUNCTION,
        CLASS,
        NAMESPACE,
        UNKNOWN
    }


    public final Kind kind;

    public final String name;

    public final Scope owner;

    public final Set<Modifier> modifiers;

    public int address;


    Symbol(Kind kind, String name, Set<Modifier> modifiers, Scope owner, int addr) {
        this.kind = kind;
        this.name = name;
        this.owner = owner;
        this.modifiers = modifiers;
        this.address = addr;
    }

    public boolean isConstant() {
        return modifiers.contains(Modifier.CONSTANT);
    }

    public boolean isStatic() {
        return modifiers.contains(Modifier.STATIC);
    }

    public boolean isPrivate() {
        return modifiers.contains(Modifier.PRIVATE);
    }

    public boolean isProtected() {
        return modifiers.contains(Modifier.PROTECTED);
    }

    public boolean isPublic() {
        return modifiers.contains(Modifier.PUBLIC);
    }


    public static final class VarSymbol extends Symbol {

        public Type type;

        public VarSymbol(String name, Set<Modifier> modifiers, Scope owner, int addr) {
            super(Kind.VARIABLE, name, modifiers, owner, addr);
        }

    }


    public static final class FunctionSymbol extends Symbol {

        public final Scope.FunctionScope subScope;

        public FunctionSymbol(String name, Set<Modifier> modifiers, Scope owner, int addr) {
            super(Kind.FUNCTION, name, modifiers, owner, addr);
            this.subScope = new Scope.FunctionScope(owner);
        }

        public boolean isNative(){
            return modifiers.contains(Modifier.NATIVE);
        }

        public boolean isAbstract(){
            return modifiers.contains(Modifier.ABSTRACT);
        }

        public boolean isOverridden(){
            return modifiers.contains(Modifier.OVERRIDDEN);
        }
    }


    public static final class ClassSymbol extends Symbol {

        public ClassSymbol superClass;

        public final Scope.ClassScope subScope;

        public ClassSymbol(String name, Set<Modifier> modifiers, Scope owner, int addr) {
            super(Kind.CLASS, name, modifiers, owner, addr);
            this.subScope = new Scope.ClassScope(owner);
        }

        public boolean isAbstract(){
            return modifiers.contains(Modifier.ABSTRACT);
        }
    }


    public static final class NamespaceSymbol extends Symbol {

        public final Scope.NamespaceScope subScope;

        public NamespaceSymbol(String name, Set<Modifier> modifiers, Scope owner, int addr) {
            super(Kind.NAMESPACE, name, modifiers, owner, addr);
            this.subScope = new Scope.NamespaceScope(owner);
        }
    }

}
