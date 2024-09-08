package com.tscript.tscriptc.generation.generators.impls;

import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.Location;
import com.tscript.tscriptc.utils.TreeVisitor;

import java.util.List;
import java.util.Set;

public class LambdaFunction implements LambdaTree, FunctionTree {

    private final LambdaTree lambda;
    private final String name;

    public LambdaFunction(LambdaTree lambda, String name) {
        this.lambda = lambda;
        this.name = name;
    }

    @Override
    public ModifiersTree getModifiers() {
        return new ModifiersTree() {
            @Override
            public Set<Modifier> getModifiers() {
                return Set.of();
            }

            @Override
            public Location getLocation() {
                return lambda.getLocation();
            }
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<? extends ClosureTree> getClosures() {
        return lambda.getClosures();
    }

    @Override
    public List<? extends ParameterTree> getParameters() {
        return lambda.getParameters();
    }

    @Override
    public BlockTree getBody() {
        return lambda.getBody();
    }

    @Override
    public <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return FunctionTree.super.accept(visitor, p);
    }

    @Override
    public Location getLocation() {
        return lambda.getLocation();
    }
}
