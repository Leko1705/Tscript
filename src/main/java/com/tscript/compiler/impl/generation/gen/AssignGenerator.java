package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.generation.compiled.instruction.*;
import com.tscript.compiler.impl.utils.Scope;
import com.tscript.compiler.impl.utils.Symbol;
import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.impl.utils.TCTreeScanner;
import com.tscript.compiler.source.tree.MemberAccessTree;
import com.tscript.compiler.source.tree.ThisTree;

public class AssignGenerator extends TCTreeScanner<Void, Void> {

    /**
     * The scanner who uses this AssignGenerator
     */
    private final FunctionGenerator user;

    private final Context context;

    private final CompFunc func;

    private boolean requireReload = false;

    public AssignGenerator(FunctionGenerator user, Context context, CompFunc func) {
        this.user = user;
        this.context = context;
        this.func = func;
    }


    @Override
    public Void visitVariable(TCTree.TCVariableTree node, Void unused) {
        user.stackShrinks();
        Symbol sym = node.sym;

        if (sym.owner == null || sym.owner.kind == Scope.Kind.GLOBAL){
            dupIfRequireReload();
            func.getInstructions().add(new StoreGlobal(sym.address));
            return null;
        }

        if (node.sym.owner.kind == Scope.Kind.CLASS){
            dupIfRequireReload();

            if (node.sym.isStatic()){
                func.getInstructions().add(new StoreStatic(PoolPutter.putUtf8(context, node.name)));
            }
            else if (node.sym.inSuperClass) {
                func.getInstructions().add(new StoreSuper(PoolPutter.putUtf8(context, node.name)));
            }
            else {
                func.getInstructions().add(new StoreInternal(node.sym.address));
            }
            return null;
        }

        dupIfRequireReload();
        func.getInstructions().add(new StoreLocal(sym.address));
        return null;
    }

    @Override
    public Void visitMemberAccess(TCTree.TCMemberAccessTree node, Void unused) {

        if (node.expression instanceof ThisTree){
            dupIfRequireReload();
            func.getInstructions().add(new StoreInternal(node.sym.address));
            user.stackShrinks();
            return null;
        }

        user.scan(node.expression, null);
        dupIfRequireReload();
        user.newLine(node);
        func.getInstructions().add(new StoreExternal(PoolPutter.putUtf8(context, node.memberName)));
        user.stackShrinks(2);
        return null;
    }

    @Override
    public Void visitContainerAccess(TCTree.TCContainerAccessTree node, Void unused) {
        user.scan(node.key, null);
        user.scan(node.container, null);
        dupIfRequireReload();
        user.newLine(node);
        func.getInstructions().add(new ContainerWrite());
        user.stackShrinks(3);
        return null;
    }

    @Override
    public Void visitAssign(TCTree.TCAssignTree node, Void unused) {
        AssignGenerator gen = new AssignGenerator(user, context, func);
        gen.requireReload = true;
        node.right.accept(gen, null);
        gen.requireReload = false;
        node.left.accept(gen, null);
        return null;
    }

    @Override
    public Void visitSuper(TCTree.TCSuperTree node, Void unused) {
        dupIfRequireReload();
        func.getInstructions().add(new StoreSuper(PoolPutter.putUtf8(context, node.name)));
        user.stackShrinks();
        return null;
    }

    private void dupIfRequireReload(){
        if (requireReload){
            func.getInstructions().add(new Dup());
            user.stackGrows();
        }
    }

}
