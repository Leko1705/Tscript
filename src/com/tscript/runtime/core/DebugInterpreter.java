package com.tscript.runtime.core;

public class DebugInterpreter extends InterpreterWrapper {
    
    
    public interface DebugHaltListener {
        void onDebugAction();
    }
    
    private final DebugHaltListener haltListener;
    
    public DebugInterpreter(Interpreter prev, DebugHaltListener listener) {
        super(prev);
        this.haltListener = listener;
    }

    private void haltDebug(){
        haltListener.onDebugAction();
    }

    
    public void onBreakPoint() {
        haltDebug();
    }

    public void containerWrite() {
        super.containerWrite();
        haltDebug();
    }

    public void storeLocal(int address) {
        super.storeLocal(address);
        haltDebug();
    }

    public void storeGlobal(int address) {
        super.storeGlobal(address);
        haltDebug();
    }

    public void branchIterator(int address) {
        super.branchIterator(address);
        haltDebug();
    }

    public void branchOn(boolean when, int address) {
        super.branchOn(when, address);
        haltDebug();
    }

    public void storeStatic(int utf8Address) {
        super.storeStatic(utf8Address);
        haltDebug();
    }

    public void call(int argc) {
        haltDebug();
        super.call(argc);
    }

    public void callSuper(int argc) {
        haltDebug();
        super.callSuper(argc);
    }

    public void throwError() {
        haltDebug();
        super.throwError();
    }

}
