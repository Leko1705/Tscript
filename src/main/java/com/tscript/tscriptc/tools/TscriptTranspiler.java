package com.tscript.tscriptc.tools;

import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.TreeScanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * A Transpiler transpiling the tree format to tscript code.
 * The transpiler can be specified for the web version.
 * (<a href=https://tglas.github.io/tscript/>web version</a>)
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public class TscriptTranspiler extends TreeScanner<StringBuilder, Void> implements Transpiler {

    private final boolean webVersion;

    /**
     * Creates a new tscript transpiler instance.
     * @param webVersion true if this transpiler targets the web version, else false
     */
    public TscriptTranspiler(boolean webVersion) {
        this.webVersion = webVersion;
    }


    @Override
    public void run(InputStream in, OutputStream out, String[] args) {
        StringBuilder s = new StringBuilder();
        //tree.accept(this, s);
        try {
            out.write(s.toString().getBytes());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Void visitArgument(ArgumentTree node, StringBuilder stringBuilder) {
        stringBuilder.append(node.getName());
        if (node.getExpression() != null){
            stringBuilder.append("=");
            scan(node.getExpression(), stringBuilder);
        }
        return null;
    }

    @Override
    public Void visitArray(ArrayTree node, StringBuilder stringBuilder) {
        stringBuilder.append("[");
        Iterator<? extends ExpressionTree> itr = node.getContents().iterator();

        if (itr.hasNext()){
            scan(itr.next(), stringBuilder);
            while (itr.hasNext()){
                stringBuilder.append(", ");
                scan(itr.next(), stringBuilder);
            }
        }
        stringBuilder.append("]");
        return null;
    }

    @Override
    public Void visitAssign(AssignTree node, StringBuilder stringBuilder) {
        scan(node.getLeftOperand(), stringBuilder);
        stringBuilder.append(" = ");
        scan(node.getRightOperand(), stringBuilder);
        return null;
    }

    @Override
    public Void visitBinaryOperation(BinaryOperationTree node, StringBuilder stringBuilder) {

        stringBuilder.append("(");

        switch (node.getOperationType()){
            case SHIFT_AL -> {
                if (webVersion) {
                    scan(node.getLeftOperand(), stringBuilder);
                    stringBuilder.append(" // (2^");
                    scan(node.getRightOperand(), stringBuilder);
                    stringBuilder.append(")");
                }
                else {
                    scan(node.getLeftOperand(), stringBuilder);
                    stringBuilder.append(" ").append(node.getOperationType().encoding).append(" ");
                    scan(node.getRightOperand(), stringBuilder);
                }
            }
            case SHIFT_AR -> {
                if (webVersion) {
                    scan(node.getLeftOperand(), stringBuilder);
                    stringBuilder.append(" * (2^");
                    scan(node.getRightOperand(), stringBuilder);
                    stringBuilder.append(")");
                }
                else {
                    scan(node.getLeftOperand(), stringBuilder);
                    stringBuilder.append(" ").append(node.getOperationType().encoding).append(" ");
                    scan(node.getRightOperand(), stringBuilder);
                }
            }
            default -> {
                scan(node.getLeftOperand(), stringBuilder);
                stringBuilder.append(" ").append(node.getOperationType().encoding).append(" ");
                scan(node.getRightOperand(), stringBuilder);
            }
        }


        stringBuilder.append(")");
        return null;
    }

    @Override
    public Void visitBlock(BlockTree node, StringBuilder stringBuilder) {
        stringBuilder.append("{");
        for (StatementTree statement : node.getStatements()) {
            statement.accept(this, stringBuilder);
        }
        stringBuilder.append("}");
        return null;
    }

    @Override
    public Void visitBoolean(BooleanTree node, StringBuilder stringBuilder) {
        stringBuilder.append(node.get());
        return null;
    }

    @Override
    public Void visitBreak(BreakTree node, StringBuilder stringBuilder) {
        stringBuilder.append("break;");
        return null;
    }

    @Override
    public Void visitCall(CallTree node, StringBuilder stringBuilder) {
        scan(node.getCalled(), stringBuilder);
        stringBuilder.append("(");
        Iterator<? extends ArgumentTree> argItr = node.getArguments().iterator();
        if (argItr.hasNext()){
            scan(argItr.next(), stringBuilder);
            while (argItr.hasNext()){
                stringBuilder.append(", ");
                scan(argItr.next(), stringBuilder);
            }
        }
        stringBuilder.append(")");
        return null;
    }

    @Override
    public Void visitClass(ClassTree node, StringBuilder stringBuilder) {
        return null;
    }

    @Override
    public Void visitClosure(ClosureTree node, StringBuilder stringBuilder) {
        stringBuilder.append(node.getName());
        if (node.getInitializer() != null){
            stringBuilder.append("=");
            scan(node.getInitializer(), stringBuilder);
        }
        return null;
    }

    @Override
    public Void visitConstructor(ConstructorTree node, StringBuilder stringBuilder) {
        scan(node.getModifiers(), stringBuilder);
        stringBuilder.append(" constructor(");
        Iterator<? extends ParameterTree> paramItr = node.getParameters().iterator();
        if (paramItr.hasNext()){
            scan(paramItr.next(), stringBuilder);
            while (paramItr.hasNext()){
                stringBuilder.append(", ");
                scan(paramItr.next(), stringBuilder);
            }
        }
        stringBuilder.append(")");
        if (!node.getSuperArguments().isEmpty()){
            stringBuilder.append(" : super(");
            Iterator<? extends ArgumentTree> superItr = node.getSuperArguments().iterator();
            if (superItr.hasNext()){
                scan(superItr.next(), stringBuilder);
                while (superItr.hasNext()){
                    stringBuilder.append(", ");
                    scan(superItr.next(), stringBuilder);
                }
            }
            stringBuilder.append(")");
        }

        scan(node.getBody(), stringBuilder);
        return null;
    }

    @Override
    public Void visitContainerAccess(ContainerAccessTree node, StringBuilder stringBuilder) {
        scan(node.getContainer(), stringBuilder);
        stringBuilder.append("[");
        scan(node.getKey(), stringBuilder);
        stringBuilder.append("]");
        return null;
    }

    @Override
    public Void visitContinue(ContinueTree node, StringBuilder stringBuilder) {
        stringBuilder.append("continue;");
        return null;
    }

    @Override
    public Void visitDictionary(DictionaryTree node, StringBuilder stringBuilder) {
        Iterator<? extends ExpressionTree> keyItr = node.getKeys().iterator();
        Iterator<? extends ExpressionTree> valItr = node.getValues().iterator();

        stringBuilder.append("{");

        if (keyItr.hasNext()){
            scan(keyItr.next(), stringBuilder);
            stringBuilder.append(": ");
            scan(valItr.next(), stringBuilder);

            while (keyItr.hasNext()){
                stringBuilder.append(", ");
                scan(keyItr.next(), stringBuilder);
                stringBuilder.append(": ");
                scan(valItr.next(), stringBuilder);
            }
        }

        stringBuilder.append("}");
        return null;
    }

    @Override
    public Void visitDoWhileLoop(DoWhileTree node, StringBuilder stringBuilder) {
        stringBuilder.append("do ");
        scan(node.getStatement(), stringBuilder);
        stringBuilder.append(" while ");
        scan(node.getCondition(), stringBuilder);
        stringBuilder.append(";");
        return null;
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatementTree node, StringBuilder stringBuilder) {
        scan(node.getExpression(), stringBuilder);
        stringBuilder.append(";");
        return null;
    }

    @Override
    public Void visitFloat(FloatTree node, StringBuilder stringBuilder) {
        stringBuilder.append(node.get());
        return null;
    }

    @Override
    public Void visitForLoop(ForLoopTree node, StringBuilder stringBuilder) {
        stringBuilder.append("for ");
        if (node.getVariable() != null){
            stringBuilder.append("var ");
            scan(node.getVariable(), stringBuilder);
            stringBuilder.append(" in ");
        }
        scan(node.getIterable(), stringBuilder);
        stringBuilder.append(" do ");
        scan(node.getStatement(), stringBuilder);

        return null;
    }

    @Override
    public Void visitFromImport(FromImportTree node, StringBuilder stringBuilder) {
        if (!webVersion) return null;

        stringBuilder.append("from ");
        Iterator<String> itr = node.getFromAccessChain().iterator();
        if (itr.hasNext()){
            stringBuilder.append(itr.next());
            while (itr.hasNext()){
                stringBuilder.append(".");
                stringBuilder.append(itr.next());
            }
        }

        stringBuilder.append(" import ");
        itr = node.getImportAccessChain().iterator();
        if (itr.hasNext()){
            stringBuilder.append(itr.next());
            while (itr.hasNext()){
                stringBuilder.append(".");
                stringBuilder.append(itr.next());
            }
        }

        stringBuilder.append(";");
        return null;
    }

    @Override
    public Void visitFunction(FunctionTree node, StringBuilder stringBuilder) {
        scan(node.getModifiers(), stringBuilder);
        stringBuilder.append("function ").append(node.getName()).append("(");
        Iterator<? extends ParameterTree> paramItr = node.getParameters().iterator();
        if (paramItr.hasNext()){
            scan(paramItr.next(), stringBuilder);
            while (paramItr.hasNext()){
                stringBuilder.append(", ");
                scan(paramItr.next(), stringBuilder);
            }
        }
        stringBuilder.append(")");
        scan(node.getBody(), stringBuilder);
        return null;
    }

    @Override
    public Void visitGetType(GetTypeTree node, StringBuilder stringBuilder) {
        if (webVersion){
            stringBuilder.append("Type(");
            scan(node.getOperand(), stringBuilder);
            stringBuilder.append(")");
        }
        else {
            stringBuilder.append("typeof ");
            scan(node.getOperand(), stringBuilder);
        }
        return null;
    }

    @Override
    public Void visitIfElse(IfElseTree node, StringBuilder stringBuilder) {
        stringBuilder.append("if ");
        scan(node.getCondition(), stringBuilder);
        stringBuilder.append(" then ");
        scan(node.getThenStatement(), stringBuilder);
        if (node.getElseStatement() != null){
            stringBuilder.append(" else ");
            scan(node.getElseStatement(), stringBuilder);
        }
        return null;
    }

    @Override
    public Void visitImport(ImportTree node, StringBuilder stringBuilder) {
        if (!webVersion) return null;

        stringBuilder.append("import ");
        Iterator<String> impItr = node.getAccessChain().iterator();
        if (impItr.hasNext()){
            stringBuilder.append(impItr.next());
            while (impItr.hasNext()){
                stringBuilder.append(".");
                stringBuilder.append(impItr.next());
            }
        }

        stringBuilder.append(";");
        return null;
    }

    @Override
    public Void visitInteger(IntegerTree node, StringBuilder stringBuilder) {
        stringBuilder.append(node.get());
        return null;
    }

    @Override
    public Void visitLambda(LambdaTree node, StringBuilder stringBuilder) {
        stringBuilder.append("function ");
        if (!node.getClosures().isEmpty()){
            stringBuilder.append("[");
            Iterator<? extends ClosureTree> closures = node.getClosures().iterator();
            if (closures.hasNext()){
                scan(closures.next(), stringBuilder);
                while (closures.hasNext()){
                    stringBuilder.append(", ");
                    scan(closures.next(), stringBuilder);
                }
            }
            stringBuilder.append("]");
        }

        stringBuilder.append("(");
        Iterator<? extends ParameterTree> paramItr = node.getParameters().iterator();
        if (paramItr.hasNext()){
            scan(paramItr.next(), stringBuilder);
            while (paramItr.hasNext()){
                stringBuilder.append(", ");
                scan(paramItr.next(), stringBuilder);
            }
        }
        stringBuilder.append(")");
        scan(node.getBody(), stringBuilder);
        return null;
    }

    @Override
    public Void visitMemberAccess(MemberAccessTree node, StringBuilder stringBuilder) {
        scan(node.getExpression(), stringBuilder);
        stringBuilder.append(".");
        stringBuilder.append(node.getMemberName());
        return null;
    }

    @Override
    public Void visitModifiers(ModifiersTree node, StringBuilder stringBuilder) {
        for (Modifier modifier : node.getModifiers()) {
            if (modifier.isVisibility()){
                stringBuilder.append(modifier.name).append(": ");
            }
        }

        if (!webVersion && node.getModifiers().contains(Modifier.CONSTANT))
            stringBuilder.append("const");

        if (node.getModifiers().contains(Modifier.STATIC))
            stringBuilder.append("static");

        if (!webVersion) {
            if (node.getModifiers().contains(Modifier.ABSTRACT))
                stringBuilder.append("abstract");

            if (node.getModifiers().contains(Modifier.OVERRIDDEN))
                stringBuilder.append("overridden");

            if (node.getModifiers().contains(Modifier.NATIVE))
                stringBuilder.append("native");
        }

        return null;
    }

    @Override
    public Void visitNamespace(NamespaceTree node, StringBuilder stringBuilder) {
        scan(node.getModifiers(), stringBuilder);
        stringBuilder.append("namespace ").append(node.getName()).append("{");
        scan(node.getDefinitions(), stringBuilder);
        scan(node.getStatements(), stringBuilder);
        return null;
    }

    @Override
    public Void visitNot(NotTree node, StringBuilder stringBuilder) {
        stringBuilder.append("not ");
        scan(node.getOperand(), stringBuilder);
        return null;
    }

    @Override
    public Void visitNull(NullTree node, StringBuilder stringBuilder) {
        stringBuilder.append("null");
        return null;
    }

    @Override
    public Void visitParameter(ParameterTree node, StringBuilder stringBuilder) {
        if (!webVersion && node.getModifiers().getModifiers().contains(Modifier.CONSTANT)){
            stringBuilder.append("const ");
        }
        stringBuilder.append(node.getName());
        if (node.getDefaultValue() != null){
            stringBuilder.append("=");
            scan(node.getDefaultValue(), stringBuilder);
        }
        return null;
    }

    @Override
    public Void visitRange(RangeTree node, StringBuilder stringBuilder) {
        scan(node.getFrom(), stringBuilder);
        stringBuilder.append(":");
        scan(node.getTo(), stringBuilder);
        return null;
    }

    @Override
    public Void visitReturn(ReturnTree node, StringBuilder stringBuilder) {
        stringBuilder.append("return");
        if (node.getExpression() != null){
            stringBuilder.append(" ");
            scan(node.getExpression(), stringBuilder);
        }
        stringBuilder.append(";");
        return null;
    }

    @Override
    public Void visitRoot(RootTree node, StringBuilder stringBuilder) {
        scan(node.getDefinitions(), stringBuilder);
        scan(node.getStatements(), stringBuilder);
        return null;
    }

    @Override
    public Void visitSign(SignTree node, StringBuilder stringBuilder) {
        stringBuilder.append("-");
        scan(node.getOperand(), stringBuilder);
        return null;
    }

    @Override
    public Void visitString(StringTree node, StringBuilder stringBuilder) {
        stringBuilder.append("\"").append(node.get()).append("\"");
        return null;
    }

    @Override
    public Void visitSuper(SuperTree node, StringBuilder stringBuilder) {
        stringBuilder.append("super.").append(node.getName());
        return null;
    }

    @Override
    public Void visitThis(ThisTree node, StringBuilder stringBuilder) {
        stringBuilder.append("this");
        return null;
    }

    @Override
    public Void visitThrow(ThrowTree node, StringBuilder stringBuilder) {
        stringBuilder.append("throw ");
        scan(node.getThrown(), stringBuilder);
        stringBuilder.append(";");
        return null;
    }

    @Override
    public Void visitTryCatch(TryCatchTree node, StringBuilder stringBuilder) {
        stringBuilder.append("try ");
        scan(node.getTryStatement(), stringBuilder);
        stringBuilder.append(" catch var ");
        scan(node.getExceptionVariable(), stringBuilder);
        stringBuilder.append(" do ");
        scan(node.getCatchStatement(), stringBuilder);
        return null;
    }

    @Override
    public Void visitUse(UseTree node, StringBuilder stringBuilder) {
        stringBuilder.append("use ");
        scan(node.getVariable(), stringBuilder);
        stringBuilder.append(";");
        return null;
    }

    @Override
    public Void visitVarDefs(VarDefsTree node, StringBuilder stringBuilder) {
        if (!webVersion && node.getModifiers().getModifiers().contains(Modifier.CONSTANT)){
            stringBuilder.append("const ");
        }
        else {
            stringBuilder.append("var ");
        }

        Iterator<? extends VarDefTree> varDefItr = node.getDefinitions().iterator();
        if (varDefItr.hasNext()){
            scan(varDefItr.next(), stringBuilder);
            while (varDefItr.hasNext()){
                stringBuilder.append(",");
                scan(varDefItr.next(), stringBuilder);
            }
        }

        stringBuilder.append(";");
        return null;
    }

    @Override
    public Void visitVarDef(VarDefTree node, StringBuilder stringBuilder) {
        stringBuilder.append(node.getName());
        if (node.getInitializer() != null){
            stringBuilder.append(" = ");
            scan(node.getInitializer(), stringBuilder);
        }
        return null;
    }

    @Override
    public Void visitVariable(VariableTree node, StringBuilder stringBuilder) {
        stringBuilder.append(node.getName());
        return null;
    }

    @Override
    public Void visitWhileDoLoop(WhileDoTree node, StringBuilder stringBuilder) {
        stringBuilder.append("while ");
        scan(node.getCondition(), stringBuilder);
        stringBuilder.append(" do ");
        scan(node.getStatement(), stringBuilder);
        return null;
    }
}
