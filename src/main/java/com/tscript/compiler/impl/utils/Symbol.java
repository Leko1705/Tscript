package com.tscript.compiler.impl.utils;

import com.tscript.compiler.source.tree.Modifier;
import com.tscript.compiler.source.utils.Location;

import java.util.Set;

public abstract class Symbol {

    public static final int NO_ADDRESS = -1;

    public enum Kind {
        VARIABLE,
        FUNCTION,
        CLASS,
        NAMESPACE,
        UNKNOWN
    }


    public final Kind kind;

    public final Location location;

    public final String name;

    public final Scope owner;

    public final Set<Modifier> modifiers;

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

    public abstract void generate(Generator generator);


    public static final class VarSymbol extends Symbol {

        public VarSymbol(String name, Set<Modifier> modifiers, Scope owner, int addr, Location location) {
            super(Kind.VARIABLE, name, modifiers, owner, addr, location);
        }

        @Override
        public void generate(Generator generator) {
            generator.generate(this);
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

        @Override
        public void generate(Generator generator) {
            generator.generate(this);
        }
    }


    public static final class ClassSymbol extends Symbol {

        public ClassSymbol superClass;

        public final Scope.ClassScope subScope;

        public ClassSymbol(String name, Set<Modifier> modifiers, Scope owner, int addr, Location location) {
            super(Kind.CLASS, name, modifiers, owner, addr, location);
            this.subScope = new Scope.ClassScope(owner, this);
        }

        public boolean isAbstract(){
            return modifiers.contains(Modifier.ABSTRACT);
        }

        @Override
        public void generate(Generator generator) {
            generator.generate(this);
        }
    }


    public static final class NamespaceSymbol extends Symbol {

        public final Scope.NamespaceScope subScope;

        public NamespaceSymbol(String name, Set<Modifier> modifiers, Scope owner, int addr, Location location) {
            super(Kind.NAMESPACE, name, modifiers, owner, addr, location);
            this.subScope = new Scope.NamespaceScope(owner);
        }

        @Override
        public void generate(Generator generator) {
            generator.generate(this);
        }
    }

    public static final class UnknownSymbol extends Symbol {
        public UnknownSymbol(String name, Location location) {
            super(Kind.UNKNOWN, name, Set.of(), null, NO_ADDRESS, location);
        }

        @Override
        public void generate(Generator generator) {
            generator.generate(this);
        }
    }


    public interface Generator {
        void generate(VarSymbol symbol);
        void generate(FunctionSymbol symbol);
        void generate(ClassSymbol symbol);
        void generate(NamespaceSymbol symbol);
        void generate(UnknownSymbol symbol);
    }

}
