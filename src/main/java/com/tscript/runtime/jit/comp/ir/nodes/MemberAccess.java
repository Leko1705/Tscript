package com.tscript.runtime.jit.comp.ir.nodes;

/**
 * How a member of an object is accessed.
 */
public enum MemberAccess {

    /**
     * The object is a class instance whose exact type was guarded: the member is read at a fixed
     * index without any lookup or check.
     */
    FIELD,

    /**
     * Access of a member of the owner of the running function by index ({@code this.x} inside of
     * a method). Cannot fail.
     */
    INTERNAL,

    /**
     * Public access by name ({@code obj.x}): the member is looked up like the interpreter does
     * and errors are reported the same way.
     */
    EXTERNAL,

    /**
     * Access of a member of the super object by name ({@code super.x}, or an inherited member
     * inside of a method): like {@link #EXTERNAL}, but protected members are accessible.
     */
    SUPER
}
