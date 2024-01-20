package tscriptc.scope;

import tscriptc.tree.Modifier;
import tscriptc.util.Assertion;

import java.util.HashSet;
import java.util.Set;

public class Symbol {
    private final String name;
    private int address;
    private final SymbolKind kind;
    private final Set<Modifier> modifiers;
    private final Scope owner;
    public Symbol(Scope owner, SymbolKind kind, String name, int address, Set<Modifier> modifiers) {
        this.name = name;
        this.address = address;
        this.kind = kind;
        this.modifiers = modifiers;
        this.owner = owner;
    }
    public Symbol(Scope owner, SymbolKind kind, String name, int address) {
        this(owner, kind, name, address, new HashSet<>());
    }
    public SymbolKind getKind(){
        return kind;
    }

    public String getName(){
        return name;
    }

    public int getAddress(){
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    public Scope getOwner() {
        return owner;
    }

    public Modifier getVisibility(){
        for (Modifier m : modifiers)
            if (m.isVisibility())
                return m;
        return Assertion.error("symbol " + name + " has no visibility");
    }
}
