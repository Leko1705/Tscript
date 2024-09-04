package com.tscript.tscriptc.analyze.structures;

import java.util.List;

public interface Hierarchy {

    Symbol resolveDefinition(List<String> accessChain);

}
