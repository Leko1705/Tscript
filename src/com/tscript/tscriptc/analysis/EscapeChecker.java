package com.tscript.tscriptc.analysis;

import com.tscript.tscriptc.log.Logger;
import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.util.Diagnostics;
import com.tscript.tscriptc.util.Errors;
import com.tscript.tscriptc.util.TreeScanner;

import java.util.Objects;

public class EscapeChecker extends Checker<Void, Void> {

    private boolean inFunction = false;
    private boolean inConstructor = false;
    private boolean inClass = false;
    private boolean inLoop = false;
    private boolean inLambda = false;
    private boolean inAbstractScope = false;

    @Override
    public Void visitClassTree(ClassTree classTree, Void unused) {
        boolean inClass = this.inClass;
        boolean inAbstractScope = this.inAbstractScope;
        this.inClass = true;
        this.inAbstractScope = classTree.getModifiers().contains(Modifier.ABSTRACT);
        super.visitClassTree(classTree, unused);
        this.inClass = inClass;
        this.inAbstractScope = inAbstractScope;
        return null;
    }

    @Override
    public Void visitFunctionTree(FunctionTree functionTree, Void unused) {
        boolean inFunction = this.inFunction;
        this.inFunction = true;
        super.visitFunctionTree(functionTree, unused);
        this.inFunction = inFunction;
        return null;
    }

    @Override
    public Void visitConstructorTree(ConstructorTree constructorTree, Void unused) {
        boolean inConstructor = this.inConstructor;
        this.inConstructor = true;
        super.visitConstructorTree(constructorTree, unused);
        this.inConstructor = inConstructor;
        return null;
    }

    @Override
    public Void visitWhileDoTree(WhileDoTree whileDoTree, Void unused) {
        boolean inLoop = this.inLoop;
        this.inLoop = true;
        super.visitWhileDoTree(whileDoTree, unused);
        this.inLoop = inLoop;
        return null;
    }

    @Override
    public Void visitDoWhileTree(DoWhileTree doWhileTree, Void unused) {
        boolean inLoop = this.inLoop;
        this.inLoop = true;
        super.visitDoWhileTree(doWhileTree, unused);
        this.inLoop = inLoop;
        return null;
    }

    @Override
    public Void visitForLoopTree(ForLoopTree forLoopTree, Void unused) {
        boolean inLoop = this.inLoop;
        this.inLoop = true;
        super.visitForLoopTree(forLoopTree, unused);
        this.inLoop = inLoop;
        return null;
    }

    @Override
    public Void visitLambdaTree(LambdaTree lambdaTree, Void unused) {
        boolean inFunction = this.inFunction;
        boolean inClass = this.inClass;
        boolean inLoop = this.inClass;
        boolean inLambda = this.inLambda;
        boolean inConstructor = this.inConstructor;
        boolean inAbstractScope = this.inAbstractScope;
        this.inFunction = true;
        this.inClass = false;
        this.inLoop = false;
        this.inLambda = true;
        this.inConstructor = false;
        super.visitLambdaTree(lambdaTree, unused);
        this.inFunction = inFunction;
        this.inClass = inClass;
        this.inLoop = inLoop;
        this.inLambda = inLambda;
        this.inConstructor = inConstructor;
        this.inAbstractScope = inAbstractScope;
        return null;
    }

    @Override
    public Void visitBreakTree(BreakTree breakTree, Void unused) {
        if (!inLoop)
            report(Errors.canNotBreakOutOfLoop(breakTree.getLocation()));
        return null;
    }

    @Override
    public Void visitContinueTree(ContinueTree continueTree, Void unused) {
        if (!inLoop)
            report(Errors.canNotContinueOutOfLoop(continueTree.getLocation()));
        return null;
    }

    @Override
    public Void visitReturnTree(ReturnTree returnTree, Void unused) {
        if (!inFunction && !inConstructor)
            report(Errors.canNotReturnOutOfFunction(returnTree.getLocation()));
        else if (inConstructor)
            report(Errors.canNotReturnFromConstructor(returnTree.getLocation()));
        return null;
    }

    @Override
    public Void visitThisTree(ThisTree thisTree, Void unused) {
        if (!inClass && !inLambda && !inFunction)
            report(Errors.canNotThisOutOfClassOrFunction(thisTree.getLocation()));
        return null;
    }

    @Override
    public Void visitSuperTree(SuperTree superTree, Void unused) {
        if (!inClass)
            report(Errors.canNotSuperOutOfClass(superTree.getLocation()));
        return null;
    }

    @Override
    public Void visitAbstractMethodTree(AbstractMethodTree abstractMethodTree, Void unused) {

        if (!inAbstractScope)
            report(Errors.canNotDefineOutOfAbstractClass(abstractMethodTree.getLocation()));

        return null;
    }
}
