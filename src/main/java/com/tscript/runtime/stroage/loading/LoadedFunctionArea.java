package com.tscript.runtime.stroage.loading;

import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.stroage.FunctionArea;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.utils.Tuple;

class LoadedFunctionArea implements FunctionArea {

    protected final VirtualMetadataImpl[] data;

    protected LoadedFunctionArea(int size) {
        this.data = new VirtualMetadataImpl[size];
    }

    @Override
    public synchronized VirtualFunction loadFunction(int index) {
        return data[index].create();
    }


    protected static class VirtualMetadataImpl extends VirtualFunction.VirtualMetadata {

        protected Module module;
        private final String name;
        private final int stackSize;
        private final int locals;
        private final byte[][] instructions;
        private final Tuple<String, Integer>[] parameters;

        protected VirtualMetadataImpl(String name, int stackSize, int locals, byte[][] instructions, Tuple<String, Integer>[] parameters) {
            this.name = name;
            this.stackSize = stackSize;
            this.locals = locals;
            this.instructions = instructions;
            this.parameters = parameters;
        }

        @Override
        public Module module() {
            return module;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int stackSize() {
            return stackSize;
        }

        @Override
        public int locals() {
            return locals;
        }

        @Override
        public byte[][] instructions() {
            return instructions;
        }

        @Override
        public Tuple<String, Integer>[] parameters() {
            return parameters;
        }

    }

}
