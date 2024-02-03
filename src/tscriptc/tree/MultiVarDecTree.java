package tscriptc.tree;

import tscriptc.util.TreeVisitor;

import java.util.List;
import java.util.Set;

public interface MultiVarDecTree extends DefinitionTree, StatementTree {

    List<VarDecTree> getDeclarations();

    default Set<Modifier> getModifiers(){
        throw new UnsupportedOperationException("getModifiers");
    }

    default String getName(){
        throw new UnsupportedOperationException("getName");
    }
    default boolean isConstant(){
        throw new UnsupportedOperationException("isConstant");
    }

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitMultiVarDecTree(this, p);
    }
}
