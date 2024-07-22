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

    ArgumentTree ArgumentTree(Location location,
                              String name,
                              ExpressionTree value);

    ArrayTree ArrayTree(Location location,
                        List<? extends ExpressionTree> content);

    AssignTree AssignTree(Location location,
                          ExpressionTree assigned,
                          ExpressionTree value);

    BinaryOperationTree BinaryOperationTree(Location location,
                                            ExpressionTree left,
                                            ExpressionTree right,
                                            Operation operation);

    BlockTree BlockTree(Location location,
                        List<? extends StatementTree> statements);

    BooleanTree BooleanTree(Location location,
                            boolean value);

    BreakTree BreakTree(Location location);

    CallTree CallTree(Location location,
                      ExpressionTree called,
                      List<? extends ArgumentTree> arguments);

    ClassTree ClassTree(Location location,
                        ModifiersTree modifiers,
                        String name,
                        String superName,
                        ConstructorTree constructor,
                        List<? extends DefinitionTree> members);

    ClosureTree ClosureTree(Location location,
                            String name,
                            ExpressionTree initializer);

    ConstructorTree ConstructorTree(Location location,
                                    ModifiersTree modifiers,
                                    List<? extends ParameterTree> parameters,
                                    List<? extends ArgumentTree> superArguments,
                                    BlockTree body);

    ContainerAccessTree ContainerAccessTree(Location location,
                                            ExpressionTree container,
                                            ExpressionTree key);

    ContinueTree ContinueTree(Location location);

    DictionaryTree DictionaryTree(Location location,
                                  List<? extends ExpressionTree> keys,
                                  List<? extends ExpressionTree> values);

    DoWhileTree DoWhileTree(Location location,
                            StatementTree statement,
                            ExpressionTree condition);

    ExpressionStatementTree ExpressionStatementTree(Location location,
                                                    ExpressionTree expression);

    FloatTree FloatTree(Location location,
                        double value);

    ForLoopTree ForLoopTree(Location location,
                            VarDefTree runVar,
                            ExpressionTree iterable,
                            StatementTree statement);

    FromImportTree FromImportTree(Location location,
                                  List<String> fromAccessChain,
                                  List<String> importAccessChain);

    FunctionTree FunctionTree(Location location,
                              ModifiersTree modifiers,
                              String name,
                              List<? extends ParameterTree> parameters,
                              BlockTree body);

    GetTypeTree GetTypeTree(Location location,
                            ExpressionTree operand);

    IfElseTree IfElseTree(Location location,
                          ExpressionTree condition,
                          StatementTree thenStatement,
                          StatementTree elseStatement);

    ImportTree ImportTree(Location location,
                          List<String> importAccessChain);

    IntegerTree IntegerTree(Location location,
                            int value);

    LambdaTree LambdaTree(Location location,
                          List<? extends ClosureTree> closures,
                          List<? extends ParameterTree> parameters,
                          BlockTree body);

    MemberAccessTree MemberAccessTree(Location location,
                                      ExpressionTree accessed,
                                      String memberName);

    ModifiersTree ModifiersTree(Location location,
                                Set<Modifier> modifiers);

    NamespaceTree NamespaceTree(Location location,
                                ModifiersTree modifiers,
                                String name,
                                List<? extends DefinitionTree> definitions,
                                List<? extends StatementTree> statements);

    NotTree NotTree(Location location,
                    ExpressionTree operand);

    NullTree NullTree(Location location);

    ParameterTree ParameterTree(Location location,
                                String name,
                                ModifiersTree modifiers,
                                ExpressionTree defaultValue);

    RangeTree RangeTree(Location location,
                        ExpressionTree from,
                        ExpressionTree to);

    ReturnTree ReturnTree(Location location,
                          ExpressionTree returned);

    RootTree RootTree(Location location,
                      List<? extends DefinitionTree> definitions,
                      List<? extends StatementTree> statements);

    SignTree SignTree(Location location,
                      boolean isNegation,
                      ExpressionTree operand);

    StringTree StringTree(Location location,
                          String value);

    ThisTree ThisTree(Location location);

    ThrowTree ThrowTree(Location location,
                        ExpressionTree thrown);

    TryCatchTree TryCatchTree(Location location,
                              StatementTree tryBody,
                              VarDefTree exceptionVar,
                              StatementTree catchBody);

    UseTree UseTree(Location location,
                    VariableTree variable);

    VarDefsTree VarDefsTree(Location location,
                            ModifiersTree modifiers,
                            List<? extends VarDefTree> definedVars);

    VarDefTree VarDefTree(Location location,
                          String name,
                          ExpressionTree initializer);

    VariableTree VariableTree(Location location,
                              String name);

    WhileDoTree WhileDoTree(Location location,
                            ExpressionTree condition,
                            StatementTree statement);
}
