package com.tscript.runtime.jit.comp.ir;

import java.util.List;

public interface IRNode {

    List<IRValue> getInputs();

    void replaceInput(IRValue oldInput, IRValue newInput);

}
