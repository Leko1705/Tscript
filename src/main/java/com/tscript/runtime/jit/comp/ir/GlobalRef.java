package com.tscript.runtime.jit.comp.ir;

/**
 * Identifies a global variable: the module (ObjectCache id) and its index in the module.
 */
public record GlobalRef(int moduleId, int index) {
}
