package com.tscript.tscriptc.analyze;

import com.tscript.tscriptc.analyze.scoping.*;
import com.tscript.tscriptc.analyze.search.*;
import com.tscript.tscriptc.analyze.structures.*;
import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.Errors;
import com.tscript.tscriptc.utils.TreeScanner;

import java.util.*;

public class UsageChecker {

    public static void check(Tree tree, Scope rootScope) {
        tree.accept(new Checker(), rootScope);
    }


    private static class Checker extends TreeScanner<Scope, Symbol> {

        private boolean inStaticContext = false;


        @Override
        public Symbol visitRoot(RootTree node, Scope scope) {
            return super.visitRoot(node, scope);
        }

        @Override
        public Symbol visitBlock(BlockTree node, Scope scope) {
            scope = enterScope(scope, node);
            super.visitBlock(node, scope);
            return null;
        }

        @Override
        public Symbol visitConstructor(ConstructorTree node, Scope scope) {
            boolean prev = inStaticContext;
            inStaticContext = false;
            scope = enterScope(scope, node);
            super.visitConstructor(node, scope);
            inStaticContext = prev;
            return null;
        }

        @Override
        public Symbol visitClass(ClassTree node, Scope scope) {
            boolean prev = inStaticContext;
            inStaticContext = node.getModifiers().getModifiers().contains(Modifier.STATIC);
            scope = enterScope(scope, node);
            super.visitClass(node, scope);
            inStaticContext = prev;
            return null;
        }

        @Override
        public Symbol visitFunction(FunctionTree node, Scope scope) {
            boolean prev = inStaticContext;
            inStaticContext = node.getModifiers().getModifiers().contains(Modifier.STATIC);
            scope = enterScope(scope, node);
            super.visitFunction(node, scope);
            inStaticContext = prev;
            return null;
        }

        @Override
        public Symbol visitNamespace(NamespaceTree node, Scope scope) {
            boolean prev = inStaticContext;
            inStaticContext = true;
            scope = enterScope(scope, node);
            super.visitNamespace(node, scope);
            inStaticContext = prev;
            return null;
        }

        @Override
        public Symbol visitLambda(LambdaTree node, Scope scope) {
            boolean prev = inStaticContext;
            inStaticContext = false;
            scope = enterScope(scope, node);
            super.visitLambda(node, scope);
            inStaticContext = prev;
            return null;
        }

        @Override
        public Symbol visitForLoop(ForLoopTree node, Scope scope) {
            return super.visitForLoop(node, enterScope(scope, node));
        }

        @Override
        public Symbol visitTryCatch(TryCatchTree node, Scope scope) {
            scan(node.getTryStatement(), scope);
            scope = enterScope(scope, node);
            scan(node.getExceptionVariable(), scope);
            scan(node.getCatchStatement(), scope);
            return null;
        }

        @Override
        public Symbol visitVariable(VariableTree node, Scope scope) {

            Symbol sym = scope.accept(new SymbolSearcher(), node.getName());

            if (sym == null) {
                sym = scope.accept(new ThisClassAccessResolver(), node.getName());
                if (sym == null){
                    sym = scope.accept(new SuperClassAccessResolver(), node.getName());
                    if (sym == null){
                        throw Errors.canNotFindSymbol(node.getName(), node.getLocation());
                    }
                    if (sym.visibility == Visibility.PRIVATE)
                        throw Errors.memberIsNotVisible(node.getLocation(), Modifier.PRIVATE, node.getName());
                }
            }

            if (sym.scope instanceof ClassScope && !sym.isStatic && inStaticContext){
                throw Errors.canNotAccessFromStaticContext(node.getLocation());
            }

            return sym;
        }

        @Override
        public Symbol visitMemberAccess(MemberAccessTree node, Scope scope) {
            ExpressionTree exp = node.getExpression();
            String memberName = node.getMemberName();

            scan(exp, scope);

            if (exp instanceof ThisTree){
                Symbol member = scope.accept(new ThisClassAccessResolver(), memberName);
                if (member == null) {
                    String thisClassName = scope.accept(new ThisClassNameResolver(), null);
                    throw Errors.noSuchMemberFound(node.getLocation(), Objects.requireNonNull(thisClassName), memberName);
                }
                return member;
            }

            return null;
        }

