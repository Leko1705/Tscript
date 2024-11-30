package com.tscript.compiler.impl.generation.gen.adapter;

import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.source.tree.Modifier;

import java.util.HashSet;
import java.util.Set;

public class NamespaceClass extends TCTree.TCClassTree {

    public NamespaceClass(TCNamespaceTree tree) {
        super(tree.location,
                tree.name,
                modifiers(tree.modifiers),
                null,
                tree.definitions);
        this.sym = tree.sym;
        Set<Modifier> addedAbstract = new HashSet<>(this.sym.modifiers);
        addedAbstract.add(Modifier.ABSTRACT);
        this.sym.modifiers = addedAbstract;
    }

    private static TCModifiersTree modifiers(TCModifiersTree old){
        Set<Modifier> set = new HashSet<>(old.flags);
        set.add(Modifier.ABSTRACT);
        return new TCModifiersTree(old.location, set);
    }
}
