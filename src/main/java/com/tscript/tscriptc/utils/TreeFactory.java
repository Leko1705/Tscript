package com.tscript.tscriptc.utils;

import com.tscript.tscriptc.tree.*;

import java.util.List;
import java.util.Set;

/**
 * A factory interface for creating Nodes for the abstract syntax tree.
 * @since 1.0
 * @author Lennart Köhler
 */
public interface TreeFactory {

    ArgumentTree ArgumentTree(String name,
                              ExpressionTree value);

    ArrayTree ArrayTree(List<? extends ExpressionTree> contents);

    AssignTree AssignTree(ExpressionTree assigned,
                          ExpressionTree value);

    BinaryOperationTree BinaryOperationTree(ExpressionTree left,
                                            ExpressionTree right,
                                            Operation operation);

    BlockTree BlockTree(List<? extends StatementTree> statements);

    BooleanTree BooleanTree(boolean value);

    BreakTree BreakTree();

    CallTree CallTree(ExpressionTree called,
                      List<? extends ArgumentTree> arguments);

    ClassTree ClassTree(String name,
                        String superName,
                        ConstructorTree constructor,
                        List<? extends DefinitionTree> members);

    ClosureTree ClosureTree(String name,
                            ExpressionTree initializer);

    ConstructorTree ConstructorTree(ModifiersTree modifiers,
                                    List<? extends ParameterTree> parameters,
                                    List<? extends ArgumentTree> superArguments,
                                    BlockTree body);

    ContainerAccessTree ContainerAccessTree(ExpressionTree container,
                                            ExpressionTree key);

    ContinueTree ContinueTree();

    DictionaryTree DictionaryTree(List<? extends ExpressionTree> keys,
                                  List<? extends ExpressionTree> values);

    DoWhileTree DoWhileTree(StatementTree statement,
                            ExpressionTree condition);

    ExpressionStatementTree ExpressionStatementTree(ExpressionTree expression);

    FloatTree FloatTree(double value);

    ForLoopTree ForLoopTree(VarDefTree runVar,
                            ExpressionTree iterable,
                            StatementTree statement);

    FromImportTree FromImportTree(List<String> fromAccessChain,
                                  List<String> importAccessChain);

    FunctionTree FunctionTree(ModifiersTree modifiers,
                              String name,
                              List<? extends ParameterTree> parameters,
                              BlockTree body);

    GetTypeTree GetTypeTree(ExpressionTree operand);

    IfElseTree IfElseTree(ExpressionTree condition,
                          StatementTree thenStatement,
                          StatementTree elseStatement);

    ImportTree ImportTree(List<String> importAccessChain);

    IntegerTree IntegerTree(int value);

    LambdaTree LambdaTree(List<? extends ClosureTree> closures,
                          List<? extends ParameterTree> parameters,
                          BlockTree body);

    MemberAccessTree MemberAccessTree(ExpressionTree accessed,
                                      String memberName);

    ModifiersTree ModifiersTree(Set<Modifier> modifiers);

    NamespaceTree NamespaceTree(ModifiersTree modifiers,
                                String name,
                                List<? extends DefinitionTree> definitions,
                                List<? extends StatementTree> statements);

    NotTree NotTree(ExpressionTree operand);

    NullTree NullTree();

    ParameterTree ParameterTree(String name,
                                ExpressionTree defaultValue);

    RangeTree RangeTree(ExpressionTree from,
                        ExpressionTree to);

    ReturnTree ReturnTree(ExpressionTree returned);

    RootTree RootTree(List<? extends DefinitionTree> definitions,
                      List<? extends StatementTree> statements);

    SignTree SignTree(boolean isNegation,
                      ExpressionTree operand);

    StringTree StringTree(String value);

    ThisTree ThisTree();

    ThrowTree ThrowTree(ExpressionTree thrown);

    TryCatchTree TryCatchTree(StatementTree tryBody,
                              VarDefTree exceptionVar,
                              StatementTree catchBody);

    UseTree UseTree(VariableTree variable);

    VarDefsTree VarDefsTree(ModifiersTree modifiers,
                            List<? extends VarDefTree> definedVars);

    VarDefTree VarDefTree(String name,
                          ExpressionTree initializer);

    VariableTree VariableTree(String name);

    WhileDoTree WhileDoTree(ExpressionTree condition,
                            StatementTree statement);
}
