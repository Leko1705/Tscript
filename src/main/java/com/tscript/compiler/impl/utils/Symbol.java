package com.tscript.compiler.impl.utils;

import com.tscript.compiler.source.tree.Modifier;
import com.tscript.compiler.source.utils.Location;
import com.tscript.runtime.core.Builtins;

import java.util.Set;

public abstract class Symbol {

    public static final int NO_ADDRESS = -1;

    public enum Kind {
        VARIABLE,
        FUNCTION,
        CLASS,
        UNKNOWN,
        BUILTIN
    }


    public final Kind kind;

    public final Location location;

    public final String name;

    public final Scope owner;

    public Set<Modifier> modifiers;

    public int address;


    Symbol(Kind kind, String name, Set<Modifier> modifiers, Scope owner, int addr, Location location) {
        this.kind = kind;
        this.name = name;
        this.owner = owner;
        this.modifiers = modifiers;
        this.address = addr;
        this.location = location;
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

        public VarSymbol(String name, Set<Modifier> modifiers, Scope owner, int addr, Location location) {
            super(Kind.VARIABLE, name, modifiers, owner, addr, location);
        }

    }


    public static final class FunctionSymbol extends Symbol {

        public final Scope.FunctionScope subScope;

        public FunctionSymbol(String name, Set<Modifier> modifiers, Scope owner, int addr, Location location) {
            super(Kind.FUNCTION, name, modifiers, owner, addr, location);
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

        public final int classIndex;

        public final boolean isNamespace;

        public ClassSymbol(String name, Set<Modifier> modifiers, Scope owner, int addr, int classIndex, boolean isNamespace, Location location) {
            super(Kind.CLASS, name, modifiers, owner, addr, location);
            this.subScope = new Scope.ClassScope(owner, this);
            this.classIndex = classIndex;
            this.isNamespace = isNamespace;
        }

        public boolean isAbstract(){
            return modifiers.contains(Modifier.ABSTRACT);
        }

    }

    public static final class UnknownSymbol extends Symbol {
        public UnknownSymbol(String name, Location location) {
            super(Kind.UNKNOWN, name, Set.of(), null, NO_ADDRESS, location);
        }
    }

    public static final class Builtin extends Symbol {
        public Builtin(String name, Location location) {
            super(Kind.BUILTIN, name, Set.of(Modifier.CONSTANT), null, Builtins.indexOf(name), location);
        }
    }

}
