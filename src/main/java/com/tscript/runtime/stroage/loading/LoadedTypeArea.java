package com.tscript.runtime.stroage.loading;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.core.VirtualType;
import com.tscript.runtime.stroage.TypeArea;
import com.tscript.runtime.typing.Function;
import com.tscript.runtime.typing.Type;

import java.util.List;

class LoadedTypeArea implements TypeArea {

    protected final VirtualType[] virtualTypes;
    protected final Function[] unloadedStaticBlocks;

    LoadedTypeArea(int size) {
        this.virtualTypes = new VirtualType[size];
        this.unloadedStaticBlocks = new Function[size];
    }

    @Override
    public synchronized VirtualType loadType(TThread thread, int index) {
        VirtualType type = virtualTypes[index];

        if (unloadedStaticBlocks[index] != null) {
            Function staticBlock = unloadedStaticBlocks[index];
            staticBlock.setOwner(type);
            unloadedStaticBlocks[index] = null;
            thread.call(staticBlock, List.of());
        }

        return type;
    }

}
