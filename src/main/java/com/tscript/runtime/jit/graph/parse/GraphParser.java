package com.tscript.runtime.jit.graph.parse;

import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.instructions.Instruction;

import java.util.List;

public interface GraphParser {

    Node parse(List<Instruction> instructions);

}
