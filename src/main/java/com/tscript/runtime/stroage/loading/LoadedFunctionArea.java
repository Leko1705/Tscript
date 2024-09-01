package com.tscript.runtime.stroage.loading;

import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.stroage.FunctionArea;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.utils.Conversion;
import com.tscript.runtime.utils.Tuple;

class LoadedFunctionArea implements FunctionArea {

    protected final VirtualFunctionMetaData[] data;

    protected LoadedFunctionArea(int size) {
        this.data = new VirtualFunctionMetaData[size];
    }

    @Override
    public synchronized VirtualFunction loadFunction(int index, Module module) {
        VirtualFunctionMetaData d = data[index];

        Parameters parameters = Parameters.newInstance();
        for (Tuple<String, byte[]> parameter : d.parameters) {
            byte[] poolAddr = parameter.getSecond();
            TObject value = null;
            if (Conversion.from2Bytes(poolAddr[0], poolAddr[1]) != -1) {
                value = module.getPool().loadConstant(poolAddr[0], poolAddr[1]);
            }
            parameters.add(parameter.getFirst(), value);
        }

        return new VirtualFunction(d.name, parameters, d.instructions, d.stackSize, d.locals, module);
    }

    protected record VirtualFunctionMetaData(String name,
                                             Tuple<String, byte[]>[] parameters,
                                             int stackSize,
                                             int locals,
                                             byte[][] instructions){

    }

}
