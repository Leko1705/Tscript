package com.tscript.tscriptc.parse;

import com.tscript.tscriptc.tree.DefinitionTree;
import com.tscript.tscriptc.tree.Trees;
import com.tscript.tscriptc.util.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class Builtins {
    private Builtins(){}

    private static final List<DefinitionTree> builtins = new ArrayList<>();
    public static List<DefinitionTree> getBuiltins(){
        return builtins;
    }



    static {
        init();
    }

    private static void loadNative(String name){
        builtins.add(new Trees.BasicNativeFunctionTree(Location.emptyLocation(), name, Set.of()));
    }


    private static void init(){
        loadNative("print");
        loadNative("exit");
    }



}
