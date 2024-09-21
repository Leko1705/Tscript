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
        for (Tuple<String, Integer> parameter : d.parameters) {
            int poolAddr = parameter.getSecond();
            TObject value = null;
            if (poolAddr != -1) {
                byte[] addrArr = Conversion.to2Bytes(poolAddr);
                value = module.getPool().loadConstant(addrArr[0], addrArr[1]);
            }
            parameters.add(parameter.getFirst(), value);
        }

        return new VirtualFunction(d.name, parameters, d.instructions, d.stackSize, d.locals, module);
    }

    protected record VirtualFunctionMetaData(String name,
                                             Tuple<String, Integer>[] parameters,
                                             int stackSize,
                                             int locals,
                                             byte[][] instructions){

    }

}
