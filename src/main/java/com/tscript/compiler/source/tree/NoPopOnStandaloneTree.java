package com.tscript.compiler.source.tree;

/**
 * Flag for expressions that do not follow a POP operation
 * if used in an {@link ExpressionStatementTree}.
 * This flag is used in imageKey of stack machines.
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface NoPopOnStandaloneTree extends ExpressionTree {
}
