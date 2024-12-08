package com.tscript.runtime.typing;

import com.tscript.runtime.core.Argument;
import com.tscript.runtime.core.ExecutionException;
import com.tscript.runtime.core.InternalRuntimeErrorMessages;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.utils.FastMergedList;


import java.util.*;

public interface Callable extends TObject {

    
    String getName();

    
    Parameters getParameters(TThread thread);


    boolean isVirtual();

    
    default TObject call(TThread thread, List<TObject> arguments) {
        Parameters parameters = getParameters(thread);

        if (arguments.isEmpty() && parameters.count() == 0) {
            return eval(thread, List.of());
        }

        if (arguments.size() > parameters.count()) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.tooManyParameters(this));
            return null;
        }

        int lastNonDefined = parameters.getLastNonDefinedParameterIndex();

        if (arguments.size() <= lastNonDefined){
            List<String> names = parameters.getNames();
            String missing = names.get(lastNonDefined);
            thread.reportRuntimeError(InternalRuntimeErrorMessages.missingParameter(missing));
            return null;
        }

        List<TObject> missingDefaults = parameters.getTail(arguments.size());
        return eval(thread, new FastMergedList<>(arguments, missingDefaults));
    }


    default TObject call(TThread thread, List<String> names, List<TObject> arguments) {
        if (names.size() != arguments.size()) {
            throw new ExecutionException("names.size() != arguments.size()");
        }

        Parameters parameters = getParameters(thread);

        if (names.size() > parameters.count()){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.tooManyParameters(this));
            return null;
        }

        LinkedHashMap<String, TObject> finals = new LinkedHashMap<>();

        int index = 0;

        Iterator<TObject> valueItr = arguments.iterator();
        for (String name : names){
            if (name != null){
                if (!parameters.hasName(name)){
                    thread.reportRuntimeError(InternalRuntimeErrorMessages.hasNoParameters(this, name));
                    return null;
                }
                boolean success = finals.put(name, valueItr.next()) == null;
                if (!success) {
                    thread.reportRuntimeError(InternalRuntimeErrorMessages.parameterAlreadyAssigned(name));
                    return null;
                }
            }
            else {
                String nextParam = parameters.getNames().get(index);
                boolean success = finals.put(nextParam, valueItr.next()) == null;
                if (!success) {
                    thread.reportRuntimeError(InternalRuntimeErrorMessages.parameterAlreadyAssigned(nextParam));
                    return null;
                }
            }
            index++;
        }

        for (Map.Entry<String, TObject> defaultParam : parameters.toMap().entrySet()){
            if (!finals.containsKey(defaultParam.getKey()) && defaultParam.getValue() != null)
                finals.put(defaultParam.getKey(), defaultParam.getValue());
        }

        if (finals.size() < parameters.count()){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.tooFewParameters(this));
            return null;
        }

        TObject[] flatten = finals.values().toArray(new TObject[0]);
        return eval(thread, Arrays.asList(flatten));
    }

    
    TObject eval(TThread thread, List<TObject> params);

}
