package com.tscript.runtime.jit.instructions;

public interface InstructionHandler {

    void handlePushNull(PushNull pushNull);

    void handlePushInt(PushInt pushInt);

}
