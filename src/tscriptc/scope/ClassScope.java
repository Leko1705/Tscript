package tscriptc.scope;

import tscriptc.tree.ClassTree;
import tscriptc.tree.Modifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ClassScope extends Scope implements Iterable<Symbol> {
    private ClassScope superClass;
    private final ClassTree tree;
    private int constructorID;
    private int staticBlockID;
    private final Map<String, Symbol> abstractMethodSymbols = new HashMap<>();

    public ClassScope(Scope enclosingScope, ClassTree classTree){
        this.tree = classTree;
        setEnclosingScope(enclosingScope);
    }

    public String getName() {
        return tree.getName();
    }

    public ClassTree getTree() {
        return tree;
    }

    @Override
    public Symbol get(String name) {
        Symbol sym = super.get(name);
        return sym != null ? sym : abstractMethodSymbols.get(name);
    }

    public void setConstructorID(int constructorID) {
        this.constructorID = constructorID;
    }

    public int getConstructorID() {
        return constructorID;
    }

    public void setStaticBlockID(int staticBlockID) {
        this.staticBlockID = staticBlockID;
    }

    public int getStaticBlockID() {
        return staticBlockID;
    }

    @Override
    public void putIfAbsent(SymbolKind kind, String name, Set<Modifier> modifiers) {
        super.putIfAbsent(kind, name, modifiers);
        updateAddresses();
        if (superClass != null) nextFreeAddress = superClass.nextFreeAddress;
    }

    public void putAbstractMethod(String name, Set<Modifier> modifiers){
        Symbol symbol = new Symbol(this, SymbolKind.FUNCTION, name, -1);
        symbol.getModifiers().addAll(modifiers);
        abstractMethodSymbols.put(name, symbol);
    }

    @Override
    void notifyVariableAdded(int currentLocalsAmount) {}

    public void updateAddresses(){
        updateAddresses0();
    }

    private int updateAddresses0(){
        int base = superClass != null ? superClass.updateAddresses0() : 0;
        int offs = 0;
        for (Symbol symbol : content.values())
            symbol.setAddress(base + offs++);
        return base + offs;
    }

    public ClassScope getSuperClass() {
        return superClass;
    }
    public void setSuperClass(ClassScope superClass) {
        this.superClass = superClass;
        if (superClass != null)
            this.nextFreeAddress = superClass.nextFreeAddress;
    }
    @Override
    public <P, R> R accept(ScopeVisitor<P, R> visitor, P p) {
        return visitor.visitClassScope(this, p);
    }

    @Override
    public Iterator<Symbol> iterator() {
        return new SymbolItr();
    }

    private class SymbolItr implements Iterator<Symbol> {

        Iterator<Symbol> baseItr = content.values().iterator();
        Iterator<Symbol> superItr = null;

        public SymbolItr(){
            if (superClass != null)
                superItr = superClass.iterator();
        }

        @Override
        public boolean hasNext() {
            return (superItr != null && superItr.hasNext()) || baseItr.hasNext();
        }

        @Override
        public Symbol next() {
            if (superItr != null && superItr.hasNext())
                return superItr.next();
            return baseItr.next();
        }
    }
}