        @Override
        public Symbol visitSuper(SuperTree node, Scope scope) {
            boolean hasSuperClass = scope.accept(new HasSuperClassChecker(), null);
            if (!hasSuperClass) {
                String thisClassName = scope.accept(new ThisClassNameResolver(), null);
                throw Errors.invalidSuperAccessFound(node.getLocation(), Objects.requireNonNull(thisClassName));
            }
            Symbol member = scope.accept(new SuperClassAccessResolver(), node.getName());
            if (member == null) {
                String superClassName = scope.accept(new SuperClassNameResolver(), null);
                throw Errors.noSuchMemberFound(node.getLocation(), Objects.requireNonNull(superClassName), node.getName());
            }
            if (member.visibility == Visibility.PRIVATE)
                throw Errors.memberIsNotVisible(node.getLocation(), Modifier.PRIVATE, node.getName());
            return member;
        }

        @Override
        public Symbol visitAssign(AssignTree node, Scope scope) {
            Symbol sym = scan(node.getLeftOperand(), scope);
            if (sym != null && sym.kind == Symbol.Kind.CONSTANT){
                throw Errors.canNotReassignToConstant(node.getLocation());
            }
            scan(node.getRightOperand(), scope);
            return null;
        }

        private static Scope enterScope(Scope scope, Object key){
            return Objects.requireNonNull(scope.getChildScope(key));
        }

    }


    private static class HasSuperClassChecker implements ScopeVisitor<Void, Boolean> {

        @Override
        public Boolean visitBlock(BlockScope scope, Void unused) {
            return scope.getEnclosingScope().accept(this, unused);
        }

        @Override
        public Boolean visitClass(ClassScope scope, Void unused) {
            return scope.getSuperClassScope() != null;
        }

        @Override
        public Boolean visitFunction(FunctionScope scope, Void unused) {
            return scope.getEnclosingScope().accept(this, unused);
        }

        @Override
        public Boolean visitGlobal(GlobalScope scope, Void unused) {
            return false;
        }

        @Override
        public Boolean visitLambda(LambdaScope scope, Void unused) {
            return false;
        }

        @Override
        public Boolean visitNamespace(NamespaceScope scope, Void unused) {
            return false;
        }

        @Override
        public Boolean visitExternal(ExternalScope scope, Void unused) {
            return true;
        }
    }


    private static class SuperClassNameResolver implements ScopeVisitor<Void, String> {

        @Override
        public String visitBlock(BlockScope scope, Void unused) {
            return scope.getEnclosingScope().accept(this, unused);
        }

        @Override
        public String visitClass(ClassScope scope, Void unused) {
            Scope superScope = scope.getSuperClassScope();
            if (superScope instanceof ClassScope cls) return cls.getClassName();
            else if (superScope instanceof ExternalScope ext) return ext.getName();
            throw new AssertionError();
        }

        @Override
        public String visitFunction(FunctionScope scope, Void unused) {
            return scope.getEnclosingScope().accept(this, unused);
        }

        @Override
        public String visitGlobal(GlobalScope scope, Void unused) {
            return null;
        }

        @Override
        public String visitLambda(LambdaScope scope, Void unused) {
            return null;
        }

        @Override
        public String visitNamespace(NamespaceScope scope, Void unused) {
            return null;
        }

        @Override
        public String visitExternal(ExternalScope scope, Void unused) {
            return scope.getName();
        }
    }


    private static class ThisClassNameResolver implements ScopeVisitor<Void, String> {

        @Override
        public String visitBlock(BlockScope scope, Void unused) {
            return scope.getEnclosingScope().accept(this, unused);
        }

        @Override
        public String visitClass(ClassScope scope, Void unused) {
            return scope.getClassName();
        }

        @Override
        public String visitFunction(FunctionScope scope, Void unused) {
            return scope.getEnclosingScope().accept(this, unused);
        }

        @Override
        public String visitGlobal(GlobalScope scope, Void unused) {
            return null;
        }

        @Override
        public String visitLambda(LambdaScope scope, Void unused) {
            return null;
        }

        @Override
        public String visitNamespace(NamespaceScope scope, Void unused) {
            return null;
        }

        @Override
        public String visitExternal(ExternalScope scope, Void unused) {
            return null;
        }
    }

}
